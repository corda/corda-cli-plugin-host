package net.corda.cli.plugins.packaging.cpb

import net.corda.cli.api.CordaCliPlugin
import org.pf4j.Extension
import org.pf4j.Plugin
import org.pf4j.PluginWrapper
import picocli.CommandLine

@Suppress("unused")
class CreateCpbPluginWrapper(wrapper: PluginWrapper) : Plugin(wrapper) {

    @Extension
    @CommandLine.Command(
        name = "create-cpb",
        subcommands = [CreateCpb::class],
        description = ["create-cpb-description."]
    )
    class CreateCpbPlugin : CordaCliPlugin
}