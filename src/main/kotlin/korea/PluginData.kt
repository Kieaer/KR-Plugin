package korea
import arc.struct.Seq
import korea.Main.Companion.pluginRoot
import korea.core.Log
import korea.event.feature.Vote
import korea.event.feature.VoteType
import korea.form.Config
import mindustry.gen.Nulls
import mindustry.gen.Playerc
import org.hjson.JsonArray
import org.hjson.JsonObject
import java.security.SecureRandom
import kotlin.reflect.full.declaredMemberProperties

object PluginData : Config() {
    val dataPort: Int = SecureRandom().nextInt(65535)
    val playerDataFieldSize = PlayerData::class.declaredMemberProperties.size - 1
    val playerData = Seq<PlayerData>()
    var version: Int = 0
    var totalConnected: Int = 0
    var totalUptime: Long = 0L
    var worldTime: Long = 0L
    var banned = Seq<Banned>()

    var votingClass: Vote? = null
    var isVoting: Boolean = false
    var votingType: VoteType? = null
    var votingPlayer: Playerc = Nulls.player

    operator fun get(uuid: String): PlayerData? {
        return playerData.find { d -> uuid == d.uuid }
    }

    fun remove(uuid: String) {
        playerData.remove { d -> uuid == d.uuid }
    }

    override fun save() {
        val json = JsonObject()
        json.add("totalConnected", totalConnected)
        json.add("totalUptime", totalUptime)

        val banlist = JsonArray()
        banned.forEach {
            val j = JsonObject()
            j.add("name", it.name)
            j.add("address", it.address)
            j.add("uuid", it.uuid)
            banlist.add(j)
        }

        json.add("banned", banlist)

        pluginRoot.child("data/PluginData.obj").writeString(json.toString())
    }

    override fun load() {
        if (pluginRoot.child("data/PluginData.obj").exists()) {
            val json = JsonObject.readJSON(pluginRoot.child("data/PluginData.obj").readString())
            if (json != null) {
                totalConnected = json.asObject().getInt("totalConnected", totalConnected)
                totalUptime = json.asObject().getLong("totalUptime", totalUptime)

                val arr = json.asObject().get("banned").asArray()
                for (a in arr){
                    val obj = a.asObject()
                    banned.add(Banned(obj.get("name").asString(), obj.get("address").asString(), obj.get("uuid").asString()))
                }

                Log.system("플러그인 데이터 로드됨!")
            }
        }
    }

    override fun createFile() {
        // 파일 생성
        if (!pluginRoot.child("motd").exists()) {
            pluginRoot.child("motd").mkdirs()

            val message = """
                motd.txt 에서 편집후 콘솔에서 config motd off 를 하게되면 이 메세지가 대신 출력하게 됩니다.
                언어별로 설정하고 싶다면 motd_en.txt 또는 motd_ko.txt 등과 같이 뒤에 해당 국가의 언어 태그를 붙여주세요.
            """.trimIndent()
            pluginRoot.child("motd/motd.txt").writeString(message)
        }

        if (!pluginRoot.child("data").exists()){
            pluginRoot.child("data").mkdirs()
        }
    }

    class Banned(val name: String, val address: String, val uuid: String)
}