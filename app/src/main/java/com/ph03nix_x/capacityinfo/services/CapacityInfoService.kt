package com.ph03nix_x.capacityinfo.services

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.DeadSystemException
import android.os.IBinder
import android.os.PowerManager
import android.view.Display
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeFragment
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.maxChargeCurrent
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryDischarged
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_WAKELOCK_TIMEOUT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FULL_CHARGE_REMINDER_FREQUENCY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLE_WAKELOCK
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_DBG
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NOMINAL_BATTERY_VOLTAGE_PREF
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UPDATE_TEMP_SCREEN_TIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface,
    NavigationInterface {

    private lateinit var pref: SharedPreferences
    private var screenTimeJob: Job? = null
    private var jobService: Job? = null
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isGooglePlay = false
    private var isScreenTimeJob = false
    private var isJob = false
    private var currentCapacity = 0

    var isFull = false
    var isSaveNumberOfCharges = true
    var isPluggedOrUnplugged = false
    var sourceOfPower: Int = -1
    var batteryLevelWith = -1
    var seconds = 0
    var screenTime = 0L
    var secondsFullCharge = 0
    var voltageLastCharge = 0f
    var statusLastCharge = BatteryManager.BATTERY_STATUS_UNKNOWN
    var currentCapacityLastCharge = 0

    companion object {
        var NOMINAL_BATTERY_VOLTAGE = 3.87
        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        if(instance == null) {
            super.onCreate()
            isGooglePlay = isGooglePlay(this)
            if(!isGooglePlay) return
            instance = this
            onCreateServiceNotification(this)
            pref = PreferenceManager.getDefaultSharedPreferences(this@CapacityInfoService)
            NOMINAL_BATTERY_VOLTAGE = pref.getInt(NOMINAL_BATTERY_VOLTAGE_PREF,
                resources.getInteger(R.integer.nominal_battery_voltage_default)).toDouble() / 100.0
            screenTime = if(MainApp.tempScreenTime > 0L) MainApp.tempScreenTime
            else if(MainApp.isUpdateApp) pref.getLong(UPDATE_TEMP_SCREEN_TIME, 0L)
            else screenTime
            MainApp.tempScreenTime = 0L
            MainApp.isUpdateApp = false
            pref.apply {
                if(contains(UPDATE_TEMP_SCREEN_TIME)) edit { remove(UPDATE_TEMP_SCREEN_TIME) }
            }
            batteryIntent = registerReceiver(null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))
            when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB,
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> {
                    isPowerConnected = true
                    sourceOfPower = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) ?: -1
                    batteryLevelWith = getBatteryLevel(this@CapacityInfoService) ?: 0
                    tempBatteryLevelWith = batteryLevelWith
                    tempCurrentCapacity = getCurrentCapacity(this@CapacityInfoService)
                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
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
                                ContextCompat.getDrawable(this@CapacityInfoService, it)
                            }
                    }
                }
            }
            registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))
            registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if(screenTimeJob == null)
            screenTimeJob = CoroutineScope(Dispatchers.Default).launch {

                isScreenTimeJob = !isScreenTimeJob

                while(isScreenTimeJob) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    if((status == BatteryManager.BATTERY_STATUS_DISCHARGING ||
                                status == BatteryManager.BATTERY_STATUS_NOT_CHARGING)
                        && !isPowerConnected) {

                        val displayManager = getSystemService(DISPLAY_SERVICE)
                                as? DisplayManager

                        if(displayManager != null)
                            display@for(display in displayManager.displays)
                                if(display.state == Display.STATE_ON) {
                                    screenTime++
                                    break@display
                                }
                    }

                    delay(1.seconds)
                }
            }

        if(jobService == null)
            jobService = CoroutineScope(Dispatchers.Default).launch {
                isJob = !isJob
                while (isJob) {
                    if(instance == null) instance = this@CapacityInfoService
                    if(!isGooglePlay || (pref.getBoolean(IS_ENABLE_WAKELOCK, resources.getBoolean(
                            R.bool.is_enable_wakelock)) && wakeLock == null && !isFull
                                && isPowerConnected)) {
                        if(powerManager == null) powerManager = getSystemService(POWER_SERVICE) as
                                PowerManager
                        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "${packageName}:service_wakelock")
                        if(wakeLock?.isHeld != true) wakeLock?.acquire(SERVICE_WAKELOCK_TIMEOUT)
                        else if(isGooglePlay && wakeLock?.isHeld == true) wakeLockRelease()
                    }

                    if((getBatteryLevel(this@CapacityInfoService) ?: 0) < batteryLevelWith)
                        batteryLevelWith = getBatteryLevel(this@CapacityInfoService) ?: 0

                    batteryIntent = registerReceiver(null, IntentFilter(
                        Intent.ACTION_BATTERY_CHANGED))

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN)

                    val temperature = getTemperatureInCelsius(this@CapacityInfoService)

                    if(!isPluggedOrUnplugged) {
                        BatteryInfoInterface.apply {
                            maximumTemperature =
                                getMaximumTemperature(this@CapacityInfoService,
                                    maximumTemperature)
                            minimumTemperature =
                                getMinimumTemperature(this@CapacityInfoService,
                                minimumTemperature)
                            averageTemperature =
                                getAverageTemperature(this@CapacityInfoService,
                                maximumTemperature, minimumTemperature)
                        }
                    }

                    if(!::pref.isInitialized) pref = PreferenceManager
                        .getDefaultSharedPreferences(this@CapacityInfoService)

                    if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) && (temperature >= pref
                                .getInt(OVERHEAT_DEGREES, resources.getInteger(R.integer
                                    .overheat_degrees_default)) ||
                                temperature <= pref.getInt(OVERCOOL_DEGREES, resources.getInteger(
                            R.integer.overcool_degrees_default))))
                        withContext(Dispatchers.Main) {
                            onNotifyOverheatOvercool(this@CapacityInfoService, temperature)
                        }

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING && secondsFullCharge < 3600
                        && secondsFullCharge >= 0) batteryCharging()
                    else if(status == BatteryManager.BATTERY_STATUS_CHARGING &&
                        secondsFullCharge >= 3600) batteryCharged()

                    else if(status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected &&
                        !isFull) batteryCharged()

                    else {
                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                                R.bool.is_notify_battery_is_discharged)) && !isBatteryDischarged &&
                            (getBatteryLevel(this@CapacityInfoService) ?: 0) <= pref.getInt(
                                BATTERY_LEVEL_NOTIFY_DISCHARGED, 20))
                            withContext(Dispatchers.Main) {
                                onNotifyBatteryDischarged(this@CapacityInfoService)
                            }
                        withContext(Dispatchers.Main) {
                            updateServiceNotification()
                            delay(1.495.seconds)
                        }
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
        MainApp.isUpdateApp = false
        MainApp.tempScreenTime = screenTime
        wakeLockRelease()
        stopSelf()
        super.onDestroy()
    }

    private fun updateServiceNotification(isBatteryCharged: Boolean = false) =
        try {
            if(isGooglePlay) onUpdateServiceNotification(this) else {}
        }
        catch(_: RuntimeException) {
            ServiceHelper.restartService(this, CapacityInfoService::class.java)
        }
        catch(_: DeadSystemException) {
            ServiceHelper.restartService(this, CapacityInfoService::class.java)
        }
        finally { if(isBatteryCharged) wakeLockRelease() }

    private suspend fun batteryCharging() {
        val batteryLevel = getBatteryLevel(this@CapacityInfoService) ?: 0
        withContext(Dispatchers.IO) {
            if(!pref.getBoolean(IS_FAST_CHARGE, resources.getBoolean(R.bool.is_fast_charge)) &&
                (isTurboCharge(this@CapacityInfoService) || isFastCharge(this@CapacityInfoService)
                        || pref.getBoolean(IS_FAST_CHARGE_DBG, resources.getBoolean(
                    R.bool.is_fast_charge_dbg))))
                pref.edit { putBoolean(IS_FAST_CHARGE, true) }
        }
        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        statusLastCharge = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        currentCapacityLastCharge = (getCurrentCapacity(this@CapacityInfoService) *
                if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                    1000.0 else 100.0).toInt()
        withContext(Dispatchers.Main) {
            val mainActivity = MainActivity.instance
            val chargeDischargeNavigation = mainActivity?.navigation?.menu?.findItem(
                R.id.charge_discharge_navigation)
            mainActivity?.apply {
                if(fragment is ChargeDischargeFragment) toolbar.title = getString(R.string.charge)
            }
            chargeDischargeNavigation?.apply {
                title = getString(R.string.charge)
                icon = ContextCompat.getDrawable(this@CapacityInfoService,
                    mainActivity.getChargeDischargeNavigationIcon(true))
            }
        }
        if(batteryLevel == 100) {
            currentCapacity = (getCurrentCapacity(this@CapacityInfoService) * if(pref.getString(
                    UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                1000.0 else 100.0).toInt()
            if(secondsFullCharge >= 0) secondsFullCharge++
        }
        val displayManager = getSystemService(DISPLAY_SERVICE)
                as? DisplayManager
        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged)) && !isBatteryCharged &&
            (getBatteryLevel(this@CapacityInfoService) ?: 0) >=
            pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED, 80))
            withContext(Dispatchers.Main) {
                onNotifyBatteryCharged(this@CapacityInfoService)
            }
        if(displayManager != null)
            for(display in displayManager.displays)
                if(display.state == Display.STATE_ON) {
                    delay(if(getCurrentCapacity(this@CapacityInfoService) > 0.0)
                        0.95.seconds else 0.956.seconds)
                    seconds++
                }
                else {
                    delay(if(getCurrentCapacity(this@CapacityInfoService) > 0.0)
                        1.936.seconds else 1.933.seconds)
                    seconds += 2
                }
        withContext(Dispatchers.Main) {
            updateServiceNotification()
        }
    }

    private suspend fun batteryCharged() {
        if(!isInstalledFromGooglePlay())
            throw RuntimeException("Application not installed from Google Play")
        secondsFullCharge = -1
        MainActivity.instance?.loadAdsCount = 0
        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        statusLastCharge = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        withContext(Dispatchers.Main) {
            val mainActivity = MainActivity.instance
            val chargeDischargeNavigation = mainActivity?.navigation?.menu?.findItem(
                R.id.charge_discharge_navigation)
            mainActivity?.apply {
                if(fragment is ChargeDischargeFragment)
                    toolbar.title = getString(R.string.discharge)
            }
            chargeDischargeNavigation?.apply {
                title = getString(R.string.discharge)
                icon =  ContextCompat.getDrawable(this@CapacityInfoService,
                    mainActivity.getChargeDischargeNavigationIcon(false))
            }
            val fullChargeReminderFrequency = pref.getString(FULL_CHARGE_REMINDER_FREQUENCY,
                "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}")?.toInt()
            ServiceHelper.jobSchedule(this@CapacityInfoService,
                FullChargeReminderJobService::class.java, IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID,
                fullChargeReminderFrequency?.minutes?.inWholeMilliseconds ?: resources
                    .getInteger(R.integer.full_charge_reminder_frequency_default).minutes
                    .inWholeMilliseconds)
        }
        isFull = true
        if(currentCapacity == 0)
            currentCapacity = (getCurrentCapacity(this@CapacityInfoService) *
                    if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
                        == "μAh") 1000.0 else 100.0).toInt()
        currentCapacityLastCharge = currentCapacity
        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
            R.integer.min_design_capacity)).toDouble() * if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0
        val residualCapacity =
            if(pref.getBoolean(IS_FAST_CHARGE, resources.getBoolean(R.bool.is_fast_charge)))
                    (currentCapacity.toDouble() +
                            ((NOMINAL_BATTERY_VOLTAGE / 100.0) * designCapacity)).toInt()
            else currentCapacity
        val residualCapacityAverage = if(isInstalledFromGooglePlay())
            getResidualCapacityAverage(this, residualCapacity) else
                residualCapacity * (2..10).random()
        val currentDate = DateHelper.getDate(DateHelper.getCurrentDay(),
            DateHelper.getCurrentMonth(), DateHelper.getCurrentYear())
        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_fully_charged)))
            withContext(Dispatchers.Main) {
                onNotifyBatteryFullyCharged(this@CapacityInfoService)
            }
        val batteryLevel = getBatteryLevel(this@CapacityInfoService) ?: 0
        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (batteryLevelWith / 100f)
        voltageLastCharge = getVoltage(this@CapacityInfoService).toFloat()
        pref.edit().apply {
            val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)
            if(seconds > 1) putLong(NUMBER_OF_CHARGES, numberOfCharges + 1).apply()
            putInt(RESIDUAL_CAPACITY, residualCapacityAverage)
            putLong(NUMBER_OF_FULL_CHARGES, pref.getLong(NUMBER_OF_FULL_CHARGES, 0) + 1)
            putFloat(CAPACITY_ADDED, capacityAdded.toFloat())
            putInt(PERCENT_ADDED, percentAdded)
            if(isSaveNumberOfCharges) putFloat(NUMBER_OF_CYCLES, numberOfCycles)
            putInt(PreferencesKeys.BATTERY_LEVEL_LAST_CHARGE, batteryLevel)
            putInt(PreferencesKeys.CHARGING_TIME_LAST_CHARGE, seconds)
            putFloat(PreferencesKeys.CAPACITY_ADDED_LAST_CHARGE, capacityAdded.toFloat())
            putInt(PreferencesKeys.PERCENT_ADDED_LAST_CHARGE, percentAdded)
            putInt(PreferencesKeys.CURRENT_CAPACITY_LAST_CHARGE, currentCapacityLastCharge)
            putString(PreferencesKeys.STATUS_LAST_CHARGE,
                getStatus(this@CapacityInfoService, statusLastCharge))
            putString(PreferencesKeys.SOURCE_OF_POWER_LAST_CHARGE,
                getSourceOfPowerLastCharge(this@CapacityInfoService, sourceOfPower))
            putBoolean(PreferencesKeys.IS_FAST_CHARGE_LAST_CHARGE,
                isFastCharge(this@CapacityInfoService))
            if(isFastCharge(this@CapacityInfoService))
                putFloat(PreferencesKeys.FAST_CHARGE_WATTS_LAST_CHARGE,
                    getFastChargeWattLastCharge().toFloat())
            putInt(PreferencesKeys.MAX_CHARGE_LAST_CHARGE, maxChargeCurrent)
            putInt(PreferencesKeys.AVERAGE_CHARGE_LAST_CHARGE,
                BatteryInfoInterface.averageChargeCurrent)
            putInt(PreferencesKeys.MIN_CHARGE_LAST_CHARGE, BatteryInfoInterface.minChargeCurrent)
            putFloat(PreferencesKeys.MAX_TEMP_CELSIUS_LAST_CHARGE,
                BatteryInfoInterface.maximumTemperature.toFloat())
            putFloat(PreferencesKeys.MAX_TEMP_FAHRENHEIT_LAST_CHARGE,
                getTemperatureInFahrenheit(BatteryInfoInterface.maximumTemperature).toFloat())
            putFloat(PreferencesKeys.AVERAGE_TEMP_CELSIUS_LAST_CHARGE,
                BatteryInfoInterface.averageTemperature.toFloat())
            putFloat(PreferencesKeys.AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE,
                getTemperatureInFahrenheit(BatteryInfoInterface.averageTemperature).toFloat())
            putFloat(PreferencesKeys.MIN_TEMP_CELSIUS_LAST_CHARGE,
                BatteryInfoInterface.minimumTemperature.toFloat())
            putFloat(PreferencesKeys.MIN_TEMP_FAHRENHEIT_LAST_CHARGE,
                getTemperatureInFahrenheit(BatteryInfoInterface.minimumTemperature).toFloat())
            putFloat(PreferencesKeys.VOLTAGE_LAST_CHARGE, if(voltageLastCharge > 0f) voltageLastCharge
            else getVoltage(this@CapacityInfoService).toFloat())
            putInt(LAST_CHARGE_TIME, seconds)
            putInt(BATTERY_LEVEL_WITH, batteryLevelWith)
            putInt(BATTERY_LEVEL_TO, batteryLevel)
            apply()
        }
        withContext(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                if(residualCapacityAverage > 0)
                    HistoryHelper.addHistory(this@CapacityInfoService, currentDate,
                        residualCapacityAverage)
            }
            if(PremiumInterface.isPremium) {
                if(HistoryHelper.isHistoryNotEmpty(this@CapacityInfoService)) {
                    val historyFragment = HistoryFragment.instance
                    historyFragment?.binding?.apply {
                        refreshEmptyHistory.isVisible = false
                        emptyHistoryLayout.isVisible = false
                        historyRecyclerView.isVisible = true
                        refreshHistory.isVisible = true
                    }
                    MainActivity.instance?.toolbar?.menu?.apply {
                        findItem(R.id.history_premium)?.isVisible = false
                        findItem(R.id.clear_history)?.isVisible = true
                    }
                    if(HistoryHelper.getHistoryCount(this@CapacityInfoService) == 1L) {
                        val historyDB = withContext(Dispatchers.IO) {
                            HistoryDB(this@CapacityInfoService)
                        }
                        historyFragment?.apply {
                            historyAdapter = HistoryAdapter(withContext(Dispatchers.IO) {
                                historyDB.readDB()
                            })
                            binding?.apply {
                                historyRecyclerView.setItemViewCacheSize(historyAdapter.itemCount)
                                historyRecyclerView.adapter = historyAdapter
                            }
                        }
                    }
                    else HistoryAdapter.instance?.update(this@CapacityInfoService)
                }
                else {
                    HistoryFragment.instance?.binding?.apply {
                        historyRecyclerView.isVisible = false
                        refreshHistory.isVisible = false
                        emptyHistoryLayout.isVisible = true
                        refreshEmptyHistory.isVisible = true
                        emptyHistoryText.text = resources.getText(R.string.empty_history_text)
                    }
                    MainActivity.instance?.toolbar?.menu?.apply {
                        findItem(R.id.history_premium)?.isVisible = false
                        findItem(R.id.clear_history)?.isVisible = false
                    }
                }
                return@withContext
            }
            else {
                HistoryFragment.instance?.binding?.apply {
                    historyRecyclerView.isVisible = false
                    refreshHistory.isVisible = false
                    emptyHistoryLayout.isVisible = true
                    refreshEmptyHistory.isVisible = true
                    emptyHistoryText.text = resources.getText(R.string.required_to_access_premium_feature)
                }
                MainActivity.instance?.toolbar?.menu?.apply {
                    findItem(R.id.history_premium)?.isVisible = true
                    findItem(R.id.clear_history)?.isVisible = false
                }
            }
        }
        isSaveNumberOfCharges = false
        withContext(Dispatchers.Main) {
            LastChargeFragment.instance?.lastCharge()
            updateServiceNotification(isBatteryCharged = true)
        }
    }
    
    fun wakeLockRelease() {
        try {
            if(wakeLock?.isHeld == true) wakeLock?.release()
        }
        catch (_: RuntimeException) {}
    }

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay() =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == packageManager.getInstallSourceInfo(packageName)
                .installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == packageManager.getInstallerPackageName(packageName)
}