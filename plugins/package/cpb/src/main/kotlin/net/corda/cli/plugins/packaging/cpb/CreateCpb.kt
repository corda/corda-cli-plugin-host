package net.corda.cli.plugins.packaging.cpb

import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.StandardOpenOption.READ
import java.util.jar.Attributes
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry

@Command(
    name = "create-cpb",
    description = [""]
)
class CreateCpb : Runnable {

    @Option(names = ["--cpks"], required = true, split = " ", description = ["CPK files to convert into CPB"])
    lateinit var cpks: List<String>

//    @Option(names = ["--file", "-f"], description = ["Output file", "If omitted, the CPB filename with .cpi as a filename extension is used"])
//    var outputFileName: String? = null
//
//    @Option(names = ["--keystore", "-s"], required = true, description = ["Keystore holding siging keys"])
//    lateinit var keyStoreFileName: String
//
//    @Option(names = ["--storepass", "--password", "-p"], required = true, description = ["Keystore password"])
//    lateinit var keyStorePass: String
//
//    @Option(names = ["--key", "-k"], required = true, description = ["Key alias"])
//    lateinit var keyAlias: String

    companion object {
        /**
         * Check file exists and returns a Path object pointing to the file, throws error if file does not exist
         */
        private fun checkFileExists(fileName: String): Path {
            val path = Path.of(fileName)
            require(Files.isReadable(path)) { "\"$fileName\" does not exist or is not readable" }
            return path
        }
    }

    override fun run() {
        val cpbPath = packCpksToUnsignedCpb(cpks.asSequence(), false)
        println(cpbPath)
    }

    internal fun packCpksToUnsignedCpb(cpks: Sequence<String>, cpbUpgrade: Boolean): Path {
        val unsignedCpb = Files.createTempFile("temp-unsigned-cpb-${UUID.randomUUID()}", ".cpb")

        JarOutputStream(
            Files.newOutputStream(unsignedCpb, WRITE),
            Manifest().apply {
                mainAttributes.apply {
                    this[Attributes.Name.MANIFEST_VERSION] = "1.0"
                    this[Attributes.Name("Corda-CPB-Format")] = "2.0"
                    this[Attributes.Name("Corda-CPB-Upgrade")] = cpbUpgrade.toString()
                }
            }).use { cpb ->
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
}