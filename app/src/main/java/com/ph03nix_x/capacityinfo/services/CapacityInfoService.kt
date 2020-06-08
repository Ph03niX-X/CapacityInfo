package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.batteryLevel
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.residualCapacity
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.utils.Utils.capacityAdded
import com.ph03nix_x.capacityinfo.utils.Utils.isPowerConnected
import com.ph03nix_x.capacityinfo.utils.Utils.percentAdded
import com.ph03nix_x.capacityinfo.utils.Utils.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.utils.Utils.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationManager
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import kotlinx.coroutines.*

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var jobService: Job? = null
    private var isJob = false
    var isFull = false
    var batteryLevelWith = -1
    var seconds = 0
    var isStopService = false
    var isSaveNumberOfCharges = true

    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {

        if(instance == null) {

            super.onCreate()

            instance = this

            pref = PreferenceManager.getDefaultSharedPreferences(this)

            batteryIntent = registerReceiver(null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))

            val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)

            when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB,
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    isPowerConnected = true

                    batteryLevelWith = onGetBatteryLevel(this) ?: 0

                    tempBatteryLevelWith = batteryLevelWith

                    tempCurrentCapacity = onGetCurrentCapacity(this)

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING) pref.edit().putLong(
                        NUMBER_OF_CHARGES, numberOfCharges + 1).apply()

                    if(MainActivity.instance?.fragment != null) {

                        if(MainActivity.instance?.fragment is ChargeDischargeFragment)
                            MainActivity.instance?.toolbar?.title = getString(if(status ==
                                BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                                R.string.discharge)

                        val chargeDischargeNavigation = MainActivity.instance?.navigation
                            ?.menu?.findItem(R.id.charge_discharge_navigation)

                        chargeDischargeNavigation?.title = getString(if(status == BatteryManager
                                .BATTERY_STATUS_CHARGING) R.string.charge else R.string.discharge)

                        chargeDischargeNavigation?.icon = MainActivity.instance
                            ?.getChargeDischargeNavigationIcon(status == BatteryManager
                                .BATTERY_STATUS_CHARGING)?.let {
                                getDrawable(it)
                            }
                    }
                }
            }

            registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))

            registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

            LocaleHelper.setLocale(this, pref.getString(LANGUAGE,
                null) ?: defLang)

            onCreateServiceNotification(this@CapacityInfoService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(jobService == null)
            jobService = CoroutineScope(Dispatchers.Default).launch {

                isJob = !isJob

                while (isJob && !isStopService) {

                    if(!::wakeLock.isInitialized) {

                        if(!::powerManager.isInitialized) powerManager = getSystemService(Context
                            .POWER_SERVICE) as PowerManager

                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "${packageName}:service_wakelock")
                    }

                    if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(
                        45 * 1000)

                    if((onGetBatteryLevel(this@CapacityInfoService) ?: 0) < batteryLevelWith)
                        batteryLevelWith = onGetBatteryLevel(this@CapacityInfoService) ?: 0

                    batteryIntent = registerReceiver(null, IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED))

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    val temperature = onGetTemperatureInDouble(
                        this@CapacityInfoService)

                    if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) &&
                        NotificationInterface.isNotifyOverheatOvercool && (temperature >= 45.0
                                || temperature <= 0))
                        withContext(Dispatchers.Main) {

                            onNotifyOverheatOvercool(this@CapacityInfoService, temperature)
                        }

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING
                        && !isStopService) batteryCharging()
                    
                    else if(status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected &&
                        !isFull
                        && onGetBatteryLevel(this@CapacityInfoService) == 100
                        && !isStopService) batteryCharged()

                    else if(!isStopService) {

                        NotificationInterface.isNotifyBatteryFullyCharged = true
                        NotificationInterface.isNotifyBatteryCharged = true

                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                                R.bool.is_notify_battery_is_discharged)) &&
                            (onGetBatteryLevel(this@CapacityInfoService) ?: 0)
                            <= pref.getInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20)
                            && NotificationInterface.isNotifyBatteryDischarged)
                            withContext(Dispatchers.Main) {

                                onNotifyBatteryDischarged(this@CapacityInfoService)
                            }

                            onUpdateServiceNotification(this@CapacityInfoService)

                            if(::wakeLock.isInitialized && wakeLock.isHeld) wakeLock.release()

                            delay(2998L)
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
        jobService = null

        NotificationInterface.isNotifyOverheatOvercool = true
        NotificationInterface.isNotifyBatteryFullyCharged = true
        NotificationInterface.isNotifyBatteryCharged = true
        NotificationInterface.isNotifyBatteryDischarged = true

        val numberOfCycles = pref.getFloat(NUMBER_OF_CYCLES, 0f) + (
                (onGetBatteryLevel(this) ?: 0) / 100f) - (batteryLevelWith / 100f)

        notificationManager?.cancelAll()

        if(!::pref.isInitialized) pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(!isFull && seconds > 0) {

            pref.edit().apply {

                if(residualCapacity > 0) {

                    if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
                        == "μAh") putInt(RESIDUAL_CAPACITY, (residualCapacity * 1000).toInt())
                    else putInt(RESIDUAL_CAPACITY, residualCapacity.toInt())
                }

                putInt(LAST_CHARGE_TIME, if(seconds >= 60) seconds + ((seconds / 100) * (
                        seconds / 3600)) else seconds)

                putInt(BATTERY_LEVEL_WITH, batteryLevelWith)

                putInt(BATTERY_LEVEL_TO, (onGetBatteryLevel(this@CapacityInfoService) ?: 0))

                if(capacityAdded > 0) putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                if(percentAdded > 0) putInt(PERCENT_ADDED, percentAdded)

                if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)

                apply()
            }

            percentAdded = 0

            capacityAdded = 0.0
        }

        batteryLevel = 0

        if(isStopService)
            Toast.makeText(this, R.string.service_stopped_successfully,
                Toast.LENGTH_LONG).show()

        super.onDestroy()
    }
    
    private suspend fun batteryCharging() {

        NotificationInterface.isNotifyBatteryDischarged = true

        val displayManager = getSystemService(Context.DISPLAY_SERVICE)
                as? DisplayManager

        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged)) &&
            (onGetBatteryLevel(this) ?: 0) >= pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED,
                80) && NotificationInterface.isNotifyBatteryCharged)
            withContext(Dispatchers.Main) {

                onNotifyBatteryCharged(this@CapacityInfoService)
            }

        if(displayManager != null)
        for(display in displayManager.displays)
            if(display.state == Display.STATE_ON)
                delay(if(onGetCurrentCapacity(this@CapacityInfoService) > 0) 949L
                else 956L)
            else delay(if(onGetCurrentCapacity(this@CapacityInfoService) > 0) 926L
            else 926L)

        seconds++

        try {

            onUpdateServiceNotification(this@CapacityInfoService)
        }
        catch(e: RuntimeException) {

            Toast.makeText(this@CapacityInfoService, e.message ?: e.toString(),
            Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun batteryCharged() {

        isFull = true

        NotificationInterface.isNotifyBatteryDischarged = true

        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_fully_charged)) &&
            NotificationInterface.isNotifyBatteryFullyCharged)
            withContext(Dispatchers.Main) {

                onNotifyBatteryFullyCharged(this@CapacityInfoService)
            }

        val numberOfCycles = pref.getFloat(NUMBER_OF_CYCLES, 0f) + (
                (onGetBatteryLevel(this@CapacityInfoService) ?: 0) / 100f) - (
                batteryLevelWith / 100f)

        pref.edit().apply {

            putInt(LAST_CHARGE_TIME, if(seconds >= 60) seconds + ((seconds / 100) * (
                    seconds / 3600)) else seconds)
            putInt(BATTERY_LEVEL_WITH, batteryLevelWith)
            putInt(BATTERY_LEVEL_TO, (onGetBatteryLevel(this@CapacityInfoService) ?: 0))

            if(onGetCurrentCapacity(this@CapacityInfoService) > 0) {

                if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                    putInt(RESIDUAL_CAPACITY, (onGetCurrentCapacity(
                        this@CapacityInfoService) * 1000).toInt())
                else putInt(RESIDUAL_CAPACITY, onGetCurrentCapacity(
                    this@CapacityInfoService).toInt())

                putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                putInt(PERCENT_ADDED, percentAdded)
            }

            if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)

            apply()
        }

        isSaveNumberOfCharges = false

        try {

            onUpdateServiceNotification(this@CapacityInfoService)
        }
        catch(e: RuntimeException) {

            Toast.makeText(this@CapacityInfoService, e.message ?: e.toString(),
                Toast.LENGTH_LONG).show()
        }
    }
}