import java.util.*

object PlayerData {
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
    var join: Long = 0L

    /** 플레이어 레벨 */
    var level: Long = 0L

    /** 플레이어 경험치 */
    var exp: Long = 0L

    /** 최초 접속일 */
    var joinDate: Long = 0L

    /** 마지막 접속일. 기본값으로 숨김. */
    var lastDate: Long = 0L

    /** 플레이 시간 */
    var time: Long = 0L

    /** 공격 맵 클리어 횟수 */
    var attackWinner: Long = 0L

    /** PvP 승리 횟수 */
    var pvpWinner: Long = 0L

    /** PvP 패배 횟수 */
    var pvpLoser: Long = 0L

    /** 무지개 닉네임 활성화 여부 */
    var rainbowName: Boolean = false

    /** 서버 접속여부 */
    var isConnected: Boolean = false

    /** 채팅 금지 여부 */
    var isMute: Boolean = false

    /** 로그인 여부 */
    var isLogged: Boolean = false

    /** 잠수 시간 확인 */
    var afkTime: Long = 0L

    /** 플레이어 언어 */
    var country: String = Locale.getDefault().toLanguageTag()
}