package com.ph03nix_x.capacityinfo

import android.app.Application
import android.content.res.Configuration
import androidx.preference.PreferenceManager

class MainApp : Application() {

    companion object {

        fun getLanguagesList() = arrayListOf("en", "ro", "be", "ru", "uk")

        var defLang: String = "en"
    }

    override fun onCreate() {

        super.onCreate()

        defLang()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        pref.edit().remove(Preferences.Language.prefKey).apply()

        defLang = "en"

        defLang()
    }

    private fun defLang() {

        val resLang = resources.configuration.locale.country.toLowerCase(resources.configuration.locale)

        if(resLang in getLanguagesList()) defLang = resLang

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(Preferences.Language.prefKey, null) == null)
            pref.edit().putString(Preferences.Language.prefKey, defLang).apply()
    }
}