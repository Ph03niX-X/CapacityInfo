package com.ph03nix_x.capacityinfo.fragment

import android.app.NotificationManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.activity.sleepArray
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

var tempSeconds = 1
var tempBatteryLevelWith = -1
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences
    private var showStopService: SwitchPreferenceCompat? = null
    private var showInformationWhileCharging: SwitchPreferenceCompat? = null
    private var showInformationDuringDischarge: SwitchPreferenceCompat? = null
    private var notificationRefreshRate: Preference? = null
    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var showLastChargeTime: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        temperatureInFahrenheit = findPreference(Preferences.Fahrenheit.prefName)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefName)

        showInformationWhileCharging = findPreference(Preferences.IsShowInformationWhileCharging.prefName)

        showInformationDuringDischarge = findPreference(Preferences.IsShowInformationDuringDischarge.prefName)

        notificationRefreshRate = findPreference(Preferences.NotificationRefreshRate.prefName)

        showLastChargeTime = findPreference(Preferences.ShowLastChargeTime.prefName)

        voltageInMv = findPreference(Preferences.VoltageInMv.prefName)

        showStopService?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)

        showInformationWhileCharging?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)

        showInformationDuringDischarge?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)

        notificationRefreshRate?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefName, true)

        showInformationWhileCharging?.setOnPreferenceChangeListener { _ , newValue ->

            pref.edit().putBoolean(Preferences.IsShowInformationWhileCharging.prefName, newValue as Boolean).apply()

            if(CapacityInfoService.instance != null) {

                tempSeconds = CapacityInfoService.instance?.seconds!!

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                context?.stopService(Intent(context, CapacityInfoService::class.java))
            }

            Handler().postDelayed({

                startService()

            }, 1000)

            return@setOnPreferenceChangeListener true
        }

        showInformationDuringDischarge?.setOnPreferenceChangeListener { _ , newValue ->

            pref.edit().putBoolean(Preferences.IsShowInformationDuringDischarge.prefName, newValue as Boolean).apply()

            notificationRefreshRate?.isEnabled = newValue

            if(CapacityInfoService.instance != null) {

                tempSeconds = CapacityInfoService.instance?.seconds!!

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                context?.stopService(Intent(context, CapacityInfoService::class.java))
            }

            Handler().postDelayed({

                startService()

            }, 1000)

            return@setOnPreferenceChangeListener true
        }

        temperatureInFahrenheit?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.Fahrenheit.prefName, newValue as Boolean).apply()

            if(pref.getBoolean(Preferences.EnableService.prefName, true)) {

                if(CapacityInfoService.instance != null) {

                    tempSeconds = CapacityInfoService.instance?.seconds!!

                    tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                    context?.stopService(Intent(context, CapacityInfoService::class.java))
                }

                Handler().postDelayed({

                    startService()

                }, 1000)
            }

            return@setOnPreferenceChangeListener true
        }

        showLastChargeTime?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.ShowLastChargeTime.prefName, newValue as Boolean).apply()

            if(pref.getBoolean(Preferences.EnableService.prefName, true)) {

                if(CapacityInfoService.instance != null) {

                    tempSeconds = CapacityInfoService.instance?.seconds!!

                    tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                    context?.stopService(Intent(context, CapacityInfoService::class.java))
                }

                Handler().postDelayed({

                    startService()

                }, 1000)
            }

            return@setOnPreferenceChangeListener true
        }

        voltageInMv?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.VoltageInMv.prefName, newValue as Boolean).apply()

            if(pref.getBoolean(Preferences.EnableService.prefName, true)) {

                if(CapacityInfoService.instance != null) {

                    tempSeconds = CapacityInfoService.instance?.seconds!!

                    tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                    context?.stopService(Intent(context, CapacityInfoService::class.java))
                }

                Handler().postDelayed({

                    startService()

                }, 1000)
            }

            return@setOnPreferenceChangeListener true
        }

        val darkMode: SwitchPreferenceCompat = findPreference(Preferences.DarkMode.prefName)!!
        darkMode.setOnPreferenceChangeListener { _, _ ->
            MainActivity.instance!!.recreate()
            requireActivity().recreate()
            return@setOnPreferenceChangeListener true
        }

        val openNotificationCategorySettings = findPreference<Preference>("open_notification_category_settings")

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.EnableService.prefName, true)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

        openNotificationCategorySettings?.setOnPreferenceClickListener {

            openNotificationCategorySettings()

            return@setOnPreferenceClickListener true
        }

        else openNotificationCategorySettings?.isVisible = false

        notificationRefreshRate?.setOnPreferenceClickListener {
            notificationRefreshRateDialog()
            return@setOnPreferenceClickListener true
        }

        val enableService: SwitchPreferenceCompat = findPreference(Preferences.EnableService.prefName)!!

        enableService.setOnPreferenceChangeListener { _, newValue ->

            val b = newValue as Boolean

            if(!b) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else {

                if(CapacityInfoService.instance == null) startService()
            }

            showInformationWhileCharging?.isEnabled = b
            showInformationDuringDischarge?.isEnabled = b
            showStopService?.isEnabled = b
            openNotificationCategorySettings?.isEnabled = b
            notificationRefreshRate?.isEnabled = b
            return@setOnPreferenceChangeListener true
        }

        showStopService?.setOnPreferenceChangeListener { _, b ->

            pref.edit().putBoolean(Preferences.IsShowServiceStop.prefName, b as Boolean).apply()

            if(CapacityInfoService.instance != null) {

                tempSeconds = CapacityInfoService.instance?.seconds!!

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                context?.stopService(Intent(context, CapacityInfoService::class.java))
            }

            Handler().postDelayed( {

                startService()

            }, 1000)

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

                tempSeconds = CapacityInfoService.instance?.seconds!!

                tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                context.stopService(Intent(context, CapacityInfoService::class.java))

                Handler().postDelayed( {

                    startService()

                }, 1000)
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

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefName, changeDesignCapacity.text.toString().toInt()).apply()

            if(pref.getBoolean(Preferences.EnableService.prefName, true)) {

                if(CapacityInfoService.instance != null) {

                    tempSeconds = CapacityInfoService.instance?.seconds!!

                    tempBatteryLevelWith = CapacityInfoService.instance?.batteryLevelWith!!

                    context?.stopService(Intent(context, CapacityInfoService::class.java))
                }

                Handler().postDelayed({

                    startService()

                }, 1000)
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(Intent(context, CapacityInfoService::class.java))

        else context?.startService(Intent(context, CapacityInfoService::class.java))
    }
}