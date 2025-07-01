package com.ph03nix_x.capacityinfo.services

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface

/**
 * Created by Ph03niX-X on 24.06.2025
 * Ph03niX-X@outlook.com
 */

@SuppressLint("SpecifyJobSchedulerIdRange")
class AdsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        if(!PremiumInterface.isPremium) {
            val mainActivity = MainActivity.instance
            mainActivity?.loadAdsCount?.let {
                if(it > 0) mainActivity.loadAdsCount = 0
            }
        }
        return false
    }

    override fun onStopJob(params: JobParameters?) = true
}