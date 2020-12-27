package command

import arc.util.CommandHandler
import data.Config
import mindustry.Vars
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ServerCommand {
    val service: ExecutorService = Executors.newFixedThreadPool(4)

    fun register(handler: CommandHandler) {
        if(Config.debug) {
            handler.register("skipWave", "Skip many waves", ::skipWave)
        }
    }

    private fun skipWave(arg: Array<String>){
        for (i in 1..35){
            Vars.logic.runWave()
        }
    }
}