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
                "Usage: example-plugin [-hV] [COMMAND]\n" +
                        "Example plugin using class based subcommands\n" +
                        "  -h, --help      Show this help message and exit.\n" +
                        "  -V, --version   Print version information and exit.\n" +
                        "Commands:\n" +
                        "  sub-command  Example subcommand."
            )
        ) { outText }
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

    @Test
    fun testUnknownCommand() {

        val app = ExamplePlugin.ExamplePluginEntry()
        val outText = tapSystemErrNormalized {
            CommandLine(
                app
            ).execute("unknown-command")
        }

        assertTrue(outText.contains("Unmatched argument at index 0: 'unknown-command'"), "Actual: $outText")
    }
}