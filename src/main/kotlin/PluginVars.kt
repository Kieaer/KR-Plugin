import arc.struct.Seq
import java.security.SecureRandom
import kotlin.reflect.full.declaredMemberProperties

class PluginVars {
    companion object{
        val random = SecureRandom()
        val dataPort: Int = random.nextInt(65535)
        val playerDataFieldSize = PlayerData::class.declaredMemberProperties.size-1
        val playerData = Seq<PlayerData>()
    }
}