package core

import Main.Companion.pluginRoot
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Log {
    private const val tag = "KR-Plugin"

    fun info(message: String){
        arc.util.Log.infoTag(tag, message)
    }

    fun err(message: String){
        arc.util.Log.errTag(tag, message)
    }

    fun warn(message: String, vararg parameter: Any){
        arc.util.Log.warn("$tag$message", parameter)
    }

    fun debug(message: String, vararg parameter: Any){
        arc.util.Log.debug("$tag$message", parameter)
    }

    fun write(type: LogType, value: String, vararg params: String?) {
        val date = DateTimeFormatter.ofPattern("yyyy-MM-dd HH_mm_ss").format(LocalDateTime.now())

        if (!pluginRoot.child("log").exists()){
            pluginRoot.child("log/old").mkdirs()
            for (day in LogType.values()) {
                if(!pluginRoot.child("log/$type.log").exists()) {
                    pluginRoot.child("log/$type.log").writeString("")
                }
            }
        }

        val new = Paths.get(pluginRoot.child("log/$type.log").path())
        val old = Paths.get(pluginRoot.child("log/old/$type/$date.log").path())
        var log = pluginRoot.child("log/$type.log")
        val path = pluginRoot.child("log")

        if (log != null && log.length() > 1024 * 256) {
            log.writeString(Bundle()["log.file-end", date], true)
            try {
                if(!pluginRoot.child("log/old/$type").exists()){
                    pluginRoot.child("log/old/$type").mkdirs()
                }
                Files.move(new, old, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            log = null
        }
        if (log == null) log = path.child("$type.log")

        log!!.writeString("[${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))}] ${value}\n", true)
    }

    enum class LogType {
        PlayerJoin, PlayerLeave, Activity, Chat;
    }
}