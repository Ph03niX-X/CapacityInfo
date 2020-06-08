package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION_WHEN_CHARGING
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION_WHEN_DISCHARGING
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STOP_THE_SERVICE_WHEN_THE_CD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded

class UnpluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null && isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                val seconds = CapacityInfoService.instance?.seconds ?: 0

                val batteryLevel = CapacityInfoService.instance?.onGetBatteryLevel(context) ?: 0

                val batteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0

                val numberOfCycles = pref.getFloat(NUMBER_OF_CYCLES, 0f) +
                        (batteryLevel / 100f) - (batteryLevelWith / 100f)

                pref.edit().apply {

                    if(residualCapacity > 0) {

                        if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
                            == "μAh")
                        putInt(RESIDUAL_CAPACITY,
                            ((CapacityInfoService.instance?.onGetCurrentCapacity(context)
                                ?.toInt() ?: 0) * 1000))
                        else putInt(RESIDUAL_CAPACITY, CapacityInfoService.instance
                            ?.onGetCurrentCapacity(context)?.toInt() ?: 0)
                    }

                    if((CapacityInfoService.instance?.isFull != true) && seconds > 0) {

                        putInt(LAST_CHARGE_TIME, if(seconds >= 60) seconds +
                                ((seconds / 100) * (seconds / 3600)) else seconds)

                        putInt(BATTERY_LEVEL_WITH, CapacityInfoService.instance
                            ?.batteryLevelWith ?: 0)

                        putInt(BATTERY_LEVEL_TO, batteryLevel)

                        if(CapacityInfoService.instance?.isSaveNumberOfCharges != false)
                            putFloat(NUMBER_OF_CYCLES, numberOfCycles)

                        if(capacityAdded > 0) putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                        if(percentAdded > 0) putInt(PERCENT_ADDED, percentAdded)

                        percentAdded = 0

                        capacityAdded = 0.0
                    }

                    apply()
                }

                batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                    .ACTION_BATTERY_CHANGED))

                CapacityInfoService.instance?.seconds = 0

                BatteryInfoInterface.batteryLevel = 0

                BatteryInfoInterface.maxChargeCurrent = 0
                BatteryInfoInterface.averageChargeCurrent = 0
                BatteryInfoInterface.minChargeCurrent = 0
                BatteryInfoInterface.maxDischargeCurrent = 0
                BatteryInfoInterface.averageDischargeCurrent = 0
                BatteryInfoInterface.minDischargeCurrent = 0

                CapacityInfoService.instance?.isFull = false

                if(pref.getBoolean(IS_STOP_THE_SERVICE_WHEN_THE_CD,
                        context.resources.getBoolean(R.bool.is_stop_the_service_when_the_cd))) {

                    NotificationInterface.notificationManager?.cancel(NotificationInterface
                        .NOTIFICATION_SERVICE_ID)

                    ServiceHelper.stopService(context, CapacityInfoService::class.java)
                }

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)

                if(MainActivity.instance?.fragment != null) {

                    if(MainActivity.instance?.fragment is ChargeDischargeFragment)
                        MainActivity.instance?.toolbar?.title = context.getString(
                            R.string.discharge)

                    val chargeDischargeNavigation = MainActivity.instance?.navigation
                        ?.menu?.findItem(R.id.charge_discharge_navigation)

                    chargeDischargeNavigation?.title = context.getString(R.string.discharge)

                    chargeDischargeNavigation?.icon = MainActivity.instance
                        ?.getChargeDischargeNavigationIcon(false)?.let {
                            context.getDrawable(it)
                        }
                }

                if(!pref.getBoolean(IS_SHOW_EXPANDED_NOTIFICATION_WHEN_DISCHARGING, context
                        .resources.getBoolean(
                            R.bool.is_show_expanded_notification_when_discharging)) || !pref
                        .getBoolean(IS_SHOW_EXPANDED_NOTIFICATION_WHEN_CHARGING, context.resources
                            .getBoolean(R.bool.is_show_expanded_notification_when_charging)))
                    ServiceHelper.restartService(context, CapacityInfoService::class.java)
            }
        }
    }
}