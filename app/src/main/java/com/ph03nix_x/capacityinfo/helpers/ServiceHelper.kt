package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.Preference
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.*

object ServiceHelper {

    private var isStartedService = false

    fun startService(context: Context, serviceName: Class<*>, isStartedService: Boolean = false) {

        this.isStartedService = isStartedService

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

            if(serviceName == CapacityInfoService::class.java) {

                if(isStartedService) delay(2500)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(Intent(context, serviceName))
                else context.startService(Intent(context, serviceName))

                if(isStartedService) delay(1000)
                this@ServiceHelper.isStartedService = false
            }

            else context.startService(Intent(context, serviceName))
        }
    }

    fun isStartedService() = isStartedService

    fun stopService(context: Context, serviceName: Class<*>) =
        context.stopService(Intent(context, serviceName))

    fun restartService(context: Context, serviceName: Class<*>) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500)

                startService(context, serviceName)
            }
        }
    }

    fun restartService(context: Context, serviceName: Class<*>, preference: Preference? = null) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500)

                startService(context, serviceName)

                delay(1000)
                preference?.isEnabled = true
            }
        }
    }

    fun restartService(context: Context, serviceName: Class<*>,
                       preferencesList: ArrayList<Preference?>? = null) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500)

                startService(context, serviceName)

                delay(1000)
                preferencesList?.forEach {

                    it?.isEnabled = true
                }
            }
        }
    }
}