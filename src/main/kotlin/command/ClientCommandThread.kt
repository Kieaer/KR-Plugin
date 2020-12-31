package command

import PluginData
import command.ClientCommand.Command.*
import data.PlayerCore
import external.RegularExpression
import mindustry.Vars
import mindustry.gen.Call
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
                player.unit().kill()
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
                TODO()
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