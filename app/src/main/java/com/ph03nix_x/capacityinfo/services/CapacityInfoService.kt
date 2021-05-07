package com.ph03nix_x.capacityinfo.services

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationManager
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DISCHARGE_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_CHARGING_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import kotlinx.coroutines.*
import java.util.*

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var screenTimeJob: Job? = null
    private var jobService: Job? = null
    var chargingCurrentTemp: Int? = null
    var dischargeCurrentTemp: Int? = null
    private var isScreenTimeJob = false
    private var isJob = false
    var isFull = false
    var isStopService = false
    var isSaveNumberOfCharges = true
    var batteryLevelWith = -1
    var seconds = 0
    var screenTime = 0L

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

                    batteryLevelWith = getOnBatteryLevel(this) ?: 0

                    tempBatteryLevelWith = batteryLevelWith

                    tempCurrentCapacity = getOnCurrentCapacity(this)

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN

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
                                ContextCompat.getDrawable(this, it)
                            }
                    }
                }
            }

            applicationContext.registerReceiver(PluggedReceiver(), IntentFilter(
                Intent.ACTION_POWER_CONNECTED))

            applicationContext.registerReceiver(UnpluggedReceiver(), IntentFilter(
                Intent.ACTION_POWER_DISCONNECTED))

            LocaleHelper.setLocale(this, pref.getString(LANGUAGE,
                null) ?: defLang)

            onCreateServiceNotification(this@CapacityInfoService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(pref.getBoolean(PreferencesKeys.IS_AUTO_BACKUP_SETTINGS, resources.getBoolean(
                R.bool.is_auto_backup_settings)) && ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ServiceHelper.jobSchedule(this, AutoBackupSettingsJobService::class.java,
                Constants.AUTO_BACKUP_SETTINGS_JOB_ID, (pref.getString(
                    PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                    ?.toLong() ?: 1L) * 60L * 60L * 1000L)

        else ServiceHelper.cancelJob(this, Constants.AUTO_BACKUP_SETTINGS_JOB_ID)

        if(screenTimeJob == null)
            screenTimeJob = CoroutineScope(Dispatchers.Default).launch {

                isScreenTimeJob = !isScreenTimeJob

                while(isScreenTimeJob && !isStopService) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    if((status == BatteryManager.BATTERY_STATUS_DISCHARGING ||
                                status == BatteryManager.BATTERY_STATUS_NOT_CHARGING)
                        && !isPowerConnected) {

                        val displayManager = getSystemService(Context.DISPLAY_SERVICE)
                                as? DisplayManager

                        if(displayManager != null)
                            for(display in displayManager.displays)
                                if(display.state == Display.STATE_ON) screenTime++
                    }

                    delay(998L)
                }
            }

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

                    if((getOnBatteryLevel(this@CapacityInfoService) ?: 0) < batteryLevelWith)
                        batteryLevelWith = getOnBatteryLevel(this@CapacityInfoService) ?: 0

                    batteryIntent = registerReceiver(null, IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED))

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    val temperature = getOnTemperatureInCelsius(this@CapacityInfoService)

                    if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) &&
                        NotificationInterface.isNotifyOverheatOvercool && (temperature >= pref.getInt(
                            OVERHEAT_DEGREES, resources.getInteger(R.integer
                                .overheat_degrees_default)) || temperature <= pref.getInt(
                            OVERCOOL_DEGREES, resources.getInteger(R.integer
                                .overcool_degrees_default))))
                        withContext(Dispatchers.Main) {

                            onNotifyOverheatOvercool(this@CapacityInfoService, temperature)
                        }

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING
                        && !isStopService) batteryCharging()
                    
                    else if(status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected &&
                        !isFull && !isStopService) batteryCharged()

                    else if(!isStopService) {

                        NotificationInterface.isNotifyBatteryFullyCharged = true
                        NotificationInterface.isNotifyBatteryCharged = true

                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                                R.bool.is_notify_battery_is_discharged)) &&
                            (getOnBatteryLevel(this@CapacityInfoService) ?: 0)
                            <= pref.getInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20)
                            && NotificationInterface.isNotifyBatteryDischarged)
                            withContext(Dispatchers.Main) {

                                onNotifyBatteryDischarged(this@CapacityInfoService)
                            }

                        if(pref.getBoolean(IS_NOTIFY_DISCHARGE_CURRENT, resources.getBoolean(
                                R.bool.is_notify_discharge_current))) {

                            val dischargeCurrent =  getOnChargeDischargeCurrent(
                                this@CapacityInfoService)

                            if(dischargeCurrentTemp == null) dischargeCurrentTemp = dischargeCurrent

                            if(dischargeCurrent < dischargeCurrentTemp ?: -1) {

                                notificationManager?.cancel(NotificationInterface
                                    .NOTIFICATION_DISCHARGE_CURRENT_ID)

                                dischargeCurrentTemp = null
                            }

                            else if(dischargeCurrent >= pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY,
                                    resources.getInteger(R.integer
                                        .discharge_current_notify_level_min)) &&
                                NotificationInterface.isNotifyDischargeCurrent)
                                withContext(Dispatchers.Main) {

                                    onNotifyDischargeCurrent(this@CapacityInfoService,
                                        dischargeCurrent)
                                }
                        }

                            onUpdateServiceNotification(this@CapacityInfoService)

                        if(::wakeLock.isInitialized && wakeLock.isHeld) {

                            try {

                                wakeLock.release()
                            }

                            catch (e: java.lang.RuntimeException) {}
                        }

                        delay(1494L)
                    }
                }
            }

        return START_STICKY
    }

    override fun onDestroy() {

        if(::wakeLock.isInitialized && wakeLock.isHeld) {

            try {

                wakeLock.release()
            }

            catch (e: java.lang.RuntimeException) {}
        }

        instance = null
        isScreenTimeJob = false
        isJob = false
        screenTimeJob?.cancel()
        jobService?.cancel()
        screenTimeJob = null
        jobService = null

        NotificationInterface.isNotifyOverheatOvercool = true
        NotificationInterface.isNotifyBatteryFullyCharged = true
        NotificationInterface.isNotifyBatteryCharged = true
        NotificationInterface.isNotifyBatteryDischarged = true
        NotificationInterface.isChargingCurrent = true
        NotificationInterface.isDischargeCurrent = true

        val batteryLevel = getOnBatteryLevel(this) ?: 0

        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)

        notificationManager?.cancelAll()

        if(!::pref.isInitialized) pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(!isFull && seconds > 1) {

            pref.edit().apply {

                putInt(LAST_CHARGE_TIME, if(seconds >= 60) seconds + ((seconds / 100) * (
                        seconds / 3600)) else seconds)

                putInt(BATTERY_LEVEL_WITH, batteryLevelWith)

                putInt(BATTERY_LEVEL_TO, batteryLevel)

                if(capacityAdded > 0) putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                if(percentAdded > 0) putInt(PERCENT_ADDED, percentAdded)

                if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)

                apply()
            }

            percentAdded = 0

            capacityAdded = 0.0
        }

        if(BatteryInfoInterface.residualCapacity > 0 && isFull) {

            pref.edit().apply {

                if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
                    == "μAh")
                    putInt(RESIDUAL_CAPACITY,
                        (getOnCurrentCapacity(applicationContext) * 1000.0).toInt())
                else putInt(RESIDUAL_CAPACITY,
                    (getOnCurrentCapacity(applicationContext) * 100.0).toInt())

                apply()
            }

            HistoryHelper.autoClearHistory(this)
            HistoryHelper.addHistory(this, DateHelper.getDate(DateHelper.getCurrentDay(),
                DateHelper.getCurrentMonth(), DateHelper.getCurrentYear()), pref.getInt(
                RESIDUAL_CAPACITY, 0))
        }

        BatteryInfoInterface.batteryLevel = 0

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
            (getOnBatteryLevel(this) ?: 0) >= pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED,
                80) && NotificationInterface.isNotifyBatteryCharged)
            withContext(Dispatchers.Main) {

                onNotifyBatteryCharged(this@CapacityInfoService)
            }

        if(pref.getBoolean(IS_NOTIFY_CHARGING_CURRENT, resources.getBoolean(
                R.bool.is_notify_charging_current))) {

            val chargingCurrent =  getOnChargeDischargeCurrent(this)

            if(chargingCurrentTemp == null) chargingCurrentTemp = chargingCurrent

            if(chargingCurrent > chargingCurrentTemp ?: -1) {

                notificationManager?.cancel(NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

                chargingCurrentTemp = null
            }

            else if(chargingCurrent <= pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY, resources
                    .getInteger(R.integer.charging_current_notify_level_min)) &&
                NotificationInterface.isNotifyChargingCurrent && seconds >= 15)
                    withContext(Dispatchers.Main) {

                        onNotifyChargingCurrent(this@CapacityInfoService, chargingCurrent)

                    }
        }

        if(displayManager != null)
        for(display in displayManager.displays)
            if(display.state == Display.STATE_ON)
                delay(if(getOnCurrentCapacity(this@CapacityInfoService) > 0.0) 948L
                else 955L)
            else delay(if(getOnCurrentCapacity(this@CapacityInfoService) > 0.0) 925L
            else 924L)

        seconds++

        try {

            onUpdateServiceNotification(this@CapacityInfoService)
        }
        catch(e: RuntimeException) {

            withContext(Dispatchers.Main) {

                Toast.makeText(this@CapacityInfoService, e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }
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

        val batteryLevel = getOnBatteryLevel(this@CapacityInfoService) ?: 0

        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)

        pref.edit().apply {

            putInt(LAST_CHARGE_TIME, if(seconds >= 60) seconds + ((seconds / 100) * (
                    seconds / 3600)) else seconds)
            putInt(BATTERY_LEVEL_WITH, batteryLevelWith)
            putInt(BATTERY_LEVEL_TO, batteryLevel)

            if(getOnCurrentCapacity(this@CapacityInfoService) > 0.0) {

                if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                    putInt(RESIDUAL_CAPACITY, (getOnCurrentCapacity(
                        this@CapacityInfoService) * 1000.0).toInt())
                else putInt(RESIDUAL_CAPACITY, (getOnCurrentCapacity(
                    this@CapacityInfoService) * 100.0).toInt())

                putFloat(CAPACITY_ADDED, capacityAdded.toFloat())

                putInt(PERCENT_ADDED, percentAdded)
            }

            if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)

            apply()
        }

        isSaveNumberOfCharges = false

        notificationManager?.cancel(NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

        onUpdateServiceNotification(this@CapacityInfoService)
    }
}