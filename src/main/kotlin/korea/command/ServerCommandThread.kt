package korea.command

import arc.Core
import arc.util.Align
import arc.util.Log
import korea.PluginData
import korea.command.ServerCommand.Command.*
import korea.data.auth.Discord
import korea.eof.sendMessage
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.core.GameState
import mindustry.gen.Call
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.net.Packets.KickReason

class ServerCommandThread(private val type: ServerCommand.Command, private val arg: Array<String>) {
    fun run() {
        when(type) {
            Unban -> {
                netServer.admins.unbanPlayerIP(arg[0]) || netServer.admins.unbanPlayerID(arg[0])

                val find = PluginData.banned.find { it.address == arg[0] || it.uuid == arg[0] }
                if(find != null) {
                    PluginData.banned.remove { it.address == arg[0] || it.uuid == arg[0] }
                    Log.info("Unbanned.")
                }
            }
            Ban -> {
                if(arg[0] == "id") {
                    netServer.admins.banPlayerID(arg[1])
                    Log.info("Banned.")
                } else if(arg[0] == "name") {
                    val target = Groups.player.find { p: Player -> p.name().equals(arg[1], ignoreCase = true) }
                    if(target != null) {
                        netServer.admins.banPlayer(target.uuid())
                        Log.info("Banned.")
                    } else {
                        Log.err("No matches found.")
                    }
                } else if(arg[0] == "ip") {
                    netServer.admins.banPlayerIP(arg[1])
                    Log.info("Banned.")
                } else {
                    Log.err("Invalid type.")
                }

                for(player in Groups.player) {
                    if(netServer.admins.isIDBanned(player.uuid())) {
                        Call.sendMessage("[scarlet]" + player.name + "[white] 가 서버장에 의해 즉결 처분되었습니다.")
                        player.con.kick(KickReason.banned)
                        Discord.catnip.rest().channel().createMessage("706326919972519987", "이 유저는 서버장이 직접 처리했습니다.")
                    }
                }
            }
            Blacklist -> {
                PluginData.blacklist.add(arg[0])
                Log.info("${arg[0]} 을 블랙리스트에 추가함")
            }
            Say -> {
                if(!Vars.state.`is`(GameState.State.playing)){
                    Log.err("서버가 시작되지 않았습니다. 서버를 먼저 켜 주세요.")
                    return
                }

                when(arg[0]){
                    "p" -> {
                        Core.app.post{
                            val message = """
                                ====== [scarlet][ 서버 메세지 ][white] ======
                                ${arg[1]}
                            """.trimIndent()
                            Call.infoPopup(message, 5f, Align.center, 0, 0, 0, 0)
                        }
                    }
                    "c" -> {
                        sendMessage("[scarlet][[서버]:[] " + arg[1])
                    }
                    "i" -> {
                        Core.app.post{
                            val message = """
                                ====== [scarlet][ 서버 메세지 ][white] ======
                                ${arg[1]}
                            """.trimIndent()
                            Call.infoMessage(message)
                        }
                    }
                    "t" -> {
                        Core.app.post{
                            val message = "[scarlet][[서버]:[] " + arg[1]
                            Call.infoToast(message, 1f)
                        }
                    }
                    else -> {
                        Log.info("사용법: p 는 팝업, c 는 채팅, i 는 정보창.")
                    }
                }
                Log.info("서버: ${arg[1]}")
            }
        }
    }
}