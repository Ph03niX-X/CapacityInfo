package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants.NUMBER_OF_CYCLES_PATH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AVERAGE_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_WEAR_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_ADDED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_CURRENT_LIMIT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CYCLES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_REMAINING_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MAXIMUM_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MINIMUM_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_FULL_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ONLY_VALUES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_REMAINING_BATTERY_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SOURCE_OF_POWER
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESIDUAL_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SCREEN_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_LOCATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import java.io.File
import java.text.DecimalFormat

class OverlayFragment : PreferenceFragmentCompat(), BatteryInfoInterface, OverlayInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var isResume = false

    private var dialogRequestOverlayPermission: MaterialAlertDialogBuilder? = null

    private var overlayScreen: PreferenceScreen? = null
    private var enableOverlay: SwitchPreferenceCompat? = null
    private var onlyValuesOverlay: SwitchPreferenceCompat? = null
    private var overlayLocation: ListPreference? = null

    // Appearance
    private var appearanceCategory: PreferenceCategory? = null
    private var overlaySize: ListPreference? = null
    private var overlayFont: ListPreference? = null
    private var overlayTextStyle: ListPreference? = null
    private var overlayOpacity: SeekBarPreference? = null

    // Show/Hide
    private var overlayCategory: PreferenceCategory? = null
    private var numberOfChargesOverlay: SwitchPreferenceCompat? = null
    private var numberOfFullChargesOverlay: SwitchPreferenceCompat? = null
    private var numberOfCyclesOverlay: SwitchPreferenceCompat? = null
    private var numberOfCyclesAndroidOverlay: SwitchPreferenceCompat? = null
    private var chargingTimeOverlay: SwitchPreferenceCompat? = null
    private var chargingTimeRemainingOverlay: SwitchPreferenceCompat? = null
    private var remainingBatteryTimeOverlay: SwitchPreferenceCompat? = null
    private var screenTimeOverlay: SwitchPreferenceCompat? = null
    private var batteryLevelOverlay: SwitchPreferenceCompat? = null
    private var currentCapacityOverlay: SwitchPreferenceCompat? = null
    private var capacityAddedOverlay: SwitchPreferenceCompat? = null
    private var batteryHealthOverlay: SwitchPreferenceCompat? = null
    private var residualCapacityOverlay: SwitchPreferenceCompat? = null
    private var statusOverlay: SwitchPreferenceCompat? = null
    private var sourceOfPowerOverlay: SwitchPreferenceCompat? = null
    private var chargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var fastChargeOverlay: SwitchPreferenceCompat? = null
    private var maxChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var averageChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var minChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var chargingCurrentLimitOverlay: SwitchPreferenceCompat? = null
    private var temperatureOverlay: SwitchPreferenceCompat? = null
    private var maximumTemperatureOverlay: SwitchPreferenceCompat? = null
    private var averageTemperatureOverlay: SwitchPreferenceCompat? = null
    private var minimumTemperatureOverlay: SwitchPreferenceCompat? = null
    private var voltageOverlay: SwitchPreferenceCompat? = null
    private var batteryWearOverlay: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.overlay_settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val chargingCurrentLimit = getChargingCurrentLimit(requireContext())

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

        overlayScreen = findPreference("overlay_screen")

        overlayScreen?.isEnabled = Settings.canDrawOverlays(requireContext())

        enableOverlay = findPreference(IS_ENABLED_OVERLAY)

        onlyValuesOverlay = findPreference(IS_ONLY_VALUES_OVERLAY)

        overlayLocation = findPreference(OVERLAY_LOCATION)

        enableOverlay?.setOnPreferenceChangeListener { _, newValue ->

            when(newValue as? Boolean) {

                true -> {

                    if(OverlayService.instance == null
                        && OverlayInterface.isEnabledOverlay(requireContext(),
                            newValue as? Boolean == true))
                        ServiceHelper.startService(requireContext(), OverlayService::class.java,
                            true)
                }

                false -> if(OverlayService.instance != null)
                    ServiceHelper.stopService(requireContext(), OverlayService::class.java)
                null -> {}
            }

            enableAllOverlay(newValue as? Boolean)

            true
        }

        overlayLocation?.apply {
            summary = getOverlayLocationSummary()
            setOnPreferenceChangeListener { preference, newValue ->
                if(OverlayService.instance != null && OverlayInterface.isEnabledOverlay(
                        requireContext(), enableOverlay?.isEnabled == true))
                    ServiceHelper.restartService(context, OverlayService::class.java)
                preference.summary = resources.getStringArray(R.array.overlay_location_list)[
                    (newValue as? String)?.toInt() ?: resources.getInteger(
                        R.integer.overlay_location_default)]
                true
            }
        }

        // Appearance
        appearanceCategory = findPreference("appearance_overlay")
        overlaySize = findPreference(OVERLAY_SIZE)
        overlayFont = findPreference(OVERLAY_FONT)
        overlayTextStyle = findPreference(OVERLAY_TEXT_STYLE)
        overlayOpacity = findPreference("overlay_opacity")

        overlaySize?.apply {
            summary = getOverlayTextSizeSummary()
            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = resources.getStringArray(R.array.text_size_list)[
                    (newValue as? String)?.toInt() ?: 2]

                true
            }
        }

        overlayFont?.apply {
            summary = getOverlayTextFontSummary()
            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = resources.getStringArray(R.array.fonts_list)[
                    (newValue as? String)?.toInt() ?: 6]

                true
            }
        }

        overlayTextStyle?.apply {
            summary = getOverlayTextStyleSummary()
            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = resources.getStringArray(R.array.text_style_list)[
                    (newValue as? String)?.toInt() ?: 0]

                true
            }
        }

        overlayOpacity?.apply {
            summary = getOverlayOpacitySummary()
            setOnPreferenceChangeListener { preference, newValue ->

                val progress = newValue as? Int ?: 0

                preference.summary = "${DecimalFormat("#").format(
                    (progress.toFloat() / 255f) * 100f)}%"

                true
            }
        }

        // Show/Hide
        overlayCategory = findPreference("show_hide_pref_category")
        batteryLevelOverlay = findPreference(IS_BATTERY_LEVEL_OVERLAY)
        numberOfChargesOverlay = findPreference(IS_NUMBER_OF_CHARGES_OVERLAY)
        numberOfFullChargesOverlay = findPreference(IS_NUMBER_OF_FULL_CHARGES_OVERLAY)
        numberOfCyclesOverlay = findPreference(IS_NUMBER_OF_CYCLES_OVERLAY)
        numberOfCyclesAndroidOverlay = findPreference(IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY)
        chargingTimeOverlay = findPreference(IS_CHARGING_TIME_OVERLAY)
        chargingTimeRemainingOverlay = findPreference(IS_CHARGING_TIME_REMAINING_OVERLAY)
        remainingBatteryTimeOverlay = findPreference(IS_REMAINING_BATTERY_TIME_OVERLAY)
        screenTimeOverlay = findPreference(IS_SCREEN_TIME_OVERLAY)
        currentCapacityOverlay = findPreference(IS_CURRENT_CAPACITY_OVERLAY)
        capacityAddedOverlay = findPreference(IS_CAPACITY_ADDED_OVERLAY)
        batteryHealthOverlay = findPreference(IS_BATTERY_HEALTH_OVERLAY)
        residualCapacityOverlay = findPreference(IS_RESIDUAL_CAPACITY_OVERLAY)
        statusOverlay = findPreference(IS_STATUS_OVERLAY)
        sourceOfPowerOverlay = findPreference(IS_SOURCE_OF_POWER)
        chargeDischargeCurrentOverlay = findPreference(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        fastChargeOverlay = findPreference(IS_FAST_CHARGE_OVERLAY)
        maxChargeDischargeCurrentOverlay = findPreference(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        averageChargeDischargeCurrentOverlay =
            findPreference(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        minChargeDischargeCurrentOverlay = findPreference(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        chargingCurrentLimitOverlay = findPreference(IS_CHARGING_CURRENT_LIMIT_OVERLAY)
        temperatureOverlay = findPreference(IS_TEMPERATURE_OVERLAY)
        maximumTemperatureOverlay = findPreference(IS_MAXIMUM_TEMPERATURE_OVERLAY)
        averageTemperatureOverlay = findPreference(IS_AVERAGE_TEMPERATURE_OVERLAY)
        minimumTemperatureOverlay = findPreference(IS_MINIMUM_TEMPERATURE_OVERLAY)
        voltageOverlay = findPreference(IS_VOLTAGE_OVERLAY)
        batteryWearOverlay = findPreference(IS_BATTERY_WEAR_OVERLAY)

        numberOfCyclesAndroidOverlay?.isVisible =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                    File(NUMBER_OF_CYCLES_PATH).exists()
        chargingCurrentLimitOverlay?.isVisible = chargingCurrentLimit != null &&
                chargingCurrentLimit.toInt() > 0

        enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(
            R.bool.is_enabled_overlay)))

        batteryLevelOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        numberOfChargesOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        numberOfFullChargesOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        numberOfCyclesOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        numberOfCyclesAndroidOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        chargingTimeOverlay?.setOnPreferenceChangeListener { _, newValue ->
            if(newValue as? Boolean == true && OverlayService.instance == null) {
                ServiceHelper.startService(requireContext(), OverlayService::class.java)
                OverlayInterface.chargingTime = CapacityInfoService.instance?.seconds ?: 0
            }
            else OverlayInterface.chargingTime = 0
            true
        }

        chargingTimeRemainingOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        remainingBatteryTimeOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        screenTimeOverlay?.setOnPreferenceChangeListener { _, newValue ->
            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        currentCapacityOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        capacityAddedOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        batteryHealthOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        residualCapacityOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        statusOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        sourceOfPowerOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        chargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        fastChargeOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        maxChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        averageChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        minChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        chargingCurrentLimitOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        temperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        maximumTemperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        averageTemperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        minimumTemperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        voltageOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        batteryWearOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }
    }

    override fun onResume() {
        super.onResume()
        val chargingCurrentLimit = getChargingCurrentLimit(requireContext())
        val canDrawOverlays = Settings.canDrawOverlays(requireContext())
        if(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(R.bool.is_enabled_overlay))
            && OverlayService.instance == null && !ServiceHelper.isStartedOverlayService())
            ServiceHelper.startService(requireContext(), OverlayService::class.java)
        if(isResume) {
            chargingCurrentLimitOverlay?.isVisible = chargingCurrentLimit != null &&
                    chargingCurrentLimit.toInt() > 0
            overlayLocation?.summary = getOverlayLocationSummary()
            overlaySize?.summary = getOverlayTextSizeSummary()
            overlayTextStyle?.summary = getOverlayTextStyleSummary()
            overlayOpacity?.summary = getOverlayOpacitySummary()
            enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(
                R.bool.is_enabled_overlay)))
        }
        else isResume = true
        overlayScreen?.isEnabled = canDrawOverlays
        if(dialogRequestOverlayPermission == null && !canDrawOverlays)
            requestOverlayPermission()
    }

    private fun getOverlayLocationSummary(): CharSequence? {
        if(pref.getString(OVERLAY_LOCATION, "${resources.getInteger(
                R.integer.overlay_location_default)}") !in
            resources.getStringArray(R.array.overlay_location_values))
                pref.edit().putString(OVERLAY_LOCATION, "${resources.getInteger(
                    R.integer.overlay_location_default)}").apply()

        return resources.getStringArray(R.array.overlay_location_list)[pref.getString(
            OVERLAY_LOCATION,
            "${resources.getInteger(R.integer.overlay_location_default)}")!!.toInt()]
    }

    private fun getOverlayTextSizeSummary(): CharSequence? {
        if(pref.getString(OVERLAY_SIZE, "2") !in
            resources.getStringArray(R.array.text_size_values))
            pref.edit().putString(OVERLAY_SIZE, "2").apply()

        return resources.getStringArray(R.array.text_size_list)[pref.getString(OVERLAY_SIZE,
            "2")?.toInt() ?: 2]
    }

    private fun getOverlayTextFontSummary(): CharSequence? {

        if(pref.getString(OVERLAY_FONT, "6") !in
            resources.getStringArray(R.array.fonts_values))
            pref.edit().putString(OVERLAY_FONT, "6").apply()

        return resources.getStringArray(R.array.fonts_list)[
                pref.getString(OVERLAY_FONT, "6")?.toInt() ?: 6]
    }

    private fun getOverlayTextStyleSummary(): CharSequence? {

        if(pref.getString(OVERLAY_TEXT_STYLE, "0") !in
                resources.getStringArray(R.array.text_style_values))
            pref.edit().putString(OVERLAY_TEXT_STYLE, "0").apply()

        return resources.getStringArray(R.array.text_style_list)[
                pref.getString(OVERLAY_TEXT_STYLE, "0")?.toInt() ?: 0]
    }

    private fun getOverlayOpacitySummary(): CharSequence {

        if(pref.getInt(OVERLAY_OPACITY, resources.getInteger(
                R.integer.overlay_opacity_default)) > resources.getInteger(
                R.integer.overlay_opacity_max) || pref.getInt(OVERLAY_OPACITY,
                resources.getInteger(R.integer.overlay_opacity_default)) < 0)
            pref.edit().putInt(OVERLAY_OPACITY, resources.getInteger(
                R.integer.overlay_opacity_default)).apply()

        return "${DecimalFormat("#").format((pref.getInt(OVERLAY_OPACITY,
            resources.getInteger(R.integer.overlay_opacity_default)).toFloat() / resources
            .getInteger(R.integer.overlay_opacity_max).toFloat()) * 100f)}%"
    }

    private fun requestOverlayPermission() {

        dialogRequestOverlayPermission = MaterialAlertDialogBuilder(requireContext()).apply {

            setMessage(getString(R.string.overlay_permission_dialog_message))
            setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}"))

                getResult.launch(intent)

                dialogRequestOverlayPermission = null
            }
            setNegativeButton(getString(android.R.string.cancel)) { _, _ ->

                dialogRequestOverlayPermission = null
            }
            setOnCancelListener { dialogRequestOverlayPermission = null }

            show()
        }
    }

    private fun enableAllOverlay(isEnable: Boolean?) {
        onlyValuesOverlay?.isEnabled = isEnable ?: onlyValuesOverlay?.isEnabled ?: false
        overlayLocation?.isEnabled = isEnable ?: overlayLocation?.isEnabled ?: false
        appearanceCategory?.isEnabled = isEnable ?: appearanceCategory?.isEnabled ?: false
        overlayCategory?.isEnabled = isEnable ?: overlayCategory?.isEnabled ?: false
    }
}