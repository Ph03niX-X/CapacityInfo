package com.ph03nix_x.capacityinfo.fragment

import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.activity.isJob
import com.ph03nix_x.capacityinfo.activity.sleepArray
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        val darkMode: SwitchPreference = findPreference(Preferences.DarkMode.prefName)!!
        darkMode.setOnPreferenceChangeListener { _, _ ->
            MainActivity.instance!!.recreate()
            requireActivity().recreate()
            return@setOnPreferenceChangeListener true
        }

        val openNotificationCategorySettings = findPreference<Preference>("open_notification_category_settings")

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)
                && pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        openNotificationCategorySettings?.setOnPreferenceClickListener {

            openNotificationCategorySettings()

            return@setOnPreferenceClickListener true
        }

        else openNotificationCategorySettings?.isVisible = false

        val notificationRefreshRate: Preference = findPreference(Preferences.NotificationRefreshRate.prefName)!!
        notificationRefreshRate.setOnPreferenceClickListener {
            notificationRefreshRateDialog()
            return@setOnPreferenceClickListener true
        }

        val enableService: SwitchPreference = findPreference(Preferences.EnableService.prefName)!!

        val alwaysShowNotification: SwitchPreference = findPreference(Preferences.AlwaysShowNotification.prefName)!!
        alwaysShowNotification.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)

        alwaysShowNotification.setOnPreferenceChangeListener { _, newValue ->
            val b = newValue as Boolean

            val batteryIntent = requireActivity().registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            if(!b && plugged == 0) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            pref.edit().putBoolean(Preferences.AlwaysShowNotification.prefName, b).apply()

            openNotificationCategorySettings?.isEnabled = b

            notificationRefreshRate.isEnabled = b

            if(!isJob) startJob()
            return@setOnPreferenceChangeListener true
        }

        notificationRefreshRate.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)
                && pref.getBoolean(Preferences.AlwaysShowNotification.prefName, true)

        enableService.setOnPreferenceChangeListener { _, newValue ->
            val b = newValue as Boolean

            if(!b) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else startJob()

            alwaysShowNotification.isEnabled = b
            openNotificationCategorySettings?.isEnabled = b && pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)
            notificationRefreshRate.isEnabled = b && pref.getBoolean(Preferences.AlwaysShowNotification.prefName, false)
            return@setOnPreferenceChangeListener true
        }

        val changeDesignCapacity: Preference = findPreference("change_design_capacity")!!
        changeDesignCapacity.setOnPreferenceClickListener {
            changeDesignCapacity()
            return@setOnPreferenceClickListener true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationCategorySettings() {

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager.getNotificationChannel("service_channel")

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)

        }

        startActivity(intent)

    }


    private fun notificationRefreshRateDialog() {

        val dialog = MaterialAlertDialogBuilder(requireActivity())

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.notification_refresh_rate, null)

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

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.change_design_capacity, null)

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

        val componentName = ComponentName(requireActivity(), CapacityInfoJob::class.java)

        val jobScheduler = requireActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}
