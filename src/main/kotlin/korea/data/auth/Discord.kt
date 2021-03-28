package korea.data.auth

import arc.Events
import arc.struct.ObjectMap
import arc.util.async.Threads.sleep
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import korea.PlayerData
import korea.PluginData
import korea.PluginData.banned
import korea.core.Log
import korea.data.Config
import korea.data.DB
import korea.data.PlayerCore
import korea.event.EventThread
import korea.exceptions.ErrorReport
import mindustry.Vars.netServer
import mindustry.game.EventType
import org.hjson.JsonObject
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object Discord {
    val pin: JsonObject = JsonObject()
    private lateinit var catnip: Catnip

    init {
        if (Config.discordBotToken.isNotEmpty()) {
            catnip = Catnip.catnip(Config.discordBotToken)
        }
    }

    fun start(){
        if(Discord::catnip.isInitialized) {
            catnip.observable(DiscordEvent.MESSAGE_CREATE).subscribe({
                if (it.channelIdAsLong().toString() == Config.discordChannelToken && !it.author().bot()) {
                    with(it.content()) {
                        it.delete()

                        for (a in banned){
                            val json = JsonObject.readJSON(a.json).asObject()
                            if (json.has("discord")){
                                if(it.author().idAsLong() == json.get("discord").asObject().get("id").asLong()){
                                    it.guild().blockingGet().ban(it.author().idAsLong(), "차단된 계정", 0)
                                }
                            }
                        }

                        when {
                            equals("!ping", true) -> {
                                val start = System.currentTimeMillis()
                                it.reply("pong!", true).subscribe { ping: Message ->
                                    val end = System.currentTimeMillis()
                                    ping.edit("${it.author().username()} -> pong! (" + (end - start) + "ms 소요됨).")
                                    sleep(5000)
                                    ping.delete()
                                }
                            }
                            equals("!help", true) -> {
                                val message = """
                                    ``!help`` 사용 가능한 서버 봇 명령어를 확인합니다.
                                    ``!auth PIN`` 서버에 Discord 인증을 해서 서버에 특별한 효과를 추가시킵니다.
                                    ``!ping`` 그냥 봇 작동 확인하는 용도입니다.
                                """.trimIndent()
                                it.reply(message, true).subscribe { m: Message ->
                                    sleep(7000)
                                    m.delete()
                                }
                            }
                            startsWith("!auth", true) -> {
                                if(Config.authType == Config.AuthType.Discord) {
                                    val arg = it.content().replace("!auth ", "").split(" ")
                                    if(arg.size == 1) {
                                        try {
                                            val buffer = PlayerCore.getAllData()
                                            var isMatch = false

                                            for(a in buffer) {
                                                if(a.json.has("discord")) {
                                                    if(a.json.get("discord").asObject().get("id")
                                                            .asLong() == it.author().idAsLong()
                                                    ) {
                                                        isMatch = true
                                                    }
                                                }
                                            }

                                            if(!isMatch) {
                                                var data:PlayerData? = null
                                                for(a in pin) {
                                                    if(a.value.asLong() == arg[0].toLong()) {
                                                        data = PluginData[a.name]
                                                    }
                                                }

                                                if(data != null) {
                                                    val info = JsonObject()
                                                    info.add("name", it.author().username())
                                                    info.add("id", it.author().idAsLong())
                                                    info.add("isAuthorized", true)

                                                    data.json.add("discord", info)
                                                    it.reply("성공! 서버 재접속 후에 효과가 발동됩니다.", true)
                                                        .subscribe {m:Message ->
                                                            sleep(5000)
                                                            m.delete()
                                                        }
                                                    PluginData[pin.remove(arg[0]).asString()]
                                                } else {
                                                    it.reply("등록되지 않은 계정입니다!", true)
                                                        .subscribe {m:Message ->
                                                            sleep(5000)
                                                            m.delete()
                                                        }
                                                }
                                            } else {
                                                it.reply("이미 등록된 계정입니다!", true)
                                                    .subscribe {m:Message ->
                                                        sleep(5000)
                                                        m.delete()
                                                    }
                                            }
                                        } catch(e:Exception) {
                                            it.reply("올바른 PIN 번호가 아닙니다! 사용법: ``!auth <PIN 번호>``", true).subscribe {m:Message ->
                                                sleep(5000)
                                                m.delete()
                                            }
                                        }
                                    } else {
                                        it.reply("사용법: ``!auth <PIN 번호>``", true)
                                            .subscribe {m:Message ->
                                                sleep(5000)
                                                m.delete()
                                            }
                                    }
                                } else {
                                    it.reply("현재 서버에 Discord 인증이 활성화 되어 있지 않습니다!", true)
                                }
                            }
                            // Console commands
                            equals("") -> {
                            }
                            contains("!") -> {
                                it.reply("알 수 없는 명령어 입니다!", true).subscribe { m: Message ->
                                    sleep(5000)
                                    m.delete()
                                }
                            }
                            else -> {

                            }
                        }
                    }
                }
            }) { e: Throwable -> ErrorReport(e) }

            catnip.connect()

            val blockingChannel = catnip.cache().channel(Config.discordServerToken.toLong(), 706326919972519987).blockingGet().asMessageChannel()

            // 플레이어가 차단되었을 때 작동
            Events.on(EventType.PlayerBanEvent::class.java){
                if(it.player != null) {
                    val name = it.player.name
                    val discord:Long

                    val sql = DB.database.prepareStatement("SELECT * FROM players WHERE \"uuid\"=?")
                    sql.setString(1, it.player.uuid())
                    val rs = sql.executeQuery()
                    discord = if(rs.next()) {
                        val json = JsonObject.readJSON(rs.getString("json")).asObject()
                        if(json.has("discord")) {
                            json.get("discord").asObject().get("id").asLong()
                        } else {
                            0L
                        }
                    } else {
                        0L
                    }

                    val message = """
                        시간: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))}
                        이름: $name
                        아이디: ${catnip.cache().member(Config.discordServerToken.toLong(), discord).blockingGet().asMention()}
                        IP: ${netServer.admins.findByIPs(it.player.con.address)}
                        """.trimIndent()

                    blockingChannel.sendMessage(message)
                }
            }

            /*// 플레이어가 IP 차단되었을 때 작동
            Events.on(EventType.PlayerIpBanEvent::class.java){
                EventThread(EventThread.EventTypes.PlayerIpBan, it).run()
            }*/

            Log.info("Discord 기능 활성화됨")
        }
    }

    fun stop(){
        if(Discord::catnip.isInitialized) catnip.close()
    }
}
