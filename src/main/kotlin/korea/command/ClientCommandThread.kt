package korea.command

import arc.Core
import arc.math.Mathf
import arc.struct.Seq
import arc.util.async.Threads.sleep
import korea.Main.Companion.pluginRoot
import korea.PlayerData
import korea.PluginData
import korea.PluginData.computeTime
import korea.PluginData.playerData
import korea.command.ClientCommand.Command
import korea.command.ClientCommand.Command.*
import korea.data.PlayerCore
import korea.data.auth.Discord
import korea.eof.*
import korea.event.Exp
import korea.event.feature.RainbowName
import korea.event.feature.Vote
import korea.event.feature.VoteType
import korea.event.feature.VoteType.*
import korea.exceptions.ErrorReport
import korea.external.LongToTime
import korea.external.RegularExpression
import korea.form.Garbage.EqualsIgnoreCase
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.content.Blocks
import mindustry.core.NetClient
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Playerc
import mindustry.maps.Map
import mindustry.net.Administration
import mindustry.type.UnitType
import mindustry.world.blocks.environment.Floor
import org.hjson.JsonObject
import org.mindrot.jbcrypt.BCrypt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

class ClientCommandThread(private val type: Command, private val arg: Array<String>, private val player: Playerc) {
    fun run() {
        val uuid = player.uuid()
        val sendMessage = sendMessage(player)

        try {
            if(Permissions.check(player, type.name.toLowerCase())) {
                when(type) {
                    Login -> {
                        val hashed = BCrypt.hashpw(arg[1], BCrypt.gensalt(11))

                        when {
                            playerData.find { e -> e.uuid == player.uuid() } != null -> {
                                sendMessage["이미 로그인 된 상태입니다"]
                            }

                            PlayerCore.login(arg[0], hashed) -> {
                                if(PlayerCore.load(player)) {
                                    sendMessage["로그인 성공"]
                                } else {
                                    sendMessage["[scarlet]잘못된 경로로 접근했습니다! /register 명령어부터 사용 해 주세요."]
                                }
                            }
                            else -> {
                                sendMessage["[scarlet]없는 계정이거나 아이디 또는 비밀번호가 틀립니다."]
                            }
                        }
                    }
                    Register -> {
                        if(arg.size != 1) {
                            sendMessage["아직도 /register <아아디> <비밀번호> <비밀번호 재입력> 을 쓰시나요?\n" + "이제는 그냥 /register <비밀번호> 를 입력하시면 됩니다.\n\n" + "비밀번호 정할때 구글이나 네이버에서 회원가입할때 비밀번호를 a123b 이라고 할때, 그 누구도 [scarlet]<[]a123b[scarlet]>[] 이라고 치진 않잖아요?\n" + "진짜로 비밀번호 칠때 [scarlet]<[]a123b[scarlet]>[] 처럼 친다면, 이후에도 비밀번호를 [scarlet]<[]a123b[scarlet]>[] 으로 하게 될껍니다.\n" + "남들은 그냥 손쉽게 치는데 자기 혼자만 [scarlet]<[] 하고 [scarlet]>[] 쓰니 불편하겠죠?"]
                            return
                        }

                        val pw = arg[0]

                        // 비밀번호 패턴 확인
                        val hashed = BCrypt.hashpw(arg[0], BCrypt.gensalt(11))
                        if(RegularExpression.check(pw)) {
                            val data = netServer.admins.findByName(uuid).first() // TODO country 만들기
                            val request = PlayerCore.register(name = player.name(), uuid = uuid, kickCount = data.timesKicked, joinCount = data.timesJoined, joinDate = System.currentTimeMillis(), lastDate = System.currentTimeMillis(), country = "none", rank = 0L, permission = Permissions.defaultGroup, json = JsonObject(), id = player.name(), pw = hashed)
                            if(request) {
                                sendMessage["계정 등록 성공"]
                                if(PlayerCore.load(player)) {
                                    sendMessage["로그인 성공"]
                                } else {
                                    sendMessage["[scarlet]자동 로그인 실패! 서버 관리자에게 문의하세요."]
                                }
                            } else {
                                sendMessage["[scarlet]이미 이 기기에 등록된 계정이 존재하거나, 서버 오류에 의해 계정 등록에 실패했습니다."]
                            }
                        } else {
                            sendMessage["[scarlet]비밀번호는 최소한 6자리 이상과 영문/숫자를 포함해야 합니다!\n" + "[scarlet]제발 [green]<[] 하고 [green]>[] 를 넣지 마세요."]
                        }
                    }
                    Spawn -> {
                        val type = arg[0]
                        val name = arg[1]
                        val parameter = if(arg.size == 3) arg[2].toIntOrNull() else 1

                        when {
                            type.equals("unit", true) -> {
                                val unit = Vars.content.units().find { unitType: UnitType -> unitType.name == name }
                                if(unit != null) {
                                    if(parameter != null) {
                                        if(name != "block") {
                                            for(a in 1..parameter) {
                                                val baseUnit = unit.create(player.team())
                                                baseUnit.set(player.x, player.y)
                                                baseUnit.add()
                                            }
                                        } else {
                                            sendMessage["block 는 유닛이 아닙니다! 스폰할 생각 하지 마세요."]
                                        }
                                    } else {
                                        sendMessage["스폰할 유닛/건물 개수 값은 반드시 숫자이어야 합니다!"]
                                    }
                                } else {
                                    val names = StringBuilder()
                                    Vars.content.units().each {
                                        names.append("${it.name}, ")
                                    }
                                    sendMessage["사용 가능한 유닛 이름: ${names.dropLast(2)}"]
                                }
                            }
                            type.equals("block", true) -> {
                                if(Vars.content.blocks().find { it.name.equals(name, true)} != null){
                                    constructFinish(tile = player.tileOn(), block = Vars.content.blocks().find { it.name.equals(name, true) }, builder = player.unit(), rotation = parameter?.toByte() ?: 0, team = player.team(), config = null)
                                } else {
                                    val names = StringBuilder()
                                    Vars.content.blocks().each {
                                        names.append("${it.name}, ")
                                    }
                                    sendMessage["사용 가능한 블록 이름: ${names.dropLast(2)}"]
                                }
                            }
                            else -> { // TODO 명령어 예외 만들기
                                return
                            }
                        }
                    }
                    Vote -> {
                        if(arg.isEmpty()) {
                            sendMessage["사용법: [green]/vote <kick/map/gameover/skipwave/rollback/random> [name/amount]"]
                            sendMessage["자세한 사용 방법은 [green]/help vote[] 를 입력 해 주세요."]
                        } else if(PluginData.voting.size == 1) {
                            val vote = PluginData.voting[0]
                            sendMessage["${vote.player.name()} 이 시작한 ${vote.type} 의 투표가 이미 진행 중입니다"]
                        } else if(PluginData.voting.size == 0 && arg.isNotEmpty()) {
                            try {
                                when(EqualsIgnoreCase(VoteType.values(), arg[0], None)) {
                                    Kick -> {
                                        if(arg.size == 1) {
                                            val target = Groups.player.find { e -> e.name.equals(arg[1], true) }
                                            if(target != null) {
                                                sendMessage("${player.name()} 에 의해 ${target.name()} 에 대한 강퇴 투표가 시작 되었습니다.")
                                                val t = playerData.find{e -> e.uuid == target.uuid()}.permission
                                                if(target.admin() || t.equals("owner", true) || t.equals("admin", true)){
                                                    sendMessage("하지만 ${target.name()} 유저는 서버 관리자입니다.\n이걸 노리고 투표를 시작한 ${player.name()} 유저는 제정신이 아닌 것 같군요.\n잠시 나갔다 오세요.")
                                                    kick(player, "관리자를 대상으로 투표하는 행위는 금지되어 있습니다. 3분간 강퇴 처리.")
                                                } else {
                                                    val vote = Vote(player, Kick)
                                                    vote.target = target
                                                    PluginData.voting.add(vote)
                                                    PluginData.voting.first().start()
                                                }
                                            } else {
                                                sendMessage(player, "${arg[1]} 유저를 찾을 수 없습니다!")
                                            }
                                        }
                                    }
                                    VoteType.Map -> {
                                        val world = when {
                                            arg[1].toIntOrNull() != null -> Vars.maps.all().get(arg[1].toInt())
                                            else -> Vars.maps.all().find { e -> e.name().equals(arg[1], true) }
                                        }

                                        if(world != null) {
                                            val vote = Vote(player, VoteType.Map)
                                            vote.world = world
                                            PluginData.voting.add(vote)
                                            PluginData.voting.first().start()
                                            sendMessage("${player.name()} 에 의해 ${world.name()} 맵으로 가기 위한 투표가 시작 되었습니다.")
                                        } else {
                                            sendMessage(player, "${arg[1]} 맵을 찾을 수 없습니다!")
                                        }
                                    }
                                    Gameover -> {
                                        sendMessage("${player.name()} 에 의해 항복 투표가 시작 되었습니다!")
                                        PluginData.voting.add(Vote(player, Gameover))
                                        PluginData.voting.first().start()
                                    }
                                    Skipwave -> {
                                        try {
                                            val amount = arg[1].toInt()
                                            if(amount > 20) {
                                                sendMessage(player, "20 wave 이상 한꺼번에 넘길 수 없습니다!")
                                            } else {
                                                sendMessage("${player.name()} 에 의해 $amount 웨이브 건너뛰기 투표가 시작 되었습니다!")
                                                val vote = Vote(player, Skipwave)
                                                vote.skipCount = amount
                                                PluginData.voting.add(vote)
                                                PluginData.voting.first().start()
                                            }
                                        } catch(e: NumberFormatException) {
                                            sendMessage(player, "넘길 웨이브 숫자를 입력하셔야 합니다!")
                                        }
                                    }
                                    Rollback -> {
                                        sendMessage("${player.name()} 에 의해 빽섭 투표가 시작 되었습니다!")
                                        PluginData.voting.add(Vote(player, Rollback))
                                        PluginData.voting.first().start()
                                    }
                                    Fast -> {
                                        sendMessage(player, "현재 인원수 비례 난이도 조절 기능이 활성화 되어 있어, 웨이브 고속 진행 모드가 비활성화 되어 있습니다.")
                                        /*sendMessage("${player.name()} 에 의해 웨이브 고속 진행 모드 투표가 시작 되었습니다!")
                                        PluginData.voting.add(Vote(player, Fast))
                                        PluginData.voting.first().start()*/
                                    }
                                    VoteType.Random -> {
                                        sendMessage(player, "도박 중독 신고는 국번없이 [green]1336[] 입니다.")
                                        /*sendMessage("${player.name()} 에 의해 랜덤 박스 뽑기 투표가 시작 되었습니다!")
                                        PluginData.voting.add(Vote(player, VoteType.Random))
                                        PluginData.voting.first().start()*/
                                    }
                                    None -> sendMessage["${arg[0]} 모드를 찾을 수 없습니다"]
                                }
                            } catch(e: Throwable) {
                                sendMessage["${arg[0]} 모드를 찾을 수 없습니다"]
                            }
                        }
                    }
                    Rainbow -> {
                        val data = PluginData[uuid]
                        if(data != null) {
                            if(!data.json.has("rainbow")) {
                                data.json.add("rainbow", true)
                                RainbowName.targets.add(player)
                                sendMessage["무지개 닉네임이 설정 되었습니다"]
                            } else {
                                data.json.remove("rainbow")
                                sendMessage["무지개 닉네임이 해제 되었습니다"]
                            }
                        }
                    }
                    Kill -> {
                        if(arg.isEmpty()) {
                            player.unit().kill()
                        } else {
                            val target = Groups.player.find { d -> d.name == arg[0] }
                            if(target != null) {
                                if (playerData.find{e -> e.uuid == target.uuid()}.permission.equals("owner", true)){
                                    sendMessage["권한이 없습니다."]
                                } else {
                                    target.unit().kill()
                                }
                            } else {
                                sendMessage["목표를 찾을 수 없습니다"]
                            }
                        }
                    }
                    Info -> {
                        var data: PlayerData? = null
                        var target: Playerc = player

                        if(arg.isEmpty()) {
                            val buffer = PluginData[uuid]
                            if(buffer != null) {
                                data = buffer
                            }
                        } else if(player.admin()) {
                            target = Groups.player.find { d -> d.name().equals(arg[0], true) }
                            if(target != null) {
                                val buffer = PluginData[target.uuid()]
                                if(buffer != null) {
                                    data = buffer
                                }
                            }
                        }

                        if(data != null) {
                            val message = """
                                [green]이름[white]: ${NetClient.colorizeName(target.id(), target.name())}
                                [green]블럭 설치개수[white]: ${data.placeCount}
                                [green]블럭 파괴개수[white]: ${data.breakCount}
                                [green]레벨[white]: ${data.level}
                                [green]경험치[white]: ${Exp[data]}
                                [green]최초 접속일[white]: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(data.joinDate))}
                                [green]플레이 시간[white]: ${LongToTime()[data.playTime]}
                                [green]공격 맵 클리어[white]: ${data.attackWinner}
                                [green]PvP 승리[white]: ${data.pvpWinner}
                                [green]PvP 패배[white]: ${data.pvpLoser}
                                """.trimIndent()
                            if (data.permission.equals("owner", true)){
                                sendMessage["권한이 없습니다."]
                            } else {
                                infoMessage(player, message)
                            }
                        }
                    }
                    Maps -> {
                        val message = StringBuilder()
                        val page = if(arg.isNotEmpty()) arg[0].toInt() else 0

                        val buffer = Mathf.ceil(Vars.maps.all().size.toFloat() / 6)
                        val pages = if(buffer > 1.0) buffer - 1 else 0

                        if(pages < page) {
                            sendMessage["[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다"]
                        } else {
                            message.append("[green]==[white] 서버 맵 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")

                            val maps = Seq<Map>()
                            for(map in Vars.maps.all()) maps.add(map)
                            for(a in 6 * page until (6 * (page + 1)).coerceAtMost(Vars.maps.all().size)) {
                                message.append("[gray]$a[white] ${
                                    maps.get(a).name()
                                } v${maps.get(a).version} [gray]${maps.get(a).width}x${maps.get(a).height} [green]by ${maps.get(a).author()}\n")
                            }
                            sendMessage[message.toString().dropLast(1)]
                        }
                    }
                    Motd -> {
                        if(!Administration.Config.motd.string().equals("off", ignoreCase = true)) {
                            sendMessage[Administration.Config.motd.string()]
                        } else {
                            sendMessage[pluginRoot.child("motd/motd.txt").readString("UTF-8")]
                        }
                    }
                    Players -> {
                        val message = StringBuilder()
                        val page = if(arg.isNotEmpty()) arg[0].toInt() else 0

                        val buffer = Mathf.ceil(Groups.player.size().toFloat() / 6)
                        val pages = if(buffer > 1.0) buffer - 1 else 0

                        if(pages < page) {
                            sendMessage["[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다"]
                        } else {
                            message.append("[green]==[white] 현재 서버 플레이어 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")

                            val players: Seq<Playerc> = Seq<Playerc>()
                            Groups.player.each { e: Playerc ->
                                players.add(e)
                            }

                            for(a in 6 * page until (6 * (page + 1)).coerceAtMost(PluginData.players.size)) {
                                message.append("[gray]$a[white] ${
                                    PluginData.players.get(a).name()
                                }\n")
                            }

                            sendMessage[message.toString().dropLast(1)]
                        }
                    }
                    Router -> {
                        PluginData.threads.submit {
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

                            val tiles = intArrayOf(0, 0, 1, 1, 1, 1, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 1, 2, 0, 0, 0, 0, 0, 1, 1, 2, 0, 2, 2, 0, 0, 1, 1, 2, 0, 2, 2, 0, 0, 1, 1, 2, 0, 0, 0, 0, 0, 1, 0, 2, 2, 2, 2, 2, 2, 0, 0, 0, 1, 1, 1, 1, 0, 0)

                            val pos = Seq<IntArray>()
                            for(y in 0 until 8) {
                                for(x in 0 until 8) {
                                    pos.add(intArrayOf(y, -x))
                                }
                            }

                            if (!playerData.find{e -> e.uuid == player.uuid()}.permission.equals("owner", true)){
                                sendMessage["권한이 없습니다."]
                                return@submit
                            }

                            for(a in 0 until pos.size) {
                                val tar = Vars.world.tile(player.tileX() + pos[a][0], player.tileY() + pos[a][1])
                                tar.setFloor((if(tiles[a] == 0) Blocks.stone else if(tiles[a] == 1) Blocks.basalt else Blocks.salt) as Floor?)
                            }

                            Core.app.post {
                                Groups.player.each {
                                    Call.worldDataBegin(it.con())
                                    it.reset()
                                    netServer.sendWorldData(it)
                                }
                            }

                            while(player.isNull) {
                                for(d in loop) {
                                    player.name(d)
                                    sleep(500)
                                }
                                sleep(5000)
                                for(i in loop.indices.reversed()) {
                                    player.name(loop[i])
                                    sleep(500)
                                }
                                for(d in zero) {
                                    player.name(d)
                                    sleep(500)
                                }
                            }
                        }
                    }
                    Status -> {
                        val tps = when {
                            Core.graphics.framesPerSecond > 58 -> "[green]"
                            Core.graphics.framesPerSecond < 56 -> "[yellow]"
                            Core.graphics.framesPerSecond < 55 -> "[red]"
                            else -> ""
                        }

                        val computeResult = (System.nanoTime() - computeTime) / 1000000
                        val computeTps = when {
                            computeResult >= 19 -> "[red]"
                            computeResult >= 18 -> "[yellow]"
                            computeResult <= 17 -> "[green]"
                            else -> ""
                        }

                        val message = """
                            [#2B60DE]== 서버 통계 =========================[]
                            TPS: $tps${Core.graphics.framesPerSecond}[white]/60
                            서버 CPU 연산 시간: $computeTps${computeResult}ms[white]/16ms
                            사용중인 메모리: ${Core.app.javaHeap / 1024 / 1024}MB
                            밴당한 인원: ${PluginData.banned.size}
                            총 접속인원: ${PluginData.totalConnected}
                            서버 총 온라인 시간: ${LongToTime()[PluginData.totalUptime]}
                            맵 플레이 시간: ${LongToTime()[PluginData.worldTime]}
                            """.trimIndent()
                        sendMessage[message]
                    }
                    Team -> {
                        val team = mindustry.game.Team.all.find { e -> e.name == arg[0] }
                        if(team != null) {
                            if(arg.size == 2) {
                                val target = Groups.player.find { e -> e.name == arg[1] }
                                if(target != null) {
                                    if (playerData.find{e -> e.uuid == target.uuid()}.permission.equals("owner", true)){
                                        sendMessage["권한이 없습니다."]
                                    } else {
                                        target.team(team)
                                    }
                                } else {
                                    sendMessage["${arg[1]} 플레이어를 찾을 수 없습니다"]
                                }
                            } else {
                                player.team(team)
                            }
                        } else {
                            sendMessage["${arg[0]} 팀을 찾을 수 없습니다"]
                        }
                    }
                    Ban -> {
                        TODO()
                    }
                    Tp -> {
                        val target: Playerc?
                        when(arg.size) {
                            1 -> {
                                target = if(arg[0].toIntOrNull() != null) {
                                    PluginData.players.get(arg[0].toInt())
                                } else {
                                    Groups.player.find { e -> e.name.contains(arg[0]) }
                                }

                                if(target != null) {
                                    setPosition(player, target.x, target.y)
                                    sendMessage["${target.name()} 에게로 이동했습니다."]
                                } else {
                                    sendMessage["${arg[0]} 플레이어를 찾을 수 없습니다"]
                                }
                            }
                            2 -> {
                                target = if(arg[0].toIntOrNull() != null) {
                                    PluginData.players.get(arg[0].toInt())
                                } else {
                                    Groups.player.find { e -> e.name.contains(arg[0]) }
                                }

                                if(target != null) {
                                    val other = if(arg[1].toIntOrNull() != null) {
                                        PluginData.players.get(arg[0].toInt())
                                    } else {
                                        Groups.player.find { e -> e.name.contains(arg[1]) }
                                    }
                                    if(other != null) {
                                        val t = playerData.find{e -> e.uuid == other.uuid()}.permission
                                        if (t.equals("owner", true) || t.equals("admin", true)){
                                            sendMessage["권한이 없습니다."]
                                        } else {
                                            setPosition(target, other.x, other.y)
                                            sendMessage["${target.name()} 님을 ${other.name()} 에게로 이동했습니다."]
                                        }
                                    } else {
                                        sendMessage["${arg[1]} 플레이어를 찾을 수 없습니다"]
                                    }
                                } else {
                                    try {
                                        val tileX = arg[0].toFloat()
                                        val tileY = arg[1].toFloat()
                                        setPosition(player, tileX * 8, tileY * 8)
                                    } catch(_: NumberFormatException) {
                                        sendMessage["${arg[0]} 플레이어를 찾을 수 없습니다"]
                                        //sendMessage["잘못된 명령어 입니다"]
                                    }
                                }
                            }
                        }
                    }
                    Tpp -> {
                        val target: Playerc?
                        when(arg.size) {
                            1 -> {
                                target = if(arg[0].toIntOrNull() != null) {
                                    Groups.player.find { e -> e.id == arg[0].toInt() }
                                } else {
                                    Groups.player.find { e -> e.name().contains(arg[0]) }
                                }

                                if(target != null) {
                                    PluginData.keepTp.put(player, target)
                                    setPosition(player, target.x, target.y)
                                    sendMessage["${target.name()} 에게로 이동했습니다."]
                                } else {
                                    sendMessage["${arg[0]} 플레이어를 찾을 수 없습니다"]
                                }
                            }
                        }
                    }
                    Mute -> {
                        val target = if(arg[0].toIntOrNull() != null) {
                            PluginData.players.get(arg[0].toInt())
                        } else {
                            Groups.player.find { e -> e.name.contains(arg[0]) }
                        }

                        if(target != null) {
                            val data = PluginData[target.uuid()]
                            if (playerData.find{e -> e.uuid == target.uuid()}.permission.equals("owner", true)){
                                sendMessage["권한이 없습니다."]
                            } else {
                                if(data!!.isMute) {
                                    data.isMute = false
                                    target.sendMessage("축하드립니다. 묵언 상태가 해제되었습니다!")
                                } else {
                                    data.isMute = true
                                    target.sendMessage("누군가에 의해 묵언 상태가 되었습니다.")
                                }
                            }
                        }
                    }
                    Help -> {
                        if(arg.isNotEmpty() && arg[0].toIntOrNull() == null) {
                            try {
                                sendMessage[Command.valueOf(arg[0].capitalize()).toString()]
                            } catch(e: Exception) {
                                sendMessage["${arg[0]} 명령어를 찾을 수 없습니다!"]
                            }
                        } else {
                            val message = StringBuilder()
                            val page = if(arg.isNotEmpty()) arg[0].toInt() else 0

                            val commands = Seq<String>()
                            for(a in 0 until netServer.clientCommands.commandList.size) {
                                val command = netServer.clientCommands.commandList[a]
                                if(Permissions.check(player, command.text)) {
                                    commands.add("[orange] /${command.text} [white]${command.paramText} [lightgray]- ${command.description}\n")
                                }
                            }

                            val buffer = Mathf.ceil(commands.size.toFloat() / 6)
                            val pages = if(buffer > 1.0) buffer - 1 else 0

                            if(pages < page) {
                                sendMessage["[scarlet]페이지 쪽수는 최대 [orange]$pages[] 까지 있습니다"]
                            } else {
                                message.append("[green]==[white] 사용 가능한 명령어 목록. [sky]페이지 [orange]$page[]/[orange]$pages\n")
                            }

                            for(a in 6 * page until (6 * (page + 1)).coerceAtMost(commands.size)) {
                                message.append(commands.get(a))
                            }

                            sendMessage[message.toString().dropLast(1)]
                        }
                    }
                    Command.Discord -> {
                        val data = PluginData[uuid]
                        if(data != null) {
                            if(!data.json.has("discord")) {
                                if(Discord.pin.has(player.uuid())) {
                                    sendMessage["PIN 번호: ${Discord.pin.get(player.uuid()).asLong()}"]
                                } else {
                                    val pin = abs(Random.nextLong(Int.MAX_VALUE + 1L, Long.MAX_VALUE))
                                    Discord.pin.add(player.uuid(), pin)
                                    sendMessage["Discord 채널 내에서 !auth 명령어와 함께 이 PIN 번호를 입력하세요"]
                                    sendMessage["PIN 번호: $pin"]
                                }
                            } else {
                                sendMessage["이미 인증된 계정입니다."]
                            }
                        }
                    }
                    Killall -> {
                        if (!Vars.state.rules.pvp) {
                            var team = 0
                            var enemy = 0
                            Groups.unit.each {
                                if(it.team == mindustry.game.Team.sharded){
                                    team++
                                } else {
                                    enemy++
                                }
                                it.kill()
                            }
                            sendMessage["""
                                파괴된 아군 유닛 수: [green]$team
                                파괴된 적 유닛 수: [red]$enemy
                            """.trimIndent()]
                        } else {
                            sendMessage["이 명령어는 PvP 에서 사용할 수 없습니다."]
                        }
                    }
                }
            }
        } catch(e: Exception) {
            ErrorReport(e)
        }
    }
}