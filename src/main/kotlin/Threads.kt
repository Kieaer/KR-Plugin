import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Threads {
    val worker: ExecutorService = Executors.newFixedThreadPool(3)
    val timer = Timer()

    init{
        worker.submit(Seconds())
        timer.scheduleAtFixedRate(Task(), 0, 1000)
        timer.scheduleAtFixedRate(Tick(), 0, 1000/16)
    }

    class Seconds : Thread(){
        override fun run() {
            while(!currentThread().isInterrupted){
                sleep(1000)
            }
        }
    }

    class Task : TimerTask(){
        override fun run() {
            PluginData.worldTime+1000L
        }
    }

    class Tick : TimerTask(){
        override fun run() {
            PluginData.totalUptime+(1000/16)
        }
    }
}