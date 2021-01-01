
import arc.struct.Seq
import java.security.SecureRandom
import kotlin.reflect.full.declaredMemberProperties

class PluginData {
    companion object{
        val dataPort: Int = SecureRandom().nextInt(65535)
        val playerDataFieldSize = PlayerData::class.declaredMemberProperties.size-1
        val playerData = Seq<PlayerData>()
        var version: Int = 0
        val totalConnected: Int = 0 // TODO 아래 2개 카운트 만들기
        val totalKicked: Int = 0
        val totalUptime: Long = 0L
        val worldTime: Long = 0L

        operator fun get(uuid: String): PlayerData? {
            return playerData.find { d -> uuid == d.uuid }
        }

        fun remove(uuid: String) {
            playerData.remove { d -> uuid == d.uuid }
        }
    }
}