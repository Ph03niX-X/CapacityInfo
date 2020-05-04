package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String) {

        val resources: Resources = context.resources
        val locale = Locale(if(language in resources.getStringArray(R.array.languages_codes))
            language else defLang)
        val configuration: Configuration = resources.configuration
        Locale.setDefault(locale)
        configuration.locale = locale
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun getSystemLocale(configuration: Configuration) =
        configuration.locale.toString().removeRange(2, configuration.locale.toString().count())
}