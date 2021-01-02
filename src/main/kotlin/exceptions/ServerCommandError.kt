package exceptions

class ServerCommandError : Exception() {
    override val cause: Throwable?
        get() = super.cause
    override val message: String?
        get() = super.message
}