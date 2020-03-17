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

                removeOldPref(context)

                if(CapacityInfoService.instance == null) startService(context)
            }
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

            if(pref.contains("is_show_stop_service")) remove("is_show_stop_service")

            if(pref.contains("migrated")) remove("migrated")

            apply()
        }
    }
}