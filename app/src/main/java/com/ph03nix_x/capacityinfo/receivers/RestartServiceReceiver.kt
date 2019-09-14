package com.ph03nix_x.capacityinfo.receivers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob

class RestartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {


        when(intent!!.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> startCapacityInfoJob(context!!)
        }
    }

    private fun startCapacityInfoJob(context: Context) {

        val componentName = ComponentName(context, CapacityInfoJob::class.java)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}