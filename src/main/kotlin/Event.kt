
import PluginVars.Companion.playerData
import arc.Events
import data.PlayerCore
import mindustry.game.EventType.PlayerLeave

class Event {
    fun register(){
        Events.on(PlayerLeave::class.java) { e: PlayerLeave ->
            val data = playerData.find { d -> e.player.uuid() == d.uuid }
            data.isLogged = false
            PlayerCore.save(e.player.uuid())
        }
    }
}