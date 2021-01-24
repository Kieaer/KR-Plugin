package external

import arc.graphics.Color
import arc.graphics.Colors
import arc.util.Strings
import mindustry.Vars

object FixName {
    // mindustry.core.NetServer.fixName

    operator fun get(s: String): String{
        var name = s
        name = name.trim { it <= ' ' }
        if (name == "[" || name == "]") {
            return ""
        }

        for (i in 0 until name.length) {
            if (name[i] == '[' && i != name.length - 1 && name[i + 1] != '[' && (i == 0 || name[i - 1] != '[')) {
                val prev = name.substring(0, i)
                val next = name.substring(i)
                val result: String = checkColor(next)
                name = prev + result
            }
        }

        val result = StringBuilder()
        var curChar = 0
        while (curChar < name.length && result.toString().toByteArray(Strings.utf8).size < Vars.maxNameLength) {
            result.append(name[curChar++])
        }
        return result.toString()
    }

    fun checkColor(str: String): String {
        for (i in 1 until str.length) {
            if (str[i] == ']') {
                val color = str.substring(1, i)
                if (Colors.get(color.toUpperCase()) != null || Colors.get(color.toLowerCase()) != null) {
                    val result = if (Colors.get(color.toLowerCase()) == null) Colors.get(color.toUpperCase()) else Colors.get(color.toLowerCase())
                    if (result.a <= 0.8f) {
                        return str.substring(i + 1)
                    }
                } else {
                    try {
                        val result = Color.valueOf(color)
                        if (result.a <= 0.8f) {
                            return str.substring(i + 1)
                        }
                    } catch (e: Exception) {
                        return str
                    }
                }
            }
        }
        return str
    }
}