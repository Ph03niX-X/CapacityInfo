package com.ph03nix_x.capacityinfo.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_UPDATE_APP
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD

class UpdateApplicationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                MainApp.isInstalledGooglePlay = MainApp.isGooglePlay(context)

                PremiumInterface.premiumContext = context

                removeOldPreferences(context)

                removeBackupSettings(context)

                if(!pref.getBoolean(IS_AUTO_START_UPDATE_APP, context.resources.getBoolean(
                        R.bool.is_auto_start_update_app))) return

                ServiceHelper.cancelAllJobs(context)

                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)

                if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, context.resources.getBoolean(
                        R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                        .VERSION_CODES.R && !Environment.isExternalStorageManager()
                            && !MainApp.isInstalledGooglePlay) || (Build
                        .VERSION.SDK_INT < Build.VERSION_CODES.R && checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED && checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED)))
                    ServiceHelper.jobSchedule(context, AutoBackupSettingsJobService::class.java,
                        Constants.AUTO_BACKUP_SETTINGS_JOB_ID, (pref.getString(
                            FREQUENCY_OF_AUTO_BACKUP_SETTINGS,
                            "1")?.toLong() ?: 1L) * 60L * 60L * 1000L)
            }
        }
    }

    private fun removeOldPreferences(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        arrayListOf("temperature_in_fahrenheit", "voltage_in_mv", "is_fps_overlay",
            "is_show_faq", "is_show_donate_message", "is_show_premium_info_dialog", "is_supported",
            "is_show_not_supported_dialog", "language", "is_enable_fake_battery_wear",
            "fake_battery_wear_value", "is_high_battery_wear", "is_very_high_battery_wear",
            "is_critical_battery_wear",
            "${context.packageName}_preferences.products.cache.v2_6.version",
            "${context.packageName}_preferences.products.cache.v2_6",
            "${context.packageName}_preferences.products.restored.v2_6",
            "${context.packageName}_preferences.subscriptions.cache.v2_6",
            "${context.packageName}_preferences.subscriptions.cache.v2_6.version",
            "is_battery_wear").forEach {

            with(pref) {

                edit().apply {

                    if(contains(it)) this.remove(it)

                    apply()
                }
            }
        }
    }

    private fun removeBackupSettings(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        MainApp.isInstalledGooglePlay = MainApp.isGooglePlay(context)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && MainApp.isInstalledGooglePlay)
            arrayListOf(IS_AUTO_BACKUP_SETTINGS, IS_BACKUP_SETTINGS_TO_MICROSD,
                FREQUENCY_OF_AUTO_BACKUP_SETTINGS).forEach {

                with(pref) {

                    edit().apply {

                        if(contains(it)) this.remove(it)

                        apply()
                    }
                }
            }
    }
}