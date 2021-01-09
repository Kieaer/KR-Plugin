
import PluginData.playerData
import event.feature.AutoRollback
import event.feature.RainbowName
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
        timer.scheduleAtFixedRate(Task(), 0, 1000)
        timer.scheduleAtFixedRate(Tick(), 0, 1000/16)
        timer.scheduleAtFixedRate(AutoSave(), 0, 300000)
    }

    class Seconds : Thread(){
        override fun run() {
            while(!currentThread().isInterrupted){
                PluginData.save()
                sleep(1000)
            }
        }
    }

    class Task : TimerTask(){
        override fun run() {
            // 맵 플레이 시간 1초 추가
            PluginData.worldTime = PluginData.worldTime+1000L

            // 플레이어 플레이 시간 1초 추가
            for(a in playerData){
                a.playTime = a.playTime+1000L
            }
        }
    }

    class Tick : TimerTask(){
        override fun run() {
            // 서버 켜진 시간 계산
            PluginData.totalUptime = PluginData.totalUptime+(1000/16)
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