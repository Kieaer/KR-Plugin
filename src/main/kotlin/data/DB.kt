package data

import Main.Companion.pluginRoot
import PluginVars
import essentials.special.DriverLoader.Companion.h2
import java.net.URLClassLoader
import java.sql.Connection
import java.sql.DriverManager
import java.lang.Exception


object DB {
    var database: Connection
    private lateinit var databaseServer: Any
    private lateinit var clazz: Class<*>

    init{
        if (Config.networkMode == Config.networkModes.Server) {
            try {
                clazz = Class.forName("org.h2.tools.Server", true, h2)
                databaseServer = clazz.getDeclaredConstructor().newInstance()

                val arr = arrayOf("-tcpPort", "${PluginVars.dataPort}", "-tcpAllowOthers", "-tcpDaemon", "-ifNotExists", "-baseDir", "./" + pluginRoot.child("data").path())
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
            database = DriverManager.getConnection("jdbc:h2:tcp://localhost:${PluginVars.dataPort}/player", "", "")
        } else {
            database = DriverManager.getConnection("jdbc:h2:file:./config/mods/KR-Plugin/data/player", "", "")
        }
    }

    fun connect(){
        database = if (Config.networkMode == Config.networkModes.Server) {
            DriverManager.getConnection("jdbc:h2:tcp://localhost:${PluginVars.dataPort}/player", "", "")
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

    fun createTable(){

    }
}