package net.corda.cli.plugins.packaging.cpb

import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertPath
import java.security.cert.CertificateFactory
import java.util.UUID
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CreateCpbTest {

    @TempDir
    lateinit var tempDir: Path

    val createCpb = CreateCpb()

    companion object {

        private fun checkCpbContainsEntries(cpb: Path, expectedEntries: List<String>) {
            JarInputStream(Files.newInputStream(cpb)).use {
                assertTrue(it.manifest.mainAttributes.isNotEmpty())
                assertTrue(it.manifest.mainAttributes[Attributes.Name("Corda-CPB-Format")] == "2.0")
                assertTrue(it.manifest.mainAttributes[Attributes.Name("Corda-CPB-Upgrade")] == false.toString())

                val jarEntries = mutableListOf<ZipEntry>()
                var jarEntry: JarEntry? = it.nextJarEntry
                while (jarEntry != null) {
                    jarEntries.add(jarEntry)
                    jarEntry = it.nextJarEntry
                }
                assertThat(jarEntries.map { it.name }).containsExactlyInAnyOrderElementsOf(
                    expectedEntries.map { Path.of(it).fileName.toString() }
                )
            }
        }

        private val testKeyStore = Path.of(this::class.java.getResource("/signingkeys.pfx")?.toURI()
            ?: error("signingkeys.pfx not found"))
    }

    private fun buildTestCpk(jars: List<String>): Path {
        val cpkName = Path.of(tempDir.toString(), "${UUID.randomUUID()}.cpk")
        JarOutputStream(Files.newOutputStream(cpkName, StandardOpenOption.CREATE_NEW)).use { jarOs ->
            jars.forEach {
                jarOs.putNextEntry(JarEntry(it))
                jarOs.write("TEST CONTENT".toByteArray())
            }
            jarOs.putNextEntry(ZipEntry("META-INF/MANIFEST.MF"))
            jarOs.write("TEST CONTENT".toByteArray())
        }
        return cpkName
    }

    @Test
    fun `packCpksToUnsignedArchive packs CPKs into CPB`() {
        val cpk0 = buildTestCpk(
            listOf(
                "lib/cpk0-lib.jar",
                "main-bundle0.jar"
            )
        )
        val cpk1 =  buildTestCpk(
            listOf(
                "lib/cpk1-lib.jar",
                "main-bundle1.jar"
            )
        )

        val cpb = createCpb.packCpksToUnsignedArchive(sequenceOf(cpk0.toString(), cpk1.toString()), false)

        checkCpbContainsEntries(
            cpb,
            listOf(
                cpk0.toString(),
                cpk1.toString()
            )
        )
    }

    @Test
    fun `packCpksToUnsignedArchive throws if CPK is missing`() {
        val cpk0 = buildTestCpk(
            listOf(
                "lib/cpk0-lib.jar",
                "main-bundle0.jar"
            )
        )
        val missingCpk = Path.of("missing.cpk")

        assertThrows(IllegalArgumentException::class.java) {
            createCpb.packCpksToUnsignedArchive(sequenceOf(cpk0.toString(), missingCpk.toString()), false)
        }
    }

    @Test
    fun `on signing Cpb signature is added`() {
        val cpk0 = buildTestCpk(
            listOf(
                "lib/cpk0-lib.jar",
                "main-bundle0.jar"
            )
        )
        val cpk1 =  buildTestCpk(
            listOf(
                "lib/cpk1-lib.jar",
                "main-bundle1.jar"
            )
        )

        val unisgnedCpb = createCpb.packCpksToUnsignedArchive(sequenceOf(cpk0.toString(), cpk1.toString()), false)
        val signedCpbFile = Path.of(tempDir.toString(), "signed-cpb-${UUID.randomUUID()}.cpb")

        val (privateKey, certPath) = getPrivateKeyAndCertPath()
        createCpb.signJar(unisgnedCpb, signedCpbFile, privateKey, certPath, null)
        JarInputStream(FileInputStream(signedCpbFile.toString())).use {
            val jarEntries = mutableListOf<ZipEntry>()
            var jarEntry: JarEntry? = it.nextJarEntry
            while (jarEntry != null) {
                jarEntries.add(jarEntry)
                jarEntry = it.nextJarEntry
            }

            assertThat(jarEntries.map { it.name }).containsAll(listOf("META-INF/CPB-SIG.SF", "META-INF/CPB-SIG.RSA"))
        }
    }

    private fun getPrivateKeyAndCertPath(): Pair<PrivateKey, CertPath> {
        val passwordCharArray = "keystore password".toCharArray()
        val privateKeyEntry = KeyStore.getInstance(testKeyStore.toFile(), passwordCharArray).getEntry(
            "signing key 2",
            KeyStore.PasswordProtection(passwordCharArray)
        ) as? KeyStore.PrivateKeyEntry ?: error("Alias \"${"signing key 2"}\" is not a private key")

        val privateKey = privateKeyEntry.privateKey
        val certPath = CertificateFactory
            .getInstance("X.509")
            .generateCertPath(privateKeyEntry.certificateChain.asList())
        return privateKey to certPath
    }

//    @Test
//    fun ``() {
//
//    }
}