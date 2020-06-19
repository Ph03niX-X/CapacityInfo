package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants.ACTION_MANAGE_OVERLAY_PERMISSION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_WEAR_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_ADDED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_LAST_CHARGE_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CYCLES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_REMAINING_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ONLY_VALUES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_REMAINING_BATTERY_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SOURCE_OF_POWER
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESIDUAL_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import java.text.DecimalFormat

class OverlayFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var dialogRequestOverlayPermission: MaterialAlertDialogBuilder? = null

    private var overlayScreen: PreferenceScreen? = null
    private var enableOverlay: SwitchPreferenceCompat? = null
    private var onlyValuesOverlay: SwitchPreferenceCompat? = null

    // Appearance
    private var appearanceCategory: PreferenceCategory? = null
    private var overlaySize: ListPreference? = null
    private var overlayFont: ListPreference? = null
    private var overlayTextStyle: ListPreference? = null
    private var overlayOpacity: SeekBarPreference? = null

    // Show/Hide
    private var overlayCategory: PreferenceCategory? = null
    private var numberOfChargesOverlay: SwitchPreferenceCompat? = null
    private var numberOfCyclesOverlay: SwitchPreferenceCompat? = null
    private var chargingTimeOverlay: SwitchPreferenceCompat? = null
    private var chargingTimeRemainingOverlay: SwitchPreferenceCompat? = null
    private var remainingBatteryTimeOverlay: SwitchPreferenceCompat? = null
    private var batteryLevelOverlay: SwitchPreferenceCompat? = null
    private var currentCapacityOverlay: SwitchPreferenceCompat? = null
    private var capacityAddedOverlay: SwitchPreferenceCompat? = null
    private var batteryHealthOverlay: SwitchPreferenceCompat? = null
    private var residualCapacityOverlay: SwitchPreferenceCompat? = null
    private var statusOverlay: SwitchPreferenceCompat? = null
    private var sourceOfPowerOverlay: SwitchPreferenceCompat? = null
    private var chargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var maxChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var averageChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var minChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var temperatureOverlay: SwitchPreferenceCompat? = null
    private var voltageOverlay: SwitchPreferenceCompat? = null
    private var lastChargeTimeOverlay: SwitchPreferenceCompat? = null
    private var batteryWearOverlay: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.overlay_settings)

        overlayScreen = findPreference("overlay_screen")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            overlayScreen?.isEnabled = Settings.canDrawOverlays(requireContext())

        enableOverlay = findPreference(IS_ENABLED_OVERLAY)

        onlyValuesOverlay = findPreference(IS_ONLY_VALUES_OVERLAY)

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
            }

            enableAllOverlay(newValue as? Boolean)

            true
        }

        // Appearance
        appearanceCategory = findPreference("appearance_overlay")
        overlaySize = findPreference(OVERLAY_SIZE)
        overlayFont = findPreference(OVERLAY_FONT)
        overlayTextStyle = findPreference(OVERLAY_TEXT_STYLE)
        overlayOpacity = findPreference("overlay_opacity")

        overlaySize?.summary = getOverlayTextSizeSummary()

        overlayFont?.summary = getOverlayTextFontSummary()

        overlayTextStyle?.summary = getOverlayTextStyleSummary()

        overlayOpacity?.summary = getOverlayOpacitySummary()

        overlayFont?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.fonts_list)[
                    (newValue as? String)?.toInt() ?: 6]

            true
        }

        overlaySize?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.text_size_list)[
                    (newValue as? String)?.toInt() ?: 1]

            true
        }

        overlayTextStyle?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.text_style_list)[
                    (newValue as? String)?.toInt() ?: 0]

            true
        }

        overlayOpacity?.setOnPreferenceChangeListener { preference, newValue ->

            val progress = newValue as? Int ?: 0

            preference.summary = "${DecimalFormat("#").format(
                (progress.toFloat() / 255f) * 100f)}%"

            true
        }


        // Show/Hide
        overlayCategory = findPreference("show_hide_pref_category")
        batteryLevelOverlay = findPreference(IS_BATTERY_LEVEL_OVERLAY)
        numberOfChargesOverlay = findPreference(IS_NUMBER_OF_CHARGES_OVERLAY)
        numberOfCyclesOverlay = findPreference(IS_NUMBER_OF_CYCLES_OVERLAY)
        chargingTimeOverlay = findPreference(IS_CHARGING_TIME_OVERLAY)
        chargingTimeRemainingOverlay = findPreference(IS_CHARGING_TIME_REMAINING_OVERLAY)
        remainingBatteryTimeOverlay = findPreference(IS_REMAINING_BATTERY_TIME_OVERLAY)
        currentCapacityOverlay = findPreference(IS_CURRENT_CAPACITY_OVERLAY)
        capacityAddedOverlay = findPreference(IS_CAPACITY_ADDED_OVERLAY)
        batteryHealthOverlay = findPreference(IS_BATTERY_HEALTH_OVERLAY)
        residualCapacityOverlay = findPreference(IS_RESIDUAL_CAPACITY_OVERLAY)
        statusOverlay = findPreference(IS_STATUS_OVERLAY)
        sourceOfPowerOverlay = findPreference(IS_SOURCE_OF_POWER)
        chargeDischargeCurrentOverlay = findPreference(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        maxChargeDischargeCurrentOverlay = findPreference(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        averageChargeDischargeCurrentOverlay =
            findPreference(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        minChargeDischargeCurrentOverlay = findPreference(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        temperatureOverlay = findPreference(IS_TEMPERATURE_OVERLAY)
        voltageOverlay = findPreference(IS_VOLTAGE_OVERLAY)
        lastChargeTimeOverlay = findPreference(IS_LAST_CHARGE_TIME_OVERLAY)
        batteryWearOverlay = findPreference(IS_BATTERY_WEAR_OVERLAY)

        remainingBatteryTimeOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources
            .getBoolean(R.bool.is_supported))
        currentCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        capacityAddedOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        residualCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        batteryWearOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))

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

        numberOfCyclesOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        chargingTimeOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

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

        currentCapacityOverlay?.setOnPreferenceChangeListener { preference, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null
                && pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported)))
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            else if(!pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported)))
                (preference as? SwitchPreferenceCompat)?.isVisible = false


            true
        }

        capacityAddedOverlay?.setOnPreferenceChangeListener { preference, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null
                && pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
                    R.bool.is_supported))) ServiceHelper.startService(requireContext(),
                OverlayService::class.java)

            else if(!pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported)))
                (preference as? SwitchPreferenceCompat)?.isVisible = false

            true
        }

        batteryHealthOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        residualCapacityOverlay?.setOnPreferenceChangeListener { preference, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null
                && pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
                    R.bool.is_supported))) ServiceHelper.startService(requireContext(),
                OverlayService::class.java)

            else if(!pref.getBoolean(IS_SUPPORTED, resources.getBoolean(R.bool.is_supported)))
                (preference as? SwitchPreferenceCompat)?.isVisible = false

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

        temperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        voltageOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        lastChargeTimeOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null)
                ServiceHelper.startService(requireContext(), OverlayService::class.java)

            true
        }

        batteryWearOverlay?.setOnPreferenceChangeListener { preference, newValue ->

            if(newValue as? Boolean == true && OverlayService.instance == null
                && pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
                    R.bool.is_supported))) ServiceHelper.startService(requireContext(),
                OverlayService::class.java)

            else if(!pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
                    R.bool.is_supported))) (preference as? SwitchPreferenceCompat)?.isVisible = false

            true
        }
    }

    override fun onResume() {

        super.onResume()

        remainingBatteryTimeOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources
            .getBoolean(R.bool.is_supported))
        currentCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        currentCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        capacityAddedOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        residualCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))
        batteryWearOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, resources.getBoolean(
            R.bool.is_supported))

        overlaySize?.summary = getOverlayTextSizeSummary()

        overlayFont?.summary = getOverlayTextFontSummary()

        overlayTextStyle?.summary = getOverlayTextStyleSummary()

        overlayOpacity?.summary = getOverlayOpacitySummary()

        enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(
            R.bool.is_enabled_overlay)))

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val canDrawOverlays = Settings.canDrawOverlays(requireContext())

            overlayScreen?.isEnabled = canDrawOverlays

            if(dialogRequestOverlayPermission == null && !canDrawOverlays)
                requestOverlayPermission()
        }
    }

    private fun getOverlayTextSizeSummary(): CharSequence? {

        if(pref.getString(OVERLAY_SIZE, "2") !in
            resources.getStringArray(R.array.text_size_values))
            pref.edit().putString(OVERLAY_SIZE, "2").apply()

        return resources.getStringArray(R.array.text_size_list)[
                pref.getString(OVERLAY_SIZE, "2")?.toInt() ?: 1]
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

    private fun getOverlayOpacitySummary(): CharSequence? {

        if(pref.getInt(OVERLAY_OPACITY, if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127
            else 255) > 255
            || pref.getInt(OVERLAY_OPACITY, if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127
            else 255) < 0)
            pref.edit().putInt(OVERLAY_OPACITY, if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                127 else 255).apply()

        return "${DecimalFormat("#").format((pref.getInt(OVERLAY_OPACITY,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127
            else 255).toFloat() / 255f) * 100f)}%"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestOverlayPermission() {

        dialogRequestOverlayPermission = MaterialAlertDialogBuilder(requireContext()).apply {

            setMessage(getString(R.string.overlay_permission_dialog_message))
            setPositiveButton(getString(android.R.string.ok)) { _, _ ->

                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${requireContext().packageName}"))

                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION)

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
        appearanceCategory?.isEnabled = isEnable ?: appearanceCategory?.isEnabled ?: false
        overlayCategory?.isEnabled = isEnable ?: overlayCategory?.isEnabled ?: false
    }
}