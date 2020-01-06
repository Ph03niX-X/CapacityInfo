package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.MainApp.Companion.setModeNight
import com.ph03nix_x.capacityinfo.activity.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat(), ServiceInterface, SettingsInterface, DebugOptionsInterface {

    private lateinit var pref: SharedPreferences

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
    private var currentUnitOfMeasure: ListPreference? = null
    private var changeDesignCapacity: Preference? = null

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        setModeNight(requireContext())

        // Service and Notification

        enableService = findPreference(Preferences.IsEnableService.prefKey)

        isAutoStartService = findPreference(Preferences.IsAutoStartService.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        serviceHours = findPreference(Preferences.IsServiceHours.prefKey)

        moreServiceAndNotification = findPreference("more_service_and_notification")

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

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)
                it.title = requireContext().getString(R.string.hide)

                showCapacityAddedInNotification?.isVisible = true
                showLastChargeTimeInNotification?.isVisible = true
                showCapacityAddedLastChargeTimeInNotification?.isVisible = true
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    openNotificationCategorySettings?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)
                it.title = requireContext().getString(R.string.more)

                showCapacityAddedInNotification?.isVisible = false
                showLastChargeTimeInNotification?.isVisible = false
                showCapacityAddedLastChargeTimeInNotification?.isVisible = false
                openNotificationCategorySettings?.isVisible = false
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

            darkMode?.isEnabled = !(newValue as Boolean)

            if(!newValue)
                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

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

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        currentUnitOfMeasure = findPreference(Preferences.CurrentUnitOfMeasure.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        if(pref.getString(Preferences.CurrentUnitOfMeasure.prefKey, "uA")
            !in resources.getStringArray(R.array.current_unit_of_measure))
            pref.edit().putString(Preferences.CurrentUnitOfMeasure.prefKey, "uA").apply()

        currentUnitOfMeasure?.summary = pref.getString(Preferences.CurrentUnitOfMeasure.prefKey, "uA")

        moreOther?.setOnPreferenceClickListener {

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)
                it.title = requireContext().getString(R.string.hide)

                findPreference<SwitchPreferenceCompat>(Preferences.IsShowCapacityAddedLastChargeInApp.prefKey)?.isVisible = true
                voltageInMv?.isVisible = true
                currentUnitOfMeasure?.isVisible = true
                changeDesignCapacity?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)
                it.title = requireContext().getString(R.string.more)

                findPreference<SwitchPreferenceCompat>(Preferences.IsShowCapacityAddedLastChargeInApp.prefKey)?.isVisible = false
                voltageInMv?.isVisible = false
                currentUnitOfMeasure?.isVisible = false
                changeDesignCapacity?.isVisible = false
            }

            true
        }

        currentUnitOfMeasure?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = newValue as String

            true
        }

        changeDesignCapacity?.setOnPreferenceClickListener {

            changeDesignCapacity(requireContext(), pref, it)

            true
        }


        // About & Feedback

        about = findPreference("about")

        feedback = findPreference("feedback")

        about?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireContext().getString(R.string.about)

           requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, AboutFragment())
                commit()
            }

            true
        }

        feedback?.setOnPreferenceClickListener {

            (activity as SettingsActivity).toolbar.title = requireContext().getString(R.string.feedback)

            requireActivity().supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, FeedbackFragment())
                commit()
            }

            true
        }
    }

    override fun onResume() {

        super.onResume()

        changeDesignCapacity?.summary = requireContext().getString(R.string.change_design_summary, pref.getInt(Preferences.DesignCapacity.prefKey, 0))
    }
}