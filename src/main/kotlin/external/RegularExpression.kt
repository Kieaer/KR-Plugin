package external

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 비밀번호 정규식 패턴
 * @author ohj
 * https://blog.naver.com/ohtanja/221385009361
 */
object RegularExpression {
    private lateinit var match: Matcher
    private const val pattern1 = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[$@!%*#?&])[A-Za-z0-9$@!%*#?&]{8,20}$" // 영문, 숫자, 특수문자
    private const val pattern2 = "^[A-Za-z0-9]{10,20}$" // 영문, 숫자
    private const val pattern3 = "^[0-9$@!%*#?&]{10,20}$" //영문,  특수문자
    private const val pattern4 = "^[A-Za-z$@!%*#?&]{10,20}$" // 특수문자, 숫자
    private const val pattern5 = "(\\w)\\1\\1\\1" // 같은 문자, 숫자

    /**
     * 비밀번호 정규식 체크
     * @param pwd
     * @return
     */
    fun check(newPwd: String, oldPwd: String, userId: String, isNew: Boolean): Boolean {
        var chk = false

        // 특수문자, 영문, 숫자 조합 (8~10 자리)
        match = Pattern.compile(pattern1).matcher(newPwd)
        if (match.find()) chk = true

        // 영문, 숫자 (10~20 자리)
        match = Pattern.compile(pattern2).matcher(newPwd)
        if (match.find()) chk = true

        // 영문, 특수문자 (10~20 자리)
        match = Pattern.compile(pattern3).matcher(newPwd)
        if (match.find()) chk = true

        // 특수문자, 숫자 (10~20 자리)
        match = Pattern.compile(pattern4).matcher(newPwd)
        if (match.find()) chk = true

        if (chk) {
            // 연속문자 4자리
            if (samePwd(newPwd)) return false

            // 같은문자 4자리
            if (continuousPwd(newPwd)) return false

            // 이전 아이디 4자리
            if (!isNew && newPwd == oldPwd) return false

            // 아이디와 동일 문자 4자리
            if (sameId(newPwd, userId)) return false
        }
        return true
    }

    /**
     * 같은 문자, 숫자 4자리 체크
     * @param pwd
     * @return
     */
    private fun samePwd(pwd: String): Boolean {
        match = Pattern.compile(pattern5).matcher(pwd)
        return match.find()
    }

    /**
     * 연속 문자, 숫자 4자리 체크
     * @param pwd
     * @return
     */
    private fun continuousPwd(pwd: String): Boolean {
        var o = 0
        var d = 0
        var p = 0
        var n = 0
        val limit = 4
        for (i in pwd.indices) {
            val tempVal = pwd[i]
            if (i > 0 && o - tempVal.toInt().also { p = it } > -2 && (if (p == d) n + 1 else 0.also {
                    n = it
                }) > limit - 3) {
                return true
            }
            d = p
            o = tempVal.toInt()
        }
        return false
    }

    /**
     * 아이디와 동일 문자 4자리 체크
     * @param pwd
     * @param id
     * @return
     */
    private fun sameId(pwd: String, id: String): Boolean {
        for (i in 0 until pwd.length - 3) {
            if (id.contains(pwd.substring(i, i + 4))) {
                return true
            }
        }
        return false
    }
}