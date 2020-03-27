package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StopCapacityInfoService : Service() {

    companion object {

        var isStopService = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        isStopService = true

        stopService(Intent(this, CapacityInfoService::class.java))

        isStopService = false

        stopService(Intent(this, StopCapacityInfoService::class.java))

        return START_NOT_STICKY
    }
}