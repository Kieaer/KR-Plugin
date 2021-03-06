import arc.ApplicationCore
import arc.Core
import arc.Net
import arc.Settings
import arc.backend.headless.HeadlessApplication
import arc.files.Fi
import arc.graphics.Color
import arc.util.CommandHandler
import com.github.javafaker.Faker
import korea.Main
import korea.PluginData
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
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.io.File
import java.lang.Thread.sleep
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipFile
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ClientCommandTest {
    companion object {
        private lateinit var main: Main
        private val serverCommand = CommandHandler("")
        private val clientCommand = CommandHandler("/")
        private val r = Random()
        private lateinit var player: Playerc

        @BeforeClass
        @JvmStatic
        fun init() {
            Core.settings = Settings()
            Core.settings.dataDirectory = Fi("")
            val path = Core.settings.dataDirectory

            path.child("locales").writeString("en")
            path.child("version.properties").writeString("modifier=release\ntype=official\nnumber=6\nbuild=custom build")
            Core.net = Net()

            if(!path.child("maps").exists()) {
                path.child("maps").mkdirs()

                ZipFile(Paths.get("src", "test", "resources", "maps.zip").toFile().absolutePath).use { zip ->
                    zip.entries().asSequence().forEach { entry ->
                        if(entry.isDirectory) {
                            File(path.child("maps").absolutePath(), entry.name).mkdirs()
                        } else {
                            zip.getInputStream(entry).use { input ->
                                File(path.child("maps").absolutePath(), entry.name).outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        }
                    }
                }
            }

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
                        add(NetServer().also { netServer = it })
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
                while(!begins[0]) {
                    if(exceptionThrown[0] != null) {
                        exceptionThrown[0]!!.printStackTrace()
                        Assert.fail()
                    }
                    sleep(10)
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }

            Groups.init()
            Vars.world.loadMap(testMap[0])
            Vars.state.set(GameState.State.playing)
            path.child("locales").delete()
            path.child("version.properties").delete()

            main = Main()
            main.init()
            main.registerClientCommands(clientCommand)
            main.registerServerCommands(serverCommand)

            player = createPlayer()
        }

        @AfterClass
        @JvmStatic
        fun shutdown() {
            Core.app.listeners[0].dispose()
            Core.settings.dataDirectory.child("maps").deleteDirectory()
        }

        private fun getSaltString(): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
            val salt = StringBuilder()
            while(salt.length < 25) {
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
    }

    @Test
    fun login() {
        clientCommand.handleMessage("/login test @as123P", player)
    }

    @Test
    fun register() {
        clientCommand.handleMessage("/register test @as123P @as123P", player)

        // DB가 등록되기까지 대기
        sleep(1000)

        assertFalse(PluginData.playerData.isEmpty)
    }

    @Test
    fun spawn() {
        clientCommand.handleMessage("/spawn", player)
    }

    @Test
    fun vote() {
        clientCommand.handleMessage("/vote", player)
    }

    @Test
    fun rainbow() {
        clientCommand.handleMessage("/rainbow", player)
    }

    @Test
    fun kill() {
        clientCommand.handleMessage("/kill", player)
    }

    @Test
    fun info() {
        clientCommand.handleMessage("/info", player)
    }

    @Test
    fun maps() {
        clientCommand.handleMessage("/maps", player)
    }

    @Test
    fun motd() {
        clientCommand.handleMessage("/motd", player)
    }

    @Test
    fun players() {
        clientCommand.handleMessage("/players", player)
    }

    @Test
    fun router() {
        clientCommand.handleMessage("/router", player)
    }

    @Test
    fun status() {
        clientCommand.handleMessage("/status", player)
    }

    @Test
    fun team() {
        clientCommand.handleMessage("/team", player)
    }

    @Test
    fun tp() {
        clientCommand.handleMessage("/tp", player)
    }

    @Test
    fun mute() {
        clientCommand.handleMessage("/mute", player)
    }

    @Test
    fun help() {
        clientCommand.handleMessage("/help", player)
    }

    @Test
    fun discord() {

    }
}