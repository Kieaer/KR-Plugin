package korea.command

import arc.util.Log
import korea.PluginData
import korea.command.ServerCommand.Command.Unban
import mindustry.Vars

class ServerCommandThread(private val type: ServerCommand.Command, private val arg: Array<String>){
    fun run() {
        when (type) {
            Unban -> {
                Vars.netServer.admins.unbanPlayerIP(arg[0]) || Vars.netServer.admins.unbanPlayerID(arg[0])

                val find = PluginData.banned.find { it.address == arg[0] || it.uuid == arg[0]}
                if (find != null){
                    PluginData.banned.remove { it.address == arg[0] || it.uuid == arg[0] }
                    Log.info("Unbanned.")
                } else {
                    Log.err("That IP/ID is not banned!")
                }
            }
        }
    }
}