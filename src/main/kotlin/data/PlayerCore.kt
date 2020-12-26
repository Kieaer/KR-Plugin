package data

import PlayerData
import PluginVars
import PluginVars.Companion.playerData
import mindustry.gen.Playerc

object PlayerCore {
    fun register(
        name: String,
        uuid: String,
        kickCount: Long,
        joinCount: Long,
        joinDate: Long,
        lastDate: Long,
        country: String,
        rank: Long
    ) : Boolean{
        val sql = DB.database.prepareStatement(("INSERT INTO players (" +
                "name,uuid,admin,placeCount,breakCount,kickCount,joinCount,level,exp,joinDate,lastDate,time,attackWinner,pvpWinner,pvpLoser,rainbowName,isConnected,isMute,isLogged,country,rank" +
                ") VALUES (${"?,".repeat(PluginVars.playerDataFieldSize)}").dropLast(1)+")")
        sql.setString(0, name)
        sql.setString(1, uuid)
        sql.setBoolean(2, false) // admin
        sql.setLong(3, 0L) // placeCount
        sql.setLong(4, 0L) // breakCount
        sql.setLong(5, kickCount)
        sql.setLong(6, joinCount)
        sql.setLong(7, 1L) // Level
        sql.setLong(8, 0L) // Exp
        sql.setLong(9, joinDate)
        sql.setLong(10, lastDate)
        sql.setLong(11, 0L) // time
        sql.setLong(12, 0L) // attackWinner
        sql.setLong(13, 0L) // pvpWinner
        sql.setLong(14, 0L) // pvpPloser
        sql.setBoolean(15, false) // rainbowName
        sql.setBoolean(16, false) // isConnected
        sql.setBoolean(17, false) // isMute
        sql.setBoolean(18, false) // isLogged
        sql.setString(19, country)
        sql.setLong(20, rank)
        return sql.execute()
    }

    fun permission(){

    }

    fun login(id: String, pw: String) : Boolean{
        val sql = DB.database.prepareStatement("SELECT uuid FROM players WHERE id=? and pw=?")
        sql.setString(0, id)
        sql.setString(1, pw)
        return sql.executeQuery().next()
    }

    fun getData(player: Playerc) : PlayerData{
        var data = PlayerData()

        val sql = DB.database.prepareStatement("SELECT * FROM players WHERE uuid=?")
        sql.setString(0, player.uuid())
        val rs = sql.executeQuery()
        if(rs.next()){
            data = PlayerData(
                rs.getString("name"),
                rs.getString("uuid"),
                rs.getBoolean("admin"),
                rs.getLong("placeCount"),
                rs.getLong("breakCount"),
                rs.getLong("kickCount"),
                rs.getLong("joinCount"),
                rs.getLong("level"),
                rs.getLong("exp"),
                rs.getLong("joinDate"),
                rs.getLong("lastDate"),
                rs.getLong("playTime"),
                rs.getLong("attackWinner"),
                rs.getLong("pvpWinner"),
                rs.getLong("pvpLoser"),
                rs.getBoolean("rainbowName"),
                rs.getBoolean("isMute"),
                rs.getBoolean("isLogged"),
                rs.getLong("afkTime"),
                rs.getString("country"),
                rs.getLong("rank")
            )
        }

        return data
    }

    fun load(player: Playerc){
        val data = getData(player)

        // 고정닉 설정
        player.name(data.name)

        data.joinCount++
        data.lastDate = System.currentTimeMillis()

        playerData.add(data)
    }

    fun save(uuid: String): Boolean{
        val data = playerData.find { d -> uuid == d.uuid }

        val sql = DB.database.prepareStatement("UPDATE tabel_name SET" +
                "name=?, uuid=?, admin=?, placeCount=?, breakCount=?, kickCount=?, joinCount=?, level=?," +
                "exp=?, lastDate=?, playTime=?, attackWinner=?, pvpWinner=?, pvpLoser=?, rainbowName=?," +
                "isMute=?, isLogged=?, afkTime=?, country=?, rank=? WHERE uuid=?")
        sql.setString(0, data.name)
        sql.setString(1, data.uuid)
        sql.setBoolean(2, data.admin)
        sql.setLong(3, data.placeCount)
        sql.setLong(4, data.breakCount)
        sql.setLong(5, data.kickCount)
        sql.setLong(6, data.joinCount)
        sql.setLong(7, data.level)
        sql.setLong(8, data.exp)
        sql.setLong(9, data.lastDate)
        sql.setLong(10, data.playTime)
        sql.setLong(11, data.attackWinner)
        sql.setLong(12, data.pvpWinner)
        sql.setLong(13, data.pvpLoser)
        sql.setBoolean(14, data.rainbowName)
        sql.setBoolean(15, data.isMute)
        sql.setBoolean(16, data.isLogged)
        sql.setLong(17, data.afkTime)
        sql.setString(18, data.country)
        sql.setLong(19, data.rank)
        sql.setString(20, data.uuid)
        return sql.execute()
    }
}