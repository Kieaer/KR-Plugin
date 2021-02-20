package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Playerc

class kick(player: Playerc, reason: String) {
    init {
        Core.app.post{ Call.kick(player.con(), reason)}
    }
}