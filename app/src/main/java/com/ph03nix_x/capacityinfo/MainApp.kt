package com.ph03nix_x.capacityinfo

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper

class MainApp : Application() {

    companion object {

        fun setModeNight(context: Context) {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

                if(pref.contains(Preferences.IsAutoDarkMode.prefKey)) pref.edit().remove(Preferences.IsAutoDarkMode.prefKey).apply()
            }

            else if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            else if(pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

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

        if(LocaleHelper.getSystemLocale(newConfig) != defLang) {

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

        val resLang = LocaleHelper.getSystemLocale(resources.configuration)

        if(resLang in resources.getStringArray(R.array.languages_codes)) defLang = resLang

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(Preferences.Language.prefKey, null) == null)
            pref.edit().putString(Preferences.Language.prefKey, defLang).apply()
    }
}