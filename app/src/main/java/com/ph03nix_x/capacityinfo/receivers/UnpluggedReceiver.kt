package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class UnpluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val pref = context!!.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(CapacityInfoService.instance!!.seconds > 1)
        when(intent!!.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                if(!pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)) context.stopService(Intent(context, CapacityInfoService::class.java))

                else {

                    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                    pref.edit().putInt(Preferences.BatteryLevelWith.prefName, CapacityInfoService.instance!!.batteryLevelWith).apply()

                    pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

                    if(CapacityInfoService.instance!!.seconds > 1) pref.edit().putInt(Preferences.LastChargeTime.prefName, CapacityInfoService.instance!!.seconds).apply()

                    CapacityInfoService.instance?.seconds = 1

                    CapacityInfoService.instance?.sleepTime = 40

                    CapacityInfoService.instance?.isFull = false
                }
            }
        }
    }
}