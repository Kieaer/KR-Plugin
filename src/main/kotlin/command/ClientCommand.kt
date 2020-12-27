package command

import arc.util.CommandHandler
import mindustry.gen.Playerc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors




object ClientCommand {
    val service: ExecutorService = Executors.newFixedThreadPool(4)

    fun register(handler: CommandHandler){
        handler.register("login", "<id> <password>", "Log in to player's account") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(command.login, arg, player))
        }
        handler.register("register", "<new_id> <new_password> <password_repeat>", "Register an account on the server") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(command.register, arg, player))
        }
        handler.register("spawn", "[unit/block] [name] <amount/rotation>", "Spawn any block/units") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(command.spawn, arg, player))
        }
    }

    enum class command{
        login, register, spawn
    }
}
