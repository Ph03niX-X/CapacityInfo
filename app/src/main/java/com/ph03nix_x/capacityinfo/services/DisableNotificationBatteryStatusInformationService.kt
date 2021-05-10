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

                NotificationInterface.isBatteryChargedVoltage -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE, false).apply()

                NotificationInterface.isBatteryDischarged -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED, false).apply()

                NotificationInterface.isBatteryDischargedVoltage -> putBoolean(
                    PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE, false).apply()

                NotificationInterface.isChargingCurrent -> putBoolean(PreferencesKeys
                    .IS_NOTIFY_CHARGING_CURRENT, false).apply()

                NotificationInterface.isDischargeCurrent -> putBoolean(PreferencesKeys
                    .IS_NOTIFY_DISCHARGE_CURRENT, false).apply()
            }
        }

        if(NotificationInterface.isOverheatOvercool) {
            NotificationInterface.notificationManager?.cancel(NotificationInterface
                .NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)
            return
        }
        if(NotificationInterface.isChargingCurrent) {
            NotificationInterface.notificationManager?.cancel(NotificationInterface
                .NOTIFICATION_CHARGING_CURRENT_ID)
            return
        }
        if(NotificationInterface.isDischargeCurrent) {
            NotificationInterface.notificationManager?.cancel(NotificationInterface
                .NOTIFICATION_DISCHARGE_CURRENT_ID)
            return
        }

        NotificationInterface.notificationManager?.cancel(NotificationInterface
            .NOTIFICATION_BATTERY_STATUS_ID)
    }
}