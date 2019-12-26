package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.ServiceInterface
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded

class UnpluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null && isPowerConnected)
        when(intent.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                pref.edit().apply {
                    
                    if(residualCapacity > 0) putInt(Preferences.ResidualCapacity.prefKey,
                        (CapacityInfoService.instance!!.getCurrentCapacity(context) * 1000).toInt())

                    if (!CapacityInfoService.instance!!.isFull && CapacityInfoService.instance!!.seconds > 0) {

                        putInt(Preferences.LastChargeTime.prefKey, CapacityInfoService.instance!!.seconds)

                        putInt(Preferences.BatteryLevelWith.prefKey, CapacityInfoService.instance!!.batteryLevelWith)

                        putInt(Preferences.BatteryLevelTo.prefKey, CapacityInfoService.instance!!.getBatteryLevel(context))

                        if(capacityAdded > 0) putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat())

                        if(percentAdded > 0) putInt(Preferences.PercentAdded.prefKey, percentAdded)

                        percentAdded = 0

                        capacityAdded = 0.0
                    }
                    
                    apply()
                }

                CapacityInfoService.instance!!.seconds = 0

                batteryLevel = 0
            }
        }
    }
}