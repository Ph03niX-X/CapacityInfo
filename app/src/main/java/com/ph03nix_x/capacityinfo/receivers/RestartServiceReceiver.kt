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

                if(pref.getBoolean(Preferences.EnableService.prefName, true)) startService(context) }
        }
    }

    private fun migrateToDefaultPrefs(context: Context) {
        val oldPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val newPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (!newPrefs.getBoolean("migrated", false)
            && !oldPrefs.getBoolean(Preferences.IsShowInstruction.prefName, true)) {
            val editor = newPrefs.edit()
            editor.putBoolean(Preferences.DarkMode.prefName, oldPrefs.getBoolean(Preferences.DarkMode.prefName, false))
                .putBoolean(Preferences.EnableService.prefName, oldPrefs.getBoolean(Preferences.EnableService.prefName, true))
                .putLong(Preferences.NotificationRefreshRate.prefName, oldPrefs.getLong(Preferences.NotificationRefreshRate.prefName, 40))
                .putBoolean(Preferences.Fahrenheit.prefName, oldPrefs.getBoolean("fahrenheit", false))
                .putBoolean(Preferences.ShowLastChargeTime.prefName, oldPrefs.getBoolean(Preferences.ShowLastChargeTime.prefName, true))
                .putInt(Preferences.DesignCapacity.prefName, oldPrefs.getInt(Preferences.DesignCapacity.prefName, 0))
                .putInt(Preferences.ChargeCounter.prefName, oldPrefs.getInt(Preferences.ChargeCounter.prefName, 0))
                .putBoolean(Preferences.IsShowInstruction.prefName, oldPrefs.getBoolean(Preferences.IsShowInstruction.prefName, false))
                .putBoolean(Preferences.IsSupported.prefName, oldPrefs.getBoolean(Preferences.IsSupported.prefName, true))
                .putInt(Preferences.LastChargeTime.prefName, oldPrefs.getInt(Preferences.LastChargeTime.prefName, 0))
                .putInt(Preferences.BatteryLevelWith.prefName, oldPrefs.getInt(Preferences.BatteryLevelWith.prefName, 0))
                .putInt(Preferences.BatteryLevelTo.prefName, oldPrefs.getInt(Preferences.BatteryLevelTo.prefName, 0)).apply()

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