package com.ph03nix_x.capacityinfo

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE

class MainApp : Application() {

    companion object {

        var batteryIntent: Intent? = null
        var defLang: String = "en"
        var isPowerConnected = false
        var isInstalledGooglePlay = true

        fun isGooglePlay(context: Context) =
            Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallerPackageName(
                context.packageName)
    }

    override fun onCreate() {

        super.onCreate()

        defLang()

        isInstalledGooglePlay = isInstalledGooglePlay()

        ThemeHelper.setTheme(this)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getBoolean(PreferencesKeys.IS_AUTO_BACKUP_SETTINGS, resources.getBoolean(
                R.bool.is_auto_backup_settings)) && ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ServiceHelper.jobSchedule(this, AutoBackupSettingsJobService::class.java,
                Constants.AUTO_BACKUP_SETTINGS_JOB_ID, 1 * 60 * 60 * 1000 /* 1 hour */)

        else ServiceHelper.cancelJob(this, Constants.AUTO_BACKUP_SETTINGS_JOB_ID)
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