package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.isEnabledOverlay
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.linearLayout
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.windowManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

class OverlayService : Service(), OverlayInterface {

    private var jobService: Job? = null
    private var isJob = false

    companion object {
        var instance: OverlayService? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        instance = this
        onCreateOverlay(this)
        OverlayInterface.screenTime = if(MainApp.tempScreenTime > 0L) MainApp.tempScreenTime
        else if(MainApp.isUpdateApp)
            pref.getLong(PreferencesKeys.UPDATE_TEMP_SCREEN_TIME, 0L) else
                CapacityInfoService.instance?.screenTime
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
                            getSourceOfPower(this@OverlayService, sourceOfPower) != "N/A")
                            OverlayInterface.chargingTime++
                        delay(if(getCurrentCapacity(this@OverlayService) > 0.0)
                            0.99.seconds else 1.seconds)
                    }
                    else delay(1.seconds)
                    withContext(Dispatchers.Main) {
                        if(CapacityInfoService.instance != null &&
                            OverlayInterface.screenTime == null) {
                            OverlayInterface.screenTime = CapacityInfoService.instance?.screenTime
                            if(!OverlayInterface.isScreenTimeCount)
                                OverlayInterface.isScreenTimeCount = true
                        }
                        else if(CapacityInfoService.instance != null &&
                            OverlayInterface.isScreenTimeCount &&
                            getSourceOfPower(this@OverlayService, sourceOfPower) == "N/A"
                            && status == BatteryManager.BATTERY_STATUS_DISCHARGING)
                            OverlayInterface.screenTime = (OverlayInterface.screenTime ?: 0) + 1
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
        isJob = false
        instance = null
        jobService?.cancel()
        jobService = null
        OverlayInterface.screenTime = null
        OverlayInterface.chargingTime = 0
        OverlayInterface.isScreenTimeCount = false
        if(linearLayout?.windowToken != null) windowManager?.removeView(linearLayout)
        super.onDestroy()
    }
}