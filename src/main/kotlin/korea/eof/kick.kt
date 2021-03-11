package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Playerc
import mindustry.net.Packets

class kick {
    constructor(player: Playerc, reason: String?) {
        if (reason == null){
            throw Exception("Reason is NULL!")
        } else {
            Core.app.post { player.kick(reason) }
        }
    }

    constructor(player: Playerc, reason: Packets.KickReason){
        Core.app.post { player.kick(reason) }
    }
}