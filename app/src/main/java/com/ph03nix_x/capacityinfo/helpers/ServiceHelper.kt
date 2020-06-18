package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.Preference
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import kotlinx.coroutines.*

object ServiceHelper {

    private var isStartedCapacityInfoService = false
    private var isStartedOverlayService = false

    fun startService(context: Context, serviceName: Class<*>) {

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

            if(serviceName == CapacityInfoService::class.java) {

                isStartedCapacityInfoService = true

                delay(2500)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(Intent(context, serviceName))
                else context.startService(Intent(context, serviceName))

                delay(1000)
                isStartedCapacityInfoService = false
            }

            else if(serviceName == OverlayService::class.java) {

                isStartedOverlayService = true
                delay(2000)
                context.startService(Intent(context, serviceName))
                isStartedCapacityInfoService = false
            }
        }
    }

    fun isStartedCapacityInfoService() = isStartedCapacityInfoService

    fun isStartedOverlayService() = isStartedOverlayService

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