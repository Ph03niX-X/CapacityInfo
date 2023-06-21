package com.ph03nix_x.capacityinfo

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.job.JobInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumContext
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CheckPremiumJob
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import java.io.Serializable
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class MainApp : Application(), PremiumInterface {

    companion object {

        var batteryIntent: Intent? = null
        var isPowerConnected = false
        var isInstalledGooglePlay = true

        var microSDPath: String? = null

        var currentTheme = -1

        @Suppress("DEPRECATION")
        fun isGooglePlay(context: Context) =

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallSourceInfo(
                    context.packageName).installingPackageName

            else Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager
                .getInstallerPackageName(context.packageName)

        fun restartApp(context: Context, prefArrays: HashMap<String, Any?>,
                       isRestore: Boolean = false) {

            val packageManager = context.packageManager

            val componentName = packageManager.getLaunchIntentForPackage(
                context.packageName)?.component

            val intent = Intent.makeRestartActivityTask(componentName)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

            intent?.putExtra(Constants.IMPORT_RESTORE_SETTINGS_EXTRA, prefArrays)

            if(isRestore) intent?.putExtra(Constants.IS_RESTORE_SETTINGS_EXTRA, true)

            context.startActivity(intent)

            exitProcess(0)
        }

        fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T?
        {
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                activity.intent.getSerializableExtra(name, clazz)
            else {
                @Suppress("DEPRECATION", "UNCHECKED_CAST")
                activity.intent.getSerializableExtra(name) as T
            }
        }
    }

    override fun onCreate() {

        super.onCreate()

        premiumContext = this

        isInstalledGooglePlay = isInstalledGooglePlay()

        ThemeHelper.setTheme(this)

        if(isInstalledGooglePlay) checkPremium()

        currentTheme = ThemeHelper.currentTheme(resources.configuration)

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        ServiceHelper.jobSchedule(this, CheckPremiumJob::class.java,
            Constants.CHECK_PREMIUM_JOB_ID, Constants.CHECK_PREMIUM_JOB_SERVICE_PERIODIC)

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
    }

    private fun isInstalledGooglePlay(): Boolean {

        return try {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0))
            else packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch(e: PackageManager.NameNotFoundException) { false }
    }
}