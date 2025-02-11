package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_UPDATE_APP

class UpdateApplicationReceiver : BroadcastReceiver(), PremiumInterface {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val pref = PreferenceManager.getDefaultSharedPreferences(context)
                val tabOnAppLaunch = pref.getString(
                    PreferencesKeys.TAB_ON_APPLICATION_LAUNCH, "0")?.toInt() ?: 0
                MainApp.isUpdateApp = true
                PremiumInterface.premiumContext = context
                if(tabOnAppLaunch in 1..2) {
                    pref.edit().putString(PreferencesKeys.TAB_ON_APPLICATION_LAUNCH,
                        (tabOnAppLaunch + 1).toString()).apply()
                    }
                   context.resources.getStringArray(R.array.tab_on_application_launch_values)
                    pref.edit().putString(PreferencesKeys.TAB_ON_APPLICATION_LAUNCH, "0").apply()
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
        arrayListOf("is_fast_charge_setting", "is_show_stop_service").forEach {
            with(pref) {
                edit().apply {
                    if(contains(it)) remove(it)
                    apply()
                }
            }
        }
    }
}