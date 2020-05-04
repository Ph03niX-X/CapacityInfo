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

    fun onStartService(context: Context, serviceName: Class<*>) {

        if(serviceName == CapacityInfoService::class.java && isStartedService) {

            isStartedService = false

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(Intent(context, serviceName))
            else context.startService(Intent(context, serviceName))
        }

        else context.startService(Intent(context, serviceName))
    }

    fun onRestartService(context: Context, serviceName: Class<*>) {

        try {

            Toast.makeText(context, context.getString(R.string.restarting_the_service),
                Toast.LENGTH_LONG).show()

            onStopService(context, serviceName)

            if(serviceName == CapacityInfoService::class.java) isStartedService = true

            onStartService(context, serviceName)

            Toast.makeText(context, context.getString(R.string.service_restarted_successfully),
                Toast.LENGTH_LONG).show()
        }

        catch (e: Exception) {

            isStartedService = false

            Toast.makeText(context, context.getString(R.string.service_restart_failed, e.message),
                Toast.LENGTH_LONG).show()
        }
    }

    fun onStopService(context: Context, serviceName: Class<*>) {

        context.stopService(Intent(context, serviceName))
    }
}