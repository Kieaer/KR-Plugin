import Main.Companion.pluginRoot
import arc.struct.Seq
import core.Log
import org.hjson.JsonObject
import java.security.SecureRandom
import kotlin.reflect.full.declaredMemberProperties

class PluginData {
    companion object{
        val dataPort: Int = SecureRandom().nextInt(65535)
        val playerDataFieldSize = PlayerData::class.declaredMemberProperties.size-1
        val playerData = Seq<PlayerData>()
        var version: Int = 0
        var totalConnected: Int = 0
        var totalUptime: Long = 0L
        var worldTime: Long = 0L

        operator fun get(uuid: String): PlayerData? {
            return playerData.find { d -> uuid == d.uuid }
        }

        fun remove(uuid: String) {
            playerData.remove { d -> uuid == d.uuid }
        }

        fun save(){
            val json = JsonObject()
            json.add("totalConnected", totalConnected)
            json.add("totalUptime", totalUptime)

            pluginRoot.child("data/PluginData.obj").writeString(json.toString())
            Log.info("플러그인 데이터 저장됨!")
        }

        fun load(){
            if (pluginRoot.child("data/PluginData.obj").exists()) {
                val json = JsonObject.readJSON(pluginRoot.child("data/PluginData.obj").readString())
                if (json != null) {
                    totalConnected = json.asObject().getInt("totalConnected", totalConnected)
                    totalUptime = json.asObject().getLong("totalUptime", totalUptime)
                    Log.info("플러그인 데이터 로드됨!")
                }
            }
        }
    }
}