package net.corda.cli.application.utils

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class FilesTest {

    @Test
    fun testCliHomeDirNoEnvVar() {

        EnvironmentVariables("CORDA_CLI_HOME_DIR", "").execute {
            assertEquals(
                Paths.get(System.getProperty("user.home"), "/.corda/cli/").toFile().absolutePath,
                Files.cliHomeDir().absolutePath
            )
        }
    }

    @Test
    fun testCliHomeDirWithEnvVar() {

        val workingDir = Paths.get("").toAbsolutePath().toString()

        EnvironmentVariables("CORDA_CLI_HOME_DIR", "build/test-data/.cli-host/").execute {
            assertEquals(
                Paths.get(workingDir,"build/test-data/.cli-host/").toString(),
                Files.cliHomeDir().absolutePath
            )
        }
    }
}