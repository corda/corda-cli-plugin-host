package net.corda.cli.plugins.packaging.cpb

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CreateCpbTest {

    @TempDir
    lateinit var tempDir: Path

    val createCpb = CreateCpb()

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
    fun `packCpksIntoCpb packs CPKs into CPB`() {
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

        val cpb = createCpb.packCpksToUnsignedCpb(sequenceOf(cpk0.toString(), cpk1.toString()), false)

        checkCpbContainsEntries(
            cpb,
            listOf(
                cpk0.toString(),
                cpk1.toString()
            )
        )
    }

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

//    @Test
//    fun `packCpksIntoCpb throws if CPK is missing`() {
//        val testCpk0 =  buildTestCpk()
//        val missingCpk = Path.of("missing.cpk")
//
//        packCpksIntoCpb()
//
//        println(testCpk)
//    }

    @Test
    fun `adds META-INF folder`() {

    }

//    @Test
//    fun ``() {
//
//    }

}