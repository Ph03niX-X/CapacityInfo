package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.isPremium
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BATTERY_INFORMATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BATTERY_LEVEL_IN_STATUS_BAR
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import androidx.core.net.toUri
import androidx.core.content.edit

class SettingsFragment : PreferenceFragmentCompat(), SettingsInterface, DebugOptionsInterface,
    BatteryInfoInterface, PremiumInterface, NavigationInterface {

    private lateinit var pref: SharedPreferences

    private var mainActivity: MainActivity? = null

    private var premium: Preference? = null

    private var isResume = false

    // Service & Notification
    private var stopService: SwitchPreferenceCompat? = null
    private var serviceTime: SwitchPreferenceCompat? = null
    private var isShowBatteryLevelInStatusBar: SwitchPreferenceCompat? = null
    private var isShowBatteryInformation: SwitchPreferenceCompat? = null
    private var isShowExtendedNotification: SwitchPreferenceCompat? = null
    private var batteryStatusInformation: Preference? = null
    private var openTheAppNotificationSettings: Preference? = null

    // Appearance
    private var darkMode: SwitchPreferenceCompat? = null
    private var textSize: ListPreference? = null
    private var textFont: ListPreference? = null
    private var textStyle: ListPreference? = null
    private var applicationLanguage: Preference? = null

    // Misc
    private var capacityInWh: SwitchPreferenceCompat? = null
    private var chargeDischargingCurrentInWatt: SwitchPreferenceCompat? = null
    private var resetScreenTime: SwitchPreferenceCompat? = null
    private var backupSettings: Preference? = null
    private var moreOther: Preference? = null
    private var tabOnApplicationLaunch: ListPreference? = null
    private var unitOfChargeDischargeCurrent: ListPreference? = null
    private var unitOfMeasurementOfCurrentCapacity: ListPreference? = null
    private var voltageUnit: ListPreference? = null
    private var changeDesignCapacity: Preference? = null
    private var overlay: Preference? = null
    private var replaceOfDeviceBattery: Preference? = null
    private var resetToZeroTheNumberOfCharges: Preference? = null
    private var resetToZeroTheNumberOfCycles: Preference? = null
    private var resetTheNumberOfFullChargesToZero: Preference? = null
    private var debug: Preference? = null

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        if(!isInstalledFromGooglePlay())
            throw RuntimeException("Application not installed from Google Play")

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        mainActivity = activity as? MainActivity

        premium = findPreference("get_premium")

        premium?.apply {
            isVisible = !isPremium

            if(isVisible)
                setOnPreferenceClickListener {

                    MainActivity.instance?.showPremiumDialog()

                    true
                }
        }

        // Service & Notification
        serviceTime = findPreference(IS_SERVICE_TIME)

        isShowBatteryLevelInStatusBar = findPreference(IS_SHOW_BATTERY_LEVEL_IN_STATUS_BAR)

        isShowBatteryInformation = findPreference(IS_SHOW_BATTERY_INFORMATION)

        isShowExtendedNotification = findPreference(IS_SHOW_EXPANDED_NOTIFICATION)

        batteryStatusInformation = findPreference("battery_status_information")

        openTheAppNotificationSettings = findPreference("open_the_app_notification_settings")

        stopService?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isShowBatteryLevelInStatusBar?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isShowBatteryInformation?.apply {
            isEnabled = premium?.isVisible == false
            summary = getString(if(!isEnabled) R.string.premium_feature
            else R.string.service_restart_required)
            setOnPreferenceChangeListener { preference, value -> preference.isEnabled = false
                isShowExtendedNotification?.isEnabled = false
                try {
                    ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java,
                        preference)
                }
             catch (e: Exception) {
                 Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
             }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3.5.seconds)
                    isShowExtendedNotification?.isEnabled = (value as? Boolean) == true
                }
                true
            }
        }

        isShowExtendedNotification?.apply {
            isEnabled = premium?.isVisible == false && pref.getBoolean(
                IS_SHOW_BATTERY_INFORMATION, requireContext().resources.getBoolean(
                    R.bool.is_show_battery_information))
            summary = getString(if(premium?.isVisible == true) R.string.premium_feature
            else R.string.service_restart_required)
            if(isEnabled) setOnPreferenceChangeListener { preference, _ ->
                preference.isEnabled = false
                isShowBatteryInformation?.isEnabled = false
                try {
                    ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java,
                        preference)
                }
                catch (e: Exception) {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
                }
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3.5.seconds)
                    isShowBatteryInformation?.isEnabled = true
                }
                true
            }
        }
        batteryStatusInformation?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
            if(isEnabled) setOnPreferenceClickListener {
                mainActivity?.apply {
                    fragment = BatteryStatusInformationFragment()
                    toolbar.title = requireContext().getString(
                        R.string.battery_status_information)
                    toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                        R.drawable.ic_arrow_back_24dp)
                    loadFragment(fragment ?: BatteryStatusInformationFragment(),
                        true)
                }
                true
            }
        }
        openTheAppNotificationSettings?.setOnPreferenceClickListener {
            startActivity(Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            })

            true
        }

        // Appearance
        darkMode = findPreference(IS_DARK_MODE)

        textSize = findPreference(TEXT_SIZE)

        textFont = findPreference(TEXT_FONT)

        textStyle = findPreference(TEXT_STYLE)

        applicationLanguage = findPreference("application_language")

        textSize?.summary = getTextSizeSummary()

        textFont?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(isEnabled) getTextFontSummary() else getString(R.string.premium_feature)

            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = resources.getStringArray(R.array.fonts_list)[
                        (newValue as? String)?.toInt() ?: 0]

                true
            }
        }

        textStyle?.summary = getTextStyleSummary()

        darkMode?.setOnPreferenceChangeListener { _, newValue ->
            setTheme(requireContext(), isDarkMode = newValue as? Boolean == true)
            true
        }

        textSize?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.text_size_list)[
                    (newValue as? String)?.toInt() ?: 2]

            true
        }

        textStyle?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.text_style_list)[
                    (newValue as? String)?.toInt() ?: 0]

            true
        }

        applicationLanguage?.setOnPreferenceClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                startActivity(Intent(Settings.ACTION_APP_LOCALE_SETTINGS,
                    "package:${requireContext().packageName}".toUri()))
            else it.isVisible = false
            true
        }

        // Misc
        capacityInWh = findPreference(IS_CAPACITY_IN_WH)

        chargeDischargingCurrentInWatt = findPreference(IS_CHARGING_DISCHARGE_CURRENT_IN_WATT)

        resetScreenTime = findPreference(IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL)

        backupSettings = findPreference("backup_settings")

        moreOther = findPreference("more_other")

        tabOnApplicationLaunch = findPreference(TAB_ON_APPLICATION_LAUNCH)

        unitOfChargeDischargeCurrent = findPreference(UNIT_OF_CHARGE_DISCHARGE_CURRENT)

        unitOfMeasurementOfCurrentCapacity = findPreference(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY)

        voltageUnit = findPreference(VOLTAGE_UNIT)

        changeDesignCapacity = findPreference("change_design_capacity")

        overlay = findPreference("overlay")

        replaceOfDeviceBattery = findPreference("replace_of_device_battery")

        resetToZeroTheNumberOfCharges = findPreference("reset_to_zero_the_number_of_charges")

        resetToZeroTheNumberOfCycles = findPreference("reset_to_zero_the_number_of_cycles")

        resetTheNumberOfFullChargesToZero = findPreference("reset_the_number_of_full_charges_to_zero")

        debug = findPreference("debug")

        capacityInWh?.apply {

            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        chargeDischargingCurrentInWatt?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        resetScreenTime?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            backupSettings?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }

        moreOther?.setOnPreferenceClickListener {
            if(it.title == requireContext().getString(R.string.more)) {
                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_less_24dp)
                it.title = getString(R.string.hide)

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    backupSettings?.apply {
                        isVisible = true
                        isEnabled = premium?.isVisible == false
                        summary = if(!isEnabled) getString(R.string.premium_feature) else null
                    }
                tabOnApplicationLaunch?.apply {
                    isVisible = true
                    isEnabled = premium?.isVisible == false
                    summary = if(!isEnabled) getString(R.string.premium_feature)
                    else getTabOnApplicationLaunchSummary()
                }
                unitOfChargeDischargeCurrent?.apply {
                    isVisible = true
                    summary = getUnitOfChargeDischargeCurrentSummary()
                }
                unitOfMeasurementOfCurrentCapacity?.apply {
                    isVisible = true
                    summary = getUnitOfMeasurementOfCurrentCapacitySummary()
                }
                voltageUnit?.apply {
                    isVisible = true
                    summary = getVoltageUnitSummary()
                }
                changeDesignCapacity?.apply {
                    isVisible = true
                    summary = getString(R.string.change_design_summary,
                        pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)))
                }
                overlay?.apply {
                    isVisible = true
                    if(isVisible) summary = if(premium?.isVisible == true)
                        getString(R.string.premium_feature) else null
                }
                replaceOfDeviceBattery?.isVisible = true
                resetToZeroTheNumberOfCharges?.apply {
                    isVisible = true
                    isEnabled = pref.getLong(NUMBER_OF_CHARGES, 0) > 0
                }
                resetToZeroTheNumberOfCycles?.apply {
                    isVisible = true
                    isEnabled = pref.getFloat(NUMBER_OF_CYCLES,0f) > 0f
                }
                resetTheNumberOfFullChargesToZero?.apply {
                    isVisible = true
                    isEnabled = pref.getLong(NUMBER_OF_FULL_CHARGES, 0) > 0
                }

                debug?.apply {
                    isVisible = pref.getBoolean(PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS,
                        resources.getBoolean(R.bool.is_enabled_debug_options))
                    isEnabled = premium?.isVisible == false
                    summary = if(!isEnabled) getString(R.string.premium_feature) else null
                }
            }

            else {

                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_24dp)
                it.title = requireContext().getString(R.string.more)

                backupSettings?.isVisible = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                tabOnApplicationLaunch?.isVisible = false
                unitOfChargeDischargeCurrent?.isVisible = false
                unitOfMeasurementOfCurrentCapacity?.isVisible = false
                voltageUnit?.isVisible = false
                changeDesignCapacity?.isVisible = false
                overlay?.isVisible = false
                replaceOfDeviceBattery?.isVisible = false
                resetToZeroTheNumberOfCharges?.isVisible = false
                resetToZeroTheNumberOfCycles?.isVisible = false
                resetTheNumberOfFullChargesToZero?.isVisible = false
                debug?.isVisible = false
            }

            true
        }

        backupSettings?.apply {
            if(isEnabled)
                setOnPreferenceClickListener {
                    mainActivity?.apply {
                        fragment = BackupSettingsFragment()
                        toolbar.title = requireContext().getString(R.string.backup)
                        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                            R.drawable.ic_arrow_back_24dp)
                        loadFragment(fragment ?: BackupSettingsFragment(),
                            true)
                    }
                    true
                }
        }

        tabOnApplicationLaunch?.setOnPreferenceChangeListener { preference, newValue ->
            val tab = (newValue as? String)?.toInt() ?: 0
            preference.summary = resources.getStringArray(R.array.tab_on_application_launch_list)[tab]
            true
        }

        unitOfChargeDischargeCurrent?.apply {
            setOnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(0.5.seconds)
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setIcon(R.drawable.ic_instruction_not_supported_24dp)
                        setTitle(R.string.information)
                        setMessage(R.string.setting_is_intended_to_correct)
                        setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                        show()
                    }
                }

                true
            }

            setOnPreferenceChangeListener { preference, newValue ->
                if((newValue as? String) == "μA")
                    preference.summary = resources.getStringArray(R.array
                        .unit_of_charge_discharge_current_list)[0]
                else preference.summary = resources.getStringArray(R.array
                    .unit_of_charge_discharge_current_list)[1]

                BatteryInfoInterface.apply {
                    maxChargeCurrent = 0
                    maxDischargeCurrent = 0
                    averageChargeCurrent = 0
                    averageDischargeCurrent = 0
                    minChargeCurrent = 0
                    minDischargeCurrent = 0
                }

                true

            }
        }

        unitOfMeasurementOfCurrentCapacity?.apply {
            setOnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(0.5.seconds)
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setIcon(R.drawable.ic_instruction_not_supported_24dp)
                        setTitle(R.string.information)
                        setMessage(R.string.setting_is_intended_to_correct)
                        setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                        show()
                    }
                }

                true
            }

            setOnPreferenceChangeListener { preference, newValue ->
                if((newValue as? String) == "μAh")
                    preference.summary = resources.getStringArray(R.array
                        .unit_of_measurement_of_current_capacity_list)[0]
                else preference.summary = resources.getStringArray(R.array
                    .unit_of_measurement_of_current_capacity_list)[1]

                true
            }
        }

        voltageUnit?.apply {
            setOnPreferenceClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(0.5.seconds)
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setIcon(R.drawable.ic_instruction_not_supported_24dp)
                        setTitle(R.string.information)
                        setMessage(R.string.setting_is_intended_to_correct)
                        setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                        show()
                    }
                }

                true
            }

            setOnPreferenceChangeListener { preference, newValue ->
                if((newValue as? String) == "μV")
                    preference.summary = resources.getStringArray(R.array.voltage_unit_list)[0]
                else preference.summary = resources.getStringArray(R.array.voltage_unit_list)[1]
                true
            }
        }

        changeDesignCapacity?.setOnPreferenceClickListener {
            onChangeDesignCapacity(it)
            true
        }

        overlay?.apply {
            summary = if(premium?.isVisible == true) getString(R.string.premium_feature) else null
                setOnPreferenceClickListener {
                    mainActivity?.apply {
                        fragment = OverlayFragment()
                        toolbar.title = requireContext().getString(R.string.overlay)
                        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                            R.drawable.ic_arrow_back_24dp)
                        loadFragment(fragment ?: OverlayFragment(), true)
                    }
                    true
                }
        }

        replaceOfDeviceBattery?.setOnPreferenceClickListener {
            replaceOfDeviceBatteryDialog()
            true
        }

        resetToZeroTheNumberOfCharges?.setOnPreferenceClickListener {

            if(pref.getLong(NUMBER_OF_CHARGES, 0) > 0)
                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(getString(R.string.reset_to_zero_the_number_of_charges_dialog_message))

                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                        pref.edit { remove(NUMBER_OF_CHARGES) }

                        it.isEnabled = pref.getLong(NUMBER_OF_CHARGES, 0) > 0

                        if(!it.isEnabled)
                            Toast.makeText(requireContext(),
                                R.string.number_of_charges_was_success_reset_to_zero,
                                Toast.LENGTH_LONG).show()
                    }

                    setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

                    show()
                }

            else it.isEnabled = false

            true
        }

        resetToZeroTheNumberOfCycles?.setOnPreferenceClickListener {

            if(pref.getFloat(NUMBER_OF_CYCLES, 0f) > 0f)
                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(getString(R.string.reset_to_zero_the_number_of_cycles_dialog_message))

                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                        pref.edit { remove(NUMBER_OF_CYCLES) }

                        it.isEnabled = pref.getFloat(NUMBER_OF_CYCLES, 0f) > 0f

                        if(!it.isEnabled) Toast.makeText(requireContext(),
                            R.string.number_of_cycles_was_success_reset_to_zero,
                            Toast.LENGTH_LONG).show()
                    }

                    setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

                    show()
                }

            else it.isEnabled = false

            true
        }

        resetTheNumberOfFullChargesToZero?.setOnPreferenceClickListener {

            if(pref.getLong(NUMBER_OF_FULL_CHARGES, 0) > 0)
                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(getString(R.string
                        .reset_the_number_of_full_charges_to_zero_dialog_message))

                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                        pref.edit { remove(NUMBER_OF_FULL_CHARGES) }

                        it.isEnabled = pref.getLong(NUMBER_OF_FULL_CHARGES, 0) > 0

                        if(!it.isEnabled) Toast.makeText(requireContext(),
                            R.string.number_of_full_charges_was_success_reset_to_zero,
                            Toast.LENGTH_LONG).show()
                    }

                    setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

                    show()
                }

            else it.isEnabled = false

            true
        }

        debug?.setOnPreferenceClickListener {
            mainActivity?.apply {
                fragment = DebugFragment()
                toolbar.title = requireContext().getString(R.string.debug)
                toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                    R.drawable.ic_arrow_back_24dp)
                loadFragment(fragment ?: DebugFragment(), true)
            }
            true
        }

        // About & Feedback
        about = findPreference("about")

        feedback = findPreference("feedback")

        about?.setOnPreferenceClickListener {
            mainActivity?.apply {
                fragment = AboutFragment()
                toolbar.title = requireContext().getString(R.string.about)
                toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                    R.drawable.ic_arrow_back_24dp)
                loadFragment(fragment ?: AboutFragment(), true)
            }
            true
        }

        feedback?.setOnPreferenceClickListener {
            mainActivity?.apply {
                fragment = FeedbackFragment()
                toolbar.title = requireContext().getString(R.string.feedback)
                toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(),
                    R.drawable.ic_arrow_back_24dp)
                loadFragment(fragment ?: FeedbackFragment(), true)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if(isResume) {
            if(premium?.isVisible == true) premium?.isVisible = !isPremium
            stopService?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            isShowBatteryLevelInStatusBar?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            isShowBatteryInformation?.apply {
                isEnabled = premium?.isVisible == false
                summary = getString(if(!isEnabled) R.string.premium_feature
                else R.string.service_restart_required)
            }
            isShowExtendedNotification?.apply {
                isEnabled = premium?.isVisible == false && pref.getBoolean(
                    IS_SHOW_BATTERY_INFORMATION, requireContext().resources.getBoolean(
                        R.bool.is_show_battery_information))
                summary = getString(if(premium?.isVisible == true) R.string.premium_feature
                else R.string.service_restart_required)
            }
            batteryStatusInformation?.apply {
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            capacityInWh?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            chargeDischargingCurrentInWatt?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            resetScreenTime?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            backupSettings?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
            overlay?.apply {
                summary = if(premium?.isVisible == true)
                    getString(R.string.premium_feature) else null
            }
            textSize?.summary = getTextSizeSummary()
            textFont?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(isEnabled) getTextFontSummary() else getString(R.string.premium_feature)
            }
            textStyle?.summary = getTextStyleSummary()
            tabOnApplicationLaunch?.apply {
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature)
                else getTabOnApplicationLaunchSummary()
            }
            unitOfChargeDischargeCurrent?.apply {
                if(isVisible) summary = getUnitOfChargeDischargeCurrentSummary()
            }
            unitOfMeasurementOfCurrentCapacity?.apply {
                if(isVisible) summary = getUnitOfMeasurementOfCurrentCapacitySummary()
            }
            voltageUnit?.apply {
                if(isVisible) summary = getVoltageUnitSummary()
            }
            changeDesignCapacity?.apply {
                if(isVisible) summary = getString(R.string.change_design_summary,
                    pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)))
            }
            resetToZeroTheNumberOfCharges?.isEnabled = pref.getLong(NUMBER_OF_CHARGES, 0) > 0
            resetToZeroTheNumberOfCycles?.isEnabled = pref.getFloat(NUMBER_OF_CYCLES, 0f) > 0f
            debug?.apply {
                isVisible = moreOther?.title == getString(R.string.hide) && pref.getBoolean(
                    PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS, resources.getBoolean(
                        R.bool.is_enabled_debug_options))
                isEnabled = premium?.isVisible == false
                summary = if(!isEnabled) getString(R.string.premium_feature) else null
            }
        }
        else isResume = true
    }

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay() =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
                .getInstallSourceInfo(requireContext().packageName).installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
            .getInstallerPackageName(requireContext().packageName)
}