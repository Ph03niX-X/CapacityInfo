package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import androidx.preference.*
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AUTO_START_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_ENABLE_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_STOP_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_UNIT

class SettingsFragment : PreferenceFragmentCompat(), ServiceInterface, SettingsInterface, DebugOptionsInterface {

    private lateinit var pref: SharedPreferences

    // Service and Notification
    private var enableService: SwitchPreferenceCompat? = null
    private var isAutoStartService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var serviceTime: SwitchPreferenceCompat? = null
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

        enableService = findPreference(IS_ENABLE_SERVICE)

        isAutoStartService = findPreference(IS_AUTO_START_SERVICE)

        showStopService = findPreference(IS_SHOW_STOP_SERVICE)

        serviceTime = findPreference(IS_SERVICE_TIME)

        moreServiceAndNotification = findPreference("more_service_and_notification")

        showCapacityAddedInNotification = findPreference(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION)

        showLastChargeTimeInNotification = findPreference(IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION)

        showCapacityAddedLastChargeTimeInNotification = findPreference(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION)

        openNotificationCategorySettings = findPreference("open_notification_category_settings")

        isAutoStartService?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        showStopService?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        serviceTime?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        showCapacityAddedInNotification?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        showCapacityAddedLastChargeTimeInNotification?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        showLastChargeTimeInNotification?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(IS_ENABLE_SERVICE, true)

        enableService?.setOnPreferenceChangeListener { _, newValue ->

            if(!(newValue as Boolean) && CapacityInfoService.instance != null) requireContext().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else if(newValue && CapacityInfoService.instance == null) startService(requireContext())

            isAutoStartService?.isEnabled = newValue
            serviceTime?.isEnabled = newValue
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

        autoDarkMode = findPreference(IS_AUTO_DARK_MODE)

        darkMode = findPreference(IS_DARK_MODE)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled = !pref.getBoolean(IS_AUTO_DARK_MODE, true)

        autoDarkMode?.setOnPreferenceChangeListener { _, newValue ->

            darkMode?.isEnabled = !(newValue as Boolean)

            setTheme(requireContext(), isAutoDarkMode = newValue)

            true
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->

            setTheme(requireContext(), isSystemDarkMode = newValue as Boolean)

            true
        }

        // Misc

        temperatureInFahrenheit = findPreference(TEMPERATURE_IN_FAHRENHEIT)

        moreOther = findPreference("more_other")

        voltageInMv = findPreference(VOLTAGE_IN_MV)

        unitOfChargeDischargeCurrent = findPreference(UNIT_OF_CHARGE_DISCHARGE_CURRENT)

        unitOfMeasurementOfCurrentCapacity = findPreference(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY)

        voltageUnit = findPreference(VOLTAGE_UNIT)

        changeDesignCapacity = findPreference("change_design_capacity")

        moreOther?.setOnPreferenceClickListener {

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_less_24dp)
                it.title = getString(R.string.hide)

                findPreference<SwitchPreferenceCompat>(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_APP)?.isVisible = true
                voltageInMv?.isVisible = true
                unitOfChargeDischargeCurrent?.isVisible = true
                unitOfMeasurementOfCurrentCapacity?.isVisible = true
                voltageUnit?.isVisible = true
                changeDesignCapacity?.isVisible = true
            }

            else {

                it.icon = requireContext().getDrawable(R.drawable.ic_expand_more_24dp)
                it.title = requireContext().getString(R.string.more)

                findPreference<SwitchPreferenceCompat>(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_APP)?.isVisible = false
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

        if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")
            !in resources.getStringArray(R.array.unit_of_charge_discharge_current_values))
            pref.edit().putString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA").apply()

        unitOfChargeDischargeCurrent?.summary = unitOfChargeDischargeCurrent?.entry

        if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
            !in resources.getStringArray(R.array.unit_of_measurement_of_current_capacity_values))
            pref.edit().putString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh").apply()

        unitOfMeasurementOfCurrentCapacity?.summary = unitOfMeasurementOfCurrentCapacity?.entry

        if(pref.getString(VOLTAGE_UNIT, "mV")
            !in resources.getStringArray(R.array.voltage_unit_values))
            pref.edit().putString(VOLTAGE_UNIT, "mV").apply()

        voltageUnit?.summary = voltageUnit?.entry

        changeDesignCapacity?.summary = getString(R.string.change_design_summary, pref.getInt(DESIGN_CAPACITY, 0))

        preferenceScreen.isVisible = false
    }
}