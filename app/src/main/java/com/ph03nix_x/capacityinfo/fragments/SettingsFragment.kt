package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.appcompat.app.AppCompatDelegate
import com.ph03nix_x.capacityinfo.SettingsInterface
import com.ph03nix_x.capacityinfo.services.ServiceInterface
import com.ph03nix_x.capacityinfo.activity.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat(), ServiceInterface, SettingsInterface {

    private lateinit var pref: SharedPreferences

    // Service and Notification

    private var enableService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var showInformationWhileCharging: SwitchPreferenceCompat? = null
    private var serviceHours: SwitchPreferenceCompat? = null
    private var showCapacityAddedInNotification: SwitchPreferenceCompat? = null
    private var showInformationDuringDischarge: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var showCapacityAddedLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettings: Preference? = null
    private var notificationRefreshRate: Preference? = null

    // Appearance

    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null

    // Other

    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var changeDesignCapacity: Preference? = null
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        else {

            if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true)) {

                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Service and Notification

        enableService = findPreference(Preferences.IsEnableService.prefKey)!!

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        showInformationWhileCharging = findPreference(Preferences.IsShowInformationWhileCharging.prefKey)

        serviceHours = findPreference(Preferences.IsServiceHours.prefKey)

        showCapacityAddedInNotification = findPreference(Preferences.IsShowCapacityAddedInNotification.prefKey)

        showInformationDuringDischarge = findPreference(Preferences.IsShowInformationDuringDischarge.prefKey)

        showLastChargeTimeInNotification = findPreference(Preferences.IsShowLastChargeTimeInNotification.prefKey)

        showCapacityAddedLastChargeTimeInNotification = findPreference(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey)

        openNotificationCategorySettings = findPreference("open_notification_category_settings")

        notificationRefreshRate = findPreference(Preferences.NotificationRefreshRate.prefKey)

        showStopService?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showInformationWhileCharging?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        serviceHours?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)

        showInformationDuringDischarge?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showLastChargeTimeInNotification?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        notificationRefreshRate?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        enableService?.setOnPreferenceChangeListener { _, newValue ->

            if(!(newValue as Boolean) && CapacityInfoService.instance != null) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else if(newValue && CapacityInfoService.instance == null) startService(requireContext())

            showInformationWhileCharging?.isEnabled = newValue
            serviceHours?.isEnabled = newValue
            showInformationDuringDischarge?.isEnabled = newValue
            showLastChargeTimeInNotification?.isEnabled = newValue
            showCapacityAddedLastChargeTimeInNotification?.isEnabled = newValue
            showStopService?.isEnabled = newValue
            showCapacityAddedInNotification?.isEnabled = newValue
            openNotificationCategorySettings?.isEnabled = newValue
            notificationRefreshRate?.isEnabled = newValue

            return@setOnPreferenceChangeListener true
        }

        showStopService?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        showInformationWhileCharging?.setOnPreferenceChangeListener { _ , newValue ->

            serviceHours?.isEnabled = newValue as Boolean

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        serviceHours?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        showCapacityAddedInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        showInformationDuringDischarge?.setOnPreferenceChangeListener { _ , newValue ->

            showLastChargeTimeInNotification?.isEnabled = newValue as Boolean
            notificationRefreshRate?.isEnabled = newValue

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        showLastChargeTimeInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        showCapacityAddedLastChargeTimeInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            openNotificationCategorySettings?.setOnPreferenceClickListener {

                openNotificationCategorySettings(requireContext())

                return@setOnPreferenceClickListener true
            }

        else openNotificationCategorySettings?.isVisible = false

        notificationRefreshRate?.setOnPreferenceClickListener {
            notificationRefreshRateDialog(requireContext(), pref)
            return@setOnPreferenceClickListener true
        }

        // Appearance

        autoDarkMode = findPreference(Preferences.IsAutoDarkMode.prefKey)

        darkMode = findPreference(Preferences.IsDarkMode.prefKey)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled = !pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true)

        autoDarkMode?.setOnPreferenceChangeListener { _, newValue ->

            MainActivity.instance?.recreate()

            if(!(newValue as Boolean)) {

                darkMode?.isEnabled = true

                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }

            else {

                darkMode?.isEnabled = false

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            return@setOnPreferenceChangeListener true
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->

            AppCompatDelegate.setDefaultNightMode(if(newValue as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            return@setOnPreferenceChangeListener true
        }

        // Other

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        about = findPreference("about")

        feedback = findPreference("feedback")

        temperatureInFahrenheit?.setOnPreferenceChangeListener { _, _ ->

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        voltageInMv?.setOnPreferenceChangeListener { _, _ ->

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification(requireContext())

            return@setOnPreferenceChangeListener true
        }

        changeDesignCapacity?.setOnPreferenceClickListener {

            changeDesignCapacity(requireContext(), pref)

            return@setOnPreferenceClickListener true
        }

        about?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireActivity().getString(R.string.about)

           requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, AboutFragment())
                commit()
            }

            return@setOnPreferenceClickListener true
        }

        feedback?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireActivity().getString(R.string.feedback)

            requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, FeedbackFragment())
                commit()
            }

            return@setOnPreferenceClickListener true
        }
    }


}