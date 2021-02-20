package korea.event

import arc.Core
import korea.Main.Companion.pluginRoot
import korea.PluginData
import korea.command.Permissions
import korea.command.ServerCommand
import korea.core.Log
import korea.data.Config
import korea.data.Config.AuthType.*
import korea.data.PlayerCore
import korea.eof.connect
import korea.eof.sendMessage
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.content.Blocks
import mindustry.game.EventType.*
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.net.Administration

class EventThread(private val type: EventTypes, private val event: Any) : Thread() {
    override fun run() {
        try {
            when (type) {
                EventTypes.Config -> {
                    val e = event as ConfigEvent
                    if (e.player != null && e.tile != null && e.tile.block != null) {
                        Log.write(
                            Log.LogType.Activity,
                            "${e.player.name} 가 ${e.tile.tileX()},${e.tile.tileY()} 에 있는 ${e.tile.block.name} 의 설정을 변경함"
                        )
                    }
                }
                EventTypes.Tap -> {
                    val e = event as TapEvent
                    Log.write(Log.LogType.Activity, "${e.player.name} 가 타일(${e.tile.x},${e.tile.y})을 클릭함")
                }
                EventTypes.Withdraw -> {
                    val e = event as WithdrawEvent
                    Log.write(
                        Log.LogType.Activity,
                        "${e.player.name} 가 타일(${e.tile.x},${e.tile.y})에 있는 ${e.tile.block.name} 에 ${e.item.name} 을 ${e.amount} 개 넣었음"
                    )
                }
                EventTypes.Gameover -> {
                    val e = event as GameOverEvent
                    if (Vars.state.rules.pvp) {
                        var index = 5
                        for (a in 0..4) {
                            if (Vars.state.teams[Team.all[index]].cores.isEmpty) {
                                index--
                            }
                        }
                        if (index == 1) {
                            for (player in Groups.player) {
                                val target = PluginData[player.uuid()]
                                if (target!!.isLogged) {
                                    if (player.team().name == e.winner.name) {
                                        target.pvpWinner++
                                    } else if (player.team().name != e.winner.name) {
                                        target.pvpLoser++
                                    }
                                }
                            }
                        }
                    } else if (Vars.state.rules.attackMode) {
                        for (p in Groups.player) {
                            val target = PluginData[p.uuid()]
                            if (target != null) {
                                target.attackWinner++
                            }
                        }
                    }
                }
                EventTypes.WorldLoad -> {
                    PluginData.worldTime = 0L
                }
                EventTypes.PlayerConnect -> {
                    val e = event as PlayerConnect
                    for (a in PluginData.banned){
                        if (a.uuid == e.player.uuid() || a.address == e.player.con().address){
                            connect(e.player, "mindustry.ru", 6567)
                            Log.info("${e.player.name} 유저는 밴을 당했으므로 ru 서버로 이동 시킵니다.")
                            return
                        }
                    }

                    /*if (netServer.admins.findByName(e.player.name).size != 1) {
                        Call.kick(e.player.con, "사용할 수 없는 계정입니다")
                    } else {
                        val ip = e.player.con.address

                        val br = pluginRoot.child("data/ipv4.txt").reader(1024)
                        br.use {
                            var line: String
                            while (br.readLine().also { line = it } != null) {
                                val match = IpAddressMatcher(line)
                                if (match.matches(ip)) {
                                    Call.kick(e.player.con, "VPN 사용 ㄴㄴ")
                                }
                            }
                        }
                    }*/
                }
                EventTypes.Deposit -> {

                }
                EventTypes.PlayerJoin -> {
                    val e = event as PlayerJoin
                    val uuid = e.player.uuid()

                    // 접속 인원 카운트
                    PluginData.totalConnected++

                    // 계정 인증 전까지 관리자 상태 해제
                    e.player.admin(false)

                    // motd 표시
                    if (!Administration.Config.motd.string().equals("off", ignoreCase = true)) {
                        Call.infoMessage(e.player.con, Administration.Config.motd.string())
                    } else {
                        Call.infoMessage(e.player.con, pluginRoot.child("motd/motd.txt").readString("UTF-8"))
                    }

                    // 자동 로그인
                    if (PlayerCore.check(uuid)) {
                        if(PlayerCore.load(e.player)) {
                            sendMessage(e.player, "자동 로그인이 되었습니다!")
                        } else {
                            sendMessage(e.player, "잘못된 경로로 접근된 계정입니다! 서버 관리자에게 문의하세요.")
                        }
                    } else {
                        val message: String
                        when (Config.authType) {
                            None -> {
                                message = ""
                            }
                            Password -> {
                                message = """
                                계정에 로그인 할려면 채팅창을 열고 [green]/login <ID> <비밀번호>[] 를 입력하세요.
                                계정이 없다면 [green]/register <새 비밀번호>[] 을 입력하세요.
                            """.trimIndent()
                            }
                            Discord -> {
                                message = """
                                이 서버는 Discord 서버 인증을 필요로 합니다!
                                https://discord.gg/Uhn5NRGsya 에서 계정 인증을 진행 해 주세요!
                            """.trimIndent() // TODO (PIN 번호 인증 만들기)
                            }
                            Kakaotalk -> {
                                message = """
                                이 서버는 카카오톡 인증을 필요로 합니다!
                                https://open.kakao.com/o/gogTcpyb 에서 서버 봇에게 계정 생성을 요청하세요!
                            """.trimIndent()
                            }
                        }
                        sendMessage(e.player, message)
                    }
                }
                EventTypes.PlayerLeave -> {
                    val e = event as PlayerLeave
                    val uuid = e.player.uuid()

                    val data = PluginData[uuid]
                    if (data != null) {
                        data.isLogged = false
                        PlayerCore.save(uuid)
                        PluginData.remove(uuid)
                    }
                }
                EventTypes.PlayerChat -> {
                    val e = event as PlayerChatEvent
                    val uuid = e.player.uuid()

                    val data = PluginData[uuid]
                    val type = if (data == null) "[비 로그인] " else if (data.isMute) "[묵언] " else ""

                    if (!e.message.startsWith("/")) {
                        if (data == null) Call.sendMessage(
                            "[#${
                                e.player.color.toString().toUpperCase()
                            }]${e.player.name} [orange]> [white] ${e.message}"
                        )

                        // 채팅 내용을 기록에 저장
                        Log.info("$type${e.player.name}: ${e.message}")
                        Log.write(Log.LogType.Chat, "$type ${e.player.name}: ${e.message}")

                        if (data != null) {
                            if (data.isMute) {
                                sendMessage(e.player, "[scarlet]당신은 누군가에 의해 묵언 처리가 되었습니다.")
                            } else {
                                if (Permissions.userData.has(data.uuid)) {
                                    sendMessage(
                                        (if(data.json.has("discord")) "[#738ADB][] " else "") + Permissions.userData.get(data.uuid).asObject().getString("chatFormat", "")
                                            .replace(
                                                "%1", "[#${e.player.color.toString().toUpperCase()}]${e.player.name}"
                                            ).replace("%2", e.message)
                                    )
                                } else {
                                    sendMessage(
                                        "[#${
                                            e.player.color.toString().toUpperCase()
                                        }]${e.player.name} [orange]> [white] ${e.message}"
                                    )
                                }
                            }
                        }
                    } else {
                        Log.write(Log.LogType.Command, "${e.player.name}: ${e.message}")
                    }
                }
                EventTypes.BlockBuildEnd -> {
                    val e = event as BlockBuildEndEvent

                    if(e.unit.isPlayer) {
                        val player = e.unit.player
                        val data = PluginData[player.uuid()]

                        if (player != null && data != null) {
                            if (e.breaking && player.unit() != null && player.unit().buildPlan() != null && e.tile.block() !== Blocks.air) data.breakCount++
                        }
                    }
                }
                EventTypes.BuildSelect -> {
                    val e = event as BuildSelectEvent
                    if (e.breaking && e.builder != null && e.builder.buildPlan() != null && e.builder.isPlayer) {
                        val player = e.builder.player

                        val data = PluginData[player.uuid()]
                        if (data != null) {
                            data.breakCount++
                        }
                    }
                }
                EventTypes.UnitDestroy -> {
                    val e = event as UnitDestroyEvent
                }
                EventTypes.PlayerBan -> {
                    val e = event as PlayerBanEvent
                    connect(e.player, "mindustry.ru", 6567)
                    PluginData.banned.add(PluginData.Banned(e.player.name, e.player.con().address, e.player.uuid()))
                    Core.app.post {netServer.admins.unbanPlayerID(e.player.uuid())}
                }
                EventTypes.PlayerIpBan -> {
                    val e = event as PlayerIpBanEvent
                    val uuid = netServer.admins.findByIP(e.ip)
                    PluginData.banned.add(PluginData.Banned(if(uuid != null) uuid.lastName else "none", e.ip, if(uuid != null) uuid.id else "none"))
                    Core.app.post {netServer.admins.unbanPlayerIP(e.ip)}
                }
                EventTypes.PlayerUnban -> {
                    val e = event as PlayerUnbanEvent
                    if (e.player != null) {
                        if (ServerCommand.isUnBan) {
                            PluginData.banned.remove { e.player.uuid() == it.uuid }
                        }
                        Core.app.post {netServer.admins.unbanPlayerID(e.player.uuid())}
                    }
                }
                EventTypes.PlayerIpUnban -> {
                    val e = event as PlayerIpUnbanEvent
                    if(ServerCommand.isUnBan){
                        PluginData.banned.remove { e.ip == it.address }
                    }
                        Core.app.post {netServer.admins.unbanPlayerIP(e.ip)}
                }
                EventTypes.ServerLoaded -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    enum class EventTypes{
        Config, Tap, Withdraw, Gameover, WorldLoad, PlayerConnect, Deposit, PlayerJoin, PlayerLeave, PlayerChat, BlockBuildEnd, BuildSelect, UnitDestroy, PlayerBan, PlayerIpBan, PlayerUnban, PlayerIpUnban, ServerLoaded;
    }
}