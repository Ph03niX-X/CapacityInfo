package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface ServiceInterface {

    companion object {

        private var isStartedJob = false
    }

    fun startService(context: Context) {

        if(!isStartedJob)
        GlobalScope.launch {

            isStartedJob = true

            delay(1000)
            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(Intent(context, CapacityInfoService::class.java))
                else context.startService(Intent(context, CapacityInfoService::class.java))
            }

            isStartedJob = false
        }
    }
}