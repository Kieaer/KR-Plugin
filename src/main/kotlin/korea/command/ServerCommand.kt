package korea.command

import arc.util.CommandHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ServerCommand {
    val service: ExecutorService = Executors.newCachedThreadPool()

    fun register(handler: CommandHandler) {

    }
}