package data

import Main.Companion.pluginRoot
import PluginVars
import org.h2.tools.Server
import java.sql.Connection
import java.sql.DriverManager

object DB {
    var database: Connection
    var databaseServer: Server

    init{
        databaseServer = Server.createTcpServer("-tcpPort", "${PluginVars.dataPort}", "-tcpAllowOthers", "-tcpDaemon", "-baseDir", "./" + pluginRoot.child("data").path(), "-ifNotExists").start()
        database = DriverManager.getConnection("jdbc:h2:tcp://localhost:${PluginVars.dataPort}/player", "", "")
    }

    fun connect(){
        database = DriverManager.getConnection("jdbc:h2:tcp://localhost:${PluginVars.dataPort}/player", "", "")
    }

    fun disconnect(){
        database.close()
    }

    fun reconnect(){
        disconnect()
        connect()
    }

    fun shutdown(){
        databaseServer.stop()

    }
}