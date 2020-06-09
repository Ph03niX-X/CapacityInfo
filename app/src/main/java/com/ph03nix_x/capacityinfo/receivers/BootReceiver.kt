package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" -> {

                if(CapacityInfoService.instance == null && !ServiceHelper.isStartedService())
                    ServiceHelper.startService(context, CapacityInfoService::class.java,
                        true)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context))
                    ServiceHelper.startService(context, OverlayService::class.java)
            }
        }
    }
}