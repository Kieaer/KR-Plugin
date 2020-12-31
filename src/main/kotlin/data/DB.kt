package data

import Main.Companion.pluginRoot
import PlayerData
import PluginData
import core.Log
import essentials.special.DriverLoader.Companion.h2
import java.net.URI
import java.sql.Connection
import java.sql.DriverManager
import kotlin.reflect.full.declaredMemberProperties

object DB {
    var database: Connection
    private lateinit var databaseServer: Any
    private lateinit var clazz: Class<*>

    init{
        if (Config.networkMode == Config.networkModes.Server) {
            try {
                clazz = Class.forName("org.h2.tools.Server", true, h2)
                databaseServer = clazz.getDeclaredConstructor().newInstance()

                val arr = arrayOf("-tcpPort", "${if(!Config.debug) PluginData.dataPort else 8979}", "-tcpAllowOthers", "-tcpDaemon", "-ifNotExists", "-baseDir", "./" + pluginRoot.child("data").path())
                for (m in clazz.methods) {
                    if (m.name == "createTcpServer") {
                        val any = m.invoke(databaseServer, arr)
                        for (o in clazz.methods) {
                            if (o.name == "start") {
                                o.invoke(any)
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
                            val any = m.invoke(databaseServer, ar)
                            for (o in clazz.methods) {
                                if (o.name == "start") {
                                    o.invoke(any)
                                    break
                                }
                            }
                            break
                        }
                    }
                    Log.info("DB 주소: jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}")
                    java.awt.Desktop.getDesktop().browse(URI("http://localhost:8082"))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            database = DriverManager.getConnection("jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}/player", "", "")
        } else {
            database = DriverManager.getConnection("jdbc:h2:file:./config/mods/KR-Plugin/data/player", "", "")
        }
    }

    fun connect(){
        database = if (Config.networkMode == Config.networkModes.Server) {
            DriverManager.getConnection("jdbc:h2:tcp://localhost:${if(!Config.debug) PluginData.dataPort else 8979}/player", "", "")
        } else {
            DriverManager.getConnection("jdbc:h2:file:./config/mods/KR-Plugin/data/player", "", "")
        }
    }

    fun disconnect(){
        database.close()
    }

    fun reconnect(){
        disconnect()
        connect()
    }

    fun shutdownServer(){
        if(::databaseServer.isInitialized) {
            for (m in clazz.methods) {
                if (m.name == "stop") {
                    m.invoke(databaseServer)
                    break
                }
            }
        }
        disconnect()
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
        return sql.toString().dropLast(1)
    }
}