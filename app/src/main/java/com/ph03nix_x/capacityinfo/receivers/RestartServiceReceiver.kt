package com.ph03nix_x.capacityinfo.receivers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob

class RestartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent!!.action) {

            Intent.ACTION_MY_PACKAGE_REPLACED -> {

                migrateToDefaultPrefs(context!!)

                startCapacityInfoJob(context)
            }
        }
    }

    private fun migrateToDefaultPrefs(context: Context) {
        val oldPrefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val newPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (!newPrefs.getBoolean("migrated", false)
            && !oldPrefs.getBoolean(Preferences.IsShowInstruction.prefName, true)) {
            val editor = newPrefs.edit()
            editor.putBoolean(Preferences.DarkMode.prefName, oldPrefs.getBoolean(Preferences.DarkMode.prefName, false))
                .putBoolean(Preferences.EnableService.prefName, oldPrefs.getBoolean(Preferences.EnableService.prefName, true))
                .putBoolean(
                    Preferences.AlwaysShowNotification.prefName, oldPrefs.getBoolean(
                        Preferences.AlwaysShowNotification.prefName, false))
                .putLong(Preferences.NotificationRefreshRate.prefName, oldPrefs.getLong(Preferences.NotificationRefreshRate.prefName, 40))
                .putBoolean(Preferences.Fahrenheit.prefName, oldPrefs.getBoolean(Preferences.Fahrenheit.prefName, false))
                .putBoolean(Preferences.ShowLastChargeTime.prefName, oldPrefs.getBoolean(Preferences.ShowLastChargeTime.prefName, true))
                .putInt(Preferences.DesignCapacity.prefName, oldPrefs.getInt(Preferences.DesignCapacity.prefName, 0))
                .putInt(Preferences.ChargeCounter.prefName, oldPrefs.getInt(Preferences.ChargeCounter.prefName, 0))
                .putBoolean(Preferences.IsShowInstruction.prefName, oldPrefs.getBoolean(Preferences.IsShowInstruction.prefName, false))
                .putBoolean(Preferences.IsSupported.prefName, oldPrefs.getBoolean(Preferences.IsSupported.prefName, true))
                .putInt(Preferences.LastChargeTime.prefName, oldPrefs.getInt(Preferences.LastChargeTime.prefName, 0))
                .putInt(Preferences.BatteryLevelWith.prefName, oldPrefs.getInt(Preferences.BatteryLevelWith.prefName, 0))
                .putInt(Preferences.BatteryLevelTo.prefName, oldPrefs.getInt(Preferences.BatteryLevelTo.prefName, 0)).apply()

            editor.putBoolean("migrated", true).apply()
        }
    }

    private fun startCapacityInfoJob(context: Context) {

        val componentName = ComponentName(context, CapacityInfoJob::class.java)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}