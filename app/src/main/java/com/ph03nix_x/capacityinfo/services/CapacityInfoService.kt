package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.content.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BatteryInfo
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.activity.sleepArray
import com.ph03nix_x.capacityinfo.activity.tempBatteryLevel
import com.ph03nix_x.capacityinfo.activity.tempCurrentCapacity
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver

var isPowerConnected = false
var isStopCheck = false
var capacityAdded = 0.0
const val notifyId = 101
class CapacityInfoService : Service() {

    private lateinit var pref: SharedPreferences
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var batteryManager: BatteryManager
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var doAsync: AsyncTask<Void, Void, Unit>? = null
    private var batteryStatus: Intent? = null
    private var isDoAsync = false
    private var isFull = false
    private var batteryLevelWith = -1
    var isStopService = false
    var seconds = 0
    var sleepTime: Long = 10

    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {

        super.onCreate()

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        when(plugged) {

            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                isPowerConnected = true

                val batteryInfo = BatteryInfo(this)
                
                tempBatteryLevel = batteryInfo.getBatteryLevel()

                tempCurrentCapacity = batteryInfo.getCurrentCapacity()

                applicationContext.registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

            }

            else -> applicationContext.registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))
        }

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        seconds++

        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val batteryInfo = BatteryInfo(this)

        instance = this

        isDoAsync = true

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")

        sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)

        batteryLevelWith = batteryInfo.getBatteryLevel()

        doAsync = DoAsync {

            while (isDoAsync) {

                if(!wakeLock.isHeld && !isFull && isPowerConnected) wakeLock.acquire(5 * 60 * 1000)

                batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                    Thread.sleep(914)
                    seconds++
                    updateNotification()
                }

                else if (status == BatteryManager.BATTERY_STATUS_FULL && !isFull) {
                    
                    isFull = true

                    pref.edit().putInt(Preferences.LastChargeTime.prefKey, seconds).apply()
                    pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith).apply()
                    pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, batteryInfo.getBatteryLevel()).apply()

                    if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0) {

                        pref.edit().putInt(Preferences.ChargeCounter.prefKey, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).apply()

                        pref.edit().putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat()).apply()

                        if(!pref.getBoolean(Preferences.IsSupported.prefKey, true)) pref.edit().putBoolean(Preferences.IsSupported.prefKey, true).apply()
                    }

                    else {

                        if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) pref.edit().putBoolean(Preferences.IsSupported.prefKey, false).apply()
                    }

                    updateNotification()
                    if(wakeLock.isHeld) wakeLock.release()
                }

                else {
                    
                    updateNotification()

                    if (sleepTime !in sleepArray && !isPowerConnected) {

                        sleepTime = 40

                        pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()
                    }

                    else if(isPowerConnected && sleepTime != 20.toLong() && isFull) sleepTime = 20
                    if(wakeLock.isHeld) wakeLock.release()

                    Thread.sleep(if(!isPowerConnected && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)) sleepTime * 914
                    else if(!isPowerConnected && !pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)) (60 * 914).toLong()
                    else 914)
                }
            }

        }.execute()

        return START_STICKY
    }

    override fun onDestroy() {

        val batteryInfo = BatteryInfo(this)

        try { if (wakeLock.isHeld) wakeLock.release() }

        finally {

            instance = null
            isDoAsync = false
            doAsync?.cancel(true)

            val pref = PreferenceManager.getDefaultSharedPreferences(this)

            if (!isFull && seconds > 1) {

                pref.edit().putInt(Preferences.LastChargeTime.prefKey, seconds).apply()

                pref.edit().putInt(Preferences.BatteryLevelWith.prefKey, batteryLevelWith).apply()

                pref.edit().putInt(Preferences.BatteryLevelTo.prefKey, batteryInfo.getBatteryLevel()).apply()

                if(capacityAdded > 0) pref.edit().putFloat(Preferences.CapacityAdded.prefKey, capacityAdded.toFloat()).apply()
            }

            if(pref.getBoolean(Preferences.EnableService.prefKey, true) && !isStopService) startService()

            super.onDestroy()
        }
    }

    private fun createNotification() {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val openApp = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val stopService = PendingIntent.getService(this, 1, Intent(this, StopService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.service_small_icon)
            color = ContextCompat.getColor(applicationContext, R.color.blue)
            setContentIntent(openApp)

            when(plugged) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    if(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getStatus()))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.enabled)))
                }

                else -> {

                    if(pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true))
                        setStyle(NotificationCompat.BigTextStyle().bigText(getStatus()))

                    else setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.enabled)))
                }
            }

            setShowWhen(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)
                    && pref.getBoolean(Preferences.IsServiceHours.prefKey, false))

            if(pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true))
                addAction(NotificationCompat.Action(0, getString(R.string.stop_service), stopService))
        }

        startForeground(notifyId, notificationBuilder.build())
    }

    fun updateNotification() {

        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val stopService = PendingIntent.getService(this, 1, Intent(this, StopService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.apply {

            when(plugged) {

                BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS -> {

                    if(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true))
                        notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(getStatus()))

                    else notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.enabled)))
                }

                else -> {

                    if(pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true))
                        notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(getStatus()))

                    else notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(getString(R.string.enabled)))
                }
            }

            setShowWhen(pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)
                    && pref.getBoolean(Preferences.IsServiceHours.prefKey, false))

            if(pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true) && mActions.isEmpty())
                addAction(NotificationCompat.Action(0, getString(R.string.stop_service), stopService))

            else if(!pref.getBoolean(Preferences.IsShowServiceStop.prefKey, true) && mActions.isNotEmpty()) mActions.clear()
        }

        notificationManager.notify(notifyId, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(): String {
        val channelId = "service_channel"
        val channelName = getString(R.string.service)
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_LOW)
        chan.setShowBadge(false)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getStatus(): String {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val batteryInfo = BatteryInfo(this)
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        return when(status) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                val charging = getString(R.string.status, getString(R.string.charging))
                val batteryLevel = getString(R.string.battery_level, "${batteryInfo.getBatteryLevel()}%")
                val plugged = batteryInfo.getPlugged(batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)!!)
                val currentCapacity = getString(R.string.current_capacity, batteryInfo.toDecimalFormat(batteryInfo.getCurrentCapacity()))
                val capacityAdded = batteryInfo.getCapacityAdded()
                val chargingCurrent = getString(R.string.charging_current, batteryInfo.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, batteryInfo.getTemperature())

                val voltage = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                    batteryInfo.toDecimalFormat(batteryInfo.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$charging\n$batteryLevel\n$plugged\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n$chargingCurrent\n$temperature\n$voltage"

                else "$charging\n$batteryLevel\n$plugged\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$chargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                val notCharging = getString(R.string.status, getString(R.string.not_charging))
                val batteryLevel = getString(R.string.battery_level, "${batteryInfo.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, batteryInfo.toDecimalFormat(batteryInfo.getCurrentCapacity()))
                val capacityAdded = batteryInfo.getCapacityAdded()
                val dischargingCurrent = getString(R.string.discharge_current, batteryInfo.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, batteryInfo.getTemperature())

                val voltage = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                    batteryInfo.toDecimalFormat(batteryInfo.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$notCharging\n$batteryLevel\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n$dischargingCurrent\n$temperature\n$voltage"

                else "$notCharging\n$batteryLevel\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$dischargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_FULL -> {

                val fullCharging = getString(R.string.status, getString(R.string.full))
                val batteryLevel = getString(R.string.battery_level, "${batteryInfo.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, batteryInfo.toDecimalFormat(batteryInfo.getCurrentCapacity()))
                val capacityAdded = batteryInfo.getCapacityAdded()
                val dischargingCurrent = getString(R.string.discharge_current, batteryInfo.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, batteryInfo.getTemperature())

                val voltage = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                    batteryInfo.toDecimalFormat(batteryInfo.getVoltage()))

                if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) {

                    if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                        "$fullCharging\n$batteryLevel\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$currentCapacity\n$capacityAdded\n${batteryInfo.getResidualCapacity()}\n${batteryInfo.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"

                    else "$fullCharging\n$batteryLevel\n${batteryInfo.getChargingTime(seconds.toDouble())}\n${batteryInfo.getResidualCapacity()}\n${batteryInfo.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"
                }

                else "$fullCharging\n$batteryLevel\n${batteryInfo.getChargingTime(seconds.toDouble())}\n$dischargingCurrent\n$temperature"

            }

            BatteryManager.BATTERY_STATUS_DISCHARGING -> {

                val batteryLevelWith = "${pref.getInt(Preferences.BatteryLevelWith.prefKey, 0)}%"
                val batteryLevelTo = "${pref.getInt(Preferences.BatteryLevelTo.prefKey, 0)}%"

                val discharging = getString(R.string.status, getString(R.string.discharging))
                val batteryLevel = getString(R.string.battery_level, "${batteryInfo.getBatteryLevel()}%")
                val lastChargingTime = getString(R.string.last_charge_time, batteryInfo.getLastChargeTime(), batteryLevelWith, batteryLevelTo)
                val currentCapacity = getString(R.string.current_capacity, batteryInfo.toDecimalFormat(batteryInfo.getCurrentCapacity()))
                val capacityAdded = batteryInfo.getCapacityAdded()
                val dischargingCurrent = getString(R.string.discharge_current, batteryInfo.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, batteryInfo.getTemperature())

                val voltage = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                    batteryInfo.toDecimalFormat(batteryInfo.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0) {

                    if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0 && pref.getBoolean(Preferences.IsShowLastChargeTimeInNotification.prefKey, true))

                    "$discharging\n$batteryLevel\n$lastChargingTime\n$currentCapacity\n$capacityAdded\n${batteryInfo.getResidualCapacity()}\n${batteryInfo.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"

                    else "$discharging\n$batteryLevel\n$currentCapacity\n$capacityAdded\n${batteryInfo.getResidualCapacity()}\n${batteryInfo.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"
                }

                else {

                    if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0 && pref.getBoolean(Preferences.IsShowLastChargeTimeInNotification.prefKey, true))
                        "$discharging\n$batteryLevel\n$lastChargingTime\n$dischargingCurrent\n$temperature\n$voltage"

                    else "$discharging\n$dischargingCurrent\n$temperature\n$voltage"

                }
            }

            BatteryManager.BATTERY_STATUS_UNKNOWN -> {

                val discharging = getString(R.string.status, getString(R.string.unknown))
                val batteryLevel = getString(R.string.battery_level, "${batteryInfo.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, batteryInfo.toDecimalFormat(batteryInfo.getCurrentCapacity()))
                val capacityAdded = batteryInfo.getCapacityAdded()
                val temperature = getString(if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, batteryInfo.getTemperature())

                val voltage = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                    batteryInfo.toDecimalFormat(batteryInfo.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$discharging\n$batteryLevel\n$currentCapacity\n$capacityAdded\n$temperature\n$voltage"

                else "$discharging\n$batteryLevel\n$temperature\n$voltage"
            }

            else -> "N/A"
        }
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(Intent(this, CapacityInfoService::class.java))

        else startService(Intent(this, CapacityInfoService::class.java))
    }
}