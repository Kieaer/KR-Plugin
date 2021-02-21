package korea.event.feature

import arc.Core
import arc.files.Fi
import arc.struct.Seq
import arc.util.async.Threads.sleep
import korea.eof.sendMessage
import mindustry.Vars
import mindustry.core.GameState
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.io.SaveIO
import mindustry.maps.Map

object AutoRollback {
    val savePath: Fi = Vars.saveDirectory.child("Rollback.${Vars.saveExtension}")
    var isVote = false

    fun load(map: Map?) {
        val players = Seq<Player>()
        for (p in Groups.player) {
            players.add(p)
            p.dead()
        }
        Vars.state.serverPaused = true
        if(map == null) {
            sleep(10000)
        }

        Core.app.post{
            Call.worldDataBegin()
            Vars.logic.reset()

            try {
                if (map != null) {
                    Vars.world.loadMap(map)
                } else {
                    SaveIO.load(savePath)
                }

                Vars.state.rules.sector = null
                Vars.state.set(GameState.State.playing)

                for (p in players) {
                    if (p.con() == null) continue
                    p.reset()
                    if (Vars.state.rules.pvp) {
                        p.team(Vars.netServer.assignTeam(p, Seq.SeqIterable(players)))
                    }
                    Vars.netServer.sendWorldData(p)
                    p.sendMessage("다른 플레이어들을 기다리는 중입니다...")
                }
                Vars.state.serverPaused = false
                Vars.logic.play()
            } catch (t: Exception) {
                t.printStackTrace()
                sendMessage("[scarlet][CRITICAL] ${t.cause.toString()}")
            }
            if (Vars.state.`is`(GameState.State.playing) && !isVote) sendMessage("[green]빽섭 완료.")
            isVote = false
        }
    }
}