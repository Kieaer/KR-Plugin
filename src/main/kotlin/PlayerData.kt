
import command.Permissions
import org.hjson.JsonObject
import java.util.*

class PlayerData {
    /** 플레이어의 이름 */
    var name: String = ""

    /** 플레이어의 UUID */
    var uuid: String = ""

    /** 관리자 여부 확인. 기본값으로 숨김. */
    var admin: Boolean = false

    /** 블럭 설치개수 */
    var placeCount: Long = 0L

    /** 블럭 파괴개수 */
    var breakCount: Long = 0L

    /** 강퇴당한 횟수 */
    var kickCount: Long = 0L

    /** 서버 입장횟수 */
    var joinCount: Long = 0L

    /** 플레이어 레벨 */
    var level: Long = 0L

    /** 플레이어 경험치 */
    var exp: Long = 0L

    /** 최초 접속일 */
    var joinDate: Long = 0L

    /** 마지막 접속일. 기본값으로 숨김. */
    var lastDate: Long = 0L

    /** 플레이 시간 */
    var playTime: Long = 0L

    /** 공격 맵 클리어 횟수 */
    var attackWinner: Long = 0L

    /** PvP 승리 횟수 */
    var pvpWinner: Long = 0L

    /** PvP 패배 횟수 */
    var pvpLoser: Long = 0L

    /** 무지개 닉네임 활성화 여부 */
    var rainbowName: Boolean = false

    /** 채팅 금지 여부 */
    var isMute: Boolean = false

    /** 로그인 여부 */
    var isLogged: Boolean = false

    /** 잠수 시간 확인 */
    var afkTime: Long = 0L

    /** 플레이어 언어 */
    var country: String = Locale.getDefault().toLanguageTag()

    /** 플레이어에 대한 신뢰 수치 */
    var rank: Long = 0L

    /** 플레이어 권한 그룹 */
    var permission: String = Permissions.defaultGroup

    /** 계정 ID */
    var id: String = ""

    /** 계정 비밀번호 */
    var pw: String = ""

    /** json 데이터 */
    var json: JsonObject = JsonObject()

    constructor(name: String, uuid: String, admin: Boolean, placeCount: Long, breakCount: Long, kickCount: Long, joinCount: Long, level: Long, exp: Long, joinDate: Long, lastDate: Long, playTime: Long, attackWinner: Long, pvpWinner: Long, pvpLoser: Long, rainbowName: Boolean, isMute: Boolean, isLogged: Boolean, afkTime: Long, country: String, rank: Long, json: JsonObject, id: String, pw: String){
        this.name = name
        this.uuid = uuid
        this.admin = admin
        this.placeCount = placeCount
        this.breakCount = breakCount
        this.kickCount = kickCount
        this.joinCount = joinCount
        this.level = level
        this.exp = exp
        this.joinDate = joinDate
        this.lastDate = lastDate
        this.playTime = playTime
        this.attackWinner = attackWinner
        this.pvpWinner = pvpWinner
        this.pvpLoser = pvpLoser
        this.rainbowName = rainbowName
        this.isMute = isMute
        this.isLogged = isLogged
        this.afkTime = afkTime
        this.country = country
        this.rank = rank
        this.json = json
        this.id = id
        this.pw = pw
    }

    constructor()
}