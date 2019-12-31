package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat(), ServiceInterface, SettingsInterface, DebugOptionsInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var moreServiceAndNotificationDefIcon: Drawable
    private lateinit var moreOtherDefIcon: Drawable

    // Service and Notification
    private var enableService: SwitchPreferenceCompat? = null
    private var isAutoStartService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var serviceHours: SwitchPreferenceCompat? = null
    private var moreServiceAndNotification: Preference? = null
    private var showCapacityAddedInNotification: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var showCapacityAddedLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettings: Preference? = null

    // Appearance
    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null

    // Other
    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var moreOther: Preference? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var changeDesignCapacity: Preference? = null

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            if(pref.contains(Preferences.IsAutoDarkMode.prefKey)) pref.edit().remove(Preferences.IsAutoDarkMode.prefKey).apply()
        }

        else if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        else if(pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // Service and Notification

        enableService = findPreference(Preferences.IsEnableService.prefKey)

        isAutoStartService = findPreference(Preferences.IsAutoStartService.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        serviceHours = findPreference(Preferences.IsServiceHours.prefKey)

        moreServiceAndNotification = findPreference("more_service_and_notification")

        moreServiceAndNotificationDefIcon = moreServiceAndNotification!!.icon

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

            true
        }

        moreServiceAndNotification?.setOnPreferenceClickListener {

            if(it.icon == moreServiceAndNotificationDefIcon) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)

                showCapacityAddedInNotification?.isVisible = true
                showLastChargeTimeInNotification?.isVisible = true
                showCapacityAddedInNotification?.isVisible = true
                openNotificationCategorySettings?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)

                showCapacityAddedInNotification?.isVisible = false
                showLastChargeTimeInNotification?.isVisible = false
                showCapacityAddedInNotification?.isVisible = false
                openNotificationCategorySettings?.isVisible = false

                moreServiceAndNotificationDefIcon = it.icon
            }

            true
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            openNotificationCategorySettings?.setOnPreferenceClickListener {

                openNotificationCategorySettings(requireContext())

                true
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

            true
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->

            AppCompatDelegate.setDefaultNightMode(if(newValue as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            true
        }

        // Other

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        moreOther = findPreference("more_other")

        moreOtherDefIcon = moreOther!!.icon

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        moreOther?.setOnPreferenceClickListener {

            if(it.icon == moreOtherDefIcon) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)

                showCapacityAddedLastChargeTimeInNotification?.isVisible = true
                voltageInMv?.isVisible = true
                changeDesignCapacity?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)

                showCapacityAddedLastChargeTimeInNotification?.isVisible = false
                voltageInMv?.isVisible = false
                changeDesignCapacity?.isVisible = false

                moreOtherDefIcon = it.icon
            }

            true
        }

        changeDesignCapacity?.setOnPreferenceClickListener {

            changeDesignCapacity(requireContext(), pref)

            true
        }


        // About & Feedback

        about = findPreference("about")

        feedback = findPreference("feedback")

        about?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireActivity().getString(R.string.about)

           requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, AboutFragment())
                commit()
            }

            true
        }

        feedback?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireActivity().getString(R.string.feedback)

            requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, FeedbackFragment())
                commit()
            }

            true
        }
    }
}