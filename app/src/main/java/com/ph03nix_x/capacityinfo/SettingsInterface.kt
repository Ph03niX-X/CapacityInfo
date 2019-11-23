package com.ph03nix_x.capacityinfo

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface SettingsInterface {

    companion object {

        var isJobUpdateNotification = false
        var progressSeekBar = -1
    }

    fun updateNotification(context: Context) {

        if(!isJobUpdateNotification)
            GlobalScope.launch {

                isJobUpdateNotification = true

                delay(1000)
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                if(intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) == 0) CapacityInfoService.instance?.updateNotification(context)
                isJobUpdateNotification = false
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openNotificationCategorySettings(context: Context) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager.getNotificationChannel("service_channel")

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

        }

        context.startActivity(intent)
    }

    fun notificationRefreshRateDialog(context: Context, pref: SharedPreferences) {

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.notification_refresh_rate, null)

        dialog.setView(view)

        val notificationRefreshRate = view.findViewById<TextView>(R.id.notification_refresh_rate_textView)

        val notificationRefreshRateSeekBar = view.findViewById<SeekBar>(R.id.notification_refresh_rate_seekBar)

        val notificationRefreshRatePref = pref.getLong(Preferences.NotificationRefreshRate.prefKey,40)

        setProgress(notificationRefreshRatePref, notificationRefreshRateSeekBar)

        getNotificationRefreshRateTime(context, pref, notificationRefreshRatePref, notificationRefreshRate, notificationRefreshRateSeekBar)

        notificationRefreshRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { progressChanged(context, progress, notificationRefreshRate); progressSeekBar = progress }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialogApply(dialog, pref)
    }

    private fun setProgress(notificationRefreshRate: Long, notificationRefreshRateSeekBar: SeekBar) {

        when(notificationRefreshRate) {

            5.toLong() -> notificationRefreshRateSeekBar.progress = 0
            10.toLong() -> notificationRefreshRateSeekBar.progress = 9
            15.toLong() -> notificationRefreshRateSeekBar.progress = 17
            20.toLong() -> notificationRefreshRateSeekBar.progress = 25
            25.toLong() -> notificationRefreshRateSeekBar.progress = 33
            30.toLong() -> notificationRefreshRateSeekBar.progress = 41
            35.toLong() -> notificationRefreshRateSeekBar.progress = 49
            40.toLong() -> notificationRefreshRateSeekBar.progress = 57
            45.toLong() -> notificationRefreshRateSeekBar.progress = 65
            50.toLong() -> notificationRefreshRateSeekBar.progress = 73
            55.toLong() -> notificationRefreshRateSeekBar.progress = 81
            60.toLong() -> notificationRefreshRateSeekBar.progress = 100
        }
    }

    private fun getNotificationRefreshRateTime(context: Context, pref: SharedPreferences, notificationRefreshRatePref: Long, notificationRefreshRate: TextView, notificationRefreshRateSeekBar: SeekBar) {

        val sleepArray = arrayOf<Long>(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)

        if(notificationRefreshRatePref !in sleepArray) {

            notificationRefreshRateSeekBar.progress = 57

            pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()
        }

        notificationRefreshRate.text = context.getString(if(notificationRefreshRatePref != 60.toLong()) R.string.seconds
        else R.string.minute, if(notificationRefreshRatePref < 60) notificationRefreshRatePref.toString() else "1")
    }

    private fun progressChanged(context: Context, progress: Int, notificationRefreshRate: TextView) {

        when(progress) {

            in 0..8 -> notificationRefreshRate.text = context.getString(R.string.seconds, "5")

            in 9..16 -> notificationRefreshRate.text = context.getString(R.string.seconds, "10")

            in 17..24 -> notificationRefreshRate.text = context.getString(R.string.seconds, "15")

            in 25..32 -> notificationRefreshRate.text = context.getString(R.string.seconds, "20")

            in 33..40 -> notificationRefreshRate.text = context.getString(R.string.seconds, "25")

            in 41..48 -> notificationRefreshRate.text = context.getString(R.string.seconds, "30")

            in 49..56 -> notificationRefreshRate.text = context.getString(R.string.seconds, "35")

            in 57..64 -> notificationRefreshRate.text = context.getString(R.string.seconds, "40")

            in 65..72 -> notificationRefreshRate.text = context.getString(R.string.seconds, "45")

            in 73..80 -> notificationRefreshRate.text = context.getString(R.string.seconds, "50")

            in 81..88 -> notificationRefreshRate.text = context.getString(R.string.seconds, "55")

            in 89..100 -> notificationRefreshRate.text = context.getString(R.string.minute, "1")
        }
    }

    private fun dialogApply(dialog: MaterialAlertDialogBuilder, pref: SharedPreferences) {

        dialog.apply {

            setTitle(context.getString(R.string.notification_refresh_rate))

            setPositiveButton(context.getString(R.string.apply)) { _, _ ->

                when(progressSeekBar) {

                    in 0..8 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 5).apply()

                    in 9..16 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 10).apply()

                    in 17..24 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 15).apply()

                    in 25..32 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 20).apply()

                    in 33..40 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 25).apply()

                    in 41..48 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 30).apply()

                    in 49..56 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 35).apply()

                    in 57..64 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()

                    in 65..72 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 45).apply()

                    in 73..80 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 50).apply()

                    in 81..88 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 55).apply()

                    in 89..100 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 60).apply()
                }

                progressSeekBar = -1
            }

            setNegativeButton(context.getString(android.R.string.cancel)) { _, _ -> progressSeekBar = -1 }

            show()
        }
    }

    fun changeDesignCapacity(context: Context, pref: SharedPreferences) {

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_design_capacity, null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<EditText>(R.id.change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefKey, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefKey, changeDesignCapacity.text.toString().toInt()).apply()

            CapacityInfoService.instance?.sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification(context)
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }
}