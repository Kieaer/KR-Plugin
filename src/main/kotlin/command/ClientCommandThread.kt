package command

import PlayerData
import PluginData
import command.ClientCommand.Command.*
import data.PlayerCore
import external.RegularExpression
import mindustry.Vars
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
                    val data = Vars.netServer.admins.findByName(uuid).first()
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
                val data = PluginData[uuid]
                val mesasge = """
                    이름: 
                """.trimIndent()
            }
            Maps -> {
                TODO()
            }
            Motd -> {
                TODO()
            }
            Players -> {
                TODO()
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
                TODO()
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