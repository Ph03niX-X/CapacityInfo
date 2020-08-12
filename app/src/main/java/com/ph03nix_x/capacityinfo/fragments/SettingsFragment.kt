package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.setTheme
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STOP_THE_SERVICE_WHEN_THE_CD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT

class SettingsFragment : PreferenceFragmentCompat(), SettingsInterface, DebugOptionsInterface,
    BatteryInfoInterface {

    private lateinit var pref: SharedPreferences

    private var mainActivity: MainActivity? = null

    // Service & Notification
    private var serviceTime: SwitchPreferenceCompat? = null
    private var isStopTheServiceWhenTheCD: SwitchPreferenceCompat? = null
    private var isShowExtendedNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettingsService: Preference? = null
    private var batteryStatusInformation: Preference? = null

    // Appearance
    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null
    private var textSize: ListPreference? = null
    private var textFont: ListPreference? = null
    private var textStyle: ListPreference? = null
    private var selectLanguage: ListPreference? = null

    // Misc
    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
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

    // About & Feedback
    private var about: Preference? = null
    private var feedback: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.settings)

        mainActivity = activity as? MainActivity

        // Service & Notification
        serviceTime = findPreference(IS_SERVICE_TIME)

        isStopTheServiceWhenTheCD = findPreference(IS_STOP_THE_SERVICE_WHEN_THE_CD)

        isShowExtendedNotification = findPreference(IS_SHOW_EXPANDED_NOTIFICATION)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            openNotificationCategorySettingsService =
                findPreference("open_notification_category_settings_service")

        isShowExtendedNotification?.setOnPreferenceChangeListener { preference, _ ->

            preference.isEnabled = false

            ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java,
                preference)

            true
        }

        batteryStatusInformation = findPreference("battery_status_information")

        openNotificationCategorySettingsService?.setOnPreferenceClickListener {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                onOpenNotificationCategorySettings(requireContext(), SERVICE_CHANNEL_ID)

            true
        }

        batteryStatusInformation?.setOnPreferenceClickListener {

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

        // Appearance
        autoDarkMode = findPreference(IS_AUTO_DARK_MODE)

        darkMode = findPreference(IS_DARK_MODE)

        textSize = findPreference(TEXT_SIZE)

        textFont = findPreference(TEXT_FONT)

        textStyle = findPreference(TEXT_STYLE)

        selectLanguage = findPreference(LANGUAGE)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled =
            !pref.getBoolean(IS_AUTO_DARK_MODE, resources.getBoolean(R.bool.is_auto_dark_mode))

        textSize?.summary = getOnTextSizeSummary(requireContext())

        textFont?.summary = getOnTextFontSummary(requireContext())

        textStyle?.summary = getOnTextStyleSummary(requireContext())

        selectLanguage?.summary = getOnLanguageSummary(requireContext())

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

        textFont?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.fonts_list)[
                    (newValue as? String)?.toInt() ?: 0]

            true
        }

        textStyle?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.text_style_list)[
                    (newValue as? String)?.toInt() ?: 0]

            true
        }

        selectLanguage?.setOnPreferenceChangeListener { _, newValue ->

            MainActivity.isRecreate = true

            onChangeLanguage(requireContext(), ((newValue as? String) ?: MainApp.defLang))

            true
        }

        // Misc
        temperatureInFahrenheit = findPreference(TEMPERATURE_IN_FAHRENHEIT)

        moreOther = findPreference("more_other")

        voltageInMv = findPreference(VOLTAGE_IN_MV)

        backupSettings = findPreference("backup_settings")

        tabOnApplicationLaunch = findPreference(TAB_ON_APPLICATION_LAUNCH)

        unitOfChargeDischargeCurrent = findPreference(UNIT_OF_CHARGE_DISCHARGE_CURRENT)

        unitOfMeasurementOfCurrentCapacity = findPreference(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY)

        voltageUnit = findPreference(VOLTAGE_UNIT)

        changeDesignCapacity = findPreference("change_design_capacity")

        overlay = findPreference("overlay")

        resetToZeroTheNumberOfCharges = findPreference("reset_to_zero_the_number_of_charges")

        resetToZeroTheNumberOfCycles = findPreference("reset_to_zero_the_number_of_cycles")

        backupSettings?.setOnPreferenceClickListener {

            mainActivity?.fragment = BackupSettingsFragment()

            mainActivity?.toolbar?.title = requireContext().getString(
                R.string.backup)

            mainActivity?.toolbar?.navigationIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

            mainActivity?.loadFragment(
                mainActivity?.fragment ?: BackupSettingsFragment(), true)

            true
        }

        moreOther?.setOnPreferenceClickListener {

            if(it.title == requireContext().getString(R.string.more)) {

                it.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_less_24dp)
                it.title = getString(R.string.hide)

                unitOfChargeDischargeCurrent?.isVisible = true
                unitOfMeasurementOfCurrentCapacity?.isVisible = pref.getBoolean(IS_SUPPORTED,
                    resources.getBoolean(R.bool.is_supported))
                voltageUnit?.isVisible = true
                changeDesignCapacity?.isVisible = true
                overlay?.isVisible = true
                resetToZeroTheNumberOfCharges?.isVisible = true
                resetToZeroTheNumberOfCycles?.isVisible = true
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
            }

            true
        }

        tabOnApplicationLaunch?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.tab_on_application_launch_list)[
                    (newValue as? String)?.toInt() ?: 0]

            true
        }

        unitOfChargeDischargeCurrent?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as? String) == "μA")
                preference.summary = resources.getStringArray(R.array
                    .unit_of_charge_discharge_current_list)[0]
            else preference.summary = resources.getStringArray(R.array
                .unit_of_charge_discharge_current_list)[1]

            true
        }

        unitOfMeasurementOfCurrentCapacity?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as? String) == "μAh")
                preference.summary = resources.getStringArray(R.array
                    .unit_of_measurement_of_current_capacity_list)[0]
            else preference.summary = resources.getStringArray(R.array
                .unit_of_measurement_of_current_capacity_list)[1]

            true
        }

        voltageUnit?.setOnPreferenceChangeListener { preference, newValue ->

            if((newValue as? String) == "μV")
                preference.summary = resources.getStringArray(R.array.voltage_unit_list)[0]
            else preference.summary = resources.getStringArray(R.array.voltage_unit_list)[1]

            true
        }

        changeDesignCapacity?.setOnPreferenceClickListener {

            onChangeDesignCapacity(requireContext())

            true
        }

        overlay?.setOnPreferenceClickListener {

            mainActivity?.fragment = OverlayFragment()

            mainActivity?.toolbar?.title = requireContext().getString(
                R.string.overlay)

            mainActivity?.toolbar?.navigationIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back_24dp)

            mainActivity?.loadFragment(
                mainActivity?.fragment ?: OverlayFragment(), true)

            true
        }

        resetToZeroTheNumberOfCharges?.setOnPreferenceClickListener {

            MaterialAlertDialogBuilder(requireContext()).apply {

                setMessage(getString(R.string.reset_to_zero_the_number_of_charges_dialog_message))

                setPositiveButton(getString(android.R.string.yes)) { _, _ ->

                    pref.edit().remove(NUMBER_OF_CHARGES).apply()

                    Toast.makeText(requireContext(),
                        R.string.number_of_charges_was_success_reset_to_zero,
                        Toast.LENGTH_LONG).show()
                }

                setNegativeButton(getString(android.R.string.no)) { d, _ -> d.dismiss() }

                show()
            }

            true
        }

        resetToZeroTheNumberOfCycles?.setOnPreferenceClickListener {

            MaterialAlertDialogBuilder(requireContext()).apply {

                setMessage(getString(R.string.reset_to_zero_the_number_of_cycles_dialog_message))

                setPositiveButton(getString(android.R.string.yes)) { _, _ ->

                    pref.edit().remove(NUMBER_OF_CYCLES).apply()

                    Toast.makeText(requireContext(),
                        R.string.number_of_cycles_was_success_reset_to_zero,
                        Toast.LENGTH_LONG).show()
                }

                setNegativeButton(getString(android.R.string.no)) { d, _ -> d.dismiss() }

                show()
            }

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

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled =
            !pref.getBoolean(IS_AUTO_DARK_MODE, resources.getBoolean(R.bool.is_auto_dark_mode))

        textSize?.summary = getOnTextSizeSummary(requireContext())

        textFont?.summary = getOnTextFontSummary(requireContext())

        textStyle?.summary = getOnTextStyleSummary(requireContext())

        selectLanguage?.summary = getOnLanguageSummary(requireContext())

        tabOnApplicationLaunch?.summary = getOnTabOnApplicationLaunch(requireContext())

        unitOfChargeDischargeCurrent?.summary = getOnUnitOfChargeDischargeCurrentSummary(
            requireContext())

        unitOfMeasurementOfCurrentCapacity?.isVisible = moreOther?.title == getString(R.string.hide)
                && pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported))

        if(pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported)))
            unitOfMeasurementOfCurrentCapacity?.summary =
                getOnUnitOfMeasurementOfCurrentCapacitySummary(requireContext())

        voltageUnit?.summary = getOnVoltageUnitSummary(requireContext())

        changeDesignCapacity?.summary = getString(R.string.change_design_summary,
            pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)))
    }
}