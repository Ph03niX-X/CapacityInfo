package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.Util.Companion.tempCurrentCapacity

class PluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        if(CapacityInfoService.instance != null && !isPowerConnected)
        when(intent.action) {

            Intent.ACTION_POWER_CONNECTED -> {

                isPowerConnected = true

                CapacityInfoService.instance!!.numberOfCharges = PreferenceManager.getDefaultSharedPreferences(context).getLong(Preferences.NumberOfCharges.prefKey, 0)

                CapacityInfoService.instance!!.batteryLevelWith = CapacityInfoService.instance!!.getBatteryLevel(CapacityInfoService.instance!!)

                tempBatteryLevelWith = CapacityInfoService.instance!!.batteryLevelWith

                tempCurrentCapacity = CapacityInfoService.instance!!.getCurrentCapacity(CapacityInfoService.instance!!)
            }
        }
    }
}