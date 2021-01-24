package data.auth

import PluginData
import arc.struct.ObjectMap
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import core.Log
import data.Config
import org.hjson.JsonObject

object Discord {
    val pin: ObjectMap<Long, String> = ObjectMap()
    private lateinit var catnip: Catnip

    init {
        if (Config.discordBotToken.isNotEmpty()) {
            catnip = Catnip.catnip(Config.discordBotToken)
            Log.info("Discord 기능 활성화됨")
        }
    }

    fun start(){
        if(::catnip.isInitialized) {
            catnip.observable(DiscordEvent.MESSAGE_CREATE).subscribe({ msg: Message ->
                if (msg.channelIdAsLong() == Config.discordChannelToken && !msg.author().bot()) {
                    with(msg.content()) {
                        when {
                            equals("!ping", true) -> {
                                val start = System.currentTimeMillis()
                                msg.channel().sendMessage("pong!").subscribe { ping: Message ->
                                    val end = System.currentTimeMillis()
                                    ping.edit("pong! (" + (end - start) + "ms 소요됨).")
                                }
                            }
                            startsWith("!auth", true) -> {
                                val arg = msg.content().replace("!auth ", "").split(" ")
                                if (arg.size == 1) {
                                    try {
                                        val data = PluginData[pin.get(arg[0].toLong())]
                                        if (data != null) {
                                            val info = JsonObject()
                                            info.add("name", msg.author().username())
                                            info.add("id", msg.author().idAsLong())
                                            info.add("isAuthorized", true)

                                            data.json.add("discord", info)
                                            msg.channel().sendMessage("성공~!")
                                        } else {
                                            msg.channel().sendMessage("알 수 없는 플레이어 입니다!")
                                        }
                                    } catch (e: NumberFormatException) {
                                        msg.channel().sendMessage("올바른 PIN 번호가 아닙니다!")
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
        }
    }

    fun stop(){
        if(::catnip.isInitialized) catnip.close()
    }
}