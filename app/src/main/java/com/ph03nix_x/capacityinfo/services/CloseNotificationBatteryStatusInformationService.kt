package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface

class CloseNotificationBatteryStatusInformationService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
       close()
        ServiceHelper.stopService(this,
            CloseNotificationBatteryStatusInformationService::class.java)
        return START_NOT_STICKY
    }

    private fun close() {
        with(NotificationInterface) {
            if(isOverheatOvercool) {
                notificationManager?.cancel(NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)
                return
            }
            notificationManager?.cancel(NOTIFICATION_BATTERY_STATUS_ID)
        }
    }
}