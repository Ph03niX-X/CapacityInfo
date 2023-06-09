package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.isPremium
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BATTERY_INFORMATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_STOP_SERVICE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STOP_THE_SERVICE_WHEN_THE_CD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import kotlinx.coroutines.*

class SettingsFragment : PreferenceFragmentCompat(), SettingsInterface, DebugOptionsInterface,
    BatteryInfoInterface, PremiumInterface {

    private lateinit var pref: SharedPreferences

    private var mainActivity: MainActivity? = null

    private var premium: Preference? = null

    // Service & Notification
    private var stopService: SwitchPreferenceCompat? = null
    private var serviceTime: SwitchPreferenceCompat? = null
    private var isStopTheServiceWhenTheCD: SwitchPreferenceCompat? = null
    private var isShowBatteryInformation: SwitchPreferenceCompat? = null
    private var isShowExtendedNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettingsService: Preference? = null
    private var batteryStatusInformation: Preference? = null

    // Appearance
    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null
    private var textSize: ListPreference? = null
    private var textFont: ListPreference? = null
    private var textStyle: ListPreference? = null

    // Misc
    private var capacityInWh: SwitchPreferenceCompat? = null
    private var chargeDischargingCurrentInWatt: SwitchPreferenceCompat? = null
    private var resetScreenTime: SwitchPreferenceCompat? = null
    private var tabOnApplicationLaunch: ListPreference? = null
    private var unitOfChargeDischargeCurrent: ListPreference? = null
    private var unitOfMeasurementOfCurrentCapacity: ListPreference? = null
    private var voltageUnit: ListPreference? = null
    private var backupSettings: Preference? = null
    private var moreOther: Preference? = null
    private var changeDesignCapacity: Preference? = null
    private var overlay: Preference? = null
    private var resetToZeroTheNumberOfCharges: Preference? = null
    private var resetToZeroTheNumberOfCycles: Preference? = null
    private var resetTheNumberOfFullChargesToZero: Preference? = null
    private var debug: Preference? = null

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.settings)

        val isPremium = isPremium()

        mainActivity = activity as? MainActivity

        premium = findPreference("premium")

        premium?.apply {
            isVisible = !isPremium

            if(isVisible)
                setOnPreferenceClickListener {

                    MainActivity.instance?.showPremiumDialog()

                    true
                }
        }

        // Service & Notification
        stopService = findPreference(IS_SHOW_STOP_SERVICE)

        serviceTime = findPreference(IS_SERVICE_TIME)

        isStopTheServiceWhenTheCD = findPreference(IS_STOP_THE_SERVICE_WHEN_THE_CD)

        isShowBatteryInformation = findPreference(IS_SHOW_BATTERY_INFORMATION)

        isShowExtendedNotification = findPreference(IS_SHOW_EXPANDED_NOTIFICATION)

       openNotificationCategorySettingsService =
           findPreference("open_notification_category_settings_service")

        stopService?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isStopTheServiceWhenTheCD?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isShowBatteryInformation?.apply {
            isEnabled = premium?.isVisible == false

            summary = getString(if(!isEnabled) R.string.premium_feature
            else R.string.service_restart_required)

            setOnPreferenceChangeListener { preference, value ->

                preference.isEnabled = false
                isShowExtendedNotification?.isEnabled = false

                ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java,
                    preference)

                CoroutineScope(Dispatchers.Default).launch {

                    withContext(Dispatchers.Main) {

                        delay(3500L)
                        isShowExtendedNotification?.isEnabled = (value as? Boolean) == true
                    }
                }

                true
            }
        }

        isShowExtendedNotification?.apply {

            isEnabled = if(!isPremium) true
            else pref.getBoolean(
                IS_SHOW_BATTERY_INFORMATION, requireContext().resources.getBoolean(
                    R.bool.is_show_battery_information))

            setOnPreferenceChangeListener { preference, _ ->

                preference.isEnabled = false

                ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java,
                    preference)

                true
            }
        }

        batteryStatusInformation = findPreference("battery_status_information")

        openNotificationCategorySettingsService?.setOnPreferenceClickListener {

            onOpenNotificationCategorySettings(SERVICE_CHANNEL_ID)

            true
        }

        batteryStatusInformation?.apply {

            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null

            if(isEnabled)
                setOnPreferenceClickListener {
                    mainActivity?.fragment = BatteryStatusInformationFragment()

                    mainActivity?.toolbar?.title = requireContext().getString(
                        R.string.battery_status_information)

                    mainActivity?.toolbar?.navigationIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

                    mainActivity?.loadFragment(
                        mainActivity?.fragment ?: BatteryStatusInformationFragment(),
                        true)

                    true
                }
        }

        // Appearance
        autoDarkMode = findPreference(IS_AUTO_DARK_MODE)

        darkMode = findPreference(IS_DARK_MODE)

        textSize = findPreference(TEXT_SIZE)

        textFont = findPreference(TEXT_FONT)

        textStyle = findPreference(TEXT_STYLE)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled =
            !pref.getBoolean(IS_AUTO_DARK_MODE, resources.getBoolean(R.bool.is_auto_dark_mode))

        textSize?.summary = getOnTextSizeSummary()

        textFont?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(isEnabled) getOnTextFontSummary() else getString(R.string.premium_feature)

            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = resources.getStringArray(R.array.fonts_list)[
                        (newValue as? String)?.toInt() ?: 0]

                true
            }
        }

        textStyle?.summary = getOnTextStyleSummary()

        autoDarkMode?.setOnPreferenceChangeListener { _, newValue ->

            darkMode?.isEnabled = (newValue as? Boolean) == false

            setTheme(requireContext(), isAutoDarkMode = newValue as? Boolean == true)

            true
        }

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

        // Misc
        capacityInWh = findPreference(IS_CAPACITY_IN_WH)

        chargeDischargingCurrentInWatt = findPreference(IS_CHARGING_DISCHARGE_CURRENT_IN_WATT)

        resetScreenTime = findPreference(IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL)

        moreOther = findPreference("more_other")

        backupSettings = findPreference("backup_settings")

        tabOnApplicationLaunch = findPreference(TAB_ON_APPLICATION_LAUNCH)

        unitOfChargeDischargeCurrent = findPreference(UNIT_OF_CHARGE_DISCHARGE_CURRENT)

        unitOfMeasurementOfCurrentCapacity = findPreference(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY)

        voltageUnit = findPreference(VOLTAGE_UNIT)

        changeDesignCapacity = findPreference("change_design_capacity")

        overlay = findPreference("overlay")

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

        tabOnApplicationLaunch?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        backupSettings?.apply {

            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null

            if(isEnabled)
                setOnPreferenceClickListener {
                    mainActivity?.fragment = BackupSettingsFragment()

                    mainActivity?.toolbar?.title = requireContext().getString(
                        R.string.backup)

                    mainActivity?.toolbar?.navigationIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

                    mainActivity?.loadFragment(
                        mainActivity?.fragment ?: BackupSettingsFragment(), true)

                    true
                }
        }

        moreOther?.setOnPreferenceClickListener {

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_less_24dp)
                it.title = getString(R.string.hide)

                unitOfChargeDischargeCurrent?.apply {
                    isVisible = true
                    summary = getOnUnitOfChargeDischargeCurrentSummary()
                }

                unitOfMeasurementOfCurrentCapacity?.apply {
                    isVisible = true
                    summary = getOnUnitOfMeasurementOfCurrentCapacitySummary()
                }

                voltageUnit?.apply {
                    isVisible = true
                    summary = getOnVoltageUnitSummary()
                }

                changeDesignCapacity?.isVisible = true
                overlay?.isVisible = true
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

                unitOfChargeDischargeCurrent?.isVisible = false
                unitOfMeasurementOfCurrentCapacity?.isVisible = false
                voltageUnit?.isVisible = false
                changeDesignCapacity?.isVisible = false
                overlay?.isVisible = false
                resetToZeroTheNumberOfCharges?.isVisible = false
                resetToZeroTheNumberOfCycles?.isVisible = false
                resetTheNumberOfFullChargesToZero?.isVisible = false
                debug?.isVisible = false
            }

            true
        }

        tabOnApplicationLaunch?.setOnPreferenceChangeListener { preference, newValue ->

            val tab = if(HistoryHelper.isHistoryNotEmpty(requireContext()))
                        (newValue as? String)?.toInt() ?: 0 else 0

            preference.summary = resources.getStringArray(R.array.tab_on_application_launch_list)[tab]

           HistoryHelper.isHistoryNotEmpty(requireContext())
        }

        unitOfChargeDischargeCurrent?.apply {

            setOnPreferenceClickListener {

                Toast.makeText(requireContext(), getString(R.string.setting_is_intended_to_correct),
                    Toast.LENGTH_LONG).show()

                true
            }

            setOnPreferenceChangeListener { preference, newValue ->

                if((newValue as? String) == "μA")
                    preference.summary = resources.getStringArray(R.array
                        .unit_of_charge_discharge_current_list)[0]
                else preference.summary = resources.getStringArray(R.array
                    .unit_of_charge_discharge_current_list)[1]

                true

            }
        }

        unitOfMeasurementOfCurrentCapacity?.apply {

            setOnPreferenceClickListener {

                Toast.makeText(requireContext(), getString(R.string.setting_is_intended_to_correct),
                    Toast.LENGTH_LONG).show()

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

                Toast.makeText(requireContext(), getString(R.string.setting_is_intended_to_correct),
                    Toast.LENGTH_LONG).show()

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

            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null

            if(isEnabled)
                setOnPreferenceClickListener {
                    mainActivity?.fragment = OverlayFragment()

                    mainActivity?.toolbar?.title = requireContext().getString(
                        R.string.overlay)

                    mainActivity?.toolbar?.navigationIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

                    mainActivity?.loadFragment(
                        mainActivity?.fragment ?: OverlayFragment(), true)

                    true
                }
        }

        resetToZeroTheNumberOfCharges?.setOnPreferenceClickListener {

            if(pref.getLong(NUMBER_OF_CHARGES, 0) > 0)
                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(getString(R.string.reset_to_zero_the_number_of_charges_dialog_message))

                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                        pref.edit().remove(NUMBER_OF_CHARGES).apply()

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

                        pref.edit().remove(NUMBER_OF_CYCLES).apply()

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

                        pref.edit().remove(NUMBER_OF_FULL_CHARGES).apply()

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

            mainActivity?.fragment = DebugFragment()

            mainActivity?.toolbar?.title = requireContext().getString(R.string.debug)

            mainActivity?.toolbar?.navigationIcon = ContextCompat.getDrawable(requireContext(),
                R.drawable.ic_arrow_back_24dp)

            mainActivity?.loadFragment(mainActivity?.fragment ?: DebugFragment(),
                true)

            true
        }

        // About & Feedback
        about = findPreference("about")

        feedback = findPreference("feedback")

        about?.setOnPreferenceClickListener {

            mainActivity?.fragment = AboutFragment()

            mainActivity?.toolbar?.title = requireContext().getString(
                R.string.about)

            mainActivity?.toolbar?.navigationIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

            mainActivity?.loadFragment(
                mainActivity?.fragment ?: AboutFragment(), true)

            true
        }

        feedback?.setOnPreferenceClickListener {

            mainActivity?.fragment = FeedbackFragment()

            mainActivity?.toolbar?.title = requireContext().getString(
                R.string.feedback)

            mainActivity?.toolbar?.navigationIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

            mainActivity?.loadFragment(
                mainActivity?.fragment ?: FeedbackFragment(), true)

            true
        }
    }

    override fun onResume() {

        super.onResume()

        if(premium?.isVisible == true) premium?.isVisible = !isPremium

        stopService?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isStopTheServiceWhenTheCD?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        isShowBatteryInformation?.apply {
            isEnabled = premium?.isVisible == false
            summary = getString(if(!isEnabled) R.string.premium_feature
            else R.string.service_restart_required)
        }

        isShowExtendedNotification?.apply {
            isEnabled = if(premium?.isVisible == false) true
            else pref.getBoolean(IS_SHOW_BATTERY_INFORMATION, requireContext().resources.getBoolean(
                    R.bool.is_show_battery_information))
        }

        batteryStatusInformation?.apply {
            isEnabled = premium?.isVisible == false
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

        tabOnApplicationLaunch?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        backupSettings?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        overlay?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(!isEnabled) getString(R.string.premium_feature) else null
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled =
            !pref.getBoolean(IS_AUTO_DARK_MODE, resources.getBoolean(R.bool.is_auto_dark_mode))

        textSize?.summary = getOnTextSizeSummary()

        textFont?.apply {
            isEnabled = premium?.isVisible == false
            summary = if(isEnabled) getOnTextFontSummary() else getString(R.string.premium_feature)
        }

        textStyle?.summary = getOnTextStyleSummary()

        if(isPremium) tabOnApplicationLaunch?.summary = getOnTabOnApplicationLaunch()

        changeDesignCapacity?.summary = getString(R.string.change_design_summary,
            pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)))

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
}