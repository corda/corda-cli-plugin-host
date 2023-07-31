package net.corda.cli.application.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class FilesTest {

    @Test
    fun testCliHomeDirNoEnvVar() {
        System.setProperty("CORDA_CLI_HOME_DIR", "")
        assertEquals(
            Paths.get(System.getProperty("user.home"), "/.corda/cli/").toFile().absolutePath,
            Files.cliHomeDir().absolutePath
        )
    }

    @Test
    fun testCliHomeDirWithEnvVar() {

        val workingDir = Paths.get("").toAbsolutePath().toString()
        System.setProperty("CORDA_CLI_HOME_DIR", "build/test-data/.cli-host/")
        assertEquals(
            Paths.get(workingDir,"build/test-data/.cli-host/").toString(),
            Files.cliHomeDir().absolutePath
        )
    }
}