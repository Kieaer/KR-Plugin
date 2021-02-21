package korea.exceptions

import korea.core.Log

class ErrorReport(e: Exception) {
    init {
        val sb = StringBuilder()

        sb.append(e.toString()).append("\n")
        val element = e.stackTrace
        for (error in element) sb.append("\tat ").append(error.toString()).append("\n")
        sb.append("==================================================")

        Log.write(Log.LogType.Error, sb.toString())
    }
}