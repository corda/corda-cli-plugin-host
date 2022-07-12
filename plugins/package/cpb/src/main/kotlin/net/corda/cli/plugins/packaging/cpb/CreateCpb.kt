package net.corda.cli.plugins.packaging.cpb

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.StandardOpenOption.READ
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertPath
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import jdk.security.jarsigner.JarSigner

@Command(
    name = "create-cpb",
    description = [""]
)
class CreateCpb : Runnable {

    @Option(names = ["--cpks"], required = true, split = " ", description = ["Cpks to convert into Cpb"])
    lateinit var cpks: List<String>

    // Is the below option required? If not what should be its default value?
    @Option(names = ["--upgrade"], required = true, description = ["Cpb Upgrade option"])
    lateinit var cpbUpgrade: String

    @Option(names = ["--file", "-f"], required = true, description = ["Output Cpb file name"])
    lateinit var outputCpbFileName: String

    @Option(names = ["--keystore", "-s"], required = true, description = ["Keystore holding siging keys"])
    lateinit var keyStoreFileName: String

    @Option(names = ["--storepass", "--password", "-p"], required = true, description = ["Keystore password"])
    lateinit var keyStorePass: String

    @Option(names = ["--key", "-k"], required = true, description = ["Key alias"])
    lateinit var keyAlias: String

    @Option(names = ["--tsa", "-t"], description = ["Time Stamping Authority (TSA) URL"])
    var tsaUrl: String? = null

    companion object {
        private val CPB_FORMAT = Attributes.Name("Corda-CPB-Format")

        private val CPB_UPGRADE = Attributes.Name("Corda-CPB-Upgrade")

        /**
         * Type of CertificateFactory to use. "X.509" is a required type for Java implementations.
         */
        private const val STANDARD_CERT_FACTORY_TYPE = "X.509"
        /**
         * Name of signature within Cpb file
         */
        private const val CPB_SIGNER_NAME = "CPB-SIG"
        /**
         * Check file exists and returns a Path object pointing to the file, throws error if file does not exist
         */
        private fun checkFileExists(fileName: String): Path {
            val path = Path.of(fileName)
            require(Files.isReadable(path)) { "\"$fileName\" does not exist or is not readable" }
            return path
        }

        /**
         * Check that file does not exist and returns a Path object pointing to the filename, throws error if file exists
         */
        private fun checkFileDoesNotExist(fileName: String): Path {
            val path = Path.of(fileName)
            require(Files.notExists(path)) { "\"$fileName\" already exists" }
            return path
        }
    }

    override fun run() {
        val unsignedCpbPath = packCpksToUnsignedArchive(cpks.asSequence(), cpbUpgrade.toBooleanStrict())
        val cpbPath = checkFileDoesNotExist(outputCpbFileName)
        val privateKeyEntry = getPrivateKeyEntry(keyStoreFileName, keyStorePass, keyAlias)
        val privateKey = privateKeyEntry.privateKey
        val certPath = buildCertPath(privateKeyEntry.certificateChain.asList())
        signJar(unsignedCpbPath, cpbPath, privateKey, certPath, tsaUrl)
    }

    internal fun packCpksToUnsignedArchive(cpks: Sequence<String>, cpbUpgrade: Boolean): Path {
        val unsignedCpb = Files.createTempFile("temp-unsigned-cpb-${UUID.randomUUID()}", ".cpb")

        val manifest = Manifest()
        val manifestMainAttributes = manifest.mainAttributes
        manifestMainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
        manifestMainAttributes[CPB_FORMAT] = "2.0"
        manifestMainAttributes[CPB_UPGRADE] = cpbUpgrade.toString()

        JarOutputStream(
            Files.newOutputStream(unsignedCpb, WRITE),
            manifest
        ).use { cpb ->
            cpks.onEach {
                checkFileExists(it)
            }.map {
                Path.of(it)
            }.forEach { cpkFileName ->
                Files.newInputStream(cpkFileName, READ).use { cpk ->
                    cpb.putNextEntry(ZipEntry(cpkFileName.fileName.toString()))
                    cpk.copyTo(cpb)
                    cpb.closeEntry()
                }
            }
        }
        return unsignedCpb
    }

    /**
     * Signs jar file
     */
    internal fun signJar(
        unsignedInputCpb: Path,
        signedOutputCpb: Path,
        privateKey: PrivateKey,
        certPath: CertPath,
        tsaUrl: String?
        ) {
        ZipFile(unsignedInputCpb.toFile()).use { unsignedCpi ->
            Files.newOutputStream(signedOutputCpb, WRITE, StandardOpenOption.CREATE_NEW).use { signedCpi ->

                // Create JarSigner
                val builder = JarSigner.Builder(privateKey, certPath)
                    .signerName(CPB_SIGNER_NAME)

                // Use timestamp server if provided
                tsaUrl?.let { builder.tsa(URI(it)) }

                // Sign CPI
                builder
                    .build()
                    .sign(unsignedCpi, signedCpi)
            }
        }
    }

    /**
     * Reads PrivateKeyEntry from key store
     */
    private fun getPrivateKeyEntry(keyStoreFileName: String, keyStorePass: String, keyAlias: String): KeyStore.PrivateKeyEntry {
        val passwordCharArray = keyStorePass.toCharArray()
        val keyStore = KeyStore.getInstance(File(keyStoreFileName), passwordCharArray)
        val keyEntry = keyStore.getEntry(keyAlias, KeyStore.PasswordProtection(passwordCharArray))

        when (keyEntry) {
            is KeyStore.PrivateKeyEntry -> return keyEntry
            else -> error("Alias \"${keyAlias}\" is not a private key")
        }
    }

    /**
     * Builds CertPath from certificate chain
     */
    private fun buildCertPath(certificateChain: List<Certificate>) =
        CertificateFactory
            .getInstance(STANDARD_CERT_FACTORY_TYPE)
            .generateCertPath(certificateChain)

}