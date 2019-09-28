package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.fragment.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.isPowerConnected

class UnpluggedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent!!.action) {

            Intent.ACTION_POWER_DISCONNECTED -> {

                isPowerConnected = false

                tempBatteryLevelWith = -1

                if(CapacityInfoService.instance != null) context?.stopService(Intent(context, CapacityInfoService::class.java))
            }
        }
    }
}