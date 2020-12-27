package event

import PluginVars.Companion.playerData
import core.Log
import data.Config
import data.PlayerCore
import mindustry.game.EventType.*

class EventThread(private val type: EventTypes, private val event: Any) : Thread() {
    override fun run() {
        when (type) {
            EventTypes.Config -> {
                val e = event as ConfigEvent

                if(Config.debug) Log.info("Config Event!")
            }
            EventTypes.Tap -> {
                val e = event as TapEvent

                if(Config.debug) Log.info("Tap Event!")
            }
            EventTypes.Withdraw -> {
                val e = event as WithdrawEvent

                if(Config.debug) Log.info("Withdraw Event!")
            }
            EventTypes.Gameover -> {
                val e = event as GameOverEvent

                if(Config.debug) Log.info("Withdraw Event!")
            }
            EventTypes.WorldLoad -> {
                val e = event as WorldLoadEvent

                if(Config.debug) Log.info("WorldLoad Event!")
            }
            EventTypes.PlayerConnect -> {
                val e = event as PlayerConnect

                if(Config.debug) Log.info("PlayerConnect Event!")
            }
            EventTypes.Deposit -> {
                val e = event as DepositEvent

                if(Config.debug) Log.info("Deposit Event!")
            }
            EventTypes.PlayerJoin -> {
                val e = event as PlayerJoin

                if(Config.debug) Log.info("PlayerJoin Event!")

                // 계정 인증 전까지 관리자 상태 해제
                e.player.admin(false)
            }
            EventTypes.PlayerLeave -> {
                val e = event as PlayerLeave

                if(Config.debug) Log.info("PlayerLeave Event!")

                val data = playerData.find { d -> e.player.uuid() == d.uuid }
                data.isLogged = false
                PlayerCore.save(e.player.uuid())
            }
            EventTypes.PlayerChat -> {
                val e = event as PlayerChatEvent

                if(Config.debug) Log.info("PlayerChat Event!")

                // 채팅 내용을 기록에 저장
                Log.write(Log.LogType.chat, "${e.player.name}: ${e.message}")
            }
            EventTypes.BlockBuildEnd -> {
                val e = event as BlockBuildEndEvent

                if(Config.debug) Log.info("BlockBuild Event!")
            }
            EventTypes.BuildSelect -> {
                val e = event as BuildSelectEvent

                if(Config.debug) Log.info("BuildSelect Event!")
            }
            EventTypes.UnitDestroy -> {
                val e = event as UnitDestroyEvent

                if(Config.debug) Log.info("UnitDestroy Event!")
            }
            EventTypes.PlayerBan -> {
                val e = event as PlayerBanEvent

                if(Config.debug) Log.info("PlayerBan Event!")
            }
            EventTypes.PlayerIpBan -> {
                val e = event as PlayerIpBanEvent

                if(Config.debug) Log.info("PlayerIpBan Event!")
            }
            EventTypes.PlayerUnban -> {
                val e = event as PlayerUnbanEvent

                if(Config.debug) Log.info("PlayerUnban Event!")
            }
            EventTypes.PlayerIpUnban -> {
                val e = event as PlayerIpUnbanEvent

                if(Config.debug) Log.info("PlayerIpUnban Event!")
            }
            EventTypes.ServerLoaded -> {
                val e = event as ServerLoadEvent

                if(Config.debug) Log.info("ServerLoaded Event!")
            }
        }
    }

    enum class EventTypes{
        Config, Tap, Withdraw, Gameover, WorldLoad, PlayerConnect, Deposit, PlayerJoin, PlayerLeave, PlayerChat, BlockBuildEnd, BuildSelect, UnitDestroy, PlayerBan, PlayerIpBan, PlayerUnban, PlayerIpUnban, ServerLoaded;
    }
}