object PlayerData {
    /** 플레이어의 이름 */
    var name: String = ""

    /** 플레이어의 UUID */
    var uuid: String = ""

    /** 관리자 여부 확인.
     *
     * 일반 플레이어들한테 값을 보여주지 않음.
     */
    var admin: Boolean = false

    var placeCount: Long = 0L

    var breakCount: Long = 0L

    var join: Long = 0L

    var level: Long = 0L

    var exp: Long = 0L

    var joinDate: Long = 0L

    var lastDate: Long = 0L

    var time: Long = 0L
}