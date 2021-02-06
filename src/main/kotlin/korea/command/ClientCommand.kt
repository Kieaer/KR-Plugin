package korea.command

import arc.util.CommandHandler
import mindustry.gen.Playerc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors





object ClientCommand {
    val service: ExecutorService = Executors.newFixedThreadPool(3)

    fun register(handler: CommandHandler){
        try {
            handler.register("login", "<id> <password>", "Log in to player's account") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Login, arg, player).run()
            }
            handler.register("register", "<new_id> <new_password> <password_repeat>", "Register an account on the server") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Register, arg, player).run()
            }
            handler.register("spawn", "<unit/block> <name> [amount/rotation]", "Spawn any block/units") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Spawn, arg, player).run()
            }
            handler.register("vote", "<kick/map/gameover/skipwave/rollback/op> [name/amount]", "Start various voting.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Vote, arg, player).run()
            }
            handler.register("rainbow", "Change your name to a animated rainbow color.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Rainbow, arg, player).run()
            }
            handler.register("kill", "[player_name]", "Suicide your self or kill other player.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Kill, arg, player).run()
            }
            handler.register("info", "[player_name]", "Check the player information stored by the server.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Info, arg, player).run()
            }
            handler.register("maps", "[page]", "View a list of all maps on the server.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Maps, arg, player).run()
            }
            handler.register("motd", "Check the server motd.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Motd, arg, player).run()
            }
            handler.register("players", "[page]", "Check the players name and ID currently connected to the server.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Players, arg, player).run()
            }
            handler.register("router", "Yes. This's a router. It even animated!") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Router, arg, player).run()
            }
            handler.register("status", "View statistics for the current server.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Status, arg, player).run()
            }
            handler.register("team", "<derelict/sharded/crux/green/purple/blue> [player_name]", "Switch your or other player team") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Team, arg, player).run()
            }
            handler.register("ban", "<player_name> [time]", "Block or permanently block the player for a certain amount of time.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Ban, arg, player).run()
            }
            handler.register("tp", "<player_name/tileX> [target_name/tileY]", "Can follow a specific player or move to a tile location.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Tp, arg, player).run()
            }
            handler.register("mute", "<player_name>", "Prohibits the player from chatting.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Mute, arg, player).run()
            }
            handler.register("help", "[page]", "Check the avaliable commands") { arg: Array<String>, player: Playerc ->
                try {
                    println("help command entered")
                    ClientCommandThread(Command.Help, arg, player).run()
                    println("help command started")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            handler.register("discord", "Discord certification allows you to use more features.") { arg: Array<String>, player: Playerc ->
                ClientCommandThread(Command.Discord, arg, player).run()
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    enum class Command{
        Login, Register, Spawn, Vote, Rainbow, Kill, Info, Maps, Motd, Players, Router, Status, Team ,Ban, Tp, Mute, Help, Discord
    }
}
