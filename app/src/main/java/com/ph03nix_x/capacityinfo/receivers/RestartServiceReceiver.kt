package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                if(CapacityInfoService.instance == null && !isStartedService) {

                    isStartedService = true

                    onStartService(context, CapacityInfoService::class.java)
                }

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context))
                    onStartService(context, OverlayService::class.java)
            }
        }
    }
}