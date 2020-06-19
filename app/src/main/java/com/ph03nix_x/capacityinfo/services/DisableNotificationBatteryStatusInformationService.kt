package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys

class DisableNotificationBatteryStatusInformationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        disable()

        ServiceHelper.stopService(this,
            DisableNotificationBatteryStatusInformationService::class.java)

        return START_NOT_STICKY
    }

    private fun disable() {

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        with(pref.edit()) {

            when {

                NotificationInterface.isOverheatOvercool -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL, false).apply()

                NotificationInterface.isBatteryFullyCharged -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, false).apply()

                NotificationInterface.isBatteryCharged -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED, false).apply()

                NotificationInterface.isBatteryDischarged -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED, false).apply()
            }
        }

        NotificationInterface.notificationManager?.cancel(if(!NotificationInterface
                .isOverheatOvercool) NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID else
            NotificationInterface.NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)
    }
}