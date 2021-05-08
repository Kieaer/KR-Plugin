package korea.command

import arc.util.Log
import korea.PluginData
import korea.command.ServerCommand.Command.*
import mindustry.Vars
import mindustry.Vars.netServer
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
                    }
                }
            }
            Blacklist -> {
                PluginData.blacklist.add(arg[0])
                Log.info("${arg[0]} 을 블랙리스트에 추가함")
            }
        }
    }
}