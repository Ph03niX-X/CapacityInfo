package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.preference.*
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface

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

    // Misc
    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var moreOther: Preference? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var unitOfChargeDischargeCurrent: ListPreference? = null
    private var unitOfMeasurementOfCurrentCapacity: ListPreference? = null
    private var voltageUnit: ListPreference? = null
    private var changeDesignCapacity: Preference? = null

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

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
                it.title = getString(R.string.hide)

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

            darkMode?.isEnabled = !(newValue as Boolean)

            setTheme(requireContext(), isAutoDarkMode = newValue)

            true
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->

            setTheme(requireContext(), isDarkMode = newValue as Boolean)

            true
        }

        // Misc

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        moreOther = findPreference("more_other")

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        unitOfChargeDischargeCurrent = findPreference(Preferences.UnitOfChargeDischargeCurrent.prefKey)

        unitOfMeasurementOfCurrentCapacity = findPreference(Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey)

        voltageUnit = findPreference(Preferences.VoltageUnit.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        moreOther?.setOnPreferenceClickListener {

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)
                it.title = getString(R.string.hide)

                findPreference<SwitchPreferenceCompat>(Preferences.IsShowCapacityAddedLastChargeInApp.prefKey)?.isVisible = true
                voltageInMv?.isVisible = true
                unitOfChargeDischargeCurrent?.isVisible = true
                unitOfMeasurementOfCurrentCapacity?.isVisible = true
                voltageUnit?.isVisible = true
                changeDesignCapacity?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)
                it.title = requireContext().getString(R.string.more)

                findPreference<SwitchPreferenceCompat>(Preferences.IsShowCapacityAddedLastChargeInApp.prefKey)?.isVisible = false
                voltageInMv?.isVisible = false
                unitOfChargeDischargeCurrent?.isVisible = false
                unitOfMeasurementOfCurrentCapacity?.isVisible = false
                voltageUnit?.isVisible = false
                changeDesignCapacity?.isVisible = false
            }

            true
        }

        unitOfChargeDischargeCurrent?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as String) == "μA")
                preference.summary = resources.getStringArray(R.array.unit_of_charge_discharge_current)[0]
            else preference.summary = resources.getStringArray(R.array.unit_of_charge_discharge_current)[1]

            true
        }

        unitOfMeasurementOfCurrentCapacity?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as String) == "μAh")
                preference.summary = resources.getStringArray(R.array.unit_of_measurement_of_current_capacity)[0]
            else preference.summary = resources.getStringArray(R.array.unit_of_measurement_of_current_capacity)[1]

            true
        }

        voltageUnit?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as String) == "μV")
                preference.summary = resources.getStringArray(R.array.voltage_unit)[0]
            else preference.summary = resources.getStringArray(R.array.voltage_unit)[1]

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

        if(pref.getString(Preferences.UnitOfChargeDischargeCurrent.prefKey, "μA")
            !in resources.getStringArray(R.array.unit_of_charge_discharge_current_values))
            pref.edit().putString(Preferences.UnitOfChargeDischargeCurrent.prefKey, "μA").apply()

        unitOfChargeDischargeCurrent?.summary = unitOfChargeDischargeCurrent?.entry

        if(pref.getString(Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey, "μAh")
            !in resources.getStringArray(R.array.unit_of_measurement_of_current_capacity_values))
            pref.edit().putString(Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey, "μAh").apply()

        unitOfMeasurementOfCurrentCapacity?.summary = unitOfMeasurementOfCurrentCapacity?.entry

        if(pref.getString(Preferences.VoltageUnit.prefKey, "mV")
            !in resources.getStringArray(R.array.voltage_unit_values))
            pref.edit().putString(Preferences.VoltageUnit.prefKey, "mV").apply()

        voltageUnit?.summary = voltageUnit?.entry

        changeDesignCapacity?.summary = getString(R.string.change_design_summary, pref.getInt(Preferences.DesignCapacity.prefKey, 0))

        preferenceScreen.isVisible = false
    }
}