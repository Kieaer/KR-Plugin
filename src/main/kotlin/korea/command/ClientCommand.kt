package korea.command

import arc.util.CommandHandler
import korea.data.Config
import mindustry.gen.Playerc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors





object ClientCommand {
    val service: ExecutorService = Executors.newCachedThreadPool()

    fun register(handler: CommandHandler){
        handler.removeCommand("help")

        if (Config.enableVote){
            handler.removeCommand("vote")
            handler.removeCommand("votekick")
        }

        handler.register("login", "<id> <password>", "Log in to player's account") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Login, arg, player))
        }
        handler.register("register", "[new_password]", "Register an account on the server") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Register, arg, player))
        }
        handler.register("spawn", "<unit/block> <name> [amount/rotation]", "Spawn any block/units") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Spawn, arg, player))
        }
        handler.register("vote", "<kick/map/gameover/skipwave/rollback/op> [name/amount]", "Start various voting.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Vote, arg, player))
        }
        handler.register("rainbow", "Change your name to a animated rainbow color.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Rainbow, arg, player))
        }
        handler.register("kill", "[player_name]", "Suicide your self or kill other player.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Kill, arg, player))
        }
        handler.register("info", "[player_name]", "Check the player information stored by the server.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Info, arg, player))
        }
        handler.register("maps", "[page]", "View a list of all maps on the server.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Maps, arg, player))
        }
        handler.register("motd", "Check the server motd.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Motd, arg, player))
        }
        handler.register("players", "[page]", "Check the players name and ID currently connected to the server.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Players, arg, player))
        }
        handler.register("router", "Yes. This's a router. It even animated!") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Router, arg, player))
        }
        handler.register("status", "View statistics for the current server.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Status, arg, player))
        }
        handler.register("team", "<derelict/sharded/crux/green/purple/blue> [player_name]", "Switch your or other player team") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Team, arg, player))
        }
        handler.register("ban", "<player_name> [time]", "Block or permanently block the player for a certain amount of time.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Ban, arg, player))
        }
        handler.register("tp", "<player_name/tileX> [target_name/tileY]", "Can follow a specific player or move to a tile location.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Tp, arg, player))
        }
        handler.register("mute", "<player_name>", "Prohibits the player from chatting.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Mute, arg, player))
        }
        handler.register("help", "[page]", "Check the avaliable commands") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Help, arg, player))
        }
        handler.register("discord", "Discord certification allows you to use more features.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Discord, arg, player))
        }
    }

    enum class Command{
        Login, Register, Spawn, Vote, Rainbow, Kill, Info, Maps, Motd, Players, Router, Status, Team ,Ban, Tp, Mute, Help, Discord
    }
}
