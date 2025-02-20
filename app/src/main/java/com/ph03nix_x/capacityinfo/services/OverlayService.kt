package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.MainApp.Companion.remainingBatteryTimeSeconds
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.isEnabledOverlay
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.linearLayout
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.windowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

class OverlayService : Service(), OverlayInterface {

    private var jobService: Job? = null
    private var isJob = false

    var isGetRemainingBatteryTime = true

    companion object {
        var instance: OverlayService? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if(!isGooglePlay(this)) return
        instance = this
        onCreateOverlay(this)
        OverlayInterface.chargingTime = CapacityInfoService.instance?.seconds ?: 0
        isJob = !isJob
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(jobService == null)
            jobService = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {
                    batteryIntent = registerReceiver(null,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                    if(status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        if(CapacityInfoService.instance != null &&
                            getSourceOfPower(this@OverlayService, sourceOfPower).contains("N/A"))
                            OverlayInterface.chargingTime++
                        delay(if(getCurrentCapacity(this@OverlayService) > 0.0)
                            0.99.seconds else 1.seconds)
                    }
                    else {
                        delay(1.seconds)
                        if(!isGetRemainingBatteryTime && !isPowerConnected)
                            remainingBatteryTimeSeconds++
                    }
                    withContext(Dispatchers.Main) {
                        if(isEnabledOverlay(this@OverlayService))
                            onUpdateOverlay(this@OverlayService)
                        else ServiceHelper.stopService(this@OverlayService,
                            OverlayService::class.java)
                    }
                }
            }

        return START_STICKY
    }

    override fun onDestroy() {
        isGetRemainingBatteryTime = true
        isJob = false
        instance = null
        jobService?.cancel()
        jobService = null
        OverlayInterface.chargingTime = 0
        if(linearLayout?.windowToken != null) windowManager?.removeView(linearLayout)
        if(MainActivity.instance == null) remainingBatteryTimeSeconds = 0
        super.onDestroy()
    }
}