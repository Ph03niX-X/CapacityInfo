package com.ph03nix_x.capacityinfo.services

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper.setLocale
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.maxChargeCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryChargedVoltage
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryDischarged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryDischargedVoltage
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryFullyCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isChargingCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isDischargeCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyBatteryCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyBatteryChargedVoltage
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyBatteryDischarged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyBatteryDischargedVoltage
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyBatteryFullyCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyChargingCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyDischargeCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isNotifyOverheatOvercool
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isOverheatOvercool
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationBuilder
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationManager
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.FAST_CHARGE_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DISCHARGE_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_DEBUG
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_CHARGING_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import kotlinx.coroutines.*

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface {

    private lateinit var pref: SharedPreferences
    private var screenTimeJob: Job? = null
    private var jobService: Job? = null
    private var chargingCurrentTemp: Int? = null
    private var dischargeCurrentTemp: Int? = null
    private var isScreenTimeJob = false
    private var isJob = false
    private var secondsTemperature = 0
    private var currentCapacity = 0

    var isFull = false
    var isStopService = false
    var isSaveNumberOfCharges = true
    var isPluggedOrUnplugged = false
    var batteryLevelWith = -1
    var seconds = 0
    var screenTime = 0L
    var secondsFullCharge = 0

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

                    pref.edit().putLong(NUMBER_OF_CHARGES, numberOfCharges + 1).apply()

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

            onCreateServiceNotification(this@CapacityInfoService)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        pref = PreferenceManager.getDefaultSharedPreferences(newBase)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            super.attachBaseContext(ContextWrapper(newBase.setLocale(
                pref.getString(LANGUAGE, null) ?: defLang)))
        else super.attachBaseContext(newBase)
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

                    if((getOnBatteryLevel(this@CapacityInfoService) ?: 0) < batteryLevelWith)
                        batteryLevelWith = getOnBatteryLevel(this@CapacityInfoService) ?: 0

                    batteryIntent = registerReceiver(null, IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED))

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    val temperature = getOnTemperatureInCelsius(this@CapacityInfoService)

                    if(!isPluggedOrUnplugged) {

                        BatteryInfoInterface.maximumTemperature =
                            getOnMaximumTemperature(this@CapacityInfoService,
                                BatteryInfoInterface.maximumTemperature)

                        BatteryInfoInterface.minimumTemperature =
                            getOnMinimumTemperature(this@CapacityInfoService,
                                BatteryInfoInterface.minimumTemperature)

                        BatteryInfoInterface.averageTemperature = getOnAverageTemperature(
                            this@CapacityInfoService, BatteryInfoInterface.maximumTemperature,
                            BatteryInfoInterface.minimumTemperature)
                    }

                    if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) && isNotifyOverheatOvercool
                        && (temperature >= pref.getInt(OVERHEAT_DEGREES, resources.getInteger(R.integer
                            .overheat_degrees_default)) ||
                                temperature <= pref.getInt(OVERCOOL_DEGREES, resources.getInteger(
                            R.integer.overcool_degrees_default))))
                        withContext(Dispatchers.Main) {
                            secondsTemperature = 0
                            onNotifyOverheatOvercool(this@CapacityInfoService, temperature)
                        }

                    else if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) && !isNotifyOverheatOvercool) {
                        if(secondsTemperature >= 600) isNotifyOverheatOvercool = true
                        else secondsTemperature++
                    }

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING
                        && !isStopService && secondsFullCharge < 3600) batteryCharging()

                    else if(status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected &&
                        !isFull && !isStopService) batteryCharged()

                    else if(!isStopService) {
                        isNotifyBatteryFullyCharged = true
                        isNotifyBatteryCharged = true
                        isNotifyBatteryChargedVoltage = true

                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                                R.bool.is_notify_battery_is_discharged)) && (getOnBatteryLevel(
                                this@CapacityInfoService) ?: 0) <= pref.getInt(
                                BATTERY_LEVEL_NOTIFY_DISCHARGED, 20)
                            && isNotifyBatteryDischarged)
                            withContext(Dispatchers.Main) {

                                onNotifyBatteryDischarged(this@CapacityInfoService)
                            }

                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE,
                                resources.getBoolean(R.bool.is_notify_battery_is_charged_voltage))
                            && isNotifyBatteryDischargedVoltage) {

                            val voltage = getOnVoltage(this@CapacityInfoService)

                            if(voltage <= pref.getInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources
                                    .getInteger(R.integer.battery_notify_discharged_voltage_min)))
                                withContext(Dispatchers.Main) {
                                    onNotifyBatteryDischargedVoltage(
                                        this@CapacityInfoService, voltage.toInt())
                                }
                        }

                        if(pref.getBoolean(IS_NOTIFY_DISCHARGE_CURRENT, resources.getBoolean(
                                R.bool.is_notify_discharge_current))) {

                            val dischargeCurrent = getOnChargeDischargeCurrent(
                                this@CapacityInfoService)

                            if(dischargeCurrentTemp == null) dischargeCurrentTemp = dischargeCurrent

                            if(dischargeCurrent < (dischargeCurrentTemp ?: -1)) {

                                notificationManager?.cancel(NotificationInterface
                                    .NOTIFICATION_DISCHARGE_CURRENT_ID)

                                dischargeCurrentTemp = null
                            }

                            else if(dischargeCurrent >= pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY,
                                    resources.getInteger(R.integer
                                        .discharge_current_notify_level_min)) &&
                                isNotifyDischargeCurrent)
                                withContext(Dispatchers.Main) {

                                    onNotifyDischargeCurrent(this@CapacityInfoService,
                                        dischargeCurrent)
                                }
                        }

                        withContext(Dispatchers.Main) {
                            onUpdateServiceNotification(this@CapacityInfoService)
                        }

                        delay(1496L)
                    }
                }
            }

        return START_STICKY
    }

    override fun onDestroy() {

        instance = null
        isScreenTimeJob = false
        isJob = false
        screenTimeJob?.cancel()
        jobService?.cancel()
        screenTimeJob = null
        jobService = null
        notificationBuilder = null

        isNotifyOverheatOvercool = true
        isNotifyBatteryFullyCharged = true
        isNotifyBatteryCharged = true
        isNotifyBatteryChargedVoltage = true
        isNotifyBatteryDischarged = true
        isNotifyBatteryDischargedVoltage = true
        isNotifyChargingCurrent = true
        isNotifyDischargeCurrent = true
        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val batteryLevel = getOnBatteryLevel(this) ?: 0

        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)

        notificationManager?.cancelAll()

        if(!::pref.isInitialized) pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(!isFull && seconds > 1) {

            pref.edit().apply {

                putInt(LAST_CHARGE_TIME, seconds)

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

            HistoryHelper.removeFirstRow(this)
            HistoryHelper.addHistory(this, DateHelper.getDate(DateHelper.getCurrentDay(),
                DateHelper.getCurrentMonth(), DateHelper.getCurrentYear()), pref.getInt(
                RESIDUAL_CAPACITY, 0))
        }

        BatteryInfoInterface.batteryLevel = 0
        BatteryInfoInterface.tempBatteryLevel = 0

        if(isStopService)
            Toast.makeText(this, R.string.service_stopped_successfully,
                Toast.LENGTH_LONG).show()

        super.onDestroy()
    }

    private suspend fun batteryCharging() {

        isNotifyBatteryDischarged = true
        isNotifyBatteryDischargedVoltage = true

        val batteryLevel = getOnBatteryLevel(this@CapacityInfoService) ?: 0

        if(batteryLevel == 100) {
            if(secondsFullCharge >= 3600) batteryCharged()
            currentCapacity = (getOnCurrentCapacity(this) * if(pref.getString(
                    UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                1000.0 else 100.0).toInt()
            secondsFullCharge++
        }

        val displayManager = getSystemService(Context.DISPLAY_SERVICE)
                as? DisplayManager

        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged)) &&
            (getOnBatteryLevel(this) ?: 0) >= pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED,
                80) && isNotifyBatteryCharged)
            withContext(Dispatchers.Main) {

                onNotifyBatteryCharged(this@CapacityInfoService)
            }

        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE, resources.getBoolean(R.bool
                .is_notify_battery_is_charged_voltage)) && isNotifyBatteryChargedVoltage
            && seconds >= 15) {

            val voltage = getOnVoltage(this)

            if(voltage >= pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(
                    R.integer.battery_notify_charged_voltage_min)))
                withContext(Dispatchers.Main) {
                    onNotifyBatteryChargedVoltage(this@CapacityInfoService, voltage.toInt())
                }
        }


        if(pref.getBoolean(IS_NOTIFY_CHARGING_CURRENT, resources.getBoolean(
                R.bool.is_notify_charging_current))) {

            val chargingCurrent =  getOnChargeDischargeCurrent(this)

            if(chargingCurrentTemp == null) chargingCurrentTemp = chargingCurrent

            if(chargingCurrent > (chargingCurrentTemp ?: -1)) {

                notificationManager?.cancel(NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

                chargingCurrentTemp = null
            }

            else if(chargingCurrent <= pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY, resources
                    .getInteger(R.integer.charging_current_notify_level_min)) &&
                isNotifyChargingCurrent && seconds >= 15)
                    withContext(Dispatchers.Main) {

                        onNotifyChargingCurrent(this@CapacityInfoService, chargingCurrent)

                    }
        }

        if(displayManager != null)
            for(display in displayManager.displays)
                if(display.state == Display.STATE_ON)
                    delay(if(getOnCurrentCapacity(this@CapacityInfoService) > 0.0) 949L
                    else 955L)
                else delay(if(getOnCurrentCapacity(this@CapacityInfoService) > 0.0) 938L
                else 935L)

        seconds++

        try {

            withContext(Dispatchers.Main) {
                onUpdateServiceNotification(this@CapacityInfoService)
            }
        }

        catch(_: RuntimeException) {}
    }

    private suspend fun batteryCharged() {

        isFull = true

        isNotifyBatteryDischarged = true
        isNotifyBatteryDischargedVoltage = true
        if(currentCapacity == 0)
            currentCapacity = (getOnCurrentCapacity(this) * if(pref.getString(
                    UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
            else 100.0).toInt()

        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
            R.integer.min_design_capacity)).toDouble() * if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0

        val residualCapacityCurrent = pref.getInt(RESIDUAL_CAPACITY, 0) / 1000

        val residualCapacity =
            if(residualCapacityCurrent in 1..maxChargeCurrent ||
                maxChargeCurrent >= pref.getInt(DESIGN_CAPACITY,
                    resources.getInteger(R.integer.min_design_capacity) - 250) ||
                pref.getBoolean(IS_FAST_CHARGE_DEBUG, resources.getBoolean(
                    R.bool.is_fast_charge_debug)))
                    (currentCapacity + ((FAST_CHARGE_VOLTAGE / 100.0) * designCapacity)).toInt()
            else currentCapacity

        val currentDate = DateHelper.getDate(DateHelper.getCurrentDay(),
            DateHelper.getCurrentMonth(), DateHelper.getCurrentYear())

        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_fully_charged)) && isNotifyBatteryFullyCharged)
            withContext(Dispatchers.Main) {

                onNotifyBatteryFullyCharged(this@CapacityInfoService)
            }

        val batteryLevel = getOnBatteryLevel(this@CapacityInfoService) ?: 0

        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)

        pref.edit().apply {

            putInt(LAST_CHARGE_TIME, seconds)
            putInt(RESIDUAL_CAPACITY, residualCapacity)
            putInt(BATTERY_LEVEL_WITH, batteryLevelWith)
            putInt(BATTERY_LEVEL_TO, batteryLevel)
            putLong(NUMBER_OF_FULL_CHARGES, pref.getLong(NUMBER_OF_FULL_CHARGES, 0) + 1)
            putFloat(CAPACITY_ADDED, capacityAdded.toFloat())
            putInt(PERCENT_ADDED, percentAdded)

            if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)

            apply()
        }

        withContext(Dispatchers.Main) {
            if(residualCapacity > 0 && seconds >= 10) {
                HistoryHelper.removeFirstRow(this@CapacityInfoService)
                HistoryHelper.addHistory(this@CapacityInfoService, currentDate,
                    residualCapacity)
                if(HistoryHelper.isHistoryNotEmpty(this@CapacityInfoService)) {
                    HistoryFragment.instance?.binding?.emptyHistoryLayout?.visibility = View.GONE
                    HistoryFragment.instance?.binding?.historyRecyclerView?.visibility =
                        View.VISIBLE
                    HistoryAdapter.instance?.update(this@CapacityInfoService)
                }
                else {
                    HistoryFragment.instance?.binding?.historyRecyclerView?.visibility = View.GONE
                    HistoryFragment.instance?.binding?.emptyHistoryLayout?.visibility = View.VISIBLE
                }
            }
        }

        isSaveNumberOfCharges = false

        notificationManager?.cancel(NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

        withContext(Dispatchers.Main) {
            onUpdateServiceNotification(this@CapacityInfoService)
        }
    }
}