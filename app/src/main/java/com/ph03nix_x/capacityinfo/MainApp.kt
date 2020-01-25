package com.ph03nix_x.capacityinfo

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE

class MainApp : Application() {

    companion object {

        var defLang: String = "en"
    }

    override fun onCreate() {

        super.onCreate()

        setTheme(this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isSystemDarkMode(resources.configuration)

        defLang()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isSystemDarkMode(newConfig)

        if(LocaleHelper.getSystemLocale(newConfig) != defLang) defLang()
    }

    private fun defLang() {

        defLang = "en"

        val systemLanguage = LocaleHelper.getSystemLocale(resources.configuration)

        if(systemLanguage in resources.getStringArray(R.array.languages_codes)) defLang = systemLanguage

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(LANGUAGE, null) != defLang)
            pref.edit().putString(LANGUAGE, defLang).apply()
    }
}