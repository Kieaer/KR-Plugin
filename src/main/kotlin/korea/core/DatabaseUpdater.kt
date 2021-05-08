package korea.core

import korea.Main.Companion.pluginRoot
import java.net.URL
import java.util.*

class DatabaseUpdater {
    init {
        val file = pluginRoot.child("data/ipv4.txt").file()

        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.HOUR, -24)

        if(Date(file.lastModified()).before(cal.time)) {
            DriverLoader().download(file, URL("https://raw.githubusercontent.com/ktsaou/blocklist-ipsets/master/firehol_level1.netset"))
        }
    }
}