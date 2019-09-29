package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class RestartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context!!)

        when(intent!!.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                removeOldPref(context)

                migrateToDefaultPrefs(context)

                if(pref.getBoolean(Preferences.EnableService.prefKey, true)) startService(context) }
        }
    }

    private fun migrateToDefaultPrefs(context: Context) {
        val oldPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val newPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (!newPrefs.getBoolean("migrated", false)
            && !oldPrefs.getBoolean(Preferences.IsShowInstruction.prefKey, true)) {
            val editor = newPrefs.edit()
            editor.putBoolean(Preferences.DarkMode.prefKey, oldPrefs.getBoolean(Preferences.DarkMode.prefKey, false))
                .putBoolean(Preferences.EnableService.prefKey, oldPrefs.getBoolean(Preferences.EnableService.prefKey, true))
                .putLong(Preferences.NotificationRefreshRate.prefKey, oldPrefs.getLong(Preferences.NotificationRefreshRate.prefKey, 40))
                .putBoolean(Preferences.TemperatureInFahrenheit.prefKey, oldPrefs.getBoolean("fahrenheit", false))
                .putBoolean(Preferences.ShowLastChargeTime.prefKey, oldPrefs.getBoolean(Preferences.ShowLastChargeTime.prefKey, true))
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
    }

    private fun removeOldPref(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        pref.edit().apply {

            if(pref.contains("always_show_notification")) remove("always_show_notification")

            apply()
        }
    }

    private fun startService(context: Context) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, CapacityInfoService::class.java))

        else context.startService(Intent(context, CapacityInfoService::class.java))
    }
}