package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.ServiceInterface
import com.ph03nix_x.capacityinfo.Util
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected

class UnpluggedReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null && isPowerConnected)
        when(intent.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                if(residualCapacity > 0) pref.edit().putInt(Preferences.ChargeCounter.prefKey,
                    (CapacityInfoService.instance!!.getCurrentCapacity(context) * 1000).toInt()).apply()

                if (!CapacityInfoService.instance!!.isFull && CapacityInfoService.instance!!.seconds > 1) {

                    pref.edit().putInt(Preferences.LastChargeTime.prefKey, CapacityInfoService.instance!!.seconds).apply()

                    pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, CapacityInfoService.instance!!.batteryLevelWith).apply()

                    pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, CapacityInfoService.instance!!.getBatteryLevel(CapacityInfoService.instance!!)).apply()

                    if(Util.capacityAdded > 0) pref.edit().putFloat(Preferences.CapacityAdded.prefKey, Util.capacityAdded.toFloat()).apply()

                    if(Util.percentAdded > 0) pref.edit().putInt(Preferences.PercentAdded.prefKey, Util.percentAdded).apply()

                    Util.percentAdded = 0

                    Util.capacityAdded = 0.0
                }

                CapacityInfoService.instance!!.isFull = false

                CapacityInfoService.instance!!.seconds = 0

                batteryLevel = 0
            }
        }
    }
}