package korea.command

import arc.util.CommandHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ServerCommand {
    val service: ExecutorService = Executors.newCachedThreadPool()
    val isUnBan = false

    fun register(handler: CommandHandler) {
        handler.removeCommand("unban")

        handler.register("unban", "<ip/ID>", "Completely unban a person by IP or ID.") { arg: Array<String> ->
            service.submit(ServerCommandThread(Command.Unban, arg))
        }
    }

    enum class Command{
        Unban
    }
}