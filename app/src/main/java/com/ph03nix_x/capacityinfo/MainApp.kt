package com.ph03nix_x.capacityinfo

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isDarkMode
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme

class MainApp : Application() {

    companion object {

        var defLang: String = "en"
    }

    override fun onCreate() {

        super.onCreate()

        setTheme(this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isDarkMode(resources.configuration)

        defLang()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isDarkMode(newConfig)

        if(LocaleHelper.getSystemLocale(newConfig) != defLang) {

            pref.edit().remove(Preferences.Language.prefKey).apply()

            defLang = "en"

            defLang()
        }
    }

    private fun defLang() {

        val resLang = LocaleHelper.getSystemLocale(resources.configuration)

        if(resLang in resources.getStringArray(R.array.languages_codes)) defLang = resLang

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(Preferences.Language.prefKey, null) == null)
            pref.edit().putString(Preferences.Language.prefKey, defLang).apply()
    }
}