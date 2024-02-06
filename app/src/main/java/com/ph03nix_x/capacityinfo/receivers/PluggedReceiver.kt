package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface

class PluggedReceiver : BroadcastReceiver(), PremiumInterface, NavigationInterface {

    override fun onReceive(context: Context, intent: Intent) {
        if(CapacityInfoService.instance != null && !isPowerConnected)
            when(intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                isPowerConnected = true
                CapacityInfoService.instance?.isPluggedOrUnplugged = true
                batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                    .ACTION_BATTERY_CHANGED))
                CapacityInfoService.instance?.sourceOfPower =
                    batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                    BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
                CapacityInfoService.instance?.batteryLevelWith = CapacityInfoService.instance
                    ?.getBatteryLevel(context) ?: 0
                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0
                tempCurrentCapacity = CapacityInfoService.instance
                    ?.getCurrentCapacity(context) ?: 0.0
                BatteryInfoInterface.apply {
                    maxChargeCurrent = 0
                    averageChargeCurrent = 0
                    minChargeCurrent = 0
                    maxDischargeCurrent = 0
                    averageDischargeCurrent = 0
                    minDischargeCurrent = 0
                    maximumTemperature = 0.0
                    averageTemperature = 0.0
                    minimumTemperature = 0.0
                }
                CapacityInfoService.instance?.isSaveNumberOfCharges = true
                
                NotificationInterface.apply {
                    notificationManager?.cancel(NOTIFICATION_FULLY_CHARGED_ID)
                    notificationManager?.cancel(NOTIFICATION_BATTERY_STATUS_ID)
                    notificationManager?.cancel(NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)
                    isOverheatOvercool = true
                    isBatteryFullyCharged = true
                    isBatteryCharged = true
                    isBatteryDischarged = true
                }
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
                CapacityInfoService.instance?.isPluggedOrUnplugged = false
            }
        }
    }
}