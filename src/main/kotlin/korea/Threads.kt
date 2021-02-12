package korea
import korea.Main.Companion.isDispose
import korea.PluginData.playerData
import korea.core.PermissionFileRead
import korea.event.feature.AutoRollback
import korea.event.feature.RainbowName
import mindustry.Vars
import mindustry.core.GameState
import mindustry.io.SaveIO
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object Threads {
    val worker: ExecutorService = Executors.newFixedThreadPool(4)
    val timer = Timer()

    init{
        worker.submit(Seconds())
        worker.submit(RainbowName)
        worker.submit(PermissionFileRead)
        timer.scheduleAtFixedRate(Task(), 0, 1000)
        timer.scheduleAtFixedRate(AutoSave(), 0, 300000)
    }

    class Seconds : Thread(){
        override fun run() {
            while(!isDispose){
                PluginData.save()
                sleep(1000)
            }
        }
    }

    class Task : TimerTask(){
        override fun run() {
            // 맵 플레이 시간 1초 추가
            PluginData.worldTime = PluginData.worldTime+1000L
            PluginData.totalUptime = PluginData.totalUptime+1000L

            // 플레이어 플레이 시간 1초 추가
            for(a in playerData){
                a.playTime = a.playTime+1000L
            }
        }
    }
    class AutoSave : TimerTask(){
        override fun run() {
            // 롤백 맵 저장
            try {
                if (Vars.state.`is`(GameState.State.playing)) SaveIO.save(AutoRollback.savePath)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}