package korea.data.auth

import arc.struct.ObjectMap
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import korea.PluginData
import korea.core.Log
import korea.data.Config
import org.hjson.JsonObject

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
                        when {
                            equals("!ping", true) -> {
                                val start = System.currentTimeMillis()
                                msg.channel().sendMessage("pong!").subscribe { ping: Message ->
                                    val end = System.currentTimeMillis()
                                    ping.edit("pong! (" + (end - start) + "ms 소요됨).")
                                }
                            }
                            equals("!help", true) -> {
                                val message = """
                                    ``!help`` 사용 가능한 서버 봇 명령어를 확인합니다.
                                    ``!auth PIN`` 서버에 Discord 인증을 해서 서버에 특별한 효과를 추가시킵니다.
                                    ``!ping`` 그냥 봇 작동 확인하는 용도입니다.
                                """.trimIndent()
                                msg.channel().sendMessage(message)
                            }
                            startsWith("!auth", true) -> {
                                val arg = msg.content().replace("!auth ", "").split(" ")
                                if (arg.size == 1) {
                                    try {
                                        val data = PluginData[pin.get(arg[0].toLong())]
                                        if (data != null) {
                                            msg.delete("Discord PIN 번호")

                                            val info = JsonObject()
                                            info.add("name", msg.author().username())
                                            info.add("id", msg.author().idAsLong())
                                            info.add("isAuthorized", true)

                                            data.json.add("discord", info)
                                            msg.channel().sendMessage("성공! 서버 재접속 후에 효과가 발동됩니다.")
                                            PluginData[pin.remove(arg[0].toLong())]
                                        } else {
                                            msg.channel().sendMessage("알 수 없는 플레이어 입니다!")
                                        }
                                    } catch (e: NumberFormatException) {
                                        msg.channel().sendMessage("올바른 PIN 번호가 아닙니다!")
                                        msg.channel().sendMessage("사용법: ``!auth <PIN 번호>``")
                                    }
                                } else {
                                    msg.channel().sendMessage("사용법: ``!auth <PIN 번호>``")
                                }
                            }
                            // Console commands
                            equals("") -> {

                            }
                            else -> {
                                msg.channel().sendMessage("알 수 없는 명령어 입니다!")
                            }
                        }
                    }
                }
            }) { e: Throwable -> e.printStackTrace() }

            catnip.connect()
            Log.info("Discord 기능 활성화됨")
        }
    }

    fun stop(){
        if(Discord::catnip.isInitialized) catnip.close()
    }
}