package net.corda.cli.plugins.examples

import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemErrNormalized
import com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOutNormalized
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import picocli.CommandLine

class ExamplePluginTest {

    @Test
    fun testNoOptionCommand() {

        val app = ExamplePlugin.ExamplePluginEntry()

        val outText = tapSystemErrNormalized {
            CommandLine(
                app
            ).execute("")
        }

        assertTrue(
            outText.contains(
                "Usage: example-plugin [COMMAND]\n" +
                        "Example plugin using class based subcommands\n" +
                        "Commands:\n" +
                        "  sub-command  Example subcommand."
            )
        )
    }

    @Test
    fun testSubCommand() {

        val app = ExamplePlugin.ExamplePluginEntry()
        val outText = tapSystemOutNormalized {
            CommandLine(
                app
            ).execute("sub-command")
        }

        assertTrue(outText.contains("Hello from the example plugin!"))
    }
}