package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

interface ServiceInterface {

    fun onStartService(context: Context, serviceName: Class<*>) {

        if(serviceName == CapacityInfoService::class.java && isStartedService) {

            isStartedService = false

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(Intent(context, serviceName))
            else context.startService(Intent(context, serviceName))
        }

        else context.startService(Intent(context, serviceName))
    }

    fun onStopService(context: Context, serviceName: Class<*>) {

        context.stopService(Intent(context, serviceName))
    }
}