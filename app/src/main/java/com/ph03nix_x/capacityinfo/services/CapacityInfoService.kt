package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.NotificationInterface
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.Util.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.Util.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevelWith
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
    private var jobService: Job? = null
    private var isJob = false
    var isFull = false
    var isStopService = false
    var batteryLevelWith = -1
    var seconds = 0
    var numberOfCharges: Long = 0

    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {

        super.onCreate()

        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {

            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                isPowerConnected = true

                batteryLevelWith = getBatteryLevel(this)

                tempBatteryLevelWith = batteryLevelWith

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

        numberOfCharges = pref.getLong(Preferences.NumberOfCharges.prefKey, 0)

        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        createNotification(this@CapacityInfoService)

        isJob = true

        if(jobService == null)
        jobService = GlobalScope.launch {

            while (isJob) {

                if(!::wakeLock.isInitialized) {

                    if(!::powerManager.isInitialized) powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")
                }

                if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(45 * 1000)

                if(getBatteryLevel(this@CapacityInfoService) < batteryLevelWith) batteryLevelWith = getBatteryLevel(this@CapacityInfoService)

                val status = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                    if(numberOfCharges == pref.getLong(Preferences.NumberOfCharges.prefKey, 0))
                        pref.edit().putLong(Preferences.NumberOfCharges.prefKey, numberOfCharges + 1).apply()

                    val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

                    for(display in displayManager.displays)
                        if(display.state == Display.STATE_ON)
                            delay(if(getCurrentCapacity(this@CapacityInfoService) > 0) 950 else 957)
                    else delay(if(getCurrentCapacity(this@CapacityInfoService) > 0) 920 else 927)

                    if(!isStopService) {

                        seconds++
                        updateNotification(this@CapacityInfoService)
                    }
                }

                else if (status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected && !isFull
                    && getBatteryLevel(this@CapacityInfoService) == 100) {
                    
                    isFull = true

                    numberOfCharges = pref.getLong(Preferences.NumberOfCharges.prefKey, 0)

                    pref.edit().apply {

                        putInt(Preferences.LastChargeTime.prefKey, seconds)
                        putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith)
                        putInt(Preferences.BatteryLevelTo.prefKey, getBatteryLevel(this@CapacityInfoService))

                        if (getCurrentCapacity(this@CapacityInfoService) > 0) {

                            putInt(Preferences.ResidualCapacity.prefKey, (getCurrentCapacity(this@CapacityInfoService) * 1000).toInt())

                            putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat())

                            putInt(Preferences.PercentAdded.prefKey, percentAdded)

                            if(!pref.getBoolean(Preferences.IsSupported.prefKey, true)) putBoolean(Preferences.IsSupported.prefKey, true)
                        }

                        else {

                            if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) putBoolean(Preferences.IsSupported.prefKey, false)
                        }

                        apply()
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

        if (!isFull && seconds > 0) {

            pref.edit().apply {

                if(residualCapacity > 0) putInt(Preferences.ResidualCapacity.prefKey, (residualCapacity * 1000).toInt())

                putInt(Preferences.LastChargeTime.prefKey, seconds)

                putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith)

                putInt(Preferences.BatteryLevelTo.prefKey, getBatteryLevel(this@CapacityInfoService))

                if(capacityAdded > 0) putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat())

                if(percentAdded > 0) putInt(Preferences.PercentAdded.prefKey, percentAdded)

                apply()

            }

            percentAdded = 0

            capacityAdded = 0.0
        }

        batteryLevel = 0

        super.onDestroy()
    }
}