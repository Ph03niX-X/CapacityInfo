package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper.checkPremium

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

class CheckPremiumJob : JobService() {

    companion object {
        var isCheckPremiumJob = false
    }
    override fun onStartJob(p0: JobParameters?): Boolean {
        if(isCheckPremiumJob) checkPremium() else isCheckPremiumJob = true
        return false
    }

    override fun onStopJob(p0: JobParameters?) = true
}