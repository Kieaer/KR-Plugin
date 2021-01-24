package korea.form

abstract class Config {
    abstract fun createFile()
    abstract fun save()
    abstract fun load()
}