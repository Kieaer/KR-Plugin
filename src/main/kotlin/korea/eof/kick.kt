package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Playerc

class kick(player: Playerc, reason: String?) {
    init {
        if (reason == null){
            throw Exception("Reason is NULL!")
        } else {
            Core.app.post { Call.kick(player.con(), reason) }
        }
    }
}