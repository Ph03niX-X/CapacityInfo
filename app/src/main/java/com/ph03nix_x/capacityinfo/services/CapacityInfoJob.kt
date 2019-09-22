package com.ph03nix_x.capacityinfo.services

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.*
import android.os.BatteryManager
import android.os.Build
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.activity.isJob

var isRegisterPluggedReceiver = false
var isRegisterUnpluggedReceiver = false
class CapacityInfoJob : JobService() {

    override fun onStartJob(p0: JobParameters?): Boolean {

        isJob = !isJob

        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        when (plugged) {

            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS ->

                if (pref.getBoolean(Preferences.EnableService.prefName, true) && CapacityInfoService.instance == null) {

                    isRegisterUnpluggedReceiver = !isRegisterUnpluggedReceiver

                    startService()
                }

            else -> if (pref.getBoolean(Preferences.EnableService.prefName, true)
                && pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false) && CapacityInfoService.instance == null) {

                isRegisterPluggedReceiver = !isRegisterPluggedReceiver

                startService()
            }

            else startJob()
        }

        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {

        return false
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(Intent(applicationContext, CapacityInfoService::class.java))

        else startService(Intent(applicationContext, CapacityInfoService::class.java))
    }

    private fun startJob() {

        val componentName = ComponentName(this, CapacityInfoJob::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(60 * 1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}