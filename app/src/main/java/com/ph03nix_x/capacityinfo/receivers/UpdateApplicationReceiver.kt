package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_UPDATE_APP

class UpdateApplicationReceiver : BroadcastReceiver(), PremiumInterface {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                MainApp.isUpdateApp = true
                PremiumInterface.premiumContext = context
                if(!pref.getBoolean(IS_AUTO_START_UPDATE_APP, context.resources.getBoolean(
                        R.bool.is_auto_start_update_app))) return
                ServiceHelper.cancelAllJobs(context)
                ServiceHelper.checkPremiumJobSchedule(context)
                if(CapacityInfoService.instance == null &&
                    !ServiceHelper.isStartedCapacityInfoService()) ServiceHelper.startService(
                    context, CapacityInfoService::class.java)
                if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context)
                    && !ServiceHelper.isStartedOverlayService())
                    ServiceHelper.startService(context, OverlayService::class.java)
                removeOldPref(pref)
            }
        }
    }

    private fun removeOldPref(pref: SharedPreferences) {
        arrayListOf("is_fast_charge_setting", "is_show_stop_service",
            "is_stop_the_service_when_the_cd", "is_auto_dark_mode", "is_dark_mode",
            "is_request_rate_the_app").forEach {
            with(pref) {
                edit().apply {
                    if((it == "is_dark_mode" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && contains(it)) || (it != "is_dark_mode" && contains(it))) remove(it)
                    apply()
                }
            }
        }
    }
}