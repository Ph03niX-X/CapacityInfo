package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE

class UpdateApplicationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                migratedPrefs(context)

                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)

                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)
            }
        }
    }

    @Deprecated("This function will be removed in August-September")
    private fun migratedPrefs(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {

            apply {

                edit().apply {

                    if(contains("is_auto_start_service")) remove("is_auto_start_service")

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

                    if(contains("debug_options_is_enabled")) {

                        putBoolean(IS_ENABLED_DEBUG_OPTIONS, getBoolean(
                            "debug_options_is_enabled", context.resources.getBoolean(
                                R.bool.is_enabled_debug_options)))

                        remove("debug_options_is_enabled")
                    }

                    apply()
                }
            }
        }
    }
}