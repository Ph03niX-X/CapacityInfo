package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.hardware.display.DisplayManager
import android.os.*
import android.view.Display
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.maxChargeCurrent
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryDischarged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isBatteryFullyCharged
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.isOverheatOvercool
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationBuilder
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface.Companion.notificationManager
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver
import com.ph03nix_x.capacityinfo.utilities.Constants.CHECK_PREMIUM_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.NOMINAL_BATTERY_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_WAKELOCK_TIMEOUT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FULL_CHARGE_REMINDER_FREQUENCY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_SETTING
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UPDATE_TEMP_SCREEN_TIME
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CapacityInfoService : Service(), NotificationInterface, BatteryInfoInterface,
    NavigationInterface {

    private lateinit var pref: SharedPreferences
    private var screenTimeJob: Job? = null
    private var jobService: Job? = null
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isScreenTimeJob = false
    private var isJob = false
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

            pref = PreferenceManager.getDefaultSharedPreferences(this@CapacityInfoService)

            screenTime = if(MainApp.tempScreenTime > 0L) MainApp.tempScreenTime
            else if(MainApp.isUpdateApp) pref.getLong(UPDATE_TEMP_SCREEN_TIME, 0L)
            else screenTime

            MainApp.tempScreenTime = 0L
            MainApp.isUpdateApp = false

            pref.apply {
                if(contains(UPDATE_TEMP_SCREEN_TIME)) edit().remove(UPDATE_TEMP_SCREEN_TIME).apply()
            }

            batteryIntent = registerReceiver(null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))

            when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB,
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    isPowerConnected = true

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

            try {
                onCreateServiceNotification(applicationContext)
            }
            catch (e: NullPointerException) {
                onCreateServiceNotification(this@CapacityInfoService)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

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

                while (isJob && !isStopService) {

                    if(instance == null) instance = this@CapacityInfoService

                    if(wakeLock == null) {

                        if(powerManager == null) powerManager = getSystemService(Context
                            .POWER_SERVICE) as PowerManager

                        wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                            "${packageName}:service_wakelock")
                    }

                    if(wakeLock?.isHeld != true && !isFull && isPowerConnected)
                        wakeLock?.acquire(SERVICE_WAKELOCK_TIMEOUT)

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

                    if(pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                            R.bool.is_notify_overheat_overcool)) && (temperature >= pref
                                .getInt(OVERHEAT_DEGREES, resources.getInteger(R.integer
                                    .overheat_degrees_default)) ||
                                temperature <= pref.getInt(OVERCOOL_DEGREES, resources.getInteger(
                            R.integer.overcool_degrees_default))))
                        withContext(Dispatchers.Main) {
                            onNotifyOverheatOvercool(this@CapacityInfoService, temperature)
                        }

                    if(status == BatteryManager.BATTERY_STATUS_CHARGING
                        && !isStopService && secondsFullCharge < 3600) batteryCharging()

                    else if(status == BatteryManager.BATTERY_STATUS_FULL && isPowerConnected &&
                        !isFull && !isStopService) batteryCharged()

                    else if(!isStopService) {

                        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                                R.bool.is_notify_battery_is_discharged)) && (getBatteryLevel(
                                this@CapacityInfoService) ?: 0) <= pref.getInt(
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
        notificationBuilder = null
        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryDischarged = false

        MainApp.isUpdateApp = false

        val batteryLevel = getBatteryLevel(this@CapacityInfoService) ?: 0

        if(!isStopService) MainApp.tempScreenTime = screenTime

        val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)

        notificationManager?.cancelAll()

        if(!::pref.isInitialized)
            pref = PreferenceManager.getDefaultSharedPreferences(this@CapacityInfoService)

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
                        (getCurrentCapacity(this@CapacityInfoService) * 1000.0).toInt())
                else putInt(RESIDUAL_CAPACITY,
                    (getCurrentCapacity(this@CapacityInfoService) * 100.0).toInt())

                apply()
            }

            HistoryHelper.addHistory(this@CapacityInfoService,
                DateHelper.getDate(DateHelper.getCurrentDay(), DateHelper.getCurrentMonth(),
                    DateHelper.getCurrentYear()), pref.getInt(RESIDUAL_CAPACITY, 0))
        }
        BatteryInfoInterface.apply {
            this.batteryLevel = 0
            tempBatteryLevel = 0
        }
        if(isStopService)
            Toast.makeText(this@CapacityInfoService, R.string.service_stopped_successfully,
                Toast.LENGTH_LONG).show()
        ServiceHelper.apply {
            cancelJob(this@CapacityInfoService, IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)
            cancelJob(this@CapacityInfoService, CHECK_PREMIUM_JOB_ID)
        }
        wakeLockRelease()
        super.onDestroy()
    }

    private fun updateServiceNotification(isBatteryCharged: Boolean = false) =
        try { onUpdateServiceNotification(applicationContext) }
        catch(_: RemoteException) {}
        catch(_: NullPointerException) { onUpdateServiceNotification(this) }
        finally { if(isBatteryCharged) wakeLockRelease() }

    private suspend fun batteryCharging() {
        val batteryLevel = getBatteryLevel(this@CapacityInfoService) ?: 0
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
            if(secondsFullCharge >= 3600) batteryCharged()
            currentCapacity = (getCurrentCapacity(this@CapacityInfoService) * if(pref.getString(
                    UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                1000.0 else 100.0).toInt()
            secondsFullCharge++
        }
        val displayManager = getSystemService(Context.DISPLAY_SERVICE)
                as? DisplayManager
        if(pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged)) &&
            (getBatteryLevel(this@CapacityInfoService) ?: 0) ==
            pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED, 80))
            withContext(Dispatchers.Main) {

                onNotifyBatteryCharged(this@CapacityInfoService)
            }
        if(displayManager != null)
            for(display in displayManager.displays)
                if(display.state == Display.STATE_ON)
                    delay(if(getCurrentCapacity(this@CapacityInfoService) > 0.0)
                        0.948.seconds else 0.954.seconds)
                else delay(if(getCurrentCapacity(this@CapacityInfoService) > 0.0)
                    0.937.seconds else 0.934.seconds)
        seconds++
        withContext(Dispatchers.Main) {
            updateServiceNotification()
        }
    }

    private suspend fun batteryCharged() {
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
            currentCapacity = (getCurrentCapacity(this@CapacityInfoService) * if(pref.getString(
                    UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
            else 100.0).toInt()
        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
            R.integer.min_design_capacity)).toDouble() * if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0
        val residualCapacityCurrent = if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                "μAh") == "μAh") pref.getInt(RESIDUAL_CAPACITY, 0) / 1000
        else pref.getInt(RESIDUAL_CAPACITY, 0) / 100
        val residualCapacity =
            if(residualCapacityCurrent in 1..maxChargeCurrent ||
                isTurboCharge(this@CapacityInfoService) || pref.getBoolean(
                    IS_FAST_CHARGE_SETTING, resources.getBoolean(R.bool.is_fast_charge_setting)))
                    (currentCapacity.toDouble() +
                            ((NOMINAL_BATTERY_VOLTAGE / 100.0) * designCapacity)).toInt()
            else currentCapacity
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
            NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                batteryLevelWith / 100f)
        pref.edit().apply {
            val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)
            if(seconds > 1) putLong(NUMBER_OF_CHARGES, numberOfCharges + 1).apply()
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
            if(PremiumInterface.isPremium) {
                if(residualCapacity > 0) {
                    withContext(Dispatchers.IO) {
                        HistoryHelper.addHistory(this@CapacityInfoService, currentDate,
                            residualCapacity)
                    }
                    if(HistoryHelper.isHistoryNotEmpty(this@CapacityInfoService)) {
                        val historyFragment = HistoryFragment.instance
                        historyFragment?.binding?.apply {
                            refreshEmptyHistory.visibility = View.GONE
                            emptyHistoryLayout.visibility = View.GONE
                            historyRecyclerView.visibility = View.VISIBLE
                            refreshHistory.visibility = View.VISIBLE
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
                            historyRecyclerView.visibility = View.GONE
                            refreshHistory.visibility = View.GONE
                            emptyHistoryLayout.visibility = View.VISIBLE
                            refreshEmptyHistory.visibility = View.VISIBLE
                            emptyHistoryText.text = resources.getText(R.string.empty_history_text)
                        }
                        MainActivity.instance?.toolbar?.menu?.apply {
                            findItem(R.id.history_premium)?.isVisible = false
                            findItem(R.id.clear_history)?.isVisible = false
                        }
                    }
                    return@withContext
                }
            }
            else {
                HistoryFragment.instance?.binding?.apply {
                    historyRecyclerView.visibility = View.GONE
                    refreshHistory.visibility = View.GONE
                    emptyHistoryLayout.visibility = View.VISIBLE
                    refreshEmptyHistory.visibility = View.VISIBLE
                    emptyHistoryText.text = resources.getText(R.string.history_premium_feature)
                }
                MainActivity.instance?.toolbar?.menu?.apply {
                    findItem(R.id.history_premium)?.isVisible = true
                    findItem(R.id.clear_history)?.isVisible = false
                }
            }
        }
        isSaveNumberOfCharges = false
        withContext(Dispatchers.Main) {
            updateServiceNotification(true)
        }
    }
    
    fun wakeLockRelease() {
        try {
            if(wakeLock?.isHeld == true) wakeLock?.release()
        }
        catch (_: RuntimeException) {}
    }
}