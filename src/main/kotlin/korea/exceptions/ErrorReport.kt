package korea.exceptions

import korea.core.Log
import java.io.PrintWriter
import java.io.StringWriter

class ErrorReport(e: Throwable) {
    init {
        Log.err("오류 기록됨.")
        val error = StringWriter().also { e.printStackTrace(PrintWriter(it)) }.toString().trim()
        Log.write(Log.LogType.Error, "$error\n==================================================\n")
    }
}