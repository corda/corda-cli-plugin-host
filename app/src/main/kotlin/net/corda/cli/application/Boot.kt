package net.corda.cli.application

import net.corda.cli.api.CordaCliPlugin
import net.corda.cli.application.logger.LoggerStream
import org.pf4j.CompoundPluginDescriptorFinder
import org.pf4j.DefaultPluginManager
import org.pf4j.ManifestPluginDescriptorFinder
import picocli.CommandLine
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

fun main(vararg args: String) {
    Boot.run(*args)
}

@CommandLine.Command(
    name = "corda-cli"
)
class App {
    @CommandLine.Option(names = ["-h", "--help", "-?", "-help"], usageHelp = true, description = ["Display help and exit."])
    @Suppress("unused")
    var help = false
}

/**
 * A boot class that starts picocli and loads in plugin sub commands.
 */
object Boot {
    private class PluginManager(importPaths: List<Path>) : DefaultPluginManager(importPaths) {
        override fun createPluginDescriptorFinder(): CompoundPluginDescriptorFinder {
            return CompoundPluginDescriptorFinder()
                .add(ManifestPluginDescriptorFinder())
        }
    }

    @CommandLine.Spec
    @Suppress("unused")
    lateinit var spec: CommandLine.Model.CommandSpec

    fun run(vararg args: String) {
        // Setup loggers to redirect sysOut and sysErr
        LoggerStream.redirectSystemAndErrorOut()

        // Find and load the CLI plugins
        val pluginsDir = System.getProperty("pf4j.pluginsDir", "./plugins")
        val pluginManager = PluginManager(listOf(Paths.get(pluginsDir)))
        pluginManager.loadPlugins()
        pluginManager.startPlugins()

        // Retrieves the extensions for CordaCliPlugin extension point
        val cordaCliPlugins: List<CordaCliPlugin> = pluginManager.getExtensions(CordaCliPlugin::class.java)

        // Create the Command line app and add in the subcommands from the plugins.
        val commandLine = CommandLine(App())
        cordaCliPlugins.forEach { cordaCliPlugin ->
            commandLine.addSubcommand(cordaCliPlugin)
        }

        val commandResult = commandLine
            .setCaseInsensitiveEnumValuesAllowed(true)
            .execute(*args)
        pluginManager.stopPlugins()
        exitProcess(commandResult)
    }
}
