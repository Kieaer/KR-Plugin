package korea.multi

import arc.Events
import arc.struct.Seq
import korea.core.Log
import mindustry.game.EventType
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

class Server {
    companion object {
        lateinit var serverSocket: ServerSocket
    }

    fun start() {
        Events.on(EventType.TileChangeEvent::class.java) {

        }
    }

    class server : Thread() {
        val list = Seq<serverService>()

        override fun run() {
            try {
                serverSocket = ServerSocket(9999)
                Log.info("서버 활성화됨")
                while(!serverSocket.isClosed) {
                    val socket = serverSocket.accept()
                    try {
                        val service = serverService(socket)
                        service.start()
                        list.add(service)
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    class serverService(socket: Socket) : Thread() {
        var br: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        var os: DataOutputStream = DataOutputStream(socket.getOutputStream())
        var ip: String = socket.inetAddress.toString()

        override fun run() {
            /*while (!currentThread().isInterrupted) {
                val stringBuffer: String = br.read
                if (!stringBuffer.isNullOrEmpty()) {
                    val tile = stringBuffer as
                }
            }*/
        }
    }

    class client : Thread()
}