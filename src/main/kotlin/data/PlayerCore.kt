package data

import PlayerData
import PluginData
import PluginData.playerData
import command.Permissions
import mindustry.gen.Playerc
import org.hjson.JsonObject
import org.hjson.Stringify
import java.sql.SQLException

object PlayerCore {
    fun register(
        name: String,
        uuid: String,
        kickCount: Long,
        joinCount: Long,
        joinDate: Long,
        lastDate: Long,
        country: String,
        rank: Long,
        permission: String,
        json: JsonObject,
        id: String,
        pw: String
    ) : Boolean{
        val sql = DB.database.prepareStatement(("INSERT INTO players (" +
                "\"name\",\"uuid\",\"admin\",\"placeCount\",\"breakCount\",\"kickCount\",\"joinCount\",\"level\",\"exp\",\"joinDate\",\"lastDate\",\"playTime\",\"attackWinner\",\"pvpWinner\",\"pvpLoser\",\"rainbowName\",\"isMute\",\"isLogged\",\"country\",\"rank\", \"permission\", \"json\", \"id\", \"pw\"" +
                ") VALUES (${"?,".repeat(PluginData.playerDataFieldSize)}").dropLast(1)+")")
        sql.setString(1, name)
        sql.setString(2, uuid)
        sql.setBoolean(3, false) // admin
        sql.setLong(4, 0L) // placeCount
        sql.setLong(5, 0L) // breakCount
        sql.setLong(6, kickCount)
        sql.setLong(7, joinCount)
        sql.setLong(8, 1L) // Level
        sql.setLong(9, 0L) // Exp
        sql.setLong(10, joinDate)
        sql.setLong(11, lastDate)
        sql.setLong(12, 0L) // time
        sql.setLong(13, 0L) // attackWinner
        sql.setLong(14, 0L) // pvpWinner
        sql.setLong(15, 0L) // pvpPloser
        sql.setBoolean(16, false) // rainbowName
        sql.setBoolean(17, false) // isMute
        sql.setBoolean(18, false) // isLogged
        sql.setString(19, country)
        sql.setLong(20, rank)
        sql.setString(21, permission)
        sql.setString(22, json.toString(Stringify.PLAIN))
        sql.setString(23, id)
        sql.setString(24, pw)
        return try{
            sql.execute()
        } catch (e: SQLException){
            e.printStackTrace()
            false
        }

    }

    fun login(id: String, pw: String) : Boolean{
        val sql = DB.database.prepareStatement("SELECT \"uuid\" FROM players WHERE \"id\"=? and \"pw\"=?")
        sql.setString(1, id)
        sql.setString(2, pw)
        return sql.executeQuery().next()
    }

    fun check(uuid: String) : Boolean{
        val sql = DB.database.prepareStatement("SELECT * FROM players WHERE \"uuid\"=?")
        sql.setString(1, uuid)
        return sql.executeQuery().next()
    }

    private fun getData(player: Playerc) : PlayerData{
        var data = PlayerData()

        val sql = DB.database.prepareStatement("SELECT * FROM players WHERE \"uuid\"=?")
        sql.setString(1, player.uuid())
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
                rs.getLong("rank"),
                JsonObject.readJSON(rs.getString("json")).asObject(),
                rs.getString("id"),
                rs.getString("pw")
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

        // 권한 파일 생성
        if(Permissions.userData.find { e -> e.name == player.uuid() } == null) {
            Permissions.createNewData(data)
        }
    }

    fun save(uuid: String): Boolean{
        val data = playerData.find { d -> uuid == d.uuid }

        val sql = DB.database.prepareStatement("UPDATE players SET " +
                "\"name\"=?, \"uuid\"=?, \"admin\"=?, \"placeCount\"=?, \"breakCount\"=?, \"kickCount\"=?, \"joinCount\"=?, \"level\"=?, " +
                "\"exp\"=?, \"lastDate\"=?, \"playTime\"=?, \"attackWinner\"=?, \"pvpWinner\"=?, \"pvpLoser\"=?, \"rainbowName\"=?, " +
                "\"isMute\"=?, \"isLogged\"=?, \"afkTime\"=?, \"country\"=?, \"rank\"=?, \"permission\"=?, \"json\"=? WHERE \"uuid\"=?")
        sql.setString(1, data.name)
        sql.setString(2, data.uuid)
        sql.setBoolean(3, data.admin)
        sql.setLong(4, data.placeCount)
        sql.setLong(5, data.breakCount)
        sql.setLong(6, data.kickCount)
        sql.setLong(7, data.joinCount)
        sql.setLong(8, data.level)
        sql.setLong(9, data.exp)
        sql.setLong(10, data.lastDate)
        sql.setLong(11, data.playTime)
        sql.setLong(12, data.attackWinner)
        sql.setLong(13, data.pvpWinner)
        sql.setLong(14, data.pvpLoser)
        sql.setBoolean(15, data.rainbowName)
        sql.setBoolean(16, data.isMute)
        sql.setBoolean(17, data.isLogged)
        sql.setLong(18, data.afkTime)
        sql.setString(19, data.country)
        sql.setLong(20, data.rank)
        sql.setString(21, data.permission)
        sql.setString(22, data.json.toString(Stringify.PLAIN))
        sql.setString(23, data.uuid)
        return sql.executeUpdate() != 0
    }
}