package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {

        val pref = PreferenceManager.getDefaultSharedPreferences(p0!!)

        when(p1!!.action) {

            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" ->
                if(pref.getBoolean(Preferences.EnableService.prefName, true)
                    && CapacityInfoService.instance == null) startService(p0)
        }
    }

    private fun startService(context: Context) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, CapacityInfoService::class.java))

        else context.startService(Intent(context, CapacityInfoService::class.java))
    }
}