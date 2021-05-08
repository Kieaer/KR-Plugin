package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Playerc

class infoMessage(val player: Playerc, val msg: String?) {
    init {
        if(msg == null) {
            throw Exception("Message is NULL!")
        } else {
            Core.app.post { Call.infoMessage(player.con(), msg) }
        }
    }
}