package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

interface ServiceInterface {

    fun restartService(context: Context) {

        if(CapacityInfoService.instance != null) context.stopService(Intent(context, CapacityInfoService::class.java))

        Handler().postDelayed({

            startService(context)

        }, 150)
    }

    fun startService(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, CapacityInfoService::class.java))

        else context.startService(Intent(context, CapacityInfoService::class.java))
    }
}