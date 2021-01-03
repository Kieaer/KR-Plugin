package core

import data.Config
import external.UTF8Control
import java.text.MessageFormat
import java.util.*

class Bundle {
    private var resource: ResourceBundle

    constructor(locale: Locale) {
        resource = try {
            ResourceBundle.getBundle("bundle.bundle", locale, UTF8Control())
        } catch (e: Exception) {
            ResourceBundle.getBundle("bundle.bundle", Locale.KOREAN, UTF8Control())
        }
    }

    constructor(){
        resource = ResourceBundle.getBundle("bundle.bundle", Locale.KOREAN, UTF8Control())
    }

    operator fun get(key: String, vararg params: Any?): String {
        return try {
            MessageFormat.format(resource.getString(key), *params)
        } catch (e: MissingResourceException) {
            key
        }
    }

    fun prefix(key: String, vararg params: Any?): String {
        return try {
            MessageFormat.format(Config.prefix + resource.getString(key), *params)
        } catch (e: MissingResourceException) {
            key
        }
    }
}