package com.ph03nix_x.capacityinfo

import android.app.Application
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.interfaces.JobServiceInterface
import com.ph03nix_x.capacityinfo.services.BillingJobService
import com.ph03nix_x.capacityinfo.utils.Constants
import com.ph03nix_x.capacityinfo.utils.Constants.BILLING_JOB_SERVICE_ID
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERIODIC_BILLING_JOB_SERVICE
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay

class MainApp : Application(), JobServiceInterface {

    companion object {

        var defLang: String = "en"
    }

    override fun onCreate() {

        super.onCreate()

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        setTheme(this)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            isSystemDarkMode(resources.configuration)

        defLang()

        isInstalledGooglePlay = isInstalledGooglePlay()

        if(pref.getString(PERIODIC_BILLING_JOB_SERVICE, "12") !in
            resources.getStringArray(R.array.periodic_billing_job_service_values))
            pref.edit().putString(PERIODIC_BILLING_JOB_SERVICE, "12").apply()

        if(isInstalledGooglePlay &&
            !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false))
            onScheduleJobService(this, BillingJobService::class.java, BILLING_JOB_SERVICE_ID,
            periodicHours = (pref.getString(PERIODIC_BILLING_JOB_SERVICE, "12") ?: "12")
                .toLong())
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

        if(systemLanguage in resources.getStringArray(R.array.languages_codes)) defLang =
            systemLanguage

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getString(LANGUAGE, null) != defLang)
            pref.edit().putString(LANGUAGE, defLang).apply()
    }

    private fun isInstalledGooglePlay(): Boolean {

        return try {

            packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch (e: PackageManager.NameNotFoundException) { false }
    }
}