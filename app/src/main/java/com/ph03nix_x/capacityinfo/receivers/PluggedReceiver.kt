package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class PluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action) {

            Intent.ACTION_POWER_CONNECTED -> {

                CapacityInfoService.instance?.sleepTime = 10

                val batteryManager = context?.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                CapacityInfoService.instance?.batteryLevelWith = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

                context.stopService(Intent(context, CapacityInfoService::class.java))

//                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    context.startForegroundService(Intent(context, CapacityInfoService::class.java))
//
//                else context.startService(Intent(context, CapacityInfoService::class.java))
            }
        }
    }
}