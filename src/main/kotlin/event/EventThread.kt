package event

import Main.Companion.pluginRoot
import PluginData
import core.Log
import data.Config
import data.Config.AuthType.*
import data.PlayerCore
import external.IpAddressMatcher
import mindustry.Vars
import mindustry.core.NetClient
import mindustry.game.EventType.*
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups

class EventThread(private val type: EventTypes, private val event: Any) : Thread() {
    override fun run() {
        try {
            when (type) {
                EventTypes.Config -> {
                    val e = event as ConfigEvent
                    Log.write(Log.LogType.Activity, "${e.player.name} 가 ${e.tile.tileX()},${e.tile.tileY()} 에 있는 ${e.tile.block.name} 의 설정을 변경함")
                }
                EventTypes.Tap -> {
                    val e = event as TapEvent
                    Log.write(Log.LogType.Activity, "${e.player.name} 가 타일(${e.tile.x},${e.tile.y})을 클릭함")
                }
                EventTypes.Withdraw -> {
                    val e = event as WithdrawEvent
                    Log.write(Log.LogType.Activity, "${e.player.name} 가 타일(${e.tile.x},${e.tile.y})에 있는 ${e.tile.block.name} 에 ${e.item.name} 을 ${e.amount} 개 넣었음")
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
                            for (player in Groups.player){
                                val target = PluginData[player.uuid()]
                                if (target!!.isLogged) {
                                    if (player.team().name == e.winner.name) {
                                        target.pvpWinner = target.pvpWinner + 1
                                    } else if (player.team().name != e.winner.name) {
                                        target.pvpLoser = target.pvpLoser + 1
                                    }
                                }
                            }
                        }
                    } else if (Vars.state.rules.attackMode) {
                        for (p in Groups.player) {
                            val target = PluginData[p.uuid()]
                            if (target!!.isLogged) {
                                target.attackWinner = target.attackWinner + 1
                            }
                        }
                    }
                }
                EventTypes.WorldLoad -> {
                    val e = event as WorldLoadEvent
                }
                EventTypes.PlayerConnect -> {
                    val e = event as PlayerConnect
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
                }
                EventTypes.Deposit -> {
                    val e = event as DepositEvent
                }
                EventTypes.PlayerJoin -> {
                    val e = event as PlayerJoin
                    val uuid = e.player.uuid()

                    // 접속 인원 카운트
                    PluginData.totalConnected = PluginData.totalConnected++

                    // 계정 인증 전까지 관리자 상태 해제
                    e.player.admin(false)

                    // 자동 로그인
                    if(PlayerCore.check(uuid)){
                        PlayerCore.load(e.player)
                        e.player.sendMessage("자동 로그인이 되었습니다!")
                    } else {
                        val message: String
                        when (Config.authType) {
                            None -> {
                                message = ""
                            }
                            Password -> {
                                message = """
                                계정에 로그인 할려면 채팅창을 열고 [green]/login <ID> <비밀번호>[] 를 입력하세요.
                                계정이 없다면 [green]/register <새 ID> <새 비밀번호> <비밀번호 재입력>[] 을 입력하세요.
                            """.trimIndent()
                            }
                            Discord -> {
                                message = """
                                이 서버는 Discord 서버 인증을 필요로 합니다!
                                https://discord.gg/Uhn5NRGsya 에서 계정 인증을 진행 해 주세요!
                            """.trimIndent()
                                // TODO (PIN 번호 인증 만들기)
                            }
                            Kakaotalk -> {
                                message = """
                                이 서버는 카카오톡 인증을 필요로 합니다!
                                https://open.kakao.com/o/gogTcpyb 에서 서버 봇에게 계정 생성을 요청하세요!
                            """.trimIndent()
                            }
                        }
                        e.player.sendMessage(message)
                    }
                }
                EventTypes.PlayerLeave -> {
                    val e = event as PlayerLeave
                    val uuid = e.player.uuid()

                    val data = PluginData[uuid]
                    if(data != null) {
                        data.isLogged = false
                        PlayerCore.save(uuid)
                        PluginData.remove(uuid)
                    }
                }
                EventTypes.PlayerChat -> {
                    val e = event as PlayerChatEvent
                    val uuid = e.player.uuid()

                    if (!e.message.startsWith("/")) {
                        // 채팅 내용 출력
                        val data = PluginData[uuid]
                        if(data!!.isMute){
                            e.player.sendMessage("[scarlet]당신은 누군가에 의해 묵언 처리가 되었습니다.")
                        } else {
                            Call.sendMessage("${NetClient.colorizeName(e.player.id, e.player.name)} [orange]>[white] ${e.message}")
                        }

                        // 채팅 내용을 기록에 저장
                        Log.write(Log.LogType.Chat, "${if(data.isMute) "[묵언] " else ""}${e.player.name}: ${e.message}")
                    } else {
                        Log.write(Log.LogType.Command, "${e.player.name}: ${e.message}")
                    }
                }
                EventTypes.BlockBuildEnd -> {
                    val e = event as BlockBuildEndEvent
                }
                EventTypes.BuildSelect -> {
                    val e = event as BuildSelectEvent
                }
                EventTypes.UnitDestroy -> {
                    val e = event as UnitDestroyEvent
                }
                EventTypes.PlayerBan -> {
                    val e = event as PlayerBanEvent
                }
                EventTypes.PlayerIpBan -> {
                    val e = event as PlayerIpBanEvent
                }
                EventTypes.PlayerUnban -> {
                    val e = event as PlayerUnbanEvent
                }
                EventTypes.PlayerIpUnban -> {
                    val e = event as PlayerIpUnbanEvent
                }
                EventTypes.ServerLoaded -> {
                    val e = event as ServerLoadEvent
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    enum class EventTypes{
        Config, Tap, Withdraw, Gameover, WorldLoad, PlayerConnect, Deposit, PlayerJoin, PlayerLeave, PlayerChat, BlockBuildEnd, BuildSelect, UnitDestroy, PlayerBan, PlayerIpBan, PlayerUnban, PlayerIpUnban, ServerLoaded;
    }
}