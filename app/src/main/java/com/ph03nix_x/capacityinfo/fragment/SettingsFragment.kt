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

    // Service and Notification

    private var enableService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var showInformationWhileCharging: SwitchPreferenceCompat? = null
    private var showInformationDuringDischarge: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettings: Preference? = null
    private var notificationRefreshRate: Preference? = null

    // Appearance

    private var darkMode: SwitchPreferenceCompat? = null

    // Other

    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInApp: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var changeDesignCapacity: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        // Service and Notification

        enableService = findPreference(Preferences.EnableService.prefKey)!!

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        showInformationWhileCharging = findPreference(Preferences.IsShowInformationWhileCharging.prefKey)

        showInformationDuringDischarge = findPreference(Preferences.IsShowInformationDuringDischarge.prefKey)

        showLastChargeTimeInNotification = findPreference(Preferences.IsShowLastChargeTimeInNotification.prefKey)

        openNotificationCategorySettings = findPreference("open_notification_category_settings")

        notificationRefreshRate = findPreference(Preferences.NotificationRefreshRate.prefKey)

        showStopService?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)

        showInformationWhileCharging?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)

        showInformationDuringDischarge?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)

        showLastChargeTimeInNotification?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)

        notificationRefreshRate?.isEnabled = pref.getBoolean(Preferences.EnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        enableService?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else {

                if(CapacityInfoService.instance == null) startService()
            }

            showInformationWhileCharging?.isEnabled = newValue
            showInformationDuringDischarge?.isEnabled = newValue
            showLastChargeTimeInNotification?.isEnabled = newValue
            showStopService?.isEnabled = newValue
            openNotificationCategorySettings?.isEnabled = newValue
            notificationRefreshRate?.isEnabled = newValue

            return@setOnPreferenceChangeListener true
        }

        showStopService?.setOnPreferenceChangeListener { _, b ->

            pref.edit().putBoolean(Preferences.IsShowServiceStop.prefKey, b as Boolean).apply()

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

        showInformationWhileCharging?.setOnPreferenceChangeListener { _ , newValue ->

            pref.edit().putBoolean(Preferences.IsShowInformationWhileCharging.prefKey, newValue as Boolean).apply()

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

            pref.edit().putBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, newValue as Boolean).apply()

            showLastChargeTimeInNotification?.isEnabled = newValue
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

        showLastChargeTimeInNotification?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.IsShowLastChargeTimeInNotification.prefKey, newValue as Boolean).apply()

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

        // Appearance

        darkMode = findPreference(Preferences.DarkMode.prefKey)

        darkMode?.setOnPreferenceChangeListener { _, _ ->

            MainActivity.instance!!.recreate()
            requireActivity().recreate()

            return@setOnPreferenceChangeListener true
        }

        // Other

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        showLastChargeTimeInApp = findPreference(Preferences.IsShowLastChargeTimeInApp.prefKey)

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        temperatureInFahrenheit?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.TemperatureInFahrenheit.prefKey, newValue as Boolean).apply()

            if(pref.getBoolean(Preferences.EnableService.prefKey, true)) {

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

        showLastChargeTimeInApp?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.IsShowLastChargeTimeInApp.prefKey, newValue as Boolean).apply()

            return@setOnPreferenceChangeListener true
        }

        voltageInMv?.setOnPreferenceChangeListener { _, newValue ->

            pref.edit().putBoolean(Preferences.VoltageInMv.prefKey, newValue as Boolean).apply()

            if(pref.getBoolean(Preferences.EnableService.prefKey, true)) {

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

        changeDesignCapacity?.setOnPreferenceClickListener {

            changeDesignCapacity()

            return@setOnPreferenceClickListener true
        }
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context?.startForegroundService(Intent(context, CapacityInfoService::class.java))

        else context?.startService(Intent(context, CapacityInfoService::class.java))
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

        val time = pref.getLong(Preferences.NotificationRefreshRate.prefKey,40)

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

            pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()
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

                CapacityInfoService.instance?.sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)

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

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefKey, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1).toString())

        dialog.setPositiveButton(getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefKey, changeDesignCapacity.text.toString().toInt()).apply()

            if(pref.getBoolean(Preferences.EnableService.prefKey, true)) {

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
}