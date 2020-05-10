package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.MAIN_SCREEN_TEXT_FONT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.MAIN_SCREEN_TEXT_STYLE
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class RestartServiceReceiver : BroadcastReceiver(), ServiceInterface {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                migratedPrefs(context)

                if(CapacityInfoService.instance == null && !isStartedService) {

                    isStartedService = true

                    onStartService(context, CapacityInfoService::class.java)
                }

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context))
                    onStartService(context, OverlayService::class.java)
            }
        }
    }

    private fun migratedPrefs(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {

            apply {

                edit().apply {

                    if(contains("main_window_text_font")) {

                        putString(MAIN_SCREEN_TEXT_FONT,
                            getString("main_window_text_font", "6"))

                        remove("main_window_text_font")
                    }

                    if(contains("main_window_text_style")) {

                        putString(MAIN_SCREEN_TEXT_STYLE,
                            getString("main_window_text_style", "0"))

                        remove("main_window_text_style")
                    }

                    apply()
                }

            }
        }
    }
}