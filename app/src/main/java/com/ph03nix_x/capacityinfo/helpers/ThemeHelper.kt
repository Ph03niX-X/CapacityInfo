package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE

object ThemeHelper {

    fun setTheme(context: Context, isDarkMode: Boolean? = null, isAutoDarkMode: Boolean? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(isDarkMode ?: pref.getBoolean(
                    IS_DARK_MODE, context.resources.getBoolean(R.bool.is_dark_mode)))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            if(pref.contains(IS_AUTO_DARK_MODE)) pref.edit().remove(IS_AUTO_DARK_MODE).apply()
        }

        else if(isAutoDarkMode ?: pref.getBoolean(IS_AUTO_DARK_MODE, context.resources.getBoolean(
                R.bool.is_auto_dark_mode)))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        else
            AppCompatDelegate.setDefaultNightMode(if(isDarkMode ?: pref.getBoolean(
                    IS_DARK_MODE, context.resources.getBoolean(R.bool.is_dark_mode)))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    fun isSystemDarkMode(configuration: Configuration): Boolean {

        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun currentUiMode(configuration: Configuration) =
        configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                configuration.uiMode and Configuration.UI_MODE_NIGHT_YES or
                configuration.uiMode and Configuration.UI_MODE_NIGHT_NO
}