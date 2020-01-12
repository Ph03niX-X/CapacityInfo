package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import java.lang.Exception

interface ServiceInterface {

    fun startService(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, CapacityInfoService::class.java))
        else context.startService(Intent(context, CapacityInfoService::class.java))
    }

    fun restartService(context: Context) {

        try {

            Toast.makeText(context, context.getString(R.string.restarting_the_service), Toast.LENGTH_LONG).show()

            context.stopService(Intent(context, CapacityInfoService::class.java))

            startService(context)

            Toast.makeText(context, context.getString(R.string.service_restarted_successfully), Toast.LENGTH_LONG).show()
        }

        catch (e: Exception) {

            Toast.makeText(context, context.getString(R.string.service_restart_failed, e.message), Toast.LENGTH_LONG).show()
        }
    }
}