package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_BOOT

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" -> {

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                if(!pref.getBoolean(IS_AUTO_START_BOOT, context.resources.getBoolean(R.bool
                        .is_auto_start_boot))) return

                ServiceHelper.cancelAllJobs(context)

                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)
            }
        }
    }
}