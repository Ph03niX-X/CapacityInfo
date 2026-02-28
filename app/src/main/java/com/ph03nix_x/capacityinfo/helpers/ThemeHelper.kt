package com.ph03nix_x.capacityinfo.helpers

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {

    fun setTheme() =
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun isSystemDarkMode(configuration: Configuration): Boolean {
        val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun currentTheme(configuration: Configuration) =
        configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                configuration.uiMode and Configuration.UI_MODE_NIGHT_YES or
                configuration.uiMode and Configuration.UI_MODE_NIGHT_NO
}