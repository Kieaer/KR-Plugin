package command

import PlayerData
import PluginData
import arc.Core
import arc.math.Mathf
import arc.struct.Seq
import command.ClientCommand.Command.*
import data.PlayerCore
import external.LongToTime
import external.RegularExpression
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Playerc
import mindustry.type.UnitType
import mindustry.world.Block

class ClientCommandThread(private val type: ClientCommand.Command, private val arg: Array<String>, private val player: Playerc) : Thread(){
    override fun run() {
        val uuid = player.uuid()

        when(type){
            Login -> {
                // 계정 존재 유무확인
                val isCorrect = PlayerCore.login(arg[0], arg[1])
                if(isCorrect){
                    player.sendMessage("로그인 성공!")
                    PlayerCore.load(player)
                } else {
                    player.sendMessage("로그인 실패!")
                }
            }
            Register -> {
                val id = arg[0]
                val pw = arg[1]
                val pw2 = arg[2]

                // 비밀번호 패턴 확인
                val result = RegularExpression.check(pw, pw2, id, true)

                if(result){
                    val data = netServer.admins.findByName(uuid).first()
                    // TODO country 만들기
                    PlayerCore.register(player.name(), uuid, data.timesKicked.toLong(), data.timesJoined.toLong(), System.currentTimeMillis(), System.currentTimeMillis(), "none", 0L, id, pw)
                    player.sendMessage("계정 등록 성공!")
                    PlayerCore.load(player)
                }
            }
            Spawn -> {
                val type = arg[0]
                val name = arg[1]
                val parameter = arg[2].toInt()

                when {
                    type.equals("unit", true) -> {
                        val unit = Vars.content.units().find { unitType: UnitType -> unitType.name == name }
                        if(unit != null){
                            val baseUnit = unit.create(player.team())
                            baseUnit.set(player.x, player.y)
                            baseUnit.add()
                        }
                    }
                    type.equals("block", true) -> {
                        Call.constructFinish(player.tileOn(), Vars.content.blocks().find { b: Block -> b.name == name}, player.unit(),
                            parameter.toByte(), player.team(), null)
                    }
                    else -> {
                        // TODO 명령어 예외 만들기
                        return
                    }
                }
            }
            Vote -> {
                TODO()
            }
            Rainbow -> {
                TODO()
            }
            Kill -> {
                if (arg.isEmpty()){
                    player.unit().kill()
                } else {
                    val target = Groups.player.find { d -> d.name == arg[0] }
                    if(target != null) {
                        target.unit().kill()
                    } else {
                        player.sendMessage("목표를 찾을 수 없습니다!")
                    }
                }
            }
            Info -> {
                var data: PlayerData? = null

                if (arg.isEmpty()) {
                    val buffer = PluginData[uuid]
                    if (buffer != null) {
                        data = buffer
                    }
                } else {
                    val target = Groups.player.find { d -> d.name == arg[0] }
                    if(target != null) {
                        val buffer = PluginData[uuid]
                        if (buffer != null) {
                            data = buffer
                        }
                    }
                }

                if (data != null) {
                    val message = """
                        이름: ${data.name}
                        블럭 설치개수: ${data.placeCount}
                        블럭 파괴개수: ${data.breakCount}
                        레벨: ${data.level}
                        경험치: ${data.exp}
                        최초 접속일: ${data.joinDate}
                        플레이 시간: ${data.playTime}
                        공격 맵 클리어: ${data.attackWinner}
                        PvP 승리: ${data.pvpWinner}
                        PvP 패배: ${data.pvpLoser}
                    """.trimIndent()
                    Call.infoMessage(player.con(), message)
                }
            }
            Maps -> {
                TODO()
            }
            Motd -> {
                TODO()
            }
            Players -> {
                val message = StringBuilder()
                val page = if(arg.isEmpty()) arg[0].toInt() else 1

                val buffer = Mathf.ceil(Groups.player.size().toFloat() / 6)
                val pages = if (buffer < 1.0) buffer else 1

                if (pages < page) {
                    player.sendMessage("[scarlet]페이지 쪽수는 [orange]1[]~[orange]$pages[] 안이어야 합니다!")
                } else {
                    message.append("[green]==[white] 현재 서버 플레이어 목록 페이지 [orange]$page[]/[orange]$pages\n")

                    val players: Seq<Playerc> = Seq<Playerc>()
                    Groups.player.each { e: Playerc ->
                        players.add(e)
                    }

                    for (a in 6 * page until (6 * (page)).coerceAtMost(Groups.player.size())) {
                        message.append("[gray]${players.get(a).id()}[white] ${players.get(a).name()}")
                    }

                    player.sendMessage(message.toString())
                }
            }
            Router -> {
                val zero = arrayOf("""
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040][][#404040]
                    """.trimIndent(),
                    """
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][][#404040][]
                    """.trimIndent(),
                    """
                    [stat][#404040][][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat][][stat]
                    """.trimIndent(),
                    """
                    [stat][#404040][][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    """.trimIndent(),
                    """
                    [#404040][stat][][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    """.trimIndent())
                val loop = arrayOf("""
                    [#6B6B6B][stat][#6B6B6B]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][]
                    [stat][#404040][]
                    [stat][#404040][]
                    [#6B6B6B][stat][#404040][][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][stat][#6B6B6B]
                    [#6B6B6B][stat][#404040][][#6B6B6B]
                    [stat][#404040][]
                    [#404040][]
                    [stat][#404040][]
                    [stat][#404040][]
                    [#6B6B6B][stat][#404040][][#6B6B6B]
                    [#6B6B6B][stat][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][#585858][stat][][#6B6B6B]
                    [#6B6B6B][#828282][stat][#404040][][][#6B6B6B]
                    [#585858][stat][#404040][][#585858]
                    [stat][#404040][]
                    [stat][#404040][]
                    [#585858][stat][#404040][][#585858]
                    [#6B6B6B][stat][#404040][][#828282][#6B6B6B]
                    [#6B6B6B][#585858][stat][][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][stat][][#6B6B6B]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][stat][#404040][][#585858]
                    [#585858][stat][#404040][][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#6B6B6B][stat][][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#585858][#6B6B6B][stat][][#828282][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][#6B6B6B][stat][][#828282][#585858]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][stat][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][stat][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(),
                    """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][#828282][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][#828282][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent())
                while (!player.isNull) {
                    for (d in loop) {
                        player.name(d)
                        sleep(500)
                    }
                    sleep(5000)
                    for (i in loop.indices.reversed()) {
                        player.name(loop[i])
                        sleep(500)
                    }
                    for (d in zero) {
                        player.name(d)
                        sleep(500)
                    }
                }
            }
            Status -> {
                val message = """
                    [#2B60DE]== 서버 통계 =========================[]
                    TPS: ${Core.graphics.framesPerSecond}/60
                    메모리: ${Core.app.javaHeap / 1024 / 1024}
                    밴당한 인원: ${netServer.admins.banned.size + netServer.admins.bannedIPs.size}
                    총 접속인원: ${PluginData.totalConnected}
                    총 강퇴인원: ${PluginData.totalKicked}
                    서버 총 온라인 시간: ${LongToTime()[PluginData.totalUptime]}
                    맵 플레이 시간: ${LongToTime()[PluginData.worldTime]}
                """.trimIndent()
                player.sendMessage(message)
            }
            Team -> {
                TODO()
            }
            Ban -> {
                TODO()
            }
            Tp -> {
                TODO()
            }
            Mute -> {
                TODO()
            }
        }
    }
}