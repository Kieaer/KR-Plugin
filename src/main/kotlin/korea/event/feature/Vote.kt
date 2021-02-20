package korea.event.feature

import arc.Events
import arc.struct.Seq
import arc.util.Align
import arc.util.async.Threads.sleep
import korea.PluginData.isVoting
import korea.PluginData.playerData
import korea.PluginData.votingPlayer
import korea.PluginData.votingType
import korea.data.Config
import korea.eof.infoPopup
import korea.eof.kick
import korea.eof.sendMessage
import korea.event.feature.VoteType.*
import korea.event.feature.VoteType.Map
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
    val require: Int = if (Config.debug) 1 else 3 + if (Groups.player.size() > 5) 1 else 0

    var world: mindustry.maps.Map? = null
    var target: Playerc = Nulls.player
    var amount: Int = 3

    lateinit var timer: Thread
    var isInterrupt = false

    var listener: EventType.PlayerChatEvent? = null

    fun start(){
        votingPlayer = player
        votingType = type
        voted.clear()

        when (type){
            Kick -> {
                if(arg.size == 1){
                    target = Groups.player.find{e -> e.name.equals(arg[0], true)}
                    if (!target.isNull){
                        sendMessage("${player.name()} 에 의해 ${target.name()} 에 대한 강퇴 투표가 시작 되었습니다.")
                    } else {
                        player.sendMessage("${arg[0]} 유저를 찾을 수 없습니다!")
                        isInterrupt = true
                    }
                }
            }
            Map -> {
                world = when {
                    arg[0].toIntOrNull() != null -> maps.all().get(arg[0].toInt())
                    else -> maps.all().find { e -> e.name().equals(arg[0], true) }
                }
                
                if (world != null){
                    sendMessage("${player.name()} 에 의해 ${world!!.name()} 맵으로 가기 위한 투표가 시작 되었습니다.")
                } else {
                    player.sendMessage("${arg[0]} 맵을 찾을 수 없습니다!")
                    isInterrupt = true
                }
            }
            Gameover -> {
                sendMessage("${player.name()} 에 의해 항복 투표가 시작 되었습니다!")
            }
            Skipwave -> {
                try {
                    amount = arg[0].toInt()
                    sendMessage("${player.name()} 에 의해 $amount 웨이브 건너뛰기 투표가 시작 되었습니다!")
                } catch (e: NumberFormatException){
                    player.sendMessage("넘길 웨이브 숫자를 입력하셔야 합니다!")
                    isInterrupt = true
                }
            }
            Rollback -> {
                sendMessage("${player.name()} 에 의해 빽섭 투표가 시작 되었습니다!")
            }
            OP -> {
                sendMessage("${player.name()} 에 의해 치트 사용 투표가 시작 되었습니다!")
            }
            None -> {}
        }

        if (isInterrupt){
            isVoting = false
            return
        }
        sendMessage("필요 투표 인원: $require, 총 인원 ${playerData.size}")

        timer = Thread {
            var remain = 60

            while (!isInterrupt) {
                try {
                    infoPopup("투표 시작한 유저: ${player.name()}[white]\n" +
                            "$type 투표 종료까지 ${remain}초\n" +
                            "${voted.size} 명 투표. 필요 투표 인원: ${require - voted.size}", 1f, Align.left,0, 0,0,0)
                    remain--
                    when (remain) {
                        50,40,30,20,10 -> sendMessage("투표 종료까지 ${remain}초 남았습니다.")
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
            // TODO 아무 메세지가 안뜨는 버그 수정 + 투표 인원표시
            if (it.message == "y" && playerData.find { a -> a.uuid == it.player.uuid() } != null){
                add(it.player.uuid())
            }

            listener = it
        }
    }

    private fun add(uuid: String){
        if(!voted.contains(uuid)){
            voted.add(uuid)
            if (voted.size >= require) {
                isInterrupt = true
            }
        }
    }

    fun finish(){
        try {
            if (voted.size >= require) {
                when (type) {
                    Kick -> {
                        sendMessage("강퇴 투표가 통과 되었습니다!")
                        Call.kick(target.con(), Packets.KickReason.vote)
                    }
                    Map -> {
                        sendMessage("맵 투표가 통과 되었습니다!")
                        AutoRollback.isVote = true
                        AutoRollback.load(world)
                    }
                    Gameover -> {
                        sendMessage("항복 투표가 통과 되었습니다!")
                        Vars.state.serverPaused = true
                        sleep(20000)
                        sendMessage("20초동안 구경할 수 있는 시간을 드리겠습니다.")
                        Events.fire(EventType.GameOverEvent(Team.crux))
                    }
                    Skipwave -> {
                        sendMessage("웨이브 넘기기 투표가 통과 되었습니다!")
                        if(player.admin()) {
                            for (a in 0..amount) Vars.logic.runWave()
                        } else {
                            sendMessage("하지만 투표를 시작한 유저가 관리자가 아니므로 이 투표는 무효화 처리 되었습니다.")
                            sendMessage("이때까지 비정상적으로 높게 입력하여 서버를 터트리는데 도와주셔서 감사합니다.")
                            sendMessage("명단: Sharlotte(1000), newbie(10000), 비틀(100), 네(70), FlareKR(1000)")
                            when(player.name()){
                                "Sharlotte", "newbie", "비틀", "네", "[#d1d6ff]S[#7785ff]E[#4256fe]I[#2138ff]\uF7C4" -> kick(player, "이때까지 투표를 악용 해 주셔서 감사합니다.")
                            }
                        }
                    }
                    Rollback -> {
                        sendMessage("빽섭 투표가 통과 되었습니다! 10초후 빽섭을 진행합니다.")
                        sendMessage("하지만 투표를 시작한 유저는 블랙리스트 처리가 되었으므로 이 투표는 무효화 처리 되었습니다.")
                        sendMessage("이때까지 비정상적으로 투표를 사용하여 서버를 터트리는데 도와주셔서 감사합니다.")
                        when(player.name()){
                            "Sharlotte", "newbie", "비틀", "네", "[#d1d6ff]S[#7785ff]E[#4256fe]I[#2138ff]\uF7C4" -> kick(player, "이때까지 투표를 악용 해 주셔서 감사합니다.")
                            else -> AutoRollback.load(null)
                        }

                    }
                    OP -> {
                        sendMessage("치트 투표가 통과 되었습니다!")
                        sendMessage(".. 하지만 아무것도 없었습니다")
                    }
                    None -> {}
                }
            } else {
                sendMessage("투표 실패!")
            }
            isVoting = false
            Events.remove(EventType.PlayerChatEvent::class.java) { listener }

            votingType = None
        } catch (e: Throwable){
            e.printStackTrace()
        }
    }
}