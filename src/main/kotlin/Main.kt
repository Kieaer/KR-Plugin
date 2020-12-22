import arc.Core
import arc.files.Fi
import arc.util.CommandHandler
import command.ClientCommand
import command.ServerCommand
import mindustry.mod.Plugin

class Main : Plugin() {
    companion object {
        val pluginRoot: Fi = Core.settings.dataDirectory.child("PluginData")
    }

    init {
        PlayerData.admin = true
    }

    override fun registerServerCommands(handler: CommandHandler) {
        ServerCommand.register(handler)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        ClientCommand.register(handler)
    }
}