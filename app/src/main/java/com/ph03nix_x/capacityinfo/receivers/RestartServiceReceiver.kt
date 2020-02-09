package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import java.io.File

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                migrateToDefaultPrefs(context)

                removeOldPref(context)

                if(CapacityInfoService.instance == null) startService(context)
            }
        }
    }

    // Migrate settings from 1.3.1 and below
    private fun migrateToDefaultPrefs(context: Context) {

        val oldPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val newPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (!newPrefs.getBoolean("migrated", false)
            && File("/data/data/${context.packageName}/shared_prefs/preferences.xml").exists()) {

            newPrefs.edit().apply {

                putBoolean(IS_DARK_MODE, oldPrefs.getBoolean(IS_DARK_MODE, false))
                putBoolean("is_enable_service", oldPrefs.getBoolean("is_enable_service", true))
                putBoolean(TEMPERATURE_IN_FAHRENHEIT, oldPrefs.getBoolean("fahrenheit", false))
                putBoolean(IS_SHOW_LAST_CHARGE_TIME_IN_APP, oldPrefs.getBoolean("show_last_charge_time", true))
                putInt(DESIGN_CAPACITY, oldPrefs.getInt(DESIGN_CAPACITY, 0))
                putInt(RESIDUAL_CAPACITY, oldPrefs.getInt("charge_counter", 0))
                putBoolean(IS_SHOW_INSTRUCTION, oldPrefs.getBoolean(IS_SHOW_INSTRUCTION, false))
                putBoolean(IS_SUPPORTED, oldPrefs.getBoolean(IS_SUPPORTED, true))
                putInt(LAST_CHARGE_TIME, oldPrefs.getInt(LAST_CHARGE_TIME, 0))
                putInt(BATTERY_LEVEL_WITH, oldPrefs.getInt(BATTERY_LEVEL_WITH, 0))
                putInt(BATTERY_LEVEL_TO, oldPrefs.getInt(BATTERY_LEVEL_TO, 0))
                putBoolean("migrated", true)
                apply()
            }

            oldPrefs.edit().clear().apply()
        }

        if(newPrefs.contains("show_last_charge_time")) {

            newPrefs.edit().putBoolean(IS_SHOW_LAST_CHARGE_TIME_IN_APP,
                newPrefs.getBoolean("show_last_charge_time", true)).apply()

            removeOldPref(context)
        }

        if(newPrefs.contains("dark_mode")) {

            newPrefs.edit().putBoolean(IS_DARK_MODE,
                newPrefs.getBoolean("dark_mode", true)).apply()

            removeOldPref(context)
        }

        if(newPrefs.contains("charge_counter")) {

            newPrefs.edit().putInt(RESIDUAL_CAPACITY,
                newPrefs.getInt("charge_counter", 0)).apply()

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

            if(pref.contains("notification_refresh_rate")) remove("notification_refresh_rate")

            if(pref.contains("charge_counter")) remove("charge_counter")

            if(pref.contains("is_show_debug")) remove("is_show_debug")

            if(pref.contains("is_enable_service")) remove("is_enable_service")

            apply()
        }
    }
}