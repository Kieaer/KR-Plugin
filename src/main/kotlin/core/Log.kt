package core

object Log {
    val tag = "[KR-Plugin] "

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
}