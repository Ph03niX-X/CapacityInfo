package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                removeOldPref(context)

                migrateToDefaultPrefs(context)

                if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                    && CapacityInfoService.instance == null) startService(context)
            }
        }
    }

    // Migrate settings from 1.2 and below
    private fun migrateToDefaultPrefs(context: Context) {

        val oldPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val newPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (!newPrefs.getBoolean("migrated", false)
            && !oldPrefs.getBoolean(Preferences.IsShowInstruction.prefKey, true)) {
            val editor = newPrefs.edit()
            editor.putBoolean(Preferences.IsDarkMode.prefKey, oldPrefs.getBoolean(Preferences.IsDarkMode.prefKey, false))
                .putBoolean(Preferences.IsEnableService.prefKey, oldPrefs.getBoolean(Preferences.IsEnableService.prefKey, true))
                .putLong(Preferences.NotificationRefreshRate.prefKey, oldPrefs.getLong(Preferences.NotificationRefreshRate.prefKey, 40))
                .putBoolean(Preferences.TemperatureInFahrenheit.prefKey, oldPrefs.getBoolean("fahrenheit", false))
                .putBoolean(Preferences.IsShowLastChargeTimeInApp.prefKey, oldPrefs.getBoolean("show_last_charge_time", true))
                .putInt(Preferences.DesignCapacity.prefKey, oldPrefs.getInt(Preferences.DesignCapacity.prefKey, 0))
                .putInt(Preferences.ChargeCounter.prefKey, oldPrefs.getInt(Preferences.ChargeCounter.prefKey, 0))
                .putBoolean(Preferences.IsShowInstruction.prefKey, oldPrefs.getBoolean(Preferences.IsShowInstruction.prefKey, false))
                .putBoolean(Preferences.IsSupported.prefKey, oldPrefs.getBoolean(Preferences.IsSupported.prefKey, true))
                .putInt(Preferences.LastChargeTime.prefKey, oldPrefs.getInt(Preferences.LastChargeTime.prefKey, 0))
                .putInt(Preferences.BatteryLevelWith.prefKey, oldPrefs.getInt(Preferences.BatteryLevelWith.prefKey, 0))
                .putInt(Preferences.BatteryLevelTo.prefKey, oldPrefs.getInt(Preferences.BatteryLevelTo.prefKey, 0)).apply()

            oldPrefs.edit().clear().apply()

            editor.putBoolean("migrated", true).apply()
        }

        if(newPrefs.contains("show_last_charge_time")) {

            newPrefs.edit().putBoolean(Preferences.IsShowLastChargeTimeInApp.prefKey,
                newPrefs.getBoolean("show_last_charge_time", true)).apply()

            removeOldPref(context)
        }

        if(newPrefs.contains("dark_mode")) {

            newPrefs.edit().putBoolean(Preferences.IsDarkMode.prefKey,
                newPrefs.getBoolean("dark_mode", true)).apply()

            removeOldPref(context)
        }

        if(newPrefs.contains("enable_service")) {

            newPrefs.edit().putBoolean(Preferences.IsEnableService.prefKey,
                newPrefs.getBoolean("enable_service", true)).apply()

            removeOldPref(context)
        }
    }

    private fun removeOldPref(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        pref.edit().apply {

            if(pref.contains("always_show_notification")) remove("always_show_notification")

            if(pref.contains("show_last_charge_time")) remove("show_last_charge_time")

            if(pref.contains("dark_mode")) remove("dark_mode")

            if(pref.contains("enable_service")) remove("enable_service")

            if(pref.contains("is_show_information_while_charging")) remove("is_show_information_while_charging")

            if(pref.contains("is_show_information_during_discharge")) remove("is_show_information_during_discharge")
            
            apply()
        }
    }
}