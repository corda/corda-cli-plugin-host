package net.corda.cli.application.utils

import java.io.File
import java.nio.file.Paths

class Files {
    companion object {

        fun cliHomeDir(): File {

            return if (System.getenv("CORDA_CLI_HOME_DIR")?.isNotEmpty() == true) {
                Paths.get(System.getenv("CORDA_CLI_HOME_DIR")).toFile()
            } else if (System.getProperty("CORDA_CLI_HOME_DIR")?.isNotEmpty() == true) {
                Paths.get(System.getProperty("CORDA_CLI_HOME_DIR")).toFile()
            } else {
                Paths.get(System.getProperty("user.home"), "/.corda/cli/").toFile()
            }
        }

        val profile: File by lazy {
            val profileFile = Paths.get(cliHomeDir().path, "/profile.yaml").toFile()
            if (!profileFile.exists()) {
                profileFile.createNewFile()
                profileFile.writeText("default:")
            }
            return@lazy profileFile
        }
    }
}
