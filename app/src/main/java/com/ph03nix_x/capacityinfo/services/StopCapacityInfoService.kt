package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.ph03nix_x.capacityinfo.R

/**
 * Created by Ph03niX-X on 27.09.2022
 * Ph03niX-X@outlook.com
 */
class StopCapacityInfoService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val capacityInfoService = CapacityInfoService.instance
        if(capacityInfoService != null) {
            capacityInfoService.isStopService = true
            Toast.makeText(this, R.string.stopping_service, Toast.LENGTH_LONG).show()
            capacityInfoService.stopSelf()
            stopService(Intent(this, CapacityInfoService::class.java))
            stopService(Intent(this, StopCapacityInfoService::class.java))
        }
        else Toast.makeText(this, R.string.service_stop_error, Toast.LENGTH_LONG).show()
        return START_NOT_STICKY
    }
}