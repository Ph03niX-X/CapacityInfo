package com.ph03nix_x.capacityinfo.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE

class UpdateApplicationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                migratedPrefs(context)

                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)

                if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, context.resources.getBoolean(
                        R.bool.is_auto_backup_settings)) && checkSelfPermission(context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED && checkSelfPermission(context,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED)
                    ServiceHelper.jobSchedule(context, AutoBackupSettingsJobService::class.java,
                        Constants.AUTO_BACKUP_SETTINGS_JOB_ID, 1 * 60 * 60 * 1000 /* 1 hour */)
            }
        }
    }

    @Deprecated("This function will be removed in August-September")
    private fun migratedPrefs(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {

            apply {

                edit().apply {

                    if(contains("is_auto_start_service")) remove("is_auto_start_service")

                    if(contains("main_window_text_font")) {

                        putString(TEXT_FONT,
                            getString("main_window_text_font", "6"))

                        remove("main_window_text_font")
                    }

                    if(contains("main_window_text_style")) {

                        putString(TEXT_STYLE,
                            getString("main_window_text_style", "0"))

                        remove("main_window_text_style")
                    }

                    if(contains("main_screen_text_size")) {

                        putString(TEXT_SIZE, getString("main_screen_text_size", "2"))

                        remove("main_screen_text_size")
                    }

                    if(contains("main_screen_text_font")) {

                        putString(TEXT_FONT, getString("main_screen_text_font", "6"))

                        remove("main_screen_text_font")
                    }

                    if(contains("main_screen_text_style")) {

                        putString(TEXT_STYLE, getString("main_screen_text_style", "0"))

                        remove("main_screen_text_style")
                    }

                    if(contains("debug_options_is_enabled")) {

                        putBoolean(IS_ENABLED_DEBUG_OPTIONS, getBoolean(
                            "debug_options_is_enabled", context.resources.getBoolean(
                                R.bool.is_enabled_debug_options)))

                        remove("debug_options_is_enabled")
                    }
                    
                    if(contains("is_show_capacity_added_in_notification"))
                        remove("is_show_capacity_added_in_notification")

                    if(contains("is_show_last_charge_time_in_notification"))
                        remove("is_show_last_charge_time_in_notification")

                    apply()
                }
            }
        }
    }
}