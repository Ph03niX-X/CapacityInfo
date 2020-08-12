package com.ph03nix_x.capacityinfo

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.activities.MainActivity
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

        var currentTheme = -1

        fun isGooglePlay(context: Context) =

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallSourceInfo(
                    context.packageName).installingPackageName

            else Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager
                .getInstallerPackageName(context.packageName)
    }

    override fun onCreate() {

        super.onCreate()

        defLang()

        isInstalledGooglePlay = isInstalledGooglePlay()

        ThemeHelper.setTheme(this)

        currentTheme = ThemeHelper.currentTheme(resources.configuration)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        LocaleHelper.setLocale(this, pref.getString(LANGUAGE,
            null) ?: defLang)

        if(pref.getBoolean(PreferencesKeys.IS_AUTO_BACKUP_SETTINGS, resources.getBoolean(
                R.bool.is_auto_backup_settings)) && ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ServiceHelper.jobSchedule(this, AutoBackupSettingsJobService::class.java,
                Constants.AUTO_BACKUP_SETTINGS_JOB_ID, (pref.getString(PreferencesKeys
                    .FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                    ?.toLong() ?: 1L) * 60L * 60L * 1000L)

        else ServiceHelper.cancelJob(this, Constants.AUTO_BACKUP_SETTINGS_JOB_ID)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val newTheme = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_NO

        if(newTheme != currentTheme) {

            MainActivity.tempFragment = MainActivity.instance?.fragment

            MainActivity.isRecreate = true

            MainActivity.instance?.recreate()
        }

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