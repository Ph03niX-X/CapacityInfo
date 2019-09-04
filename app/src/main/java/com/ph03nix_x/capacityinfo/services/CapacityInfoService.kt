package com.ph03nix_x.capacityinfo.services

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import java.text.DecimalFormat

class CapacityInfoService : Service() {

    private lateinit var pref: SharedPreferences
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var batteryManager: BatteryManager

    private var seconds = 1
    private var asyncSeconds: AsyncTask<Void, Void, Unit>? = null
    private var asyncChargeCounter: AsyncTask<Void, Void, Unit>? = null
    private var asyncUpdateNotification: AsyncTask<Void, Void, Unit>? = null

    private val unpluggedReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

            when(p1!!.action) {

                Intent.ACTION_POWER_DISCONNECTED -> stopService()
            }
        }
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

        pref.edit().putInt(Preferences.BatteryLevelWith.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

          asyncSeconds = DoAsync {

                while (true) {

                    if (pref.getBoolean(Preferences.ShowLastChargeTime.prefName, true)) {
                        val batteryStatus =
                            registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                        if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 100 && status == BatteryManager.BATTERY_STATUS_FULL
                            || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                            pref.edit().putInt(Preferences.LastChargeTime.prefName, seconds).apply()

                            pref.edit().putInt(Preferences.BatteryLevelTo.prefName, batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).apply()

                            break
                        }

                        else {

                            seconds++

                            Thread.sleep(1000)
                        }
                    }
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

       asyncChargeCounter = DoAsync {

            while(true) {

                val intentFilter = IntentFilter()

                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

                val batteryStatus = registerReceiver(null, intentFilter)

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 100 && status == BatteryManager.BATTERY_STATUS_FULL
                    || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                    if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0)
                        pref.edit().putInt("charge_counter", batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).apply()
                    else pref.edit().putBoolean(Preferences.IsSupported.prefName, false).apply()

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

        asyncUpdateNotification = DoAsync {

            while(true) {

                updateNotification()

                Thread.sleep(10 * 1000)
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)

        return START_STICKY
    }

    override fun onDestroy() {

        asyncChargeCounter?.cancel(true)
        asyncSeconds?.cancel(true)
        asyncUpdateNotification?.cancel(true)
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
            setContentIntent(openApp)
            setContentText(getStatus())
            setShowWhen(false)
        }

        startForeground(101, notificationBuilder.build())
    }

    private fun stopService() {

        asyncSeconds?.cancel(true)

        asyncUpdateNotification?.cancel(true)

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

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return when(batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.charging)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getString(R.string.not_charging)
            BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.full)
            BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.discharging)
            BatteryManager.BATTERY_STATUS_UNKNOWN -> getString(R.string.unknown)
            else -> ""
        }
    }

    private fun updateNotification() {

        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        val openApp = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.charging)
            setContentIntent(openApp)
            when(status) {

                BatteryManager.BATTERY_STATUS_CHARGING -> setContentText(getString(R.string.charging))
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> setContentText(getString(R.string.not_charging))
                BatteryManager.BATTERY_STATUS_FULL -> if(pref.getInt(Preferences.ChargeCounter.prefName, 0) == 0)
                    setContentText(getString(R.string.full))
                else setContentText(getString(R.string.residual_capacity, toDecimalFormat(getResidualCapacity()), "${DecimalFormat("#.#").format(
                    if (getResidualCapacity() >= 100000) ((getResidualCapacity() / 1000) / pref.getInt(
                        Preferences.DesignCapacity.prefName, 0).toDouble()) * 100

                    else (getResidualCapacity() / pref.getInt(Preferences.DesignCapacity.prefName, 0).toDouble()) * 100)}%"))
                BatteryManager.BATTERY_STATUS_DISCHARGING -> setContentText(getString(R.string.discharging))
                BatteryManager.BATTERY_STATUS_UNKNOWN -> setContentText(getString(R.string.unknown))
            }
            setShowWhen(false)
        }

    }

    private fun getResidualCapacity() = getSharedPreferences("preferences", Context.MODE_PRIVATE).getInt(Preferences.ChargeCounter.prefName, 0).toDouble()

    private fun toDecimalFormat(number: Double) = if(number >= 100000) DecimalFormat("#.#").format(number / 1000) else DecimalFormat("#.#").format(number)
}