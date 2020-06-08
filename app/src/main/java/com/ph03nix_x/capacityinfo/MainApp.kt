package com.ph03nix_x.capacityinfo

import android.app.Application
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utils.Constants
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay

class MainApp : Application() {

    companion object {

        var defLang: String = "en"
    }

    override fun onCreate() {

        super.onCreate()

        defLang()

        isInstalledGooglePlay = isInstalledGooglePlay()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        if(LocaleHelper.getSystemLocale(newConfig) != defLang) defLang()
    }

    private fun defLang() {

        defLang = "en"

        val systemLanguage = LocaleHelper.getSystemLocale(resources.configuration)

        if(systemLanguage in resources.getStringArray(R.array.languages_codes)) defLang =
            systemLanguage

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(LANGUAGE, null) !in
            resources.getStringArray(R.array.languages_codes))
            pref.edit().putString(LANGUAGE, defLang).apply()
    }

    private fun isInstalledGooglePlay(): Boolean {

        return try {

            packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch(e: PackageManager.NameNotFoundException) { false }
    }
}