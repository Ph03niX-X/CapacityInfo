package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE
import androidx.core.content.edit

/**
 * Created by Ph03niX-X on 05.05.2024
 * Ph03niX-X@outlook.com
 */

class FastChargeJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        PreferenceManager.getDefaultSharedPreferences(this).apply {
            if(getBoolean(IS_FAST_CHARGE, resources.getBoolean(R.bool.is_fast_charge))) {
                edit { putBoolean(IS_FAST_CHARGE, false) }
            }
        }
        return false
    }

    override fun onStopJob(params: JobParameters?) = false
}