package event

import PluginVars.Companion.playerData
import core.Log
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

                    // 계정 인증 전까지 관리자 상태 해제
                    e.player.admin(false)
                }
                EventTypes.PlayerLeave -> {
                    val e = event as PlayerLeave

                    val data = playerData.find { d -> e.player.uuid() == d.uuid }
                    data.isLogged = false
                    PlayerCore.save(e.player.uuid())
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