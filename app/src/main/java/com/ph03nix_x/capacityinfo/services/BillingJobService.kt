package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface.Companion.billingClient
import kotlinx.coroutines.*

class BillingJobService : JobService(), BillingInterface {

    override fun onStartJob(params: JobParameters?): Boolean {

        CoroutineScope(Dispatchers.Default).launch {

            onBillingStartConnection(this@BillingJobService)

            delay(5L * 1000L)
            billingClient?.endConnection()
            billingClient = null
        }

        return false
    }

    override fun onStopJob(params: JobParameters?) = true
}