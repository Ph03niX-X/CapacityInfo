package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.*
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.TimeSpan
import com.ph03nix_x.capacityinfo.activity.MainActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CapacityInfoService : Service() {

    private lateinit var pref: SharedPreferences
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var batteryManager: BatteryManager

    private var seconds = 0
    private var asyncTask: AsyncTask<Void, Void, Unit>? = null

    private var isUpdateNotification = false
    private var isChargeCounter = false
    private var isSeconds = false

    private val unpluggedReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

            when(p1!!.action) {

                Intent.ACTION_POWER_DISCONNECTED -> stopService()
            }
        }
    }


    companion object {

        var instance: CapacityInfoService? = null
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }

    override fun onCreate() {

        super.onCreate()

        createNotification()

        registerReceiver(unpluggedReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        instance = this

        isUpdateNotification = true
        isChargeCounter = true
        isSeconds = true

        pref.edit().putInt(Preferences.BatteryLevelWith.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

          asyncTask = DoAsync {

              DoAsync {

                  while (isSeconds) {

                      if (pref.getBoolean(Preferences.ShowLastChargeTime.prefName, true)) {
                          val batteryStatus =
                              registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                          val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                          if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 100 && status == BatteryManager.BATTERY_STATUS_FULL
                              || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                              pref.edit().putInt(Preferences.LastChargeTime.prefName, seconds).apply()

                              pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

                              isSeconds = false

                              break
                          }

                          else {

                              seconds++
                              Thread.sleep(1000)
                          }
                      }

                      else Thread.sleep(1 * 60 * 1000)
                  }

              }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

              DoAsync {

                  while(isChargeCounter) {

                      val intentFilter = IntentFilter()

                      intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

                      val batteryStatus = registerReceiver(null, intentFilter)

                      val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                      if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 100 && status == BatteryManager.BATTERY_STATUS_FULL
                          || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                          if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                              pref.edit().putInt("charge_counter", batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).apply()
                          else pref.edit().putBoolean(Preferences.IsSupported.prefName, false).apply()

                          isChargeCounter = false
                          
                          break
                      }

                      else {

                          when(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)) {

                              in 0..30 -> Thread.sleep(30 * 60 * 1000)
                              in 31..50 -> Thread.sleep(15 * 60 * 1000)
                              in 51..80 -> Thread.sleep(10 * 60 * 1000)
                              in 81..99 -> Thread.sleep(5 * 60 * 1000)
                              100 -> Thread.sleep(2 * 60 * 1000)
                          }
                      }

                  }

              }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
              
              DoAsync {

                  while(isUpdateNotification) {

                      updateNotification()

                      Thread.sleep(1000)
                  }

              }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
              

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return START_STICKY
    }

    override fun onDestroy() {

        isSeconds = false
        isChargeCounter = false
        isUpdateNotification = false
        asyncTask?.cancel(true)
        instance = null
        unregisterReceiver(unpluggedReceiver)

        super.onDestroy()
    }

    private fun createNotification() {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

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

    private fun stopService() {

        isSeconds = false
        isChargeCounter = false
        isUpdateNotification = false

        asyncTask?.cancel(true)

        stopService(Intent(this, CapacityInfoService::class.java))

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

            if (pref.getBoolean(Preferences.ShowLastChargeTime.prefName, true)
                && status != BatteryManager.BATTERY_STATUS_FULL && status != BatteryManager.BATTERY_STATUS_NOT_CHARGING)
                pref.edit().putInt(Preferences.LastChargeTime.prefName, seconds).apply()

        startJob()
    }

    private fun startJob() {

        val componentName = ComponentName(this, CapacityInfoJob::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(true)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(): String{
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

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return when(batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                val charging = getString(R.string.status, getString(R.string.charging))
                val currentCapacity = getString(R.string.current_capacity, getCurrentCapacity())
                val chargingCurrent = getString(R.string.charging_current, getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, getTemperature())

                val voltage = getString(R.string.voltage, toDecimalFormat(getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) > 0)
                    "$charging\n${getChargingTime()}\n$currentCapacity\n$chargingCurrent\n$temperature\n$voltage"

                else "$charging\n${getChargingTime()}\n$chargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                val notCharging = getString(R.string.status, getString(R.string.not_charging))
                val currentCapacity = getString(R.string.current_capacity, getCurrentCapacity())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, getTemperature())

                val voltage = getString(R.string.voltage, toDecimalFormat(getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) > 0)
                    "$notCharging\n${getChargingTime()}\n$currentCapacity\n$temperature\n$voltage"

                else "$notCharging\n${getChargingTime()}\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_FULL -> {

                val fullCharging = getString(R.string.status, getString(R.string.full))
                val currentCapacity = getString(R.string.current_capacity, getCurrentCapacity())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, getTemperature())

                val voltage = getString(R.string.voltage, toDecimalFormat(getVoltage()))

                if(pref.getBoolean(Preferences.IsSupported.prefName, true)) {

                    if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) > 0)
                        "$fullCharging\n${getChargingTime()}\n$currentCapacity\n${getResidualCapacity()}\n${getBatteryWear()}\n$temperature\n$voltage"

                    else "$fullCharging\n${getChargingTime()}\n${getResidualCapacity()}\n${getBatteryWear()}\n$temperature\n$voltage"
                }

                else "$fullCharging\n${getChargingTime()}\n$temperature"

            }

            BatteryManager.BATTERY_STATUS_DISCHARGING -> {

                val discharging = getString(R.string.status, getString(R.string.discharging))
                val currentCapacity = getString(R.string.current_capacity, getCurrentCapacity())
                val dischargingCurrent = getString(R.string.discharge_current, getChargingCurrent().toString())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, getTemperature())

                val voltage = getString(R.string.voltage, toDecimalFormat(getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) > 0)
                    "$discharging\n$currentCapacity\n$dischargingCurrent\n$temperature\n$voltage"

                else "$discharging\n$dischargingCurrent\n$temperature\n$voltage"
            }

            BatteryManager.BATTERY_STATUS_UNKNOWN -> {

                val discharging = getString(R.string.status, getString(R.string.unknown))
                val currentCapacity = getString(R.string.current_capacity, getCurrentCapacity())
                val temperature = getString(if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) R.string.temperature_fahrenheit
                else R.string.temperature_celsius, getTemperature())

                val voltage = getString(R.string.voltage, toDecimalFormat(getVoltage()))

                if(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) > 0)
                    "$discharging\n$currentCapacity\n$temperature\n$voltage"

                else "$discharging\n$temperature\n$voltage"
            }

            else -> ""
        }
    }

    private fun updateNotification() {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

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

    private fun getChargingCurrent(): Int {

        var chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if(chargingCurrent < 0) chargingCurrent /= -1

        if(chargingCurrent >= 10000) chargingCurrent /= 1000

        return chargingCurrent
    }

    private fun getTemperature(): String {

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toDouble()

        if(temp >= 100) temp /= 10

        var tempString = temp.toString()

        if(pref.getBoolean(Preferences.Fahrenheit.prefName, false)) tempString = toDecimalFormat((temp * 1.8) + 32)

        return tempString
    }

    private fun getCurrentCapacity(): String {

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var currentCapacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if (currentCapacity < 0) currentCapacity /= -1

        if (currentCapacity >= 100000) currentCapacity /= 1000

        return toDecimalFormat(currentCapacity.toDouble())
    }

    private fun getVoltage(): Double {

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()

        if(voltage >= 1000 && voltage < 1000000) voltage /= 1000 else if(voltage >= 1000000) voltage /= 1000000

        return voltage
    }

    private fun getResidualCapacity(): String {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val residualCapacity = pref.getInt(Preferences.ChargeCounter.prefName, 0).toDouble()

        return getString(R.string.residual_capacity, toDecimalFormat(residualCapacity), "${DecimalFormat("#.#").format(
            if (residualCapacity >= 100000) ((residualCapacity / 1000) / pref.getInt(
                Preferences.DesignCapacity.prefName, 0).toDouble()) * 100

            else (residualCapacity / pref.getInt(Preferences.DesignCapacity.prefName, 0).toDouble()) * 100)}%")
    }

    private fun getBatteryWear(): String {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val capacityDesign = pref.getInt(Preferences.DesignCapacity.prefName, 0).toDouble()

        val capacity = pref.getInt(Preferences.ChargeCounter.prefName, 0).toDouble()

        return getString(R.string.battery_wear,"${DecimalFormat("#.#").format(100 - ((capacity / capacityDesign) * 100))}%")
    }

    private fun toDecimalFormat(number: Double) = if(number >= 100000) DecimalFormat("#.#").format(number / 1000) else DecimalFormat("#.#").format(number)

    private fun getChargingTime(): String {

        val seconds = TimeSpan.toSeconds(this.seconds.toDouble())
        val minutes = TimeSpan.toMinutes(this.seconds.toDouble())
        val hours = TimeSpan.toHours(this.seconds.toDouble())

        val time = "$hours:$minutes:$seconds"

        return getString(R.string.charging_time,
            DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString())
    }
}