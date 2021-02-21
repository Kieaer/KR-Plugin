package korea.eof

import arc.Core
import mindustry.gen.Call
import mindustry.gen.Playerc

class infoPopup {
    constructor(msg: String?, duration: Float, align: Int, top: Int, left: Int, bottom: Int, right: Int){
        Core.app.post{
            if (msg == null){
                throw Exception("Message is NULL!")
            } else {
                Call.infoPopup(msg, duration, align, top, left, bottom, right)
            }
        }
    }

    constructor(player: Playerc, msg: String?, duration: Float, align: Int, top: Int, left: Int, bottom: Int, right: Int){
        Core.app.post{
            if (msg == null){
                throw Exception("Message is NULL!")
            } else {
                Call.infoPopup(player.con(), msg, duration, align, top, left, bottom, right)
            }
        }
    }
}