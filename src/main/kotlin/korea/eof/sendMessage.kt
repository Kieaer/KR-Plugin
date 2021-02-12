package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Nulls
import mindustry.gen.Playerc

class sendMessage {
    var player: Playerc = Nulls.player

    constructor(player: Playerc, msg: String) {
        Core.app.post {
            player.sendMessage(msg)
        }
    }

    constructor(msg: String){
        Core.app.post{
            Call.sendMessage(msg)
        }
    }

    constructor(player: Playerc){
        this.player = player
    }

    operator fun get(msg: String){
        Core.app.post {
            player.sendMessage(msg)
        }
    }
}