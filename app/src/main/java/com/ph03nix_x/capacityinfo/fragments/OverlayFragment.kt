package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
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
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NUMBER_OF_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NUMBER_OF_CYCLES_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utils.Utils.isEnabledOverlay
import java.text.DecimalFormat

class OverlayFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var dialogRequestOverlayPermission: MaterialAlertDialogBuilder? = null

    private var overlayScreen: PreferenceScreen? = null
    private var enableOverlay: SwitchPreferenceCompat? = null

    // Appearance
    private var appearanceCategory: PreferenceCategory? = null
    private var overlaySize: ListPreference? = null
    private var overlayOpacity: Preference? = null

    // Overlay
    private var overlayCategory: PreferenceCategory? = null
    private var numberOfChargesOverlay: SwitchPreferenceCompat? = null
    private var numberOfCyclesOverlay: SwitchPreferenceCompat? = null
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

        // Appearance
        appearanceCategory = findPreference("appearance_overlay")
        overlaySize = findPreference(OVERLAY_SIZE)
        overlayOpacity = findPreference("overlay_opacity")

        if(overlaySize?.value !in resources.getStringArray(R.array.overlay_size_keys)) {

            overlaySize?.value = resources.getStringArray(R.array.overlay_size_keys)[1]

            pref.edit().putString(OVERLAY_SIZE, "1").apply()
        }

        overlaySize?.summary = overlaySize?.entry

        if(pref.getInt(OVERLAY_OPACITY, 127) > 255
            || pref.getInt(OVERLAY_OPACITY, 127) < 0)
            pref.edit().putInt(OVERLAY_OPACITY, 127).apply()

        overlayOpacity?.summary = "${DecimalFormat("#")
            .format((pref.getInt(OVERLAY_OPACITY, 127).toFloat() / 255f) * 100f)}%"

        overlaySize?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array.overlay_size_list)[
                    (newValue as? String)?.toInt() ?: 1]

            true
        }

        overlayOpacity?.setOnPreferenceClickListener {

            overlayOpacityDialog()

            true
        }

        // Overlay
        overlayCategory = findPreference("overlay_category")
        batteryLevelOverlay = findPreference(IS_BATTERY_LEVEL_OVERLAY)
        numberOfChargesOverlay = findPreference(IS_NUMBER_OF_CHARGES_OVERLAY)
        numberOfCyclesOverlay = findPreference(IS_NUMBER_OF_CYCLES_OVERLAY)
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

        batteryLevelOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        numberOfChargesOverlay?.setOnPreferenceChangeListener { _, newValue ->

            if(newValue as Boolean && OverlayService.instance == null)
                requireContext().startService(Intent(requireContext(), OverlayService::class.java))

            true
        }

        numberOfCyclesOverlay?.setOnPreferenceChangeListener { _, newValue ->

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

        if(pref.getInt(OVERLAY_OPACITY, 127) > 255
            || pref.getInt(OVERLAY_OPACITY, 127) < 0)
            pref.edit().putInt(OVERLAY_OPACITY, 127).apply()

        overlayOpacity?.summary = "${DecimalFormat("#")
            .format((pref.getInt(OVERLAY_OPACITY, 127).toFloat() / 255f) * 100f)}%"

        enableAllOverlay(pref.getBoolean(IS_ENABLED_OVERLAY, false))

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            overlayScreen?.isEnabled = Settings.canDrawOverlays(requireContext())

            if(dialogRequestOverlayPermission == null)
                if(!Settings.canDrawOverlays(requireContext())) requestOverlayPermission()
        }
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

    private fun enableAllOverlay(enable: Boolean?) {

        appearanceCategory?.isEnabled = enable ?: false
        overlayCategory?.isEnabled = enable ?: false
    }

    private fun overlayOpacityDialog() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.overlay_opacity_dialog, null)

        dialog.setView(view)

        val opacityTV = view.findViewById<AppCompatTextView>(R.id.opacity_tv)

        val opacitySeekBar = view.findViewById<AppCompatSeekBar>(R.id.opacity_seekbar)

        opacitySeekBar.progress = if(pref.getInt(OVERLAY_OPACITY, 127) > 255
            || pref.getInt(OVERLAY_OPACITY, 127) < 0) 127
        else pref.getInt(OVERLAY_OPACITY, 127)

        opacityTV.text = getString(R.string.opacity_percent,
            "${DecimalFormat("#")
                .format((opacitySeekBar.progress.toFloat() / 255f) * 100f)}%")

        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                opacityTV.text = getString(R.string.opacity_percent,
                    "${DecimalFormat("#")
                        .format((opacitySeekBar.progress.toFloat() / 255f) * 100f)}%")

                overlayOpacity?.summary = "${DecimalFormat("#")
                    .format((opacitySeekBar.progress.toFloat() / 255f) * 100f)}%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {

                pref.edit().putInt(OVERLAY_OPACITY, seekBar.progress).apply()
            }
        })

        dialog.setPositiveButton(getString(android.R.string.ok)) { d, _ -> d.dismiss() }

        dialog.show()
    }
}