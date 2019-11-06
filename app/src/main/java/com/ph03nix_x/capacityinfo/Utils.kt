package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class Utils {

    companion object {

        fun startService(context: Context? = null) {

            Handler().postDelayed({

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context?.startForegroundService(Intent(context, CapacityInfoService::class.java))

                else context?.startService(Intent(context, CapacityInfoService::class.java))

            }, 100)
        }
    }
}