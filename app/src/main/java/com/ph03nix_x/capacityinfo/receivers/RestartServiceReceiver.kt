package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                removeOldPref(context)

                if(CapacityInfoService.instance == null && !isStartedService) {

                    isStartedService = true

                    startService(context)
                }
            }
        }
    }

    @Deprecated("Will be removed soon")
    private fun removeOldPref(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {
            
            edit().apply {
                
                if(contains("always_show_notification")) remove("always_show_notification")

                if(contains("show_last_charge_time")) remove("show_last_charge_time")

                if(contains("dark_mode")) remove("dark_mode")

                if(contains("enable_service")) remove("enable_service")

                if(contains("is_show_information_while_charging")) remove("is_show_information_while_charging")

                if(contains("is_show_information_during_discharge")) remove("is_show_information_during_discharge")

                if(contains("notification_refresh_rate")) remove("notification_refresh_rate")

                if(contains("charge_counter")) remove("charge_counter")

                if(contains("is_show_debug")) remove("is_show_debug")

                if(contains("is_enable_service")) remove("is_enable_service")

                if(contains("migrated")) remove("migrated")

                if(contains("is_show_charging_time_in_app")) remove("is_show_charging_time_in_app")

                if(contains("is_show_last_charge_time_in_app")) remove("is_show_last_charge_time_in_app")

                if(contains("is_show_capacity_added_in_app")) remove("is_show_capacity_added_in_app")

                if(contains("is_show_capacity_added_last_charge_in_app")) remove("is_show_capacity_added_last_charge_in_app")

                if(contains("is_show_capacity_added_last_charge_in_notification")) remove("is_show_capacity_added_last_charge_in_notification")

                apply()
            }
        }
    }
}