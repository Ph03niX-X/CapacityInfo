package com.ph03nix_x.capacityinfo

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {

        when(p1!!.action) {

            Intent.ACTION_BOOT_COMPLETED, "android.intent.action.QUICKBOOT_POWERON" -> {

                val componentName = ComponentName(p0!!, CapacityInfoService::class.java)

                val jobScheduler = p0.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

                val job = JobInfo.Builder(1, componentName).apply {

                    setMinimumLatency(10 * 1000)
                    setRequiresCharging(true)
                    setPersisted(false)
                }

                jobScheduler.schedule(job.build())
            }
        }
    }
}