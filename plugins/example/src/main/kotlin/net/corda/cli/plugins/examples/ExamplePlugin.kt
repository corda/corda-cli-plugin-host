package net.corda.cli.plugins.examples

import org.pf4j.Extension
import org.pf4j.Plugin
import net.corda.cli.api.CordaCliPlugin
import picocli.CommandLine

/**
 * An example plugin that uses class based subcommands
 */
class ExamplePlugin : Plugin() {

    override fun start() {
    }

    override fun stop() {
    }

    @Extension
    @CommandLine.Command(
        name = "example-plugin",
        subcommands = [SubCommandOne::class],
        description = ["Example plugin using class based subcommands"],
        mixinStandardHelpOptions = true
    )
    class ExamplePluginEntry : CordaCliPlugin
}
