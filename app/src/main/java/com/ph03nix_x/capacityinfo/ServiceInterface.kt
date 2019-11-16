package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface ServiceInterface {

    fun startService(context: Context) {

        GlobalScope.launch {
            delay(1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(Intent(context, CapacityInfoService::class.java))
            else context.startService(Intent(context, CapacityInfoService::class.java))
        }
    }
}