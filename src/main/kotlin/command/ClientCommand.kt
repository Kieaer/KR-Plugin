package command

import arc.util.CommandHandler
import mindustry.gen.Playerc

object ClientCommand {
    fun register(handler: CommandHandler){
        handler.register("login", "Log in to player's account", ClientCommand::login)
        handler.register("register", "Register an account on the server", ClientCommand::register)
    }

    private fun login(arg: Array<String>, player: Playerc){

    }

    private fun register(arg: Array<String>, player: Playerc){

    }
}
