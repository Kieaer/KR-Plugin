package data

import java.util.*

object Config {
    /** 플러그인에 표시되는 언어 */
    val locale = Locale.KOREAN

    /** 서버 내에서 출력되는 메세지의 앞 태그 */
    val prefix = ""

    /** 서버 내에서 출력되는 메세지의 뒷 태그 */
    val suffix = ""

    /** 플러그인 자동 업데이트 **/
    val update = true

    /** 사소한 오류 발생시 오류 메세지 출력 */
    val debug = true

    /** 맵 자동 저장시간. 단위는 1초 */
    val autoSaveTime = 0L

    /** 잠수 플레이어 강퇴 시간. 단위는 1초 */
    val kickAFK = 0L

    /** 계정 관리방식. */
    val authType = authTypes.password

    /** Discord 서버 토큰 */
    val discordServerToken = 0L

    /** Discord 채널 토큰 */
    val discordChannelToken = 0L

    /** Discord 봇 토큰 */
    val discordBotToken = 0L

    /** 무지개 닉네임 표시 갱신 시간. 단위는 1초 */
    val colorNameUpdateInterval = 0L

    /** 투표 기능 활성화 여부 */
    val enableVote = true

    /** 플러그인의 네트워크 모드.
     * 서버로 설정할 경우, DB 서버가 되며,
     */
    val networkMode = networkModes.Server

    enum class authTypes{
        none, password, discord, kakaotalk
    }

    enum class networkModes{
        Server, Client
    }
}