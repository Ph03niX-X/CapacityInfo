package com.ph03nix_x.capacityinfo.services

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

@SuppressLint("SpecifyJobSchedulerIdRange")
class CheckPremiumJob : JobService(), PremiumInterface {

    companion object {
        var isCheckPremiumJob = false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        if(isCheckPremiumJob && MainApp.isInstalledGooglePlay) checkPremiumJob()
        else isCheckPremiumJob = true
        return false
    }

    override fun onStopJob(p0: JobParameters?) = true
}