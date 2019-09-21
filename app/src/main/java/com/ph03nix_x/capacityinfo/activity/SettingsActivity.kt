package com.ph03nix_x.capacityinfo.activity

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class SettingsActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var enableService: SwitchCompat
    private lateinit var alwaysShowNotification: SwitchCompat
    private lateinit var notificationRefreshRate: TextView
    private lateinit var darkMode: SwitchCompat
    private lateinit var fahrenheit: SwitchCompat
    private lateinit var showLastChargeTime: SwitchCompat
    private lateinit var settingsLayout: LinearLayout
    private lateinit var changeDesignCapacity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(pref.getBoolean(Preferences.DarkMode.prefName, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        enableService = findViewById(R.id.enable_service)
        alwaysShowNotification = findViewById(R.id.always_show_notification)
        notificationRefreshRate = findViewById(R.id.notification_refresh_rate)
        darkMode = findViewById(R.id.dark_mode)
        fahrenheit = findViewById(R.id.temperature_in_fahrenheit)
        showLastChargeTime = findViewById(R.id.show_last_charge_time)
        settingsLayout = findViewById(R.id.settings_layout)
        changeDesignCapacity = findViewById(R.id.change_design_capacity)

        if(pref.getBoolean(Preferences.DarkMode.prefName, false)) {

            settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))

            enableService.background = getDrawable(R.drawable.selecteditem)
            darkMode.background = getDrawable(R.drawable.selecteditem)
            notificationRefreshRate.background = getDrawable(R.drawable.selecteditem)
            fahrenheit.background = getDrawable(R.drawable.selecteditem)
            showLastChargeTime.background = getDrawable(R.drawable.selecteditem)
            changeDesignCapacity.background = getDrawable(R.drawable.selecteditem)

            notificationRefreshRate.setTextColor(Color.WHITE)
            changeDesignCapacity.setTextColor(Color.WHITE)
        }

        enableService.isChecked = pref.getBoolean(Preferences.EnableService.prefName, true)
        alwaysShowNotification.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)
        alwaysShowNotification.isChecked = pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)
        notificationRefreshRate.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true) &&
                pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)
        darkMode.isChecked = pref.getBoolean(Preferences.DarkMode.prefName, false)
        fahrenheit.isChecked = pref.getBoolean(Preferences.Fahrenheit.prefName, false)
        showLastChargeTime.isChecked = pref.getBoolean(Preferences.ShowLastChargeTime.prefName, true)

        enableService.setOnCheckedChangeListener { _, b ->

            if(!b) stopService(Intent(this, CapacityInfoService::class.java))

            else startJob()

            alwaysShowNotification.isEnabled = b

            notificationRefreshRate.isEnabled = b

            pref.edit().putBoolean(Preferences.EnableService.prefName, b).apply()
        }

        alwaysShowNotification.setOnCheckedChangeListener { _, b ->

            val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            if(!b && plugged == 0) stopService(Intent(this, CapacityInfoService::class.java))

            pref.edit().putBoolean(Preferences.AlwaysShowNotification.prefName, b).apply()

            notificationRefreshRate.isEnabled = b

            startJob()
        }

        notificationRefreshRate.setOnClickListener { notificationRefreshRateDialog() }

        darkMode.setOnCheckedChangeListener { _, b ->

            pref.edit().putBoolean(Preferences.DarkMode.prefName, b).apply()
            MainActivity.instance!!.recreate()
            recreate()
        }

        fahrenheit.setOnCheckedChangeListener { _, b ->  pref.edit().putBoolean(Preferences.Fahrenheit.prefName, b).apply() }

        showLastChargeTime.setOnCheckedChangeListener { _, b -> pref.edit().putBoolean(Preferences.ShowLastChargeTime.prefName, b).apply() }

        changeDesignCapacity.setOnClickListener { changeDesignCapacity() }
    }

    private fun notificationRefreshRateDialog() {

        val dialog = AlertDialog.Builder(this)

        val view = LayoutInflater.from(this).inflate(R.layout.notification_refresh_rate, null)

        dialog.setView(view)

        val notificationRefreshRate = view.findViewById<TextView>(R.id.notification_refresh_rate_textView)

        val notificationRefreshRateSeekBar = view.findViewById<SeekBar>(R.id.notification_refresh_rate_seekBar)

        val time = pref.getLong(Preferences.NotificationRefreshRate.prefName,40)

        when(time) {

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

        if(time !in sleepArray) {

            notificationRefreshRateSeekBar.progress = 57

            pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 40).apply()
        }

        notificationRefreshRate.text = getString(if(time != 60.toLong()) R.string.seconds else R.string.minute, if(time < 60) time.toString() else "1")

        notificationRefreshRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                when(progress) {

                    in 0..8 -> notificationRefreshRate.text = getString(R.string.seconds, "5")

                    in 9..16 -> notificationRefreshRate.text = getString(R.string.seconds, "10")

                    in 17..24 -> notificationRefreshRate.text = getString(R.string.seconds, "15")

                    in 25..32 -> notificationRefreshRate.text = getString(R.string.seconds, "20")

                    in 33..40 -> notificationRefreshRate.text = getString(R.string.seconds, "25")

                    in 41..48 -> notificationRefreshRate.text = getString(R.string.seconds, "30")

                    in 49..56 -> notificationRefreshRate.text = getString(R.string.seconds, "35")

                    in 57..64 -> notificationRefreshRate.text = getString(R.string.seconds, "40")

                    in 65..72 -> notificationRefreshRate.text = getString(R.string.seconds, "45")

                    in 73..80 -> notificationRefreshRate.text = getString(R.string.seconds, "50")

                    in 81..88 -> notificationRefreshRate.text = getString(R.string.seconds, "55")

                    in 89..100 -> notificationRefreshRate.text = getString(R.string.minute, "1")
                }

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialog.apply {

            setTitle(getString(R.string.notification_refresh_rate))

            setPositiveButton(getString(R.string.apply)) { _, _ ->

                when(notificationRefreshRateSeekBar.progress) {

                    in 0..8 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 5).apply()

                    in 9..16 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 10).apply()

                    in 17..24 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 15).apply()

                    in 25..32 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 20).apply()

                    in 33..40 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 25).apply()

                    in 41..48 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 30).apply()

                    in 49..56 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 35).apply()

                    in 57..64 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 40).apply()

                    in 65..72 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 45).apply()

                    in 73..80 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 50).apply()

                    in 81..88 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 55).apply()

                    in 89..100 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefName, 60).apply()
                }

                CapacityInfoService.instance?.sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefName, 40)
            }

            setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

            show()
        }
    }

    private fun changeDesignCapacity() {

        val dialog = AlertDialog.Builder(this)

        val view = LayoutInflater.from(this).inflate(R.layout.change_design_capacity, null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<EditText>(R.id.change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefName, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefName, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefName, 0) / -1).toString())

        dialog.setPositiveButton(getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefName, changeDesignCapacity.text.toString().toInt()).apply() }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    private fun startJob() {

        val componentName = ComponentName(this, CapacityInfoJob::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}