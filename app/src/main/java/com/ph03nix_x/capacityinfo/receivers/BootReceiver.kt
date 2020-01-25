package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AUTO_START_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_ENABLE_SERVICE

class BootReceiver : BroadcastReceiver(),
    ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getBoolean(IS_AUTO_START_SERVICE, true))
        when(intent.action) {

            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" ->

                if(pref.getBoolean(IS_ENABLE_SERVICE, true)
                    && CapacityInfoService.instance == null) startService(context)
        }
    }
}