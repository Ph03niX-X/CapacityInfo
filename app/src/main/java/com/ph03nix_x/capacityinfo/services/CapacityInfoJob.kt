package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.*
import android.os.Build
import com.ph03nix_x.capacityinfo.Preferences

class CapacityInfoJob : JobService() {

    override fun onStartJob(p0: JobParameters?): Boolean {

        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(pref.getBoolean(Preferences.EnableService.prefName, true) && CapacityInfoService.instance == null) startService()

        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {

        return true
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(Intent(applicationContext, CapacityInfoService::class.java))

        else startService(Intent(applicationContext, CapacityInfoService::class.java))
    }
}