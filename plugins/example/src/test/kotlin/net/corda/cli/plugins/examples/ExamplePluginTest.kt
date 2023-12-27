package net.corda.cli.plugins.examples

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ExamplePluginTest {
    
    companion object {
        private fun tapSystemErr(function: () -> Int): String {
            val initial = System.err

            val byteArrayOutputStream = ByteArrayOutputStream()
            System.setErr(PrintStream(byteArrayOutputStream))
            try {
                function()
            } finally {
                System.setErr(initial)
            }

            return String(byteArrayOutputStream.toByteArray()).replace("\r\n", "\n")
        }

        private fun tapSystemOut(function: () -> Int): String {
            val initial = System.out

            val byteArrayOutputStream = ByteArrayOutputStream()
            System.setOut(PrintStream(byteArrayOutputStream))
            try {
                function()
            } finally {
                System.setOut(initial)
            }

            return String(byteArrayOutputStream.toByteArray()).replace("\r\n", "\n")
        }

        private val colorScheme = CommandLine.Help.ColorScheme.Builder().ansi(CommandLine.Help.Ansi.OFF).build()
    }

    @Test
    fun testNoOptionCommand() {

        val app = ExamplePlugin.ExamplePluginEntry()

        val outText = tapSystemErr {
            CommandLine(app)
                .setColorScheme(colorScheme)
                .execute("")
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
        val outText = tapSystemOut {
            CommandLine(
                app
            ).execute("sub-command")
        }

        assertTrue(outText.contains("Hello from the example plugin!"))
    }

    @Test
    fun testUnknownCommand() {

        val app = ExamplePlugin.ExamplePluginEntry()
        val outText = tapSystemErr {
            CommandLine(
                app
            ).execute("unknown-command")
        }

        assertTrue(outText.contains("Unmatched argument at index 0: 'unknown-command'"), "Actual: $outText")
    }
}