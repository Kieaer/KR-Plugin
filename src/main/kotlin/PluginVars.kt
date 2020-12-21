import java.security.SecureRandom

object PluginVars {
    val random = SecureRandom()
    val dataPort: Int = random.nextInt(65535)
}