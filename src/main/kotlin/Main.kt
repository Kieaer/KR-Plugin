
import PluginData.playerData
import arc.ApplicationListener
import arc.Core
import arc.files.Fi
import arc.util.CommandHandler
import command.ClientCommand
import command.Permissions
import command.ServerCommand
import core.DatabaseUpdater
import core.DriverLoader
import core.Log
import core.PluginUpdater
import data.DB
import data.auth.Discord
import event.Event
import mindustry.Vars
import mindustry.mod.Plugin

class Main : Plugin() {
    companion object {
        val pluginRoot: Fi = Core.settings.dataDirectory.child("mods/KR-Plugin")
    }

    init {
        Log.info("플러그인 초기화... 잠시 기다려 주세요")

        // 플러그인 업데이트 확인
        PluginUpdater()

        // IP 목록 업데이트
        DatabaseUpdater()

        // DB 드라이버 다운로드
        Log.info("DB 드라이버 설정")
        DriverLoader().init()

        // Database 서비스 시작 & Table 생성
        Log.info("DB 테이블 생성")
        DB.createTable()

        // 이벤트 등록
        Log.info("이벤트 트리거 설정")
        Event.register()

        // 플러그인 파일 생성
        PluginData.createFile()

        // 플러그인 데이터 불러오기
        PluginData.load()

        // 명령어 권한 데이터 불러오기
        Permissions.load(true)

        // 스레드 시작
        Threads

        Core.app.addListener(object : ApplicationListener {
            override fun dispose() {
                DB.shutdownServer()
                Event.service.shutdown()
                ClientCommand.service.shutdown()
                ServerCommand.service.shutdown()
                Threads.worker.shutdown()
                Threads.timer.cancel()
                PluginData.save()
            }
        })

        Discord.start()
    }

    override fun init() {
        // 채팅 포맷 변경
        Vars.netServer.admins.addChatFilter { _, _ -> null }

        // 비 로그인 유저 통제
        Vars.netServer.admins.addActionFilter { e ->
            if (e.player == null) return@addActionFilter true
            return@addActionFilter playerData.find { d -> e.player.uuid() == d.uuid } != null
        }

        Log.info("플러그인 로드 완료!")
    }

    override fun registerServerCommands(handler: CommandHandler) {
        ServerCommand.register(handler)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        ClientCommand.register(handler)
    }
}