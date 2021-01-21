
import arc.ApplicationCore
import arc.Core
import arc.Net
import arc.Settings
import arc.backend.headless.HeadlessApplication
import arc.files.Fi
import arc.graphics.Color
import arc.util.CommandHandler
import com.github.javafaker.Faker
import data.Config
import mindustry.Vars
import mindustry.Vars.netServer
import mindustry.core.FileTree
import mindustry.core.GameState
import mindustry.core.Logic
import mindustry.core.NetServer
import mindustry.game.Team
import mindustry.gen.Groups
import mindustry.gen.Player
import mindustry.gen.Playerc
import mindustry.maps.Map
import mindustry.net.NetConnection
import org.junit.Assert
import org.junit.Test
import java.lang.Thread.sleep
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ClientCommandTest {
    private val main: Main
    private val serverCommand = CommandHandler("")
    private val clientCommand = CommandHandler("/")
    private val r = Random()
    private val player: Playerc

    init {
        Core.settings = Settings()
        Core.settings.dataDirectory = Fi("")
        Core.settings.dataDirectory.child("locales").writeString("en")
        Core.settings.dataDirectory.child("version.properties").writeString("modifier=release\ntype=official\nnumber=5\nbuild=custom build")
        Core.net = Net()
        Config.debug = true

        val testMap = arrayOfNulls<Map>(1)
        try {
            val begins = booleanArrayOf(false)
            val exceptionThrown = arrayOf<Throwable?>(null)
            val core: ApplicationCore = object : ApplicationCore() {
                override fun setup() {
                    Vars.headless = true
                    Vars.net = mindustry.net.Net(null)
                    Vars.tree = FileTree()
                    Vars.init()
                    Vars.content.createBaseContent()
                    add(Logic().also { Vars.logic = it })
                    add(NetServer().also { Vars.netServer = it })
                    Vars.content.init()
                }

                override fun init() {
                    super.init()
                    begins[0] = true
                    testMap[0] = Vars.maps.loadInternalMap("maze")
                    Thread.currentThread().interrupt()
                }
            }
            HeadlessApplication(core, 60f) { throwable: Throwable? -> exceptionThrown[0] = throwable }
            while (!begins[0]) {
                if (exceptionThrown[0] != null) {
                    exceptionThrown[0]!!.printStackTrace()
                    Assert.fail()
                }
                Thread.sleep(10)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        Groups.init()
        Vars.world.loadMap(testMap[0])
        Vars.state.set(GameState.State.playing)
        Core.settings.dataDirectory.child("locales").delete()
        Core.settings.dataDirectory.child("version.properties").delete()

        main = Main()
        main.init()
        main.registerClientCommands(clientCommand)
        main.registerServerCommands(serverCommand)

        player = createPlayer()
    }

    private fun getSaltString(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
        val salt = StringBuilder()
        while (salt.length < 25){
            val index = (r.nextFloat() * chars.length).toInt()
            salt.append(chars[index])
        }
        return salt.toString()
    }

    private fun createPlayer(): Player {
        val player = Player.create()
        val faker = Faker.instance(Locale.KOREA)

        player.reset()
        player.con = object : NetConnection(r.nextInt(255).toString() + "." + r.nextInt(255) + "." + r.nextInt(255) + "." + r.nextInt(255)) {
            override fun send(o: Any, sendMode: mindustry.net.Net.SendMode) {}
            override fun close() {}
        }
        player.name(faker.name().username())
        player.uuid()
        player.con.uuid = getSaltString()
        player.con.usid = getSaltString()
        player.set(r.nextInt(300).toFloat(), r.nextInt(500).toFloat())
        player.color.set(Color.rgb(r.nextInt(255), r.nextInt(255), r.nextInt(255)))
        player.color.a = r.nextFloat()
        player.team(Team.sharded)
        //player.unit(UnitTypes.dagger.spawn(r.nextInt(300).toFloat(), r.nextInt(500).toFloat()))
        player.add()
        netServer.admins.getInfo(player.uuid())
        Groups.player.update()

        assertNotNull(player)
        //assertNotNull(player.unit())
        return player
    }

    fun login(){

    }

    @Test
    fun register(){
        clientCommand.handleMessage("/register test @as123P @as123P", player)
        sleep(1000)
        assertFalse(PluginData.playerData.isEmpty)
    }

    fun spawn(){

    }

    fun vote(){

    }

    fun rainbow(){

    }

    fun kill(){

    }

    fun info(){

    }

    fun maps(){

    }

    fun motd(){

    }

    fun players(){

    }

    fun router(){

    }

    fun status(){

    }

    fun team(){

    }

    fun tp(){

    }

    fun mute(){

    }

    fun help(){

    }

    fun discord(){

    }
}