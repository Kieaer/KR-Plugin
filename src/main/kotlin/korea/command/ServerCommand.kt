package korea.command

import arc.util.CommandHandler

object ServerCommand {
    val isUnBan = false

    fun register(handler: CommandHandler) {
        handler.removeCommand("unban")

        handler.register("unban", "<ip/ID>", "Completely unban a person by IP or ID.") { arg: Array<String> ->
            ServerCommandThread(Command.Unban, arg).run()
        }
        handler.register("blacklist", "<add/remove> [name]") {
            ServerCommandThread(Command.Blacklist, it).run()
        }
    }

    enum class Command {
        Unban, Blacklist
    }
}