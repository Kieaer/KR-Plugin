package korea.event.feature

import arc.ApplicationListener
import arc.Core
import arc.Events
import arc.graphics.Color
import arc.struct.ObjectMap
import arc.util.Align
import arc.util.Time
import arc.util.Time.nanosPerMilli
import arc.util.async.Threads.sleep
import korea.PluginData
import korea.data.Config
import korea.eof.infoPopup
import korea.eof.kick
import korea.eof.sendMessage
import korea.event.feature.VoteType.*
import korea.event.feature.VoteType.Map
import korea.exceptions.ErrorReport
import mindustry.Vars
import mindustry.Vars.content
import mindustry.Vars.netServer
import mindustry.content.Bullets
import mindustry.content.Fx
import mindustry.content.Weathers
import mindustry.entities.Effect
import mindustry.entities.bullet.BulletType
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.*
import mindustry.net.Packets
import java.time.LocalTime
import kotlin.random.Random

class Vote(val player: Playerc, val type: VoteType) {
    val task = object : ApplicationListener {
        var tick = 60
        var time = 60
        var alertTime = 0

        override fun update() {
            if(tick == 60) {
                infoPopup(
                    "투표 시작한 유저: ${player.name()}[white]\n" +
                            "$type 투표 종료까지 ${time}초\n" +
                            "${voted.size} 명 투표. 필요 투표 인원: ${require - voted.size}", 1f, Align.left, 0, 0, 0, 0
                         )
                tick = 0
                time--
            } else {
                tick++
            }

            if(alertTime == 0 || alertTime == 600 || alertTime == 1200 || alertTime == 1800 || alertTime == 2400 || alertTime == 3000){
                sendMessage("투표 종료까지 ${60 - (alertTime/60)}초 남았습니다.")
            } else if(alertTime == 3600){
                passed()
            }
            alertTime++

        }
    }

    val voted = ObjectMap<String, String>()
    val require: Int = if (Config.debug) 1 else 3 + if (Groups.player.size() > 5) 1 else 0

    var target: Playerc? = null
    var world: mindustry.maps.Map? = null
    var skipCount: Int = 3

    fun start() {
        Core.app.addListener(task)
    }

    fun passed() {
        Core.app.removeListener(task)
        try {
            if(voted.size >= require || player.ip() == "169.254.37.115") {
                when(type) {
                    Kick -> {
                        sendMessage("강퇴 투표가 통과 되었습니다!")
                        kick(target!!, Packets.KickReason.vote)
                    }
                    Map -> {
                        sendMessage("맵 투표가 통과 되었습니다!")
                        AutoRollback.isVote = true
                        AutoRollback.load(world)
                    }
                    Gameover -> {
                        sendMessage("항복 투표가 통과 되었습니다! 10초후 진행.")
                        Vars.state.serverPaused = true
                        sleep(10000)
                        Events.fire(EventType.GameOverEvent(Team.crux))
                    }
                    Skipwave -> {
                        sendMessage("웨이브 넘기기 투표가 통과 되었습니다!")
                        for(a in 0..skipCount) Vars.logic.runWave()
                    }
                    Rollback -> {
                        AutoRollback.load(null)
                        PluginData.lastVoted = LocalTime.now()
                    }
                    Fast -> {
                        sendMessage("웨이브 고속 모드 투표가 통과 되었습니다!")
                        Vars.state.rules.waveSpacing = 1800f
                    }
                    VoteType.Random -> {
                        if(PluginData.lastVoted.plusMinutes(10).isBefore(LocalTime.now())){
                            sendMessage("랜덤 박스 쿨타임이 지나지 않았습니다.")
                        } else {
                            PluginData.lastVoted = LocalTime.now()
                            sendMessage("랜덤 박스 투표가 통과되었습니다!")
                            PluginData.threads.submit {
                                val random = Random
                                sendMessage("결과는...")
                                sleep(3000)
                                when(random.nextInt(6)) {
                                    0, 1 -> {
                                        sendMessage("[scarlet]아군 유닛이 모두 터집니다!")
                                        Groups.unit.each {
                                            if(it.team == player.team()) it.kill()
                                        }
                                        sendMessage("펑! 거기에 웨이브도 진행되죠!")
                                        Vars.logic.runWave()
                                    }
                                    2 -> {
                                        sendMessage("5 웨이브가 한번에 진행됩니다!")
                                        for(a in 0..5) Vars.logic.runWave()
                                    }
                                    3 -> {
                                        sendMessage("[scarlet]모든 건물의 체력이 50% 삭제됩니다!")
                                        Groups.build.each {
                                            if(it.team == player.team()) {
                                                Core.app.post {Call.tileDamage(it, it.health() / 0.5f)}
                                                //it.health(it.health() / 0.5f)
                                            }
                                        }
                                        for(a in Groups.player) {
                                            Call.worldDataBegin(a.con);
                                            netServer.sendWorldData(a);
                                        }
                                    }
                                    4 -> {
                                        sendMessage("[green]오! 코어에 자원이 채워집니다! 물론 랜덤으로요.")
                                        for(item in content.items()) {
                                            Vars.state.teams.cores(player.team()).first().items.add(
                                                item,
                                                Random(516).nextInt(500)
                                                                                                   )
                                        }
                                    }
                                    5 -> {
                                        sendMessage("[sky]폭풍이 몰려옵니다!")
                                        sleep(1000)
                                        sendMessage("[scarlet]아군의 건물 및 유닛이 큰 피해를 입었습니다!")
                                        Groups.build.each {
                                            if(it.team == player.team()) {
                                                Core.app.post {Call.tileDamage(it, 1f)}
                                                //it.health(1f)
                                            }
                                        }
                                        Groups.unit.each {
                                            if(it.team == player.team()) {
                                                it.health(1f)
                                            }
                                        }
                                        for(a in Groups.player) {
                                            Call.worldDataBegin(a.con);
                                            netServer.sendWorldData(a);
                                        }
                                    }
                                    /*6 -> {
                                    sendMessage("[scarlet]행성의 오존층이 뚫려 큰 화염 피해를 받게 됩니다!")
                                    for(x in 0 until Vars.world.width()){
                                        for (y in 0 until Vars.world.height()){
                                            //Call.createBullet(Bullets.fireball, Team.crux, (x*8).toFloat(), (y*8).toFloat(), 0f, 0f, 1f, 1f)
                                            Core.app.post{Call.effect(Fx.fire,(x*8).toFloat(), (y*8).toFloat(),0f, Color.red)}
                                        }
                                    }

                                    Groups.unit.each {
                                        if(it.team == player.team()){
                                            it.health(it.health() - 1000f)
                                        }
                                    }
                                    Groups.build.each {
                                        if(it.team == player.team()){
                                            Core.app.post{Call.tileDamage(it, it.health() - 1000f)}
                                            //it.health(1f)
                                        }
                                    }
                                }*/
                                    6, 7 -> {
                                        sendMessage(".. 아무 일도 일어나지 않았습니다")
                                    }
                                }
                            }
                        }
                    }
                    None -> {
                    }
                }
            } else {
                sendMessage("투표 실패!")
            }
        } catch(e:Exception) {
            ErrorReport(e)
        }

        PluginData.voting.clear()
    }
}