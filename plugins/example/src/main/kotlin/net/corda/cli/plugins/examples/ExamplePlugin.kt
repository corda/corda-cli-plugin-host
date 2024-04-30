package net.corda.cli.plugins.examples

import org.pf4j.Extension
import org.pf4j.Plugin
import net.corda.cli.api.CordaCliPlugin
import net.corda.cli.api.AbstractCordaCliVersionProvider
import picocli.CommandLine

/**
 * An example plugin that uses class based subcommands
 */
class ExamplePlugin : Plugin() {

    override fun start() {
    }

    override fun stop() {
    }

    class VersionProvider : AbstractCordaCliVersionProvider()

    @Extension
    @CommandLine.Command(
        name = "example-plugin",
        subcommands = [SubCommandOne::class],
        description = ["Example plugin using class based subcommands"],
        mixinStandardHelpOptions = true,
        versionProvider = VersionProvider::class
    )
    class ExamplePluginEntry : CordaCliPlugin
}
