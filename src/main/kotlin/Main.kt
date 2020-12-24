import arc.ApplicationListener
import arc.Core
import arc.files.Fi
import arc.util.CommandHandler
import command.ClientCommand
import command.ServerCommand
import data.DB
import essentials.special.DriverLoader
import mindustry.mod.Plugin

class Main : Plugin() {
    companion object {
        val pluginRoot: Fi = Core.settings.dataDirectory.child("PluginData")
    }

    init {
        // DB 드라이버 다운로드
        DriverLoader()

        // Database 시작
        DB

        Core.app.addListener(object : ApplicationListener {
            override fun dispose() {
                DB.shutdownServer()
            }
        })
    }

    override fun registerServerCommands(handler: CommandHandler) {
        ServerCommand.register(handler)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        ClientCommand.register(handler)
    }
}