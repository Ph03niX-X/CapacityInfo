package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.os.*
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevel
import com.ph03nix_x.capacityinfo.Util.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.Util.Companion.hoursDefault
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
    private var isFull = false
    private var batteryLevelWith = -1
    var seconds = 0
    var sleepTime: Long = 10

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

                applicationContext.registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

            }

            else -> applicationContext.registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        seconds++

        createNotification(this@CapacityInfoService)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        instance = this

        if(!isJob) isJob = true

        sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)

        if(jobService == null)
        jobService = GlobalScope.launch {

            while (isJob) {
                
                if(!::wakeLock.isInitialized) {

                    if(!::powerManager.isInitialized) powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")
                }

                if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(12 * 60 * 60 * 1000)

                batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                    
                    delay(if(getCurrentCapacity(this@CapacityInfoService) > 0) 960 else 967)
                    seconds++
                    updateNotification(this@CapacityInfoService)
                }

                else if (status == BatteryManager.BATTERY_STATUS_FULL && !isFull) {
                    
                    isFull = true

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

                    val sleepArray = arrayOf<Long>(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)

                    if (sleepTime !in sleepArray && !isPowerConnected) {

                        sleepTime = 40

                        pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()
                    }

                    else if(isPowerConnected && sleepTime != 20.toLong() && isFull) sleepTime = 20
                    else if(!isPowerConnected) sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)
                    if(::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release()

                    delay(if(!isPowerConnected && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)) sleepTime * 990
                    else if(!isPowerConnected && !pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)) (90 * 990).toLong()
                    else 990)
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

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isFull && seconds > 1) {

            pref.edit().putInt(Preferences.LastChargeTime.prefKey, seconds).apply()

            pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith).apply()

            pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, getBatteryLevel(this@CapacityInfoService)).apply()

            if(capacityAdded > 0) pref.edit().putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat()).apply()

            if(percentAdded > 0) pref.edit().putInt(Preferences.PercentAdded.prefKey, percentAdded).apply()

            percentAdded = 0

            capacityAdded = 0.0
        }

        hoursDefault = 0

        super.onDestroy()
    }
}