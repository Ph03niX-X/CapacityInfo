package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String) {

        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val configuration: Configuration = resources.configuration
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun getSystemLocale(configuration: Configuration) =
        configuration.locale.toString().removeRange(2, configuration.locale.toString().count())
}