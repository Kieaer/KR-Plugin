package korea.external

// validate that a password adheres to the "rules".
object RegularExpression {
    fun check(password: String?) : Boolean{
        if (password == null || password.length < 6) {
            return false
        }
        var containsChar = false
        var containsDigit = false
        for (c in password.toCharArray()) {
            if (Character.isLetter(c)) {
                containsChar = true
            } else if (Character.isDigit(c)) {
                containsDigit = true
            }
            if (containsChar && containsDigit) {
                return true
            }
        }
        return false
    }
}