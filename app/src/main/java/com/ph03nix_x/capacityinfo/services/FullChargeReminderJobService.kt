package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_FULL_CHARGE_REMINDER

/**
 * Created by Ph03niX-X on 07.05.2023
 * Ph03niX-X@outlook.com
 */

class FullChargeReminderJobService : JobService(), NotificationInterface {

    override fun onStartJob(params: JobParameters?): Boolean {

        if(CapacityInfoService.instance == null)
            ServiceHelper.cancelJob(this, Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)

        else {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)

            val isNotifyBatteryIsFullyCharged = pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED,
                resources.getBoolean(R.bool.is_notify_battery_is_fully_charged))

            val isNotifyFullyChargeReminder = pref.getBoolean(
                IS_NOTIFY_FULL_CHARGE_REMINDER,
                resources.getBoolean(R.bool.is_notify_full_charge_reminder_default_value))

            if(isNotifyBatteryIsFullyCharged && isNotifyFullyChargeReminder &&
                CapacityInfoService.instance?.isFull == true)
                onNotifyBatteryFullyCharged(this)
        }

        return false
    }

    override fun onStopJob(params: JobParameters?) = false
}