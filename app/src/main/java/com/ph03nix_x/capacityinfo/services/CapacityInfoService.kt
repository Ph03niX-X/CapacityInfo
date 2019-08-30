package com.ph03nix_x.capacityinfo.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.*
import android.os.AsyncTask
import android.os.BatteryManager
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.enums.Preferences

class CapacityInfoService : JobService() {

    private val powerReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

            when(p1!!.action) {

                Intent.ACTION_POWER_DISCONNECTED -> startJob()
            }
        }
    }

    override fun onStartJob(p0: JobParameters?): Boolean {

        regReceiver(applicationContext)

        DoAsync {

            val intentFilter = IntentFilter()

            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

            val batteryStatus = registerReceiver(null, intentFilter)

            val batteryManager =
                applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            val pref = applicationContext.getSharedPreferences("preferences", Context.MODE_PRIVATE)

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

            if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) == 100 && status == BatteryManager.BATTERY_STATUS_FULL
                || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                if (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) > 0) pref.edit().putInt("charge_counter",
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)).apply()

                else pref.edit().putBoolean(Preferences.IsSupported.prefName, false).apply()

                startJob(720)
            }

            else startJob()

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)


        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {

        return true
    }

    private fun startJob(minutes: Long = 1) {

        val componentName = ComponentName(this, CapacityInfoService::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            if(minutes in 1..720) setMinimumLatency(minutes * 60 * 1000)

            else setMinimumLatency(60 * 1000)

            setRequiresCharging(true)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }

    private fun regReceiver(context: Context) {

        context.registerReceiver(powerReceiver, IntentFilter(Intent.ACTION_POWER_DISCONNECTED))
    }
}