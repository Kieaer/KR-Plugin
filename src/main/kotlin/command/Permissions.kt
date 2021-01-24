package command

import Main.Companion.pluginRoot
import PlayerData
import PluginData
import core.Log
import mindustry.gen.Playerc
import org.hjson.*

object Permissions {
    var defaultGroup: String = "default"
    var userData: JsonObject = JsonObject()
    var permissionData: JsonObject = JsonObject()
    var isError: Boolean = false

    operator fun get(name: String): JsonObject? {
        return userData.get(name)?.asObject()
    }

    fun check(player: Playerc, command: String): Boolean {
        val p = PluginData[player.uuid()]
        if (p != null) {
            val data = userData[player.uuid()]
            if (data != null) {
                val obj = data.asObject()
                val size = permissionData[obj["group"].asString()].asObject()["nodes"].asArray().size()
                for (a in 0 until size) {
                    val node = permissionData[obj["group"].asString()].asObject()["nodes"].asArray()[a].asString()
                    if (node == command || node == "ALL") return true
                }
            } else {
                return false
            }
        }
        return when (command){
            "login", "register" -> true
            else -> false
        }
    }

    fun createNewData(playerData: PlayerData) {
        val data = JsonObject()
        data.add("name", playerData.name)
        data.add("group", defaultGroup)
        data.add("chatFormat", permissionData[playerData.permission].asObject().getString("chatFormat", "%1[orange] >[white] %2"))
        data.add("admin", playerData.admin)
        userData.add(playerData.uuid, data)

        pluginRoot.child("permission_user.hjson").writeString(userData.toString(Stringify.HJSON))
        Log.system("${playerData.name} 의 권한 데이터가 생성되었습니다.")
    }

    fun load(isInit: Boolean){
        if (pluginRoot.child("permission.hjson").exists()) {
            try {
                permissionData = JsonValue.readHjson(pluginRoot.child("permission.hjson").reader()).asObject()
            } catch (e: ParseException){
                Log.err("permission.json 파일에서 구문 오류로 인해 기본값으로 로드됩니다.")
                Log.err("오류가 발생한 위치: ${e.line}줄에서 ${e.column}번째 글자.\n" +
                        "오류는 ${e.message}.")
                isError = true
                createDefault(false)
            }
        } else {
            createDefault(true)
            Log.system("permission.json 파일 생성됨!")
        }

        if (pluginRoot.child("permission_user.hjson").exists()) {
            try {
                userData = JsonValue.readHjson(pluginRoot.child("permission_user.hjson").reader()).asObject()
            } catch (e: ParseException){
                Log.err("permission_user.json 파일에서 구문 오류로 인해 플레이어 데이터를 사용하지 않습니다!")
                Log.err("오류가 발생한 위치: ${e.line}줄에서 ${e.column}번째 글자.\n" +
                        "오류는 ${e.message}.")
                isError = true
            }
        }
    }

    fun save(){
        if(!isError) pluginRoot.child("permission_user.hjson").writeString(userData.toString(Stringify.FORMATTED))
    }

    private fun createDefault(isSave: Boolean){
        val json = JsonObject()
        var data = JsonObject()
        var nodes = JsonArray()

        nodes.add("ALL")
        data.add("admin", true)
        data.add("chatFormat", "[sky][Owner] %1[orange] > [white]%2")
        data.add("nodes", nodes)
        json.add("owner", data)

        data = JsonObject()
        nodes = JsonArray()
        nodes.add("mute", "spawn")

        nodes.add("info")
        nodes.add("login")
        nodes.add("register")
        nodes.add("maps")
        nodes.add("motd")
        nodes.add("r")
        nodes.add("players")
        nodes.add("status")
        nodes.add("tp")
        nodes.add("vote")
        nodes.add("help")

        data.add("admin", true)
        data.add("chatFormat", "[yellow][Admin] %1[orange] > [white]%2")
        data.add("nodes", nodes)
        json.add("admin", data)

        data = JsonObject()
        nodes = JsonArray()
        nodes.add("info")
        nodes.add("login")
        nodes.add("register")
        nodes.add("maps")
        nodes.add("motd")
        nodes.add("r")
        nodes.add("players")
        nodes.add("status")
        nodes.add("tp")
        nodes.add("vote")
        nodes.add("help")
        data.add("chatFormat", "%1[orange] >[white] %2")
        data.add("nodes", nodes)
        json.add("default", data)

        permissionData = json
        if(isSave) pluginRoot.child("permission.hjson").writeString(json.toString(Stringify.HJSON))
    }
}