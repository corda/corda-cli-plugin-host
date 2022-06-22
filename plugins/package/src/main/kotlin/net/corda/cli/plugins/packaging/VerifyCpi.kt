package net.corda.cli.plugins.packaging

import net.corda.libs.packaging.PackageType
import net.corda.libs.packaging.verify.VerifierBuilder
import picocli.CommandLine
import picocli.CommandLine.Command
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyStore
import java.security.cert.X509Certificate

@Command(
    name = "verify",
    description = ["Verifies a CPI signature."]
)
class VerifyCpi : Runnable {

    @CommandLine.Option(names = ["--file", "-f"], required = true, description = ["CPK/CPB/CPI file name", "Use \"-\" to read package from standard input"])
    lateinit var fileName: String

    @CommandLine.Option(names = ["--type", "-t"], description = ["Package type (CPK/CPB/CPI)", "Detected from file name extension if not specified"])
    var type: PackageType? = null

    @CommandLine.Option(names = ["--version", "-v"], description = ["Package format version", "Detected from package's Manifest if not specified"])
    var format: String? = null

    @CommandLine.Option(names = ["--keystore", "-s"], required = true, description = ["Keystore holding trusted certificates"])
    lateinit var keyStoreFileName: String

    @CommandLine.Option(names = ["--storepass", "--password", "-p"], required = true, description = ["Keystore password"])
    lateinit var keyStorePass: String

    @Suppress("TooGenericExceptionCaught")
    override fun run() =
        try {
            VerifierBuilder()
                .type(type)
                .format(format)
                .name(fileName)
                .inputStream(getInputStream(fileName))
                .trustedCerts(readCertificates(keyStoreFileName, keyStorePass))
                .build()
                .verify()
        } catch (e: Exception) {
            println("Error verifying corda package: ${e.message}")
        }

    /**
     * Check file exists and returns a Path object pointing to the file, throws error if file does not exist
     */
    private fun checkFileExists(fileName: String) =
        require(Files.isReadable(Path.of(fileName))) { "\"$fileName\" does not exist or is not readable" }

    private fun getInputStream(fileName: String): InputStream =
        if (fileName == "-") {
            System.`in`
        } else {
            checkFileExists(fileName)
            FileInputStream(fileName)
        }

    private fun readCertificates(keyStoreFileName: String, keyStorePass: String): Collection<X509Certificate> {
        checkFileExists(keyStoreFileName)
        val keyStore = KeyStore.getInstance(File(keyStoreFileName), keyStorePass.toCharArray())
        return keyStore.aliases().asSequence()
            .filter(keyStore::isCertificateEntry)
            .map { keyStore.getCertificate(it) as X509Certificate }
            .toList()
    }
}