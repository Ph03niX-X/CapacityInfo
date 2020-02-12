package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.Utils.isPowerConnected
import com.ph03nix_x.capacityinfo.utils.Utils.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.utils.Utils.tempCurrentCapacity

class PluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        if(CapacityInfoService.instance != null && !isPowerConnected)
            when(intent.action) {

            Intent.ACTION_POWER_CONNECTED -> {

                isPowerConnected = true

                CapacityInfoService.instance!!.numberOfCharges = PreferenceManager.getDefaultSharedPreferences(context).getLong(NUMBER_OF_CHARGES, 0)

                CapacityInfoService.instance!!.batteryLevelWith = CapacityInfoService.instance!!.getBatteryLevel(context)

                tempBatteryLevelWith = CapacityInfoService.instance!!.batteryLevelWith

                tempCurrentCapacity = CapacityInfoService.instance!!.getCurrentCapacity(context)
            }
        }
    }
}