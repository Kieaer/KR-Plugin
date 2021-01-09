package event.feature

import PluginData.isVoting
import PluginData.playerData
import PluginData.votingPlayer
import PluginData.votingType
import arc.Events
import arc.struct.Seq
import arc.util.Align
import arc.util.async.Threads.sleep
import data.Config
import event.feature.VoteType.*
import event.feature.VoteType.Map
import mindustry.Vars
import mindustry.Vars.maps
import mindustry.game.EventType
import mindustry.game.Team
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Nulls
import mindustry.gen.Playerc
import mindustry.net.Packets

class Vote(val player: Playerc, val type: VoteType, vararg val arg: String) {
    val voted: Seq<String> = Seq<String>()
    val require: Int = if (Config.debug) 1 else if (playerData.size > 8) 6 else if (playerData.size > 4) 3 else 1

    var world: mindustry.maps.Map? = null
    var target: Playerc = Nulls.player
    var amount: Int = 3

    lateinit var timer: Thread
    var isInterrupt = false

    var listener: EventType.PlayerChatEvent? = null

    fun start(){
        votingPlayer = player
        votingType = type

        when (type){
            Kick -> {
                if(arg.size == 1){
                    target = Groups.player.find{e -> e.name.equals(arg[0], true)}
                    if (!target.isNull){
                        Call.sendMessage("${player.name()} 에 의해 ${target.name()} 에 대한 강퇴 투표가 시작 되었습니다.")
                    } else {
                        player.sendMessage("${arg[0]} 유저를 찾을 수 없습니다!")
                        isInterrupt = true
                    }
                }
            }
            Map -> {
                world = maps.all().find{ e -> e.name().equals(arg[0], true)}
                if (world != null){
                    Call.sendMessage("${player.name()} 에 의해 ${world!!.name()} 맵으로 가기 위한 투표가 시작 되었습니다.")
                } else {
                    player.sendMessage("${arg[0]} 유저를 찾을 수 없습니다!")
                    isInterrupt = true
                }
            }
            Gameover -> {
                Call.sendMessage("${player.name()} 에 의해 항복 투표가 시작 되었습니다!")
            }
            Skipwave -> {
                try {
                    amount = arg[0].toInt()
                    Call.sendMessage("${player.name()} 에 의해 $amount 웨이브 건너뛰기 투표가 시작 되었습니다!")
                } catch (e: NumberFormatException){
                    player.sendMessage("넘길 웨이브 숫자를 입력하셔야 합니다!")
                    isInterrupt = true
                }
            }
            Rollback -> {
                Call.sendMessage("${player.name()} 에 의해 빽섭 투표가 시작 되었습니다!")
            }
            OP -> {
                Call.sendMessage("${player.name()} 에 의해 치트 사용 투표가 시작 되었습니다!")
            }
        }

        if (isInterrupt){
            isVoting = false
            return
        }
        Call.sendMessage("필요 투표 인원: $require, 총 인원 ${playerData.size}")

        timer = Thread {
            var remain = 60

            while (!isInterrupt) {
                try {
                    Call.infoPopup("투표 시작한 유저: ${player.name()}\n" +
                            "$type 투표 종료까지 ${remain}초", 1f, Align.left,0, 0,0,0)
                    remain--
                    when (remain) {
                        50,40,30,20,10 -> Call.sendMessage("투표 종료까지 ${remain}초 남았습니다.")
                        0 -> isInterrupt = true
                    }
                    sleep(1000)
                } catch (e: InterruptedException) {
                    isInterrupt = true
                }
            }

            finish()
        }

        timer.start()
        Events.on(EventType.PlayerChatEvent::class.java){
            if (it.message == "y" && playerData.find { a -> a.uuid == it.player.uuid() } != null){
                add(it.player.uuid())
            }

            listener = it
        }
    }

    private fun add(uuid: String){
        if(!voted.contains(uuid)){
            voted.add(uuid)
            Call.sendMessage("${voted.size} 명 투표. 필요 투표 인원: ${require - voted.size}")
            if (voted.size >= require) {
                isInterrupt = true
            }
        }
    }

    private fun finish(){
        try {
            if (voted.size >= require) {
                when (type) {
                    Kick -> {
                        Call.sendMessage("강퇴 투표가 통과 되었습니다!")
                        Call.kick(target.con(), Packets.KickReason.vote)
                    }
                    Map -> {
                        Call.sendMessage("맵 투표가 통과 되었습니다!")
                        AutoRollback.load(world)
                    }
                    Gameover -> {
                        Call.sendMessage("항복 투표가 통과 되었습니다!")
                        Events.fire(EventType.GameOverEvent(Team.crux))
                    }
                    Skipwave -> {
                        Call.sendMessage("웨이브 넘기기 투표가 통과 되었습니다!")
                        var a = 0
                        while (a < amount) {
                            Vars.logic.runWave()
                            a++
                        }
                    }
                    Rollback -> {
                        Call.sendMessage("빽섭 투표가 통과 되었습니다! 10초후 빽섭을 진행합니다.")
                        AutoRollback.load(null)
                    }
                    OP -> {
                        Call.sendMessage("치트 투표가 통과 되었습니다!")
                        Call.sendMessage(".. 하지만 아무것도 없었습니다")
                    }
                }
            } else {
                Call.sendMessage("투표 실패!")
            }
            isVoting = false
            Events.remove(EventType.PlayerChatEvent::class.java) { listener }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}