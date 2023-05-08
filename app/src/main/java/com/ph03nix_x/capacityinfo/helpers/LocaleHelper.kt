package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    fun Context.setLocale(language: String): Context {
        val locale = Locale(language)
        val config = resources.configuration
        Locale.setDefault(locale)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return createConfigurationContext(config)
    }

    fun Configuration.getSystemLocale(): String = locales[0].language
}