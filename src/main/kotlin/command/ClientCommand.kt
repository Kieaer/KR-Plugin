package command

import arc.util.CommandHandler
import external.RegularExpression
import mindustry.gen.Playerc

object ClientCommand {
    fun register(handler: CommandHandler){
        handler.register("login", "<id> <password>", "Log in to player's account", ::login)
        handler.register("register", "<new_id> <new_password> <password_repeat>", "Register an account on the server", ::register)
    }

    private fun login(arg: Array<String>, player: Playerc){

    }

    private fun register(arg: Array<String>, player: Playerc){
        val id = arg[0]
        val pw = arg[1]
        val pw2 = arg[2]
        val result = RegularExpression.check(pw, pw2, id, true)
        if(result){
            player.sendMessage("계정 등록 성공!")
        }
    }
}
