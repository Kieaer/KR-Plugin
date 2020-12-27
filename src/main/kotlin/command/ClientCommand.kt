package command

import arc.util.CommandHandler
import data.PlayerCore
import external.RegularExpression
import mindustry.Vars
import mindustry.gen.Playerc

object ClientCommand {
    fun register(handler: CommandHandler){
        handler.register("login", "<id> <password>", "Log in to player's account", ::login)
        handler.register("register", "<new_id> <new_password> <password_repeat>", "Register an account on the server", ::register)
    }

    private fun login(arg: Array<String>, player: Playerc){
        // 계정 존재 유무확인
        val isCorrect = PlayerCore.login(arg[0], arg[1])
        if(isCorrect){
            player.sendMessage("로그인 성공!")
        } else {
            PlayerCore.load(player)
        }
    }

    private fun register(arg: Array<String>, player: Playerc){
        val id = arg[0]
        val pw = arg[1]
        val pw2 = arg[2]

        // 비밀번호 패턴 확인
        val result = RegularExpression.check(pw, pw2, id, true)

        if(result){
            val data = Vars.netServer.admins.findByName(player.uuid()).first()
            // TODO country 만들기
            PlayerCore.register(player.name(), player.uuid(), data.timesKicked.toLong(), data.timesJoined.toLong(), System.currentTimeMillis(), System.currentTimeMillis(), "none", 0L, id, pw)
            player.sendMessage("계정 등록 성공!")
            PlayerCore.load(player)
        }
    }
}
