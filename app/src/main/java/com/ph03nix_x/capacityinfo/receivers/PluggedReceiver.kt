package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity

class PluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if(CapacityInfoService.instance != null && !isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_CONNECTED -> {

                isPowerConnected = true

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)

                batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                    .ACTION_BATTERY_CHANGED))

                val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

                if(status == BatteryManager.BATTERY_STATUS_CHARGING) pref.edit().putLong(
                    NUMBER_OF_CHARGES, numberOfCharges + 1).apply()

                CapacityInfoService.instance?.batteryLevelWith = CapacityInfoService.instance
                    ?.getOnBatteryLevel(context) ?: 0

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0

                tempCurrentCapacity = CapacityInfoService.instance
                    ?.getOnCurrentCapacity(context) ?: 0.0

                BatteryInfoInterface.maxChargeCurrent = 0
                BatteryInfoInterface.averageChargeCurrent = 0
                BatteryInfoInterface.minChargeCurrent = 0
                BatteryInfoInterface.maxDischargeCurrent = 0
                BatteryInfoInterface.averageDischargeCurrent = 0
                BatteryInfoInterface.minDischargeCurrent = 0

                CapacityInfoService.instance?.isSaveNumberOfCharges = true

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
                        MainActivity.instance?.toolbar?.title = context.getString(if(status ==
                            BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                            R.string.discharge)

                    val chargeDischargeNavigation = MainActivity.instance?.navigation
                        ?.menu?.findItem(R.id.charge_discharge_navigation)

                    chargeDischargeNavigation?.title = context.getString(if(status ==
                        BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                        R.string.discharge)

                    chargeDischargeNavigation?.icon = MainActivity.instance
                        ?.getChargeDischargeNavigationIcon(status ==
                                BatteryManager.BATTERY_STATUS_CHARGING)?.let {
                            ContextCompat.getDrawable(context, it)
                        }
                }
            }
        }
    }
}