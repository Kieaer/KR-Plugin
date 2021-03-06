package korea.data.auth

import arc.struct.ObjectMap
import arc.util.async.Threads.sleep
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import korea.PluginData
import korea.PluginData.banned
import korea.core.Log
import korea.data.Config
import korea.data.PlayerCore
import korea.exceptions.ErrorReport
import org.hjson.JsonObject
import java.lang.Exception
import java.util.concurrent.TimeUnit

object Discord {
    val pin: ObjectMap<Long, String> = ObjectMap()
    private lateinit var catnip: Catnip

    init {
        if (Config.discordBotToken.isNotEmpty()) {
            catnip = Catnip.catnip(Config.discordBotToken)
        }
    }

    fun start(){
        if(Discord::catnip.isInitialized) {
            catnip.observable(DiscordEvent.MESSAGE_CREATE).subscribe({ msg: Message ->
                if (msg.channelIdAsLong().toString() == Config.discordChannelToken && !msg.author().bot()) {
                    with(msg.content()) {
                        msg.delete()

                        for (a in banned){
                            val json = JsonObject.readJSON(a.json).asObject()
                            if (json.has("discord")){
                                if(msg.author().idAsLong() == json.get("discord").asObject().get("id").asLong()){
                                    msg.guild()?.ban(msg.author().idAsLong(), "차단된 계정", 0)
                                }
                            }
                        }

                        when {
                            equals("!ping", true) -> {
                                val start = System.currentTimeMillis()
                                msg.channel().sendMessage("${msg.author().username()} -> pong!").subscribe { ping: Message ->
                                    val end = System.currentTimeMillis()
                                    ping.edit("${msg.author().username()} -> pong! (" + (end - start) + "ms 소요됨).")
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
                                msg.channel().sendMessage(message).subscribe { m: Message ->
                                    sleep(7000)
                                    m.delete()
                                }
                            }
                            startsWith("!auth", true) -> {
                                val arg = msg.content().replace("!auth ", "").split(" ")
                                if (arg.size == 1) {
                                    try {
                                        val buffer = PlayerCore.getAllData()
                                        var isMatch = false

                                        for(a in buffer){
                                            if(a.json.has("discord")){
                                                if (a.json.get("discord").asObject().get("id").asLong() == msg.author().idAsLong()){
                                                    isMatch = true
                                                }
                                            }
                                        }

                                        if (!isMatch) {
                                            val data = PluginData[pin.get(arg[0].toLong())]
                                            if(data != null) {
                                                val info = JsonObject()
                                                info.add("name", msg.author().username())
                                                info.add("id", msg.author().idAsLong())
                                                info.add("isAuthorized", true)

                                                data.json.add("discord", info)
                                                msg.channel().sendMessage("${msg.author().username()} -> 성공! 서버 재접속 후에 효과가 발동됩니다.")
                                                    .subscribe {m:Message ->
                                                        sleep(5000)
                                                        m.delete()
                                                    }
                                                PluginData[pin.remove(arg[0].toLong())]
                                            } else {
                                                msg.channel().sendMessage("${msg.author().username()} -> 알 수 없는 플레이어 입니다!").subscribe {m:Message ->
                                                    sleep(5000)
                                                    m.delete()
                                                }
                                            }
                                        } else {
                                            msg.channel().sendMessage("${msg.author().username()} -> 이미 등록된 계정입니다!").subscribe {m:Message ->
                                                sleep(5000)
                                                m.delete()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        msg.channel().sendMessage("${msg.author().username()} -> 올바른 PIN 번호가 아닙니다! 사용법: ``!auth <PIN 번호>``").subscribe { m: Message ->
                                            sleep(5000)
                                            m.delete()
                                        }
                                    }
                                } else {
                                    msg.channel().sendMessage("${msg.author().username()} -> 사용법: ``!auth <PIN 번호>``").subscribe { m: Message ->
                                        sleep(5000)
                                        m.delete()
                                    }
                                }
                            }
                            // Console commands
                            equals("") -> {
                            }
                            contains("!") -> {
                                msg.channel().sendMessage("${msg.author().username()} -> 알 수 없는 명령어 입니다!").subscribe { m: Message ->
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
            Log.info("Discord 기능 활성화됨")
        }
    }

    fun stop(){
        if(Discord::catnip.isInitialized) catnip.close()
    }
}
