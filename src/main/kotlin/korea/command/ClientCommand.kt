package korea.command

import arc.util.CommandHandler
import korea.data.Config
import mindustry.gen.Playerc
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors





object ClientCommand {
    fun register(handler: CommandHandler){
        handler.removeCommand("help")

        if (Config.enableVote){
            handler.removeCommand("vote")
            handler.removeCommand("votekick")
        }

        handler.register("login", "<닉네임> <비밀번호>", "플레이어의 계정에 로그인 합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Login, arg, player).run()
        }
        handler.register("register", "[새_비밀번호] [제발좀] [비밀번호만_치세요]", "서버에 계정을 등록합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Register, arg, player).run()
        }
        handler.register("spawn", "<unit/block> <이름> [개수/방향]", "블록이나 유닛을 스폰합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Spawn, arg, player).run()
        }
        handler.register("vote", "<kick/map/gameover/skipwave/rollback/random> [플레이어_이름/방향]", "투표기능.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Vote, arg, player).run()
        }
        handler.register("rainbow", "움직이는 무지개 닉 기능을 켜고 끕니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Rainbow, arg, player).run()
        }
        handler.register("kill", "[플레이어_이름]", "자폭하거나, 아니면 다른 유저의 유닛을 터트리거나.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Kill, arg, player).run()
        }
        handler.register("info", "[플레이어_이름]", "서버가 저장하고 있는 플레이어의 정보를 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Info, arg, player).run()
        }
        handler.register("maps", "[페이지]", "서버에 있는 맵 목록들을 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Maps, arg, player).run()
        }
        handler.register("motd", "서버 메세지를 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Motd, arg, player).run()
        }
        handler.register("players", "[페이지]", "현재 서버에 접속해있는 유저들의 이름이나 ID를 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Players, arg, player).run()
        }
        handler.register("router", "네. 이것은 분배기 입니다. 무려 움직이기까지 하죠!") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Router, arg, player).run()
        }
        handler.register("status", "실시간 서버 통계를 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Status, arg, player).run()
        }
        handler.register("team", "<derelict/sharded/crux/green/purple/blue> [플레이어_이름]", "자신 또는 다른 유저들의 팀을 변경합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Team, arg, player).run()
        }
        handler.register("ban", "<플레이어_이름> [time]", "일정시간 밴을 먹이거나, 영구밴을 먹입니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Ban, arg, player).run()
        }
        handler.register("tp", "<플레이어_이름/tileX> [목표_플레이어_이름/tileY]", "특정 플레이어에게로 이동하거나, 타일 위치로 이동할 수 있습니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Tp, arg, player).run()
        }
        handler.register("mute", "<플레이어_이름>", "플레이어의 채팅을 금지 시킵니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Mute, arg, player).run()
        }
        handler.register("help", "[페이지]", "플레이어별로 사용 가능한 모든 명령어를 확인합니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Help, arg, player).run()
        }
        handler.register("discord", "Discord 인증을 통해 더 많은 기능을 사용할 수 있습니다.") { arg: Array<String>, player: Playerc ->
            ClientCommandThread(Command.Discord, arg, player).run()
        }
    }

    enum class Command{
        Login {
            override fun toString(): String {
                return """
                    명령어: [green]/login <닉네임> <비밀번호>[]
                    [yellow]사용 예시: /login Asaka qwerty123[]
                    
                    이 명령어를 사용하여 기존에 생성했던 계정으로 로그인합니다.
                    기기를 옮겨가며 계정에 데이터를 쌓고 싶을때 유용합니다.
                """.trimIndent()
            }
        }, Register {
            override fun toString(): String {
                return """
                    명령어: [green]/register <비밀번호>[]
                    [yellow]사용 예시: /register qwerty123[]
                    
                    이 명령어를 사용하여 서버에 계정을 등록합니다.
                    비밀번호는 반드시 최소 6자 이상에 영문, 숫자를 포함해야 하며, 꺽쇄 ([scarlet]<[], [scarlet]>[]) 를 사용할 수 없습니다.
                """.trimIndent()
            }
        }, Spawn {
            override fun toString(): String {
                return """
                    명령어: [green]/spawn <unit/block> <유닛/블록 이름> [개수 (유닛의 경우)][white]
                    [sky]/spawn unit omega[] - 현재 위치에 오메가를 1마리 생성합니다.
                    [sky]/spawn unit crawler 100[] - 현재 위치에 크롤러 100마리를 생성합니다. 이경우 모든 유닛이 한자리에 뭉쳐 있으니 주의 하시길 바랍니다.
                    [sky]/spawn block item-source[] - 현재 위치에 무한 자원블록을 만듭니다.
                    [sky]/spawn block container[] - 현재 위치에 컨테이너를 만듭니다.
                    
                    이 명령어를 사용하여 현재 일반적인 방법으로 건설 또는 생성할 수 없는 건물/유닛들을 생성 할 수 있습니다.
                    남용에 주의하세요!
                """.trimIndent()
            }
        }, Vote {
            override fun toString(): String {
                return """
                    명령어: [green]/vote <kick/map/gameover/skipwave/rollback/random> [플레이어_이름/방향]
                    [sky]/vote kick VisitorPlayer[] - VisitorPlayer 라는 플레이어가 있을 경우 강퇴 투표를 시작합니다.
                    [sky]/vote map <숫자>[] - /maps 명령어를 사용하여 확인한 맵 번호를 입력하면, 해당 맵으로 이동하는 투표를 시작합니다.
                    [sky]/vote gameover[] - 앞날이 막막할 때 항복 투표를 진행합니다.
                    [sky]/vote skipwave <숫자>[] - 일정 wave 를 한꺼번에 진행합니다. 이미 높은 Wave 상태에서 입력할 경우 서버 랙 또는 한순간에 모든 것을 파멸로 이끌 수 있으니 주의하세요.
                    [sky]/vote rollback[] - 테러 복구에 유용하며, 서버가 마지막으로 저장한 시간대의 맵으로 다시 되돌립니다.
                    [sky]/vote fast[] - 서버의 건설 수준이 높아져서 게임이 노잼화가 되었을 때 웨이브간 간격을 0초로 만들어 버립니다.
                    [sky]/vote random[] - 랜덤 박스를 열어봅시다. 무슨 효과가 일어날까요?
                """.trimIndent()
            }
        }, Rainbow {
            override fun toString(): String {
                return """
                    명령어: [green]/rainbow[]
                    자기 자신에게 움직이는 무지개 닉네임을 설정시킵니다. 이 설정은 기본값으로 관리자 이상만 사용할 수 있습니다.
                """.trimIndent()
            }
        }, Kill {
            override fun toString(): String {
                return """
                    명령어: [green]/kill[]
                    [sky]/kill <플레이어 이름>[] - 다른 플레이어의 유닛을 파괴 시킵니다. 관리자 전용.
                    
                    이 명령어를 사용하면 자기 자신의 유닛을 파괴 시킬 수 있습니다.
                """.trimIndent()
            }
        }, Info {
            override fun toString(): String {
                return """
                    명령어: [green]/info[]
                    [sky]/info <플레이어 이름>[] - 다른 플레이어의 정보를 확인합니다. 관리자 전용.
                    
                    현재 서버에서 저장하고 있는 계정 정보를 확인합니다.
                """.trimIndent()
            }
        }, Maps {
            override fun toString(): String {
                return """
                    명령어: [green]/maps[]
                    [sky]/maps <숫자>[] - 모든 맵 목록을 한 화면에 표시할 수 없기 때문에 페이지별로 분리 되어 있습니다.
                    
                    맵 선택 투표를 하기 전에 사용하여 원하는 맵을 고를 수 있습니다.
                    투표시 맵 이름 일부분만 입력해도 되고, 왼쪽에 숫자만 입력해도 됩니다.
                """.trimIndent()
            }
        }, Motd {
            override fun toString(): String {
                return """
                    명령어: [green]/motd[]
                    서버 공지를 확인합니다. 이 메세지는 서버 입장시에도 나타납니다.
                """.trimIndent()
            }
        }, Players {
            override fun toString(): String {
                return """
                    명령어: [green]/players[]
                    [sky]/players <숫자>[] - 모든 플레이어 목록을 한 화면에 표시할 수 없기 때문에 페이지별로 분리 되어 있습니다.

                    현재 서버에 접속해 있는 플레이어의 목록을 확인합니다.
                    강퇴 투표를 할때 숫자만 입력해도 되므로, 입력 불가능한 닉네임들을 강퇴할때 유용합니다.
                """.trimIndent()
            }
        }, Router {
            override fun toString(): String {
                return """
                    명령어: [green]/router[]
                    BasedUser 가 만든 분배기 밈 그 자체. 기본값으로 서버장 전용.
                    이 명령어를 사용할 경우 자기 자신의 닉네임이 움직이는 분배기 블럭으로 변경되며, 그 자리에 분배기 바닥도 생깁니다.
                """.trimIndent()
            }
        }, Status {
            override fun toString(): String {
                return """
                    명령어: [green]/status[]
                    실시간 서버 정보를 확인합니다.
                    CPU 계산 시간의 경우 명령어를 여러번 입력해야 제대로 표시되는 경우가 있습니다.
                """.trimIndent()
            }
        }, Team {
            override fun toString(): String {
                return """
                    명령어: [green]/team <팀 이름> [플레이어 이름][]
                    [sky]/team crux[] - 자신의 팀을 crux 팀으로 변경합니다.
                    [sky]/team green Visitor[] - 서버에 Visitor 라는 플레이어가 있을 경우, 해당 플레이어의 팀을 연두색 팀으로 변경시킵니다.
                    
                    사용 가능한 팀 이름: derelict, sharded, crux, green, purple, blue
                    기본 팀 이름은 sharded 입니다.
                """.trimIndent()
            }
        },Ban {
            override fun toString(): String {
                return """
                    명령어: [green]/ban <플레이어 이름> [이유][]
                    밴 명령어 입니다만, 아직 완성되지 않아서 사용 불가능한 명령어 입니다.
                """.trimIndent()
            }
        }, Tp {
            override fun toString(): String {
                return """
                    명령어: [green]/tp[]
                    [sky]/tp <플레이어 이름>[] - 해당 유저의 위치로 이동합니다.
                    [sky]/tp <플레이어 이름> <목표 플레이어 이름>[] - 해당 유저를 목표 플레이어에게 이동 시킵니다.
                    [sky]/tp <x> <y>[] - 블럭 좌표 <x>, <y> 위치로 이동합니다.
                    
                    테러를 찾아내거나 도움을 줄때 유용한 명령어.
                """.trimIndent()
            }
        }, Mute {
            override fun toString(): String {
                return """
                    명령어: [green]/mute <플레이어 이름>[]
                    해당 유저의 채팅을 금지 또는 해제 시킵니다.
                """.trimIndent()
            }
        }, Help {
            override fun toString(): String {
                return """
                    명령어: [green]/help[]
                    [sky]/help <숫자>[] - 모든 명령어를 한 화면에 표시할 수 없기 때문에 페이지별로 분리 되어 있습니다.
                    [sky]/help <명령어>[] - 해당 명령어에 대한 자세한 사용 방법을 확인합니다.
                    
                    이 명령어를 사용하여 표시되는 명령어들은 해당 계정의 권한에 따라 다르게 표시됩니다.
                """.trimIndent()
            }
        }, Discord {
            override fun toString(): String {
                return """
                    명령어: [green]/discord[]
                    이 명령어를 사용하면 무작위 PIN 번호가 생성됩니다.
                    그리고 한국서버의 Discord 에 있는 [green]#봇[] 채널에서 [yellow]!auth[] 명령어를 사용해 이 계정에 Discord 인증을 할 수 있습니다.
                    
                    Discord 내에서의 명령어는 !auth <PIN 번호> 입니다.
                    [yellow]사용 예시: !auth 12867409823637148
                """.trimIndent()
            }
        }
    }
}
