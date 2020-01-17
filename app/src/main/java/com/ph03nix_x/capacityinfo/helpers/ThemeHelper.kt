package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences

object ThemeHelper {

    fun setTheme(context: Context, isDarkMode: Boolean? = null, isAutoDarkMode: Boolean? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(isDarkMode ?: pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            if(pref.contains(Preferences.IsAutoDarkMode.prefKey)) pref.edit().remove(Preferences.IsAutoDarkMode.prefKey).apply()
        }

        else if(isAutoDarkMode ?: pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        else
            AppCompatDelegate.setDefaultNightMode(if(isDarkMode ?: pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun isDarkMode(configuration: Configuration): Boolean {

        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}