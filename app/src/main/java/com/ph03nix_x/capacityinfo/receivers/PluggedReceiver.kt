package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.utils.Utils.isPowerConnected
import com.ph03nix_x.capacityinfo.utils.Utils.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.utils.Utils.tempCurrentCapacity

class PluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        if(CapacityInfoService.instance != null && !isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_CONNECTED -> {

                isPowerConnected = true

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)

                batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                    .ACTION_BATTERY_CHANGED))

                if(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ==
                    BatteryManager.BATTERY_STATUS_CHARGING) pref.edit().putLong(NUMBER_OF_CHARGES,
                    numberOfCharges + 1).apply()

                CapacityInfoService.instance?.batteryLevelWith = CapacityInfoService.instance
                    ?.getBatteryLevel(context) ?: 0

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0

                tempCurrentCapacity = CapacityInfoService.instance
                    ?.getCurrentCapacity(context) ?: 0.0

                BatteryInfoInterface.maxChargeCurrent = 0
                BatteryInfoInterface.averageChargeCurrent = 0
                BatteryInfoInterface.minChargeCurrent = 0
                BatteryInfoInterface.maxDischargeCurrent = 0
                BatteryInfoInterface.averageDischargeCurrent = 0
                BatteryInfoInterface.minDischargeCurrent = 0

                CapacityInfoService.instance?.isSaveNumberOfCharges = true
            }
        }
    }
}