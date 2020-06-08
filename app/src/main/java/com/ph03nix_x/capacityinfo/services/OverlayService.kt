package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.IBinder
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.isEnabledOverlay
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.linearLayout
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface.Companion.windowManager
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import kotlinx.coroutines.*

class OverlayService : Service(), OverlayInterface {

    private var jobService: Job? = null
    var isJob = false

    companion object {

        var instance: OverlayService? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {

        super.onCreate()

        instance = this

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        LocaleHelper.setLocale(this, pref.getString(PreferencesKeys.LANGUAGE,
            null) ?: MainApp.defLang)

        onCreateOverlay(this)

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

                    delay(if(status == BatteryManager.BATTERY_STATUS_CHARGING) 992L else 3000L)

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

        isJob = false

        instance = null

        jobService?.cancel()

        jobService = null

        windowManager.removeView(linearLayout)

        super.onDestroy()
    }
}