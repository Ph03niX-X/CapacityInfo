package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper.isStartedService

class UpdateApplicationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                migratedPrefs(context)

                if(CapacityInfoService.instance == null && !isStartedService) {

                    isStartedService = true

                    ServiceHelper.startService(context, CapacityInfoService::class.java)
                }

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context))
                    ServiceHelper.startService(context, OverlayService::class.java)
            }
        }
    }

    private fun migratedPrefs(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {

            apply {

                edit().apply {

                    if(contains("main_window_text_font")) {

                        putString(TEXT_FONT,
                            getString("main_window_text_font", "6"))

                        remove("main_window_text_font")
                    }

                    if(contains("main_window_text_style")) {

                        putString(TEXT_STYLE,
                            getString("main_window_text_style", "0"))

                        remove("main_window_text_style")
                    }

                    if(contains("main_screen_text_size")) {

                        putString(TEXT_SIZE, getString("main_screen_text_size", "2"))

                        remove("main_screen_text_size")
                    }

                    if(contains("main_screen_text_font")) {

                        putString(TEXT_FONT, getString("main_screen_text_font", "6"))

                        remove("main_screen_text_font")
                    }

                    if(contains("main_screen_text_style")) {

                        putString(TEXT_STYLE, getString("main_screen_text_style", "0"))

                        remove("main_screen_text_style")
                    }

                    apply()
                }
            }
        }
    }
}