package data

import Main.Companion.pluginRoot
import form.Garbage.EqualsIgnoreCase
import org.hjson.JsonObject
import org.hjson.JsonValue
import org.hjson.Stringify
import java.util.*
import form.Config as DataConfig

object Config : DataConfig() {
    /** 플러그인에 표시되는 언어 */
    var locale: Locale = Locale.KOREAN

    /** 서버 내에서 출력되는 메세지의 앞 태그 */
    var prefix = ""

    /** 플러그인 자동 업데이트 **/
    var update = true

    /** 디버그를 위한 기능 작동 */
    var debug = true

    /** 잠수 플레이어 강퇴 시간. 단위는 1초 */
    var kickAFK = 0L

    /** 계정 관리방식. */
    var authType = AuthType.Password

    /** Discord 서버 토큰 */
    var discordServerToken = 0L

    /** Discord 채널 토큰 */
    var discordChannelToken = 0L

    /** Discord 봇 토큰 */
    var discordBotToken = ""

    /** 투표 기능 활성화 여부 */
    var enableVote = true

    /** 플러그인의 네트워크 모드.
     * Server 으로 설정할 경우 DB 서버가 켜지고, Client 으로 하면 받기만 함
     */
    var networkMode = NetworkMode.Server

    /** 이메일 인증에 사용될 메일의 ID */
    var emailID = ""

    /** 이메일 인증에 사용될 계정의 비밀번호 */
    var emailPassword = ""

    /** 메일 전송에 사용할 SMTP 서버 주소 */
    var smtpServer = "smtp.gmail.com"

    /** 메일 전송에 사용될 SMTP 서버 포트 */
    var smtpPort = 587

    enum class AuthType{
        None, Password, Discord, Kakaotalk;
    }

    enum class NetworkMode{
        Server, Client;
    }

    override fun createFile() {
        if (!pluginRoot.child("config.hjson").exists()) save()
    }

    override fun save() {
        val data = JsonObject()
        data.add("prefix", prefix, "서버 내에서 출력되는 메세지의 앞 태그")
        data.add("update", update, "플러그인 자동 업데이트")
        data.add("debug", debug, "디버그를 위한 기능 작동")
        data.add("kickAFK", kickAFK, "잠수 플레이어 강퇴 시간. 단위는 1초")
        data.add("authType", authType.toString(), "계정 관리방식")
        data.add("discordServerToken", discordServerToken, "Discord 서버 토큰")
        data.add("discordChannelToken", discordChannelToken, "Discord 채널 토큰")
        data.add("discordBotToken", discordBotToken, "Discord 봇 토큰")
        data.add("enableVote", enableVote, "투표 기능 활성화 여부")
        data.add("networkMode", networkMode.toString(), "플러그인의 네트워크 모드. Server 으로 설정할 경우 DB 서버가 켜지고, Client 으로 하면 받기만 함")
        data.add("emailID", emailID, "이메일 인증에 사용될 메일의 ID")
        data.add("emailPassword", emailPassword, "이메일 인증에 사용될 계정의 비밀번호")
        data.add("smtpServer", smtpServer, "메일 전송에 사용할 SMTP 서버 주소")
        data.add("smtpPort", smtpPort, "메일 전송에 사용될 SMTP 서버 포트")

        pluginRoot.child("config.hjson").writeString(data.toString(Stringify.HJSON_COMMENTS))
    }

    override fun load() {
        val data = JsonValue.readHjson(pluginRoot.child("config.hjson").reader()).asObject()

        prefix = data.getString("prefix", prefix)
        update = data.getBoolean("update", update)
        debug = data.getBoolean("debug", debug)
        kickAFK = data.getLong("kickAFK", kickAFK)
        authType = EqualsIgnoreCase(AuthType.values(), "authType", authType)
        discordServerToken = data.getLong("discordServerToken", discordServerToken)
        discordChannelToken = data.getLong("discordChannelToken", discordChannelToken)
        discordBotToken = data.getString("discordBotToken", discordBotToken)
        enableVote = data.getBoolean("enableVote", enableVote)
        networkMode = EqualsIgnoreCase(NetworkMode.values(), "networkMode", networkMode)
        emailID = data.getString("emailID", emailID)
        emailPassword = data.getString("emailPassword", emailPassword)
        smtpServer = data.getString("smtpServer", smtpServer)
        smtpPort = data.getInt("smtpPort", smtpPort)
    }
}