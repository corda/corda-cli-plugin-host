package net.corda.cli.plugins.examples

import picocli.CommandLine

@CommandLine.Command(name = "sub-command", description = ["Example subcommand."], mixinStandardHelpOptions = true)
class SubCommandOne : Runnable {
    override fun run() {
        println("Hello from the example plugin!")
    }
}