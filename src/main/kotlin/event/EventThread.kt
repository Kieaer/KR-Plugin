package event

import PluginData
import core.Log
import data.Config
import data.Config.authTypes.*
import data.PlayerCore
import mindustry.core.NetClient
import mindustry.game.EventType.*
import mindustry.gen.Call

class EventThread(private val type: EventTypes, private val event: Any) : Thread() {
    override fun run() {
        try {
            when (type) {
                EventTypes.Config -> {
                    val e = event as ConfigEvent
                }
                EventTypes.Tap -> {
                    val e = event as TapEvent
                }
                EventTypes.Withdraw -> {
                    val e = event as WithdrawEvent
                }
                EventTypes.Gameover -> {
                    val e = event as GameOverEvent
                }
                EventTypes.WorldLoad -> {
                    val e = event as WorldLoadEvent
                }
                EventTypes.PlayerConnect -> {
                    val e = event as PlayerConnect
                }
                EventTypes.Deposit -> {
                    val e = event as DepositEvent
                }
                EventTypes.PlayerJoin -> {
                    val e = event as PlayerJoin
                    val uuid = e.player.uuid()

                    // 계정 인증 전까지 관리자 상태 해제
                    e.player.admin(false)

                    // 자동 로그인
                    if(PlayerCore.check(uuid)){
                        PlayerCore.load(e.player)
                    }

                    val message: String
                    when (Config.authType){
                        none -> {
                            message = ""
                        }
                        password -> {
                            message = """
                                계정에 로그인 할려면 채팅창을 열고 [green]/login <ID> <비밀번호>[] 를 입력하세요.
                                계정이 없다면 [green]/register <새 ID> <새 비밀번호> <비밀번호 재입력>[] 을 입력하세요.
                            """.trimIndent()
                        }
                        discord -> {
                            message = """
                                이 서버는 Discord 서버 인증을 필요로 합니다!
                                https://discord.gg/Uhn5NRGsya 에서 계정 인증을 진행 해 주세요!
                            """.trimIndent()
                            // TODO (PIN 번호 인증 만들기)
                        }
                        kakaotalk -> {
                            message = """
                                이 서버는 카카오톡 인증을 필요로 합니다!
                                https://open.kakao.com/o/gogTcpyb 에서 서버 봇에게 계정 생성을 요청하세요!
                            """.trimIndent()
                        }
                    }
                    e.player.sendMessage(message)
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

                    // 채팅 내용 출력
                    Call.sendMessage("${NetClient.colorizeName(e.player.id, e.player.name)} [orange]>[white] ${e.message}")

                    // 채팅 내용을 기록에 저장
                    Log.write(Log.LogType.Chat, "${e.player.name}: ${e.message}")
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