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
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.Constants.ACTION_MANAGE_OVERLAY_PERMISSION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utils.Utils.isEnabledOverlay

class OverlayFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var overlayScreen: PreferenceScreen? = null
    private var enableOverlay: SwitchPreferenceCompat? = null
    private var overlaySize: ListPreference? = null
    private var batteryLevelOverlay: SwitchPreferenceCompat? = null
    private var currentCapacityOverlay: SwitchPreferenceCompat? = null
    private var batteryHealthOverlay: SwitchPreferenceCompat? = null
    private var statusOverlay: SwitchPreferenceCompat? = null
    private var chargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var maxChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var averageChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var minChargeDischargeCurrentOverlay: SwitchPreferenceCompat? = null
    private var temperatureOverlay: SwitchPreferenceCompat? = null
    private var voltageOverlay: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.overlay)

        overlayScreen = findPreference("overlay_screen")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            overlayScreen?.isEnabled = Settings.canDrawOverlays(requireContext())

        enableOverlay = findPreference(IS_ENABLED_OVERLAY)
        overlaySize = findPreference(OVERLAY_SIZE)
        batteryLevelOverlay = findPreference(IS_BATTERY_LEVEL_OVERLAY)
        currentCapacityOverlay = findPreference(IS_CURRENT_CAPACITY_OVERLAY)
        batteryHealthOverlay = findPreference(IS_BATTERY_HEALTH_OVERLAY)
        statusOverlay = findPreference(IS_STATUS_OVERLAY)
        chargeDischargeCurrentOverlay = findPreference(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        maxChargeDischargeCurrentOverlay = findPreference(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        averageChargeDischargeCurrentOverlay = findPreference(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        minChargeDischargeCurrentOverlay = findPreference(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY)
        temperatureOverlay = findPreference(IS_TEMPERATURE_OVERLAY)
        voltageOverlay = findPreference(IS_VOLTAGE_OVERLAY)

        currentCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, true)

        enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, false))

        if(overlaySize?.value !in resources.getStringArray(R.array.overlay_size_keys)) {

            overlaySize?.value = resources.getStringArray(R.array.overlay_size_keys)[1]

            pref.edit().putString(OVERLAY_SIZE, "1").apply()
        }

        overlaySize?.summary = overlaySize?.entry

        enableOverlay?.setOnPreferenceChangeListener { _, newValue ->

            when(newValue as? Boolean) {

                true -> {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if(Settings.canDrawOverlays(requireContext()) && isEnabledOverlay(requireContext())
                            && OverlayService.instance == null)
                            requireContext().startService(Intent(requireContext(), OverlayService::class.java))
                    }

                    else if(isEnabledOverlay(requireContext()) && OverlayService.instance == null)
                        requireContext().startService(Intent(requireContext(), OverlayService::class.java))
                }

                false -> if(OverlayService.instance != null) requireContext()
                    .stopService(Intent(requireContext(), OverlayService::class.java))
            }

            enableAllOverlay(newValue as? Boolean)

            true
        }

        overlaySize?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.overlay_size_list)[(newValue as? String)?.toInt() ?: 1]

            true
        }

        batteryLevelOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        if(pref.getBoolean(IS_SUPPORTED, true))
        currentCapacityOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        batteryHealthOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        statusOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        chargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        maxChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        averageChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        minChargeDischargeCurrentOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        temperatureOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        voltageOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }
    }

    override fun onResume() {

        super.onResume()

        currentCapacityOverlay?.isVisible = pref.getBoolean(IS_SUPPORTED, true)

        if(overlaySize?.value !in resources.getStringArray(R.array.overlay_size_keys)) {

            overlaySize?.value = resources.getStringArray(R.array.overlay_size_keys)[1]

            pref.edit().putString(OVERLAY_SIZE, "1").apply()
        }

        overlaySize?.summary = overlaySize?.entry

        enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, false))

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            overlayScreen?.isEnabled = Settings.canDrawOverlays(requireContext())

            if(!Settings.canDrawOverlays(requireContext())) {
                
                MaterialAlertDialogBuilder(requireContext()).apply { 
                    
                    setMessage(getString(R.string.overlay_permission_dialog_message))
                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                        
                        requireSystemAlertWindowPermission()
                    }
                    setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

                    show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requireSystemAlertWindowPermission() {

        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${requireContext().packageName}"))

        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION)
    }

    private fun enableAllOverlay(enable: Boolean?) {

        overlaySize?.isEnabled = enable ?: false
        batteryLevelOverlay?.isEnabled = enable ?: false
        currentCapacityOverlay?.isEnabled = (enable ?: false) && (currentCapacityOverlay?.isVisible ?: false)
        batteryHealthOverlay?.isEnabled = enable ?: false
        statusOverlay?.isEnabled = enable ?: false
        chargeDischargeCurrentOverlay?.isEnabled = enable ?: false
        maxChargeDischargeCurrentOverlay?.isEnabled = enable ?: false
        averageChargeDischargeCurrentOverlay?.isEnabled = enable ?: false
        minChargeDischargeCurrentOverlay?.isEnabled = enable ?: false
        temperatureOverlay?.isEnabled = enable ?: false
        voltageOverlay?.isEnabled = enable ?: false
    }
}