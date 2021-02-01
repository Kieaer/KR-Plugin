package korea.command

import arc.Core
import arc.math.Mathf
import arc.struct.Seq
import korea.Main.Companion.pluginRoot
import korea.PlayerData
import korea.PluginData
import korea.PluginData.isVoting
import korea.PluginData.votingClass
import korea.PluginData.votingPlayer
import korea.PluginData.votingType
import korea.command.ClientCommand.Command.*
import korea.data.PlayerCore
import korea.data.auth.Discord
import korea.event.feature.RainbowName
import korea.event.feature.Vote
import korea.event.feature.VoteType
import korea.external.LongToTime
import korea.external.RegularExpression
import korea.form.Garbage.EqualsIgnoreCase
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.core.NetClient
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Playerc
import mindustry.maps.Map
import mindustry.net.Administration
import mindustry.type.UnitType
import mindustry.world.Block
import org.hjson.JsonObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random




class ClientCommandThread(private val type: ClientCommand.Command, private val arg: Array<String>, private val player: Playerc) : Thread(){
    override fun run() {
        val uuid = player.uuid()

        try {
            if (Permissions.check(player, type.toString().toLowerCase())) {
                when (type) {
                    Login -> { // 계정 존재 유무확인
                        val isCorrect = PlayerCore.login(arg[0], arg[1])
                        if (isCorrect) {
                            if (PluginData.playerData.find { e -> e.uuid == player.uuid() } != null){
                                Core.app.post{player.sendMessage("이미 로그인 된 상태입니다!")}
                            } else {
                                PlayerCore.load(player)
                                Core.app.post{player.sendMessage("로그인 성공!")}
                            }
                        } else {
                            Core.app.post{player.sendMessage("로그인 실패!")}
                        }
                    }
                    Register -> {
                        val id = arg[0]
                        val pw = arg[1]
                        val pw2 = arg[2]

                        // 비밀번호 패턴 확인
                        if (pw != pw2) {
                            Core.app.post{player.sendMessage("비밀번호 2번 입력하는 값이 일치하지 않습니다!")}
                        } else {
                            val result = RegularExpression.check(pw, "", id, true)

                            if (result == "passed") {
                                val data = netServer.admins.findByName(uuid).first() // TODO country 만들기
                                PlayerCore.register(player.name(), uuid, data.timesKicked.toLong(), data.timesJoined.toLong(), System.currentTimeMillis(), System.currentTimeMillis(), "none", 0L, Permissions.defaultGroup, JsonObject(), id, pw)
                                Core.app.post{player.sendMessage("계정 등록 성공!")}
                                PlayerCore.load(player)
                            } else {
                                Core.app.post{
                                    player.sendMessage(result)
                                    player.sendMessage("계정 등록 실패!")
                                }
                            }
                        }
                    }
                    Spawn -> {
                        val type = arg[0]
                        val name = arg[1]
                        val parameter = arg[2].toInt()

                        when {
                            type.equals("unit", true) -> {
                                val unit = Vars.content.units().find { unitType: UnitType -> unitType.name == name }
                                if (unit != null) {
                                    for (a in 0..parameter){
                                        val baseUnit = unit.create(player.team())
                                        baseUnit.set(player.x, player.y)
                                        baseUnit.add()
                                    }
                                }
                            }
                            type.equals("block", true) -> {
                                Call.constructFinish(player.tileOn(), Vars.content.blocks().find { b: Block -> b.name == name }, player.unit(), parameter.toByte(), player.team(), null)
                            }
                            else -> { // TODO 명령어 예외 만들기
                                return
                            }
                        }
                    }
                    Vote -> {
                        try {
                            if (!isVoting) {
                                if (arg.isEmpty()) {
                                    Core.app.post{player.sendMessage("사용법: [green]/vote <kick/map/gameover/skipwave/rollback/op> [name/amount]")}
                                    Core.app.post{player.sendMessage("자세한 사용 방법은 [green]/help vote[] 를 입력 해 주세요.")}
                                } else {
                                    if (arg[0].equals("kill", true) && player.admin()) {
                                        if(votingClass != null && isVoting){
                                            votingClass!!.isInterrupt = true
                                        }
                                    } else {
                                        val mode = EqualsIgnoreCase(VoteType.values(), arg[0], VoteType.None)
                                        if (mode != VoteType.None) {
                                            isVoting = true
                                            Vote(player, mode, if (arg.size == 2) arg[1] else "").start()
                                        } else {
                                            Core.app.post{player.sendMessage("${arg[0]} 모드를 찾을 수 없습니다!")}
                                        }
                                    }
                                }
                            } else {
                                Core.app.post{player.sendMessage("${votingPlayer.name()} 이 시작한 ${votingType!!.name} 의 투표가 이미 진행 중입니다!")}
                            }
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                    Rainbow -> {
                        val data = PluginData[uuid]
                        if (data != null) {
                            if (!data.json.has("rainbow")) {
                                data.json.add("rainbow", true)
                                RainbowName.targets.add(player)
                                Core.app.post{player.sendMessage("무지개 닉네임이 설정 되었습니다!")}
                            } else {
                                data.json.remove("rainbow")
                                Core.app.post{player.sendMessage("무지개 닉네임이 해제 되었습니다!")}
                            }
                        }
                    }
                    Kill -> {
                        if (arg.isEmpty()) {
                            player.unit().kill()
                        } else {
                            val target = Groups.player.find { d -> d.name == arg[0] }
                            if (target != null) {
                                target.unit().kill()
                            } else {
                                Core.app.post{player.sendMessage("목표를 찾을 수 없습니다!")}
                            }
                        }
                    }
                    Info -> {
                        var data: PlayerData? = null
                        var target: Playerc = player

                        if (arg.isEmpty()) {
                            val buffer = PluginData[uuid]
                            if (buffer != null) {
                                data = buffer
                            }
                        } else {
                            target = Groups.player.find { d -> d.name.equals(arg[0], true) }
                            if (target != null) {
                                val buffer = PluginData[target.uuid()]
                                if (buffer != null) {
                                    data = buffer
                                }
                            }
                        }

                        if (data != null) {
                            val message = """
                        [green]이름[white]: ${NetClient.colorizeName(target.id(), target.name())}
                        [green]블럭 설치개수[white]: ${data.placeCount}
                        [green]블럭 파괴개수[white]: ${data.breakCount}
                        [green]레벨[white]: ${data.level}
                        [green]경험치: ${data.exp}
                        [green]최초 접속일[white]: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(data.joinDate))}
                        [green]플레이 시간[white]: ${LongToTime()[data.playTime]}
                        [green]공격 맵 클리어[white]: ${data.attackWinner}
                        [green]PvP 승리[white]: ${data.pvpWinner}
                        [green]PvP 패배[white]: ${data.pvpLoser}
                    """.trimIndent()
                            Call.infoMessage(player.con(), message)
                        }
                    }
                    Maps -> {
                        val message = StringBuilder()
                        val page = if (arg.isNotEmpty()) arg[0].toInt() else 0

                        val buffer = Mathf.ceil(Vars.maps.all().size.toFloat() / 6)
                        val pages = if (buffer > 1.0) buffer - 1 else 0

                        if (pages < page) {
                            Core.app.post{player.sendMessage("[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다!")}
                        } else {
                            message.append("[green]==[white] 서버 맵 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")

                            val maps = Seq<Map>()
                            for (map in Vars.maps.all()) maps.add(map)
                            for (a in 6 * page until (6 * (page + 1)).coerceAtMost(Vars.maps.all().size)) {
                                message.append("[gray]$a[white] ${maps.get(a).name()} v${maps.get(a).version} [gray]${maps.get(a).width}x${maps.get(a).height}\n")
                            }
                            Core.app.post{player.sendMessage(message.toString().dropLast(2))}
                        }
                    }
                    Motd -> {
                        if (!Administration.Config.motd.string().equals("off", ignoreCase = true)) {
                            Core.app.post{player.sendMessage(Administration.Config.motd.string())}
                        } else {
                            Core.app.post{player.sendMessage(pluginRoot.child("motd/motd.txt").readString("UTF-8"))}
                        }
                    }
                    Players -> {
                        val message = StringBuilder()
                        val page = if (arg.isNotEmpty()) arg[0].toInt() else 0

                        val buffer = Mathf.ceil(Groups.player.size().toFloat() / 6)
                        val pages = if (buffer > 1.0) buffer - 1 else 0

                        if (pages < page) {
                            Core.app.post{player.sendMessage("[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다!")}
                        } else {
                            message.append("[green]==[white] 현재 서버 플레이어 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")

                            val players: Seq<Playerc> = Seq<Playerc>()
                            Groups.player.each { e: Playerc ->
                                players.add(e)
                            }

                            for (a in 6 * page until (6 * (page + 1)).coerceAtMost(Groups.player.size())) {
                                message.append("[gray]${players.get(a).id()}[white] ${players.get(a).name()}\n")
                            }

                            Core.app.post{player.sendMessage(message.toString().dropLast(2))}
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
                    """.trimIndent(), """
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][][#404040][]
                    """.trimIndent(), """
                    [stat][#404040][][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat][][stat]
                    """.trimIndent(), """
                    [stat][#404040][][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    [stat][#404040][]
                    [stat][#404040]
                    [stat][#404040][]
                    [#404040][stat]
                    [stat][#404040][]
                    """.trimIndent(), """
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
                    """.trimIndent(), """
                    [#6B6B6B][stat][#6B6B6B]
                    [#6B6B6B][stat][#404040][][#6B6B6B]
                    [stat][#404040][]
                    [#404040][]
                    [stat][#404040][]
                    [stat][#404040][]
                    [#6B6B6B][stat][#404040][][#6B6B6B]
                    [#6B6B6B][stat][#6B6B6B]
                    """.trimIndent(), """
                    [#6B6B6B][#585858][stat][][#6B6B6B]
                    [#6B6B6B][#828282][stat][#404040][][][#6B6B6B]
                    [#585858][stat][#404040][][#585858]
                    [stat][#404040][]
                    [stat][#404040][]
                    [#585858][stat][#404040][][#585858]
                    [#6B6B6B][stat][#404040][][#828282][#6B6B6B]
                    [#6B6B6B][#585858][stat][][#6B6B6B]
                    """.trimIndent(), """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][stat][][#6B6B6B]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][stat][#404040][][#585858]
                    [#585858][stat][#404040][][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#6B6B6B][stat][][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(), """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#585858][#6B6B6B][stat][][#828282][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][#6B6B6B][stat][#404040][][#828282][#585858]
                    [#585858][#6B6B6B][stat][][#828282][#585858]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(), """
                    [#6B6B6B][#585858][#6B6B6B]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][stat][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][stat][#6B6B6B][#828282][#585858]
                    [#585858][#6B6B6B][#828282][#585858]
                    [#6B6B6B][#828282][#6B6B6B]
                    [#6B6B6B][#585858][#6B6B6B]
                    """.trimIndent(), """
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
                    서버 총 온라인 시간: ${LongToTime()[PluginData.totalUptime]}
                    맵 플레이 시간: ${LongToTime()[PluginData.worldTime]}
                """.trimIndent()
                        Core.app.post{player.sendMessage(message)}
                    }
                    Team -> {
                        val team = mindustry.game.Team.all.find { e -> e.name == arg[0] }
                        if (team != null) {
                            if (arg.size == 2) {
                                val target = Groups.player.find { e -> e.name == arg[1] }
                                if (target != null) {
                                    target.team(team)
                                } else {
                                    Core.app.post{player.sendMessage("${arg[1]} 플레이어를 찾을 수 없습니다!")}
                                }
                            } else {
                                player.team(team)
                            }
                        } else {
                            Core.app.post{player.sendMessage("${arg[0]} 팀을 찾을 수 없습니다!")}
                        }
                    }
                    Ban -> {
                        TODO()
                    }
                    Tp -> {
                        val target: Playerc
                        when (arg.size) {
                            1 -> {
                                target = if (arg[0].toIntOrNull() != null){
                                    Groups.player.getByID(arg[0].toInt())
                                } else {
                                    Groups.player.find { e -> e.name().contains(arg[0]) }
                                }

                                if (target != null) {
                                    Call.setPosition(player.con(), target.x, target.y)
                                    player.unit().set(target.x, target.y)
                                    player.snapSync()
                                    Core.app.post{player.sendMessage("${target.name()} 에게로 이동했습니다.")}
                                } else {
                                    Core.app.post{player.sendMessage("${arg[0]} 플레이어를 찾을 수 없습니다!")}
                                }
                            }
                            2 -> {
                                target = if (arg[0].toIntOrNull() != null){
                                    Groups.player.getByID(arg[0].toInt())
                                } else {
                                    Groups.player.find { e -> e.name().contains(arg[0]) }
                                }

                                if (target != null) {
                                    val other = if (arg[1].toIntOrNull() != null){
                                        Groups.player.getByID(arg[1].toInt())
                                    } else {
                                        Groups.player.find { e -> e.name().contains(arg[1]) }
                                    }
                                    if (other != null) {
                                        Call.setPosition(target.con(), other.x, other.y)
                                        target.unit().set(other.x, other.y)
                                        target.snapSync()
                                        Core.app.post{player.sendMessage("${target.name()} 님을 ${other.name()} 에게로 이동했습니다.")}
                                    } else {
                                        Core.app.post{player.sendMessage("${arg[1]} 플레이어를 찾을 수 없습니다!")}
                                    }
                                } else {
                                    try {
                                        val tileX = arg[0].toFloat()
                                        val tileY = arg[1].toFloat()
                                        Call.setPosition(player.con(), tileX*8, tileY*8)
                                        player.unit().set(tileX, tileY)
                                        player.snapSync()
                                    } catch (_: NumberFormatException) {
                                        Core.app.post{player.sendMessage("잘못된 명령어 입니다!")}
                                    }
                                    Core.app.post{player.sendMessage("${arg[0]} 플레이어를 찾을 수 없습니다!")}
                                }
                            }
                        }
                    }
                    Mute -> {
                        val name = arg[0]
                        val target = Groups.player.find { e -> e.name() == name }
                        if (target != null) {
                            val data = PluginData[target.uuid()]
                            if (data!!.isMute) {
                                data.isMute = false
                                Core.app.post{target.sendMessage("축하드립니다. 묵언 상태가 해제되었습니다!")}
                            } else {
                                data.isMute = true
                                Core.app.post{target.sendMessage("누군가에 의해 묵언 상태가 되었습니다.")}
                            }
                        }
                    }
                    Help -> {
                        val message = StringBuilder()
                        val page = if (arg.isNotEmpty()) arg[0].toInt() else 0

                        val commands = Seq<String>()
                        for (a in 0 until netServer.clientCommands.commandList.size) {
                            val command = netServer.clientCommands.commandList[a]
                            if (Permissions.check(player, command.text)) {
                                commands.add("[orange] /${command.text} [white]${command.paramText} [lightgray]- ${command.description}\n")
                            }
                        }

                        val buffer = Mathf.ceil(commands.size.toFloat() / 6)
                        val pages = if (buffer > 1.0) buffer - 1 else 0

                        if (pages < page) {
                            Core.app.post{player.sendMessage("[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다!")}
                        } else {
                            message.append("[green]==[white] 사용 가능한 명령어 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")}

                            for (a in 6 * page until (6 * (page + 1)).coerceAtMost(commands.size)) {
                                message.append(commands.get(a))
                            }

                            Core.app.post{player.sendMessage(message.toString())
                        }
                    }
                    ClientCommand.Command.Discord -> {
                        val pin = abs(Random.nextLong(Int.MAX_VALUE + 1L, Long.MAX_VALUE))
                        Discord.pin.put(pin, player.uuid())
                        Core.app.post{player.sendMessage("Discord 채널 내에서 !auth 명령어와 함께 이 PIN 번호를 입력하세요!")}
                        Core.app.post{player.sendMessage("PIN 번호: $pin")}
                    }
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
}