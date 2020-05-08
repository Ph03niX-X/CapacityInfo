package com.ph03nix_x.capacityinfo.interfaces

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build

interface JobServiceInterface {

    fun onScheduleJobService(context: Context, jobServiceName: Class<*>,
                          jobServiceId: Int, isRequiresCharging: Boolean = false,
                          isRequiresBatteryNotLow: Boolean = true,
                          requiredNetworkType: Int = JobInfo.NETWORK_TYPE_ANY,
                          periodicHours: Long, isPersisted: Boolean = false) {

        val componentName = ComponentName(context, jobServiceName)
        val jobInfo = JobInfo.Builder(jobServiceId, componentName).apply {

            setRequiresCharging(isRequiresCharging)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                setRequiresBatteryNotLow(isRequiresBatteryNotLow)

            setRequiredNetworkType(requiredNetworkType)
            setPeriodic(periodicHours * 60 * 60 * 1000)
            setPersisted(isPersisted)
        }

        val jobScheduler = context.getSystemService(Application.JOB_SCHEDULER_SERVICE)
                as? JobScheduler

        jobScheduler?.cancel(jobServiceId)

        jobScheduler?.schedule(jobInfo.build())
    }

    fun onCancelJobService(context: Context, jobServiceId: Int) {

        val jobScheduler = context.getSystemService(Application.JOB_SCHEDULER_SERVICE)
                as? JobScheduler

        jobScheduler?.cancel(jobServiceId)
    }

    fun onCancelAllJobServices(context: Context) {

        val jobScheduler = context.getSystemService(Application.JOB_SCHEDULER_SERVICE)
                as? JobScheduler

        jobScheduler?.cancelAll()
    }

    fun onReScheduleJobService(context: Context, jobServiceName: Class<*>,
                             jobServiceId: Int, isRequiresCharging: Boolean = false,
                             isRequiresBatteryNotLow: Boolean = true,
                             requiredNetworkType: Int = JobInfo.NETWORK_TYPE_ANY,
                             periodicHours: Long = 12, isPersisted: Boolean = false) {

        onCancelJobService(context, jobServiceId)

        onScheduleJobService(context, jobServiceName, jobServiceId, isRequiresCharging,
            isRequiresBatteryNotLow, requiredNetworkType, periodicHours, isPersisted)
    }
}