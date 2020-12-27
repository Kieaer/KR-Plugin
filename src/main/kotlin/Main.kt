import arc.ApplicationListener
import arc.Core
import arc.files.Fi
import arc.util.CommandHandler
import command.ClientCommand
import command.ServerCommand
import core.Log
import data.DB
import essentials.special.DriverLoader
import event.Event
import mindustry.core.Version
import mindustry.mod.Plugin

class Main : Plugin() {
    companion object {
        val pluginRoot: Fi = Core.settings.dataDirectory.child("mods/KR-Plugin")
    }

    init {
        Log.info("플러그인 설정 시작. 서버 버전: ${Version.build}")

        // DB 드라이버 다운로드
        DriverLoader()

        // Database 시작
        DB

        // Database Table 생성
        DB.createTable()

        // 이벤트 등록
        Event.register()

        Core.app.addListener(object : ApplicationListener {
            override fun dispose() {
                DB.shutdownServer()
                Event.service.shutdown()
            }
        })
    }

    override fun init() {
        Log.info("플러그인 로드 완료!")
    }

    override fun registerServerCommands(handler: CommandHandler) {
        ServerCommand.register(handler)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        ClientCommand.register(handler)
    }
}