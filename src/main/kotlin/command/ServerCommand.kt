package command

import arc.util.CommandHandler
import mindustry.Vars

object ServerCommand {
    fun register(handler: CommandHandler) {
        handler.register("skipWave", "Skip many waves", ::skipWave)
    }

    private fun skipWave(arg: Array<String>){
        for (i in 1..35){
            Vars.logic.runWave()
        }
    }
}