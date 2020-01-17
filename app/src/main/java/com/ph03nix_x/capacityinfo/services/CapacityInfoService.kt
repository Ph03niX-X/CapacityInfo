package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.utils.Utils.capacityAdded
import com.ph03nix_x.capacityinfo.utils.Utils.isPowerConnected
import com.ph03nix_x.capacityinfo.utils.Utils.percentAdded
import com.ph03nix_x.capacityinfo.utils.Utils.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.utils.Utils.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import kotlinx.coroutines.*

class CapacityInfoService : Service(),
    NotificationInterface,
    BatteryInfoInterface {

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

        LocaleHelper.setLocale(this, pref.getString(Preferences.Language.prefKey, null) ?: defLang)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        
        instance = this

        numberOfCharges = pref.getLong(Preferences.NumberOfCharges.prefKey, 0)

        createNotification(this@CapacityInfoService)

        isJob = true

        jobService = CoroutineScope(Dispatchers.Default).launch {

            while (isJob) {

                if(!::wakeLock.isInitialized) {

                    if(!::powerManager.isInitialized) powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")
                }

                if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(45 * 1000)

                if(getBatteryLevel(this@CapacityInfoService) < batteryLevelWith) batteryLevelWith = getBatteryLevel(this@CapacityInfoService)

                batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

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

                            if(pref.getString(Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey, "μAh") == "μAh")
                                putInt(Preferences.ResidualCapacity.prefKey, (getCurrentCapacity(this@CapacityInfoService) * 1000).toInt())
                            else putInt(Preferences.ResidualCapacity.prefKey, getCurrentCapacity(this@CapacityInfoService).toInt())

                            putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat())

                            putInt(Preferences.PercentAdded.prefKey, percentAdded)
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
        jobService?.cancel()

        if(!::pref.isInitialized) pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (!isFull && seconds > 0) {

            pref.edit().apply {

                if(residualCapacity > 0) {

                    if(pref.getString(Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey, "μAh") == "μAh")
                        putInt(Preferences.ResidualCapacity.prefKey, (residualCapacity * 1000).toInt())
                    else putInt(Preferences.ResidualCapacity.prefKey, residualCapacity.toInt())
                }

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