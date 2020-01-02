package com.ph03nix_x.capacityinfo

import android.annotation.TargetApi
import android.app.Application
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager

class MainApp : Application() {

    companion object {

        fun getLanguagesList() = arrayListOf("en", "ro", "be", "ru", "uk")

        var defLang: String = "en"

        var isDarkMode = false
    }

    override fun onCreate() {

        super.onCreate()

        darkMode(resources.configuration)

        defLang()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        darkMode(newConfig)

        if(newConfig.locale.country.toLowerCase(newConfig.locale) != defLang) {

            val pref = PreferenceManager.getDefaultSharedPreferences(this)

            pref.edit().remove(Preferences.Language.prefKey).apply()

            defLang = "en"

            defLang()
        }
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private fun darkMode(configuration: Configuration) {

        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun defLang() {

        val resLang = resources.configuration.locale.country.toLowerCase(resources.configuration.locale)

        if(resLang in getLanguagesList()) defLang = resLang

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(Preferences.Language.prefKey, null) == null)
            pref.edit().putString(Preferences.Language.prefKey, defLang).apply()
    }
}