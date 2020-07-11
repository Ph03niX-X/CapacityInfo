package com.ph03nix_x.capacityinfo.helpers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.Preference
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import kotlinx.coroutines.*


object ServiceHelper {

    private var isStartedCapacityInfoService = false
    private var isStartedOverlayService = false

    fun startService(context: Context, serviceName: Class<*>,
                     isStartOverlayServiceFromSettings: Boolean = false) {

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

            try {

                if(serviceName == CapacityInfoService::class.java) {

                    isStartedCapacityInfoService = true

                    delay(2500L)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        context.startForegroundService(Intent(context, serviceName))
                    else context.startService(Intent(context, serviceName))

                    delay(1000L)
                    isStartedCapacityInfoService = false
                }

                else if(serviceName == OverlayService::class.java) {

                    isStartedOverlayService = true

                    if(!isStartOverlayServiceFromSettings) delay(3600L)

                    context.startService(Intent(context, serviceName))
                    isStartedCapacityInfoService = false
                }
            }
            catch(e: Exception) {

                Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    fun isStartedCapacityInfoService() = isStartedCapacityInfoService

    fun isStartedOverlayService() = isStartedOverlayService

    fun stopService(context: Context, serviceName: Class<*>) =
        context.stopService(Intent(context, serviceName))

    fun restartService(context: Context, serviceName: Class<*>) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500L)

                startService(context, serviceName)
            }
        }
    }

    fun restartService(context: Context, serviceName: Class<*>, preference: Preference? = null) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500L)

                startService(context, serviceName)

                delay(1000L)
                preference?.isEnabled = true
            }
        }
    }

    fun restartService(context: Context, serviceName: Class<*>,
                       preferencesList: ArrayList<Preference?>? = null) {

        CoroutineScope(Dispatchers.Default).launch {

            withContext(Dispatchers.Main) {

                stopService(context, serviceName)

                if(serviceName == CapacityInfoService::class.java) delay(2500L)

                startService(context, serviceName)

                delay(1000L)
                preferencesList?.forEach {

                    it?.isEnabled = true
                }
            }
        }
    }

    fun jobSchedule(context: Context, jobName: Class<*>, jobId: Int, periodic: Long) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        val serviceComponent = ComponentName(context, jobName)

        val jobInfo = JobInfo.Builder(jobId, serviceComponent).apply {

            setPeriodic(periodic)
        }

        jobScheduler?.schedule(jobInfo.build())
    }

    fun cancelJob(context: Context, jobId: Int) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        jobScheduler?.cancel(jobId)
    }

    fun cancelAllJobs(context: Context) {

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

        jobScheduler?.cancelAll()
    }
}