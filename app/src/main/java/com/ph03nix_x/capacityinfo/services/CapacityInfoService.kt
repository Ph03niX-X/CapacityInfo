package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ph03nix_x.capacityinfo.Battery
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.receivers.PluggedReceiver
import com.ph03nix_x.capacityinfo.receivers.UnpluggedReceiver

class CapacityInfoService : Service() {

    private lateinit var pref: SharedPreferences
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var batteryManager: BatteryManager
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var batteryStatus: Intent? = null
    var seconds = 1
    var sleepTime: Long = 10
    var batteryLevelWith = -1
    var isFull = false
    var isDoAsync = false

    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {

        super.onCreate()

        createNotification()

        applicationContext.registerReceiver(UnpluggedReceiver(), IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

        applicationContext.registerReceiver(PluggedReceiver(), IntentFilter(Intent.ACTION_POWER_CONNECTED))

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        instance = this

        isDoAsync = true

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "${packageName}:service_wakelock")

        batteryLevelWith = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        DoAsync {

            while (isDoAsync) {

                if(!wakeLock.isHeld && !isFull) wakeLock.acquire(20 * 1000)

                batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                    Thread.sleep(948)
                    seconds++
                    updateNotification()
                }

                else if (status == BatteryManager.BATTERY_STATUS_FULL && !isFull) {
                    
                    isFull = !isFull

                    pref.edit().putInt(Preferences.LastChargeTime.prefName, seconds).apply()
                    pref.edit().putInt(Preferences.BatteryLevelWith.prefName, batteryLevelWith).apply()
                    pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

                    if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                        pref.edit().putInt("charge_counter", batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).apply()

                    else pref.edit().putBoolean(Preferences.IsSupported.prefName, false).apply()

                    updateNotification()
                    wakeLock.release()
                }

                else {
                    
                    updateNotification()

                    Thread.sleep(sleepTime * 950)
                }

                if(wakeLock.isHeld && isFull) wakeLock.release()
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return START_STICKY
    }

    override fun onDestroy() {

        instance = null
        isDoAsync = false

        if(wakeLock.isHeld) wakeLock.release()

        if (!isFull && seconds > 1) {

            pref.edit().putInt(Preferences.LastChargeTime.prefName, seconds).apply()

            pref.edit().putInt(Preferences.BatteryLevelWith.prefName, batteryLevelWith).apply()

            pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()
        }

        startJob()

        super.onDestroy()
    }

    private fun createNotification() {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()

        else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }

        val openApp = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.charging)
            color = ContextCompat.getColor(applicationContext, R.color.blue)
            setContentIntent(openApp)
            setStyle(NotificationCompat.BigTextStyle().bigText(getStatus()))
            setShowWhen(false)
        }

        startForeground(101, notificationBuilder.build())
    }

    fun updateNotification() = createNotification()

    private fun startJob() {

        val componentName = ComponentName(this, CapacityInfoJob::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
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

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val battery = Battery(this)
        
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        return when(status) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                val charging = getString(R.string.status, getString(R.string.charging))
                val batteryLevel = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))
                val chargingCurrent = getString(R.string.charging_current, battery.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, battery.getTemperature())

                val voltage = getString(R.string.voltage, battery.toDecimalFormat(battery.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$charging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$currentCapacity\n$chargingCurrent\n$temperature\n$voltage"

                else "$charging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$chargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                val notCharging = getString(R.string.status, getString(R.string.not_charging))
                val batteryLevel = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))
                val dischargingCurrent = getString(R.string.discharge_current, battery.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, battery.getTemperature())

                val voltage = getString(R.string.voltage, battery.toDecimalFormat(battery.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$notCharging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$currentCapacity\n$dischargingCurrent\n$temperature\n$voltage"

                else "$notCharging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$dischargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_FULL -> {

                val fullCharging = getString(R.string.status, getString(R.string.full))
                val batteryLevel = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))
                val dischargingCurrent = getString(R.string.discharge_current, battery.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, battery.getTemperature())

                val voltage = getString(R.string.voltage, battery.toDecimalFormat(battery.getVoltage()))

                if(pref.getBoolean(Preferences.IsSupported.prefName, true)) {

                    if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                        "$fullCharging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$currentCapacity\n${battery.getResidualCapacity()}\n${battery.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"

                    else "$fullCharging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n${battery.getResidualCapacity()}\n${battery.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"
                }

                else "$fullCharging\n$batteryLevel\n${battery.getChargingTime(seconds.toDouble())}\n$dischargingCurrent\n$temperature"

            }

            BatteryManager.BATTERY_STATUS_DISCHARGING -> {

                val discharging = getString(R.string.status, getString(R.string.discharging))
                val batteryLevel = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))
                val dischargingCurrent = getString(R.string.discharge_current, battery.getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, battery.getTemperature())

                val voltage = getString(R.string.voltage, battery.toDecimalFormat(battery.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$discharging\n$batteryLevel\n$currentCapacity\n${battery.getResidualCapacity()}\n${battery.getBatteryWear()}\n$dischargingCurrent\n$temperature\n$voltage"

                else "$discharging\n$batteryLevel\n$dischargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_UNKNOWN -> {

                val discharging = getString(R.string.status, getString(R.string.unknown))
                val batteryLevel = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                val currentCapacity = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, battery.getTemperature())

                val voltage = getString(R.string.voltage, battery.toDecimalFormat(battery.getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                    "$discharging\n$batteryLevel\n$currentCapacity\n$temperature\n$voltage"

                else "$discharging\n$batteryLevel\n$temperature\n$voltage"
            }

            else -> "N/A"
        }
    }
}