package com.ph03nix_x.capacityinfo

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.services.BillingJobService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay


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

        isInstalledGooglePlay = isInstalledGooglePlay(this)

        if(isInstalledGooglePlay) startBillingJob()
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

    private fun startBillingJob() {

        val componentName = ComponentName(this, BillingJobService::class.java)
        val jobInfo = JobInfo.Builder(0, componentName).apply {

            setRequiresCharging(false)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setRequiresBatteryNotLow(true)

            setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            setPeriodic(12 * 60 * 60 * 1000)
        }

        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as? JobScheduler

        jobScheduler?.schedule(jobInfo.build())
    }
}