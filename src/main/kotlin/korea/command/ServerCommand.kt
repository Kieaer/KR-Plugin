package korea.command

import arc.util.CommandHandler
import mindustry.Vars.state

object ServerCommand {
    val isUnBan = false

    fun register(handler: CommandHandler) {
        handler.removeCommand("ban")
        handler.removeCommand("unban")
        handler.removeCommand("say")

        handler.register("ban", "<type-id/name/ip> <username/IP/ID...>", "Completely unban a person by IP or ID.") { arg: Array<String> ->
            ServerCommandThread(Command.Ban, arg).run()
        }
        handler.register("unban", "<ip/ID>", "Completely unban a person by IP or ID.") { arg: Array<String> ->
            ServerCommandThread(Command.Unban, arg).run()
        }
        handler.register("blacklist", "<add/remove> [name]") {
            ServerCommandThread(Command.Blacklist, it).run()
        }
        handler.register("say", "<p/c/i/t> <message...>", "Send message to players.") {
            ServerCommandThread(Command.Say, it).run()
        }
    }

    enum class Command {
        Ban, Unban, Blacklist, Say
    }
}