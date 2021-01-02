package core

import PluginData.version
import arc.Core
import arc.Net
import arc.Net.HttpStatus
import arc.util.Strings
import arc.util.serialization.Jval
import org.hjson.JsonValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import kotlin.system.exitProcess

class PluginUpdater {
    init {
        Log.info("플러그인 버전 확인중...")

        javaClass.getResourceAsStream("/plugin.hjson").use { reader ->
            BufferedReader(InputStreamReader(reader)).use { br ->
                version = JsonValue.readJSON(br).asObject()["version"].asInt()
            }
        }

        Core.net.httpGet("https://api.github.com/repos/Kieaer/KR-Plugin/releases/latest", { res: Net.HttpResponse ->
            if (res.status == HttpStatus.OK) {
                val json = Jval.read(res.resultAsString)
                val current = Strings.parseInt(json.getString("tag_name", "0"))
                if (current > version) {
                    val asset = json["assets"].asArray().find { v: Jval ->
                        v.getString("name", "").startsWith("KR-Plugin")
                    }
                    val url = asset.getString("browser_download_url", "")
                    DriverLoader().download(Core.settings.dataDirectory.child("mods/Essentials.jar").file(), URL(url))
                    Log.info("플러그인 업데이트됨! 기존: $version 최신: $current. 서버 종료됨")
                    exitProcess(0)
                }
            } else {
                Log.warn("플러그인 업데이트 확인 실패! 오류 코드 ${res.status.code}")
            }
        }){}
    }
}