package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface

class StopCapacityInfoService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val capacityInfoService = CapacityInfoService.instance

        capacityInfoService?.isStopService = true

        Toast.makeText(this, getString(R.string.stopping_service), Toast.LENGTH_LONG).show()

        NotificationInterface.notificationManager?.cancel(NotificationInterface
            .NOTIFICATION_SERVICE_ID)

        stopService(Intent(this, CapacityInfoService::class.java))

        stopService(Intent(this, StopCapacityInfoService::class.java))

        return START_NOT_STICKY
    }
}
