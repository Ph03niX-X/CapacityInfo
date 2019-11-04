package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StopService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(CapacityInfoService.instance != null) stopService(Intent(this, CapacityInfoService::class.java))

        stopService(Intent(this, StopService::class.java))

        return START_NOT_STICKY
    }
}