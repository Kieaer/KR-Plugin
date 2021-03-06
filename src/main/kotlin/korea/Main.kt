package korea

import arc.ApplicationListener
import arc.Core
import arc.files.Fi
import arc.util.CommandHandler
import korea.PluginData.blacklist
import korea.PluginData.computeTime
import korea.PluginData.playerData
import korea.command.ClientCommand
import korea.command.Permissions
import korea.command.ServerCommand
import korea.core.DatabaseUpdater
import korea.core.DriverLoader
import korea.core.Log
import korea.core.PluginUpdater
import korea.data.Config
import korea.data.DB
import korea.data.auth.Discord
import korea.eof.sendMessage
import korea.eof.setPosition
import korea.event.Event
import mindustry.Vars
import mindustry.gen.Call
import mindustry.mod.Plugin

class Main : Plugin() {
    companion object {
        val pluginRoot: Fi = Core.settings.dataDirectory.child("mods/KR-Plugin")
        var isDispose: Boolean = false
    }

    init {
        Log.system("플러그인 초기화... 잠시 기다려 주세요")

        // 플러그인 업데이트 확인
        PluginUpdater()

        // IP 목록 업데이트
        DatabaseUpdater()

        // 플러그인 파일 생성
        PluginData.createFile()
        Config.createFile()

        // DB 드라이버 다운로드
        Log.system("DB 드라이버 설정")
        DriverLoader().init()

        // Database 서비스 시작 & Table 생성
        Log.system("DB 테이블 생성")
        DB.createTable()

        // 이벤트 등록
        Log.system("이벤트 트리거 설정")
        Event.register()

        // 플러그인 데이터 불러오기
        PluginData.load()
        Config.load()

        // 명령어 권한 데이터 불러오기
        Permissions.load()

        // 스레드 시작
        Threads
        Core.app.addListener(object : ApplicationListener {
            var tick = 0

            override fun init() {
                computeTime = System.nanoTime()
            }

            override fun update() {
                computeTime = System.nanoTime()
                tick++
                for(a in PluginData.keepTp){
                    if(!a.key.isNull) {
                        Call.setPosition(a.key.con(), a.value.x, a.value.y)
                    } else {
                        PluginData.keepTp.remove(a.key)
                    }
                }
            }

            override fun dispose() {
                isDispose = true

                DB.shutdownServer()
                Threads.worker.shutdown()
                Threads.timer.cancel()
                PluginData.save()
                Discord.stop()
                Permissions.save()

                Log.system("종료중.. 잠시 기다려주세요")
            }
        })

        Discord.start()
    }

    override fun init() {
        // 채팅 포맷 변경
        Vars.netServer.admins.addChatFilter { _, _ -> null }

        // 비 로그인 유저 통제
        Vars.netServer.admins.addActionFilter { e ->
            if(e.player == null) return@addActionFilter true
            for(a in blacklist) {
                if(a.contains(e.player.name, true)) {
                    return@addActionFilter false
                }
            }

            val data = playerData.find { d -> e.player.uuid() == d.uuid }
            if(data == null) {
                sendMessage(e.player, "이 서버는 계정 등록을 하지 않으면 플레이 하실 수 없습니다!\n먼저 [green]/register[] 명령어를 사용 해 보세요.")
                return@addActionFilter false
            } else if(Config.authType == Config.AuthType.Discord && !data.json.has("discord")) {
                sendMessage(e.player, "Discord 인증이 되어있지 않은 계정입니다.\n[green]/motd[] 명령어로 서버 주소를 확인하고, [green]/discord[] 명령어로 인증을 합니다.")
                return@addActionFilter false
            } else if(Config.authType == Config.AuthType.Discord && data.json.has("discord")) {
                return@addActionFilter true
            } else if(Config.authType == Config.AuthType.Password) {
                return@addActionFilter true
            } else false
        }

        Log.system("플러그인 로드 완료!")
    }

    override fun registerServerCommands(handler: CommandHandler) {
        ServerCommand.register(handler)
    }

    override fun registerClientCommands(handler: CommandHandler) {
        ClientCommand.register(handler)
    }
}