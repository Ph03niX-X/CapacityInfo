package com.ph03nix_x.capacityinfo.helpers

import android.app.ForegroundServiceStartNotAllowedException
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast
import androidx.preference.Preference
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.CheckPremiumJob
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

object ServiceHelper {

    private var isStartedCapacityInfoService = false
    private var isStartedOverlayService = false

    fun startService(context: Context, serviceName: Class<*>,
                     isStartOverlayServiceFromSettings: Boolean = false) {

        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) {

            try {

                if(serviceName == CapacityInfoService::class.java) {

                    isStartedCapacityInfoService = true

                    delay(2.5.seconds)
                    context.startForegroundService(Intent(context, serviceName))

                    delay(1.seconds)
                    isStartedCapacityInfoService = false
                }
                else if(serviceName == OverlayService::class.java) {

                    isStartedOverlayService = true

                    if(!isStartOverlayServiceFromSettings) delay(3.6.seconds)

                    context.startService(Intent(context, serviceName))
                    isStartedCapacityInfoService = false
                }
            }
            catch(e: Exception) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e is ForegroundServiceStartNotAllowedException) return@launch
                else Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isStartedCapacityInfoService() = isStartedCapacityInfoService

    fun isStartedOverlayService() = isStartedOverlayService

    fun stopService(context: Context, serviceName: Class<*>) =
        context.stopService(Intent(context, serviceName))

    fun restartService(context: Context, serviceName: Class<*>, preference: Preference? = null) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2.5.seconds)

                startService(context, serviceName)

                delay(1.seconds)
                preference?.isEnabled = true
            }
        }
    }

    fun jobSchedule(context: Context, jobName: Class<*>, jobId: Int, periodic: Long,
                    isRequiredNetwork: Boolean = false) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        val serviceComponent = ComponentName(context, jobName)

        val jobInfo = JobInfo.Builder(jobId, serviceComponent).apply {
            if(isRequiredNetwork) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    setRequiredNetwork(NetworkRequest.Builder().apply {
                        addCapability(NET_CAPABILITY_INTERNET)
                        addCapability(NET_CAPABILITY_VALIDATED)
                    }.build())
                else setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            }
            setPeriodic(periodic)
        }.build()

        if(!isJobSchedule(context, jobId)) jobScheduler?.schedule(jobInfo)
    }

    fun checkPremiumJobSchedule(context: Context) =
        jobSchedule(context, CheckPremiumJob::class.java, Constants.CHECK_PREMIUM_JOB_ID,
            Constants.CHECK_PREMIUM_JOB_SERVICE_PERIODIC, true)

    private fun isJobSchedule(context: Context, jobId: Int): Boolean {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        jobScheduler?.allPendingJobs?.forEach {

            if(it.id == jobId) return true
        }

        return false
    }

    fun cancelJob(context: Context, jobId: Int) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        if(isJobSchedule(context, jobId)) jobScheduler?.cancel(jobId)
    }

    fun cancelAllJobs(context: Context) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        if(jobScheduler?.allPendingJobs?.isNotEmpty() == true) jobScheduler.cancelAll()
    }
}