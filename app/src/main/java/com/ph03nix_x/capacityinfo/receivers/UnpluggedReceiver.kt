package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STOP_THE_SERVICE_WHEN_THE_CD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY

class UnpluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null && isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                val seconds = CapacityInfoService.instance?.seconds ?: 0

                val batteryLevel = CapacityInfoService.instance?.getOnBatteryLevel(context) ?: 0

                val batteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0

                val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
                    NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
                    NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                        batteryLevelWith / 100f)

                pref.edit().apply {

                    if(residualCapacity > 0 && CapacityInfoService.instance?.isFull == true) {

                        val currentCapacity = ((CapacityInfoService.instance?.getOnCurrentCapacity(
                            context) ?: 0.0) * if(pref.getString(
                                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                                    1000.0 else 100.0).toInt()

                        putInt(RESIDUAL_CAPACITY, currentCapacity)

                        HistoryHelper.autoClearHistory(context)
                        HistoryHelper.addHistory(context, DateHelper.getDate(DateHelper
                            .getCurrentDay(), DateHelper.getCurrentMonth(), DateHelper
                            .getCurrentYear()), currentCapacity)
                    }

                    if((CapacityInfoService.instance?.isFull != true) && seconds > 1) {

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

                if(batteryLevel >= 90) CapacityInfoService.instance?.screenTime = 0L

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

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

                NotificationInterface.notificationManager?.cancel(NotificationInterface
                    .NOTIFICATION_DISCHARGE_CURRENT_ID)

                NotificationInterface.isOverheatOvercool = true
                NotificationInterface.isBatteryFullyCharged = true
                NotificationInterface.isBatteryCharged = true
                NotificationInterface.isBatteryChargedVoltage = true
                NotificationInterface.isBatteryDischarged = true
                NotificationInterface.isBatteryDischargedVoltage = true
                NotificationInterface.isChargingCurrent = true
                NotificationInterface.isDischargeCurrent = true

                if(MainActivity.instance?.fragment != null) {

                    if(MainActivity.instance?.fragment is ChargeDischargeFragment)
                        MainActivity.instance?.toolbar?.title = context.getString(
                            R.string.discharge)

                    val chargeDischargeNavigation = MainActivity.instance?.navigation
                        ?.menu?.findItem(R.id.charge_discharge_navigation)

                    chargeDischargeNavigation?.title = context.getString(R.string.discharge)

                    chargeDischargeNavigation?.icon = MainActivity.instance
                        ?.getChargeDischargeNavigationIcon(false)?.let {
                            ContextCompat.getDrawable(context, it)
                        }
                }
            }
        }
    }
}