package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utils.Utils.isEnabledOverlay
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                if(CapacityInfoService.instance == null && !isStartedService) {

                    isStartedService = true

                    startService(context)
                }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if(Settings.canDrawOverlays(context) && isEnabledOverlay(context)
                        && pref.getBoolean(IS_ENABLED_OVERLAY, false)
                        && OverlayService.instance == null)
                        context.startService(Intent(context, OverlayService::class.java))
                }

                else if(isEnabledOverlay(context) && OverlayService.instance == null
                    && pref.getBoolean(IS_ENABLED_OVERLAY, false))
                    context.startService(Intent(context, OverlayService::class.java))
            }
        }
    }
}