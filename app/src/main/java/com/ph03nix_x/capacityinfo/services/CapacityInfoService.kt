package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.NotificationInterface
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevel
import com.ph03nix_x.capacityinfo.Util.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var batteryManager: BatteryManager
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var batteryStatus: Intent? = null
    private var jobService: Job? = null
    private var isJob = false
    var isFull = false
    var batteryLevelWith = -1
    var seconds = 0
    var numberOfCharges: Long = 0

    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {

        super.onCreate()

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {

            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                isPowerConnected = true

                batteryLevelWith = getBatteryLevel(this)

                tempBatteryLevel = batteryLevelWith

                tempCurrentCapacity = getCurrentCapacity(this)
            }
        }

        registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))

        registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        instance = this

        createNotification(this@CapacityInfoService)

        isJob = !isJob

        if(jobService == null)
        jobService = GlobalScope.launch {

            while (isJob) {
                
                if(!::wakeLock.isInitialized) {

                    if(!::powerManager.isInitialized) powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")
                }

                if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(45 * 1000)

                batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                    if(isFull) isFull = false

                    if(numberOfCharges == pref.getLong(Preferences.NumberOfCharges.prefKey, 0))
                        pref.edit().putLong(Preferences.NumberOfCharges.prefKey, numberOfCharges + 1).apply()

                    val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

                    for(display in displayManager.displays)
                        if(display.state == Display.STATE_ON)
                            delay(if(getCurrentCapacity(this@CapacityInfoService) > 0) 957 else 964)
                    else delay(if(getCurrentCapacity(this@CapacityInfoService) > 0) 947 else 954)

                    seconds++
                    updateNotification(this@CapacityInfoService)
                }

                else if (status == BatteryManager.BATTERY_STATUS_FULL && !isFull) {
                    
                    isFull = true

                    numberOfCharges = pref.getLong(Preferences.NumberOfCharges.prefKey, 0)

                    pref.edit().putInt(Preferences.LastChargeTime.prefKey, seconds).apply()
                    pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith).apply()
                    pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, getBatteryLevel(this@CapacityInfoService)).apply()

                    if (getCurrentCapacity(this@CapacityInfoService) > 0) {

                        pref.edit().putInt(Preferences.ChargeCounter.prefKey, (getCurrentCapacity(this@CapacityInfoService) * 1000).toInt()).apply()

                        pref.edit().putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat()).apply()

                        pref.edit().putInt(Preferences.PercentAdded.prefKey, percentAdded).apply()

                        if(!pref.getBoolean(Preferences.IsSupported.prefKey, true)) pref.edit().putBoolean(Preferences.IsSupported.prefKey, true).apply()
                    }

                    else {

                        if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) pref.edit().putBoolean(Preferences.IsSupported.prefKey, false).apply()
                    }

                    updateNotification(this@CapacityInfoService)
                }

                else {

                    updateNotification(this@CapacityInfoService)

                    if(::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release()

                    delay(2 * 1000)
                }
            }

        }

        return START_STICKY
    }

    override fun onDestroy() {

        if(::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release()

        instance = null
        isJob = false
        jobService = null

        if(!::pref.isInitialized) pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isFull && seconds > 1) {

            pref.edit().putInt(Preferences.LastChargeTime.prefKey, seconds).apply()

            pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith).apply()

            pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, getBatteryLevel(this)).apply()

            if(capacityAdded > 0) pref.edit().putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat()).apply()

            if(percentAdded > 0) pref.edit().putInt(Preferences.PercentAdded.prefKey, percentAdded).apply()

            percentAdded = 0

            capacityAdded = 0.0
        }

        super.onDestroy()
    }
}