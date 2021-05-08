package korea.core

import korea.Main.Companion.pluginRoot
import korea.exceptions.ErrorReport
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object Log {
    private const val tag = "KR-Plugin"
    val service: ExecutorService = Executors.newSingleThreadExecutor()

    fun info(message: String) {
        arc.util.Log.info(message)
    }

    fun system(message: String) {
        arc.util.Log.infoTag(tag, message)
    }

    fun err(message: String) {
        arc.util.Log.errTag(tag, message)
    }

    fun warn(message: String, vararg parameter: Any) {
        arc.util.Log.warn("[$tag] $message", parameter)
    }

    fun debug(message: String, vararg parameter: Any) {
        arc.util.Log.debug("$tag$message", parameter)
    }

    fun write(type: LogType, value: String) {
        service.submit(Thread {
            val date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss").format(LocalDateTime.now())

            if(!pluginRoot.child("log").exists()) {
                pluginRoot.child("log/old").mkdirs()
                for(day in LogType.values()) {
                    if(!pluginRoot.child("log/${type.name}.log").exists()) {
                        pluginRoot.child("log/${type.name}.log").writeString("")
                    }
                }
            }

            val new = Paths.get(pluginRoot.child("log/${type.name}.log").path())
            val old = Paths.get(pluginRoot.child("log/old/${type.name}/$date.log").path())
            var log = pluginRoot.child("log/${type.name}.log")
            val path = pluginRoot.child("log")

            if(log != null && log.length() > 1024 * 256) {
                log.writeString("== 이 파일의 끝입니다. 날짜: $date", true)
                try {
                    if(!pluginRoot.child("log/old/${type.name}").exists()) {
                        pluginRoot.child("log/old/${type.name}").mkdirs()
                    }
                    Files.move(new, old)
                } catch(e: IOException) {
                    ErrorReport(e)
                }
                log = null
            }
            if(log == null) log = path.child("${type.name}.log")
            log?.writeString("[${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            }] ${value}\n", true)
        })
    }

    enum class LogType {
        PlayerJoin, PlayerLeave, Activity, Chat, Command, Error;
    }
}