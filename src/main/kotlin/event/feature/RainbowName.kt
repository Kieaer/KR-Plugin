package event.feature

import Main.Companion.isDispose
import PluginData
import arc.struct.Seq
import arc.util.async.Threads.sleep
import mindustry.gen.Playerc

object RainbowName : Runnable {
    var colorOffset = 0
    var targets = Seq<Playerc>()

    override fun run() {
        while (!isDispose) {
            // TODO 재접시 무지개 닉 설정
            for (player in targets) {
                val p = PluginData[player.uuid()]
                if (p != null) {
                    if (p.json.has("rainbow")) {
                        val name = p.name.replace("\\[(.*?)]".toRegex(), "")
                        set(name, player)
                    } else {
                        player.name(p.name)
                        targets.remove(player)
                    }
                }
            }
            sleep(750)
        }
    }

    private fun set(name: String, player: Playerc) {
        val stringBuilder = StringBuilder()
        val colors = arrayOfNulls<String>(11)
        colors[0] = "[#ff0000]"
        colors[1] = "[#ff7f00]"
        colors[2] = "[#ffff00]"
        colors[3] = "[#7fff00]"
        colors[4] = "[#00ff00]"
        colors[5] = "[#00ff7f]"
        colors[6] = "[#00ffff]"
        colors[7] = "[#007fff]"
        colors[8] = "[#0000ff]"
        colors[9] = "[#8000ff]"
        colors[10] = "[#ff00ff]"
        val newName = arrayOfNulls<String>(name.length)
        for (i in name.indices) {
            val c = name[i]
            var colorIndex = (i + colorOffset) % colors.size
            if (colorIndex < 0) {
                colorIndex += colors.size
            }
            val newtext = colors[colorIndex] + c
            newName[i] = newtext
        }
        colorOffset--
        for (s in newName) {
            stringBuilder.append(s)
        }
        player.name(stringBuilder.toString())
    }
}