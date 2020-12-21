import arc.func.Cons
import arc.util.CommandHandler
import mindustry.gen.Playerc
import kotlin.reflect.KFunction0

object ClientCommand {
    fun register(handler: CommandHandler){
        handler.register("login", "Log in to player's account", ::login)
        handler.register("register", "Register an account on the server", ::register)
    }

    private fun login(arg: Array<String>, player: Playerc){

    }

    private fun register(arg: Array<String>, player: Playerc){

    }
}
