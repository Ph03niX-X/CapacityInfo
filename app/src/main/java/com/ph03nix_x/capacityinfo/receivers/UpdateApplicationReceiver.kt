package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_UPDATE_APP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateApplicationReceiver : BroadcastReceiver(), PremiumInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                PremiumInterface.premiumContext = context

                removeOldPreferences(context)

                if(!pref.getBoolean(IS_AUTO_START_UPDATE_APP, context.resources.getBoolean(
                        R.bool.is_auto_start_update_app))) return

                ServiceHelper.cancelAllJobs(context)

                ServiceHelper.checkPremiumJobSchedule(context)

                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)
            }
        }
    }

    private fun removeOldPreferences(context: Context) {

        CoroutineScope(Dispatchers.IO).launch {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            arrayListOf("temperature_in_fahrenheit", "voltage_in_mv", "is_fps_overlay",
                "is_show_faq", "is_show_donate_message", "is_show_premium_info_dialog",
                "is_supported", "is_show_not_supported_dialog", "language",
                "is_enable_fake_battery_wear", "fake_battery_wear_value", "is_high_battery_wear",
                "is_very_high_battery_wear", "is_critical_battery_wear",
                "${context.packageName}_preferences.products.cache.v2_6.version",
                "${context.packageName}_preferences.products.cache.v2_6",
                "${context.packageName}_preferences.products.restored.v2_6",
                "${context.packageName}_preferences.subscriptions.cache.v2_6",
                "${context.packageName}_preferences.subscriptions.cache.v2_6.version",
                "is_battery_wear", "is_show_instruction", "is_show_backup_information",
                "is_auto_backup_settings", "is_backup_settings_to_microsd",
                "frequency_of_auto_backup_settings", "is_notify_battery_is_charged_voltage",
                "battery_notify_charged_voltage", "is_notify_battery_is_discharged_voltage",
                "battery_notify_discharged_voltage", "is_notify_charging_current",
                "charging_current_level_notify").forEach {

                with(pref) {

                    edit().apply {

                        if(contains(it)) this.remove(it)

                        apply()
                    }
                }
            }
        }
    }
}