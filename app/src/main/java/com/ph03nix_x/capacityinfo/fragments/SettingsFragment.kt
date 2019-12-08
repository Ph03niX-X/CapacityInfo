package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat(), ServiceInterface, SettingsInterface, DebugOptionsInterface {

    private lateinit var pref: SharedPreferences

    // Service and Notification
    private var enableService: SwitchPreferenceCompat? = null
    private var isAutoStartService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var serviceHours: SwitchPreferenceCompat? = null
    private var showCapacityAddedInNotification: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var showCapacityAddedLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettings: Preference? = null

    // Appearance
    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null

    // Other
    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var changeDesignCapacity: Preference? = null
    private var about: Preference? = null
    private var feedback: Preference? = null

    //Debug
    private var debug: PreferenceCategory? = null
    private var changePrefKey: Preference? = null
    private var removePrefKey: Preference? = null
    private var clearPref: Preference? = null
    private var hideDebug: Preference? = null

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

        enableService = findPreference(Preferences.IsEnableService.prefKey)

        isAutoStartService = findPreference(Preferences.IsAutoStartService.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        serviceHours = findPreference(Preferences.IsServiceHours.prefKey)

        showCapacityAddedInNotification = findPreference(Preferences.IsShowCapacityAddedInNotification.prefKey)

        showLastChargeTimeInNotification = findPreference(Preferences.IsShowLastChargeTimeInNotification.prefKey)

        showCapacityAddedLastChargeTimeInNotification = findPreference(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey)

        openNotificationCategorySettings = findPreference("open_notification_category_settings")

        isAutoStartService?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showStopService?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        serviceHours?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showCapacityAddedInNotification?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showCapacityAddedLastChargeTimeInNotification?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showLastChargeTimeInNotification?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        enableService?.setOnPreferenceChangeListener { _, newValue ->

            if(!(newValue as Boolean) && CapacityInfoService.instance != null) requireContext().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else if(newValue && CapacityInfoService.instance == null) startService(requireContext())

            isAutoStartService?.isEnabled = newValue
            serviceHours?.isEnabled = newValue
            showLastChargeTimeInNotification?.isEnabled = newValue
            showCapacityAddedLastChargeTimeInNotification?.isEnabled = newValue
            showStopService?.isEnabled = newValue
            showCapacityAddedInNotification?.isEnabled = newValue
            openNotificationCategorySettings?.isEnabled = newValue

            return@setOnPreferenceChangeListener true
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            openNotificationCategorySettings?.setOnPreferenceClickListener {

                openNotificationCategorySettings(requireContext())

                return@setOnPreferenceClickListener true
            }

        else openNotificationCategorySettings?.isVisible = false

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

        // Debug

        debug = findPreference("debug")

        changePrefKey = findPreference("change_preference_key")

        removePrefKey = findPreference("remove_preference_key")

        clearPref = findPreference("clear_pref")

        hideDebug = findPreference("hide_debug")

        debug?.isVisible = pref.getBoolean(Preferences.IsShowDebug.prefKey, false)

        if(pref.getBoolean(Preferences.IsShowDebug.prefKey, false)) {

            changePrefKey?.setOnPreferenceClickListener {

                changeKeyDialog(requireContext(), pref)

                return@setOnPreferenceClickListener true
            }

            removePrefKey?.setOnPreferenceClickListener {

                removeKeyDialog(requireContext(), pref)

                return@setOnPreferenceClickListener true
            }

            clearPref?.setOnPreferenceClickListener {

                clearPref(requireContext(), pref)

                return@setOnPreferenceClickListener true
            }

            hideDebug?.setOnPreferenceClickListener {

                debug?.isVisible = false

                pref.edit().putBoolean(Preferences.IsShowDebug.prefKey, false).apply()

                Toast.makeText(requireContext(), getString(R.string.debug_options_are_hidden), Toast.LENGTH_LONG).show()

                return@setOnPreferenceClickListener true
            }
        }
    }
}