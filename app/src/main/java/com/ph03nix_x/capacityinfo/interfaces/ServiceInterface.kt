package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService
import java.lang.Exception

interface ServiceInterface {

    fun startService(context: Context) {

        if(isStartedService) {

            isStartedService = false

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(Intent(context, CapacityInfoService::class.java))
            else context.startService(Intent(context, CapacityInfoService::class.java))
        }
    }

    fun restartService(context: Context) {

        try {

            Toast.makeText(context, context.getString(R.string.restarting_the_service), Toast.LENGTH_LONG).show()

            context.stopService(Intent(context, CapacityInfoService::class.java))

            isStartedService = true

            startService(context)

            Toast.makeText(context, context.getString(R.string.service_restarted_successfully), Toast.LENGTH_LONG).show()
        }

        catch (e: Exception) {

            isStartedService = false

            Toast.makeText(context, context.getString(R.string.service_restart_failed, e.message), Toast.LENGTH_LONG).show()
        }
    }
}