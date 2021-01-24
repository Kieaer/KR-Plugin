package korea.data

import korea.Main.Companion.pluginRoot
import korea.PlayerData
import korea.PluginData
import korea.core.DriverLoader.Companion.h2
import korea.core.Log
import java.sql.Connection
import java.sql.DriverManager
import kotlin.reflect.full.declaredMemberProperties

object DB {
    var database: Connection
    private lateinit var databaseServer: Any
    private lateinit var consoleServer: Any
    private lateinit var clazz: Class<*>

    init{
        if (Config.networkMode == Config.NetworkMode.Server) {
            try {
                clazz = Class.forName("org.h2.tools.Server", true, h2)
                val obj = clazz.getDeclaredConstructor().newInstance()

                val arr = arrayOf("-tcpPort", "${if(!Config.debug) PluginData.dataPort else 8979}", "-tcpAllowOthers", "-tcpDaemon", "-ifNotExists", "-baseDir", pluginRoot.child("data").absolutePath())
                for (m in clazz.methods) {
                    if (m.name == "createTcpServer") {
                        val any = m.invoke(obj, arr)
                        for (o in clazz.methods) {
                            if (o.name == "start") {
                                databaseServer = o.invoke(any)
                                break
                            }
                        }
                        break
                    }
                }

                // 디버그를 위해 웹 서버 열기
                if(Config.debug){
                    val ar = arrayOf("-webAllowOthers", "-webDaemon", "-baseDir", "jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}/player")
                    for (m in clazz.methods) {
                        if (m.name == "createWebServer") {
                            val any = m.invoke(obj, ar)
                            for (o in clazz.methods) {
                                if (o.name == "start") {
                                    consoleServer = o.invoke(any)
                                    break
                                }
                            }
                            break
                        }
                    }
                    Log.system("DB 주소: jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            database = DriverManager.getConnection("jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}/player", "", "")
        } else {
            database = DriverManager.getConnection("jdbc:h2:file:./config/mods/KR-Plugin/data/player", "", "")
        }
    }

    private fun connect(){
        database = if (Config.networkMode == Config.NetworkMode.Server) {
            DriverManager.getConnection("jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}/player", "", "")
        } else {
            DriverManager.getConnection("jdbc:h2:file:./config/mods/KR-Plugin/data/player", "", "")
        }
    }

    private fun disconnect(){
        database.close()
    }

    fun reconnect(){
        disconnect()
        connect()
    }

    fun shutdownServer(){
        disconnect()
        if(DB::databaseServer.isInitialized) {
            for (m in clazz.methods) {
                if (m.name == "stop") {
                    m.invoke(databaseServer)
                    Log.system("DB 서버 종료됨!")
                    if(DB::consoleServer.isInitialized) {
                        m.invoke(consoleServer)
                        Log.system("웹 서버 종료됨!")
                    }

                    break
                }
            }
        }
    }

    fun createTable() : Boolean{
        val sql = """
            CREATE TABLE IF NOT EXISTS players(
            ${router()}
            )
        """.trimIndent()
        return database.prepareStatement(sql).execute()
    }

    private fun router() : String{
        val sql = StringBuilder()

        val fields = PlayerData::class.declaredMemberProperties
        for (a in fields){
            when (a.returnType.toString()){
                "kotlin.Long", "kotlin.Int" -> sql.append("\"${a.name}\" BIGINT,")
                "kotlin.String" -> sql.append("\"${a.name}\" VARCHAR(255),")
                "kotlin.Boolean" -> sql.append("\"${a.name}\" BOOLEAN,")
            }
        }
        sql.append("\"json\" CLOB")
        return sql.toString()
    }
}