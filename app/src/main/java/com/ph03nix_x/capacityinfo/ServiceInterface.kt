package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

interface ServiceInterface {

    fun startService(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, CapacityInfoService::class.java))
        else context.startService(Intent(context, CapacityInfoService::class.java))
    }

    fun restartService(context: Context) {

        context.stopService(Intent(context, CapacityInfoService::class.java))

        startService(context)
    }
}