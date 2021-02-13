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

        handler.register("login", "<닉네임> <비밀번호>", "플레이어의 계정에 로그인 합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Login, arg, player))
        }
        handler.register("register", "[새 비밀번호] [제발좀] [비밀번호만 치세요]", "서버에 계정을 등록합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Register, arg, player))
        }
        handler.register("spawn", "<unit/block> <이름> [개수/방향]", "블록이나 유닛을 스폰합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Spawn, arg, player))
        }
        handler.register("vote", "<kick/map/gameover/skipwave/rollback/op> [플레이어_이름/방향]", "투표기능.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Vote, arg, player))
        }
        handler.register("rainbow", "움직이는 무지개 닉 기능을 켜고 끕니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Rainbow, arg, player))
        }
        handler.register("kill", "[player_name]", "자폭하거나, 아니면 다른 유저의 유닛을 터트리거나.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Kill, arg, player))
        }
        handler.register("info", "[player_name]", "서버가 저장하고 있는 플레이어의 정보를 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Info, arg, player))
        }
        handler.register("maps", "[page]", "서버에 있는 맵 목록들을 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Maps, arg, player))
        }
        handler.register("motd", "서버 메세지를 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Motd, arg, player))
        }
        handler.register("players", "[page]", "현재 서버에 접속해있는 유저들의 이름이나 ID를 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Players, arg, player))
        }
        handler.register("router", "네. 이것은 분배기 입니다. 무려 움직이기까지 하죠!") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Router, arg, player))
        }
        handler.register("status", "실시간 서버 통계를 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Status, arg, player))
        }
        handler.register("team", "<derelict/sharded/crux/green/purple/blue> [플레이어_이름]", "자신 또는 다른 유저들의 팀을 변경합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Team, arg, player))
        }
        handler.register("ban", "<플레이어_이름> [time]", "일정시간 밴을 먹이거나, 영구밴을 먹입니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Ban, arg, player))
        }
        handler.register("tp", "<플레이어_이름/tileX> [목표_플레이어_이름/tileY]", "특정 플레이어에게로 이동하거나, 타일 위치로 이동할 수 있습니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Tp, arg, player))
        }
        handler.register("mute", "<플레이어_이름>", "플레이어의 채팅을 금지 시킵니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Mute, arg, player))
        }
        handler.register("help", "[페이지]", "플레이어별로 사용 가능한 모든 명령어를 확인합니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Help, arg, player))
        }
        handler.register("discord", "Discord 인증을 통해 더 많은 기능을 사용할 수 있습니다.") { arg: Array<String>, player: Playerc ->
            service.submit(ClientCommandThread(Command.Discord, arg, player))
        }
    }

    enum class Command{
        Login, Register, Spawn, Vote, Rainbow, Kill, Info, Maps, Motd, Players, Router, Status, Team ,Ban, Tp, Mute, Help, Discord
    }
}
