package korea
import korea.Main.Companion.isDispose
import korea.PluginData.playerData
import korea.core.PermissionFileRead
import korea.eof.sendMessage
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
        val messages = arrayOf(
            """
                뉴비를 발견했다면 이거 비효율적인데 왜 짓냐, 아니 왜함? 같은 말을 던지지 말고 공격적으로 가르칠려고 하지 말고 친절하게 알려줍시다.
                유저와 관리자 상관없이 서버장이 발견을 하면 즉시 강퇴 처리합니다.
            """.trimIndent(),
            """
                한국 서버에서 나오는 플러그인의 명령어들은 [green]/help[] 으로 볼 수 있으며, 자세한 명령어는 [green]/help <명령어>[] 으로 확인할 수 있습니다.
            """.trimIndent(),
            """
                버그를 발견했다면 한국 공식 Discord 에서 제보 부탁드립니다!
                제보받지 못한 버그는 오랫동안 방치될 확률이 높습니다.
            """.trimIndent(),
            """
                이 서버는 밴을 당하면 일반적인 방법으로 유저들을 처리하지 않는답니다. 처신 잘 하시길 바랍니다.
            """.trimIndent(),
            """
                최근 타노스 버그에 의해 서버에 영향을 주는 숨겨진 모드를 사용하면 영구 밴 처리가 됩니다.
                예) 게임 시간 조작 또는 로직 변경 등
            """.trimIndent()
        )
        var next = 0

        override fun run() {
            // 롤백 맵 저장
            try {
                if (Vars.state.`is`(GameState.State.playing)) {
                    SaveIO.save(AutoRollback.savePath)

                    sendMessage(messages[next])
                    next++
                    if (next > messages.size) next = 0
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}