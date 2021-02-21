package korea.core

class PluginException : Exception {
    constructor(message: String?) : super(message)
    constructor(cause: Exception?) : super(cause)
}