package com.ph03nix_x.capacityinfo.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.databinding.ChangeBatteryIsChargedDischargedVoltageDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ChangeChargingCurrentNotifyLevelDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ChangeDischargeCurrentNotifyLevelDialogBinding
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.FullChargeReminderJobService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.Constants.POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DISCHARGE_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FULL_CHARGE_REMINDER_FREQUENCY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_CHARGING_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_FULL_CHARGE_REMINDER
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.minutes

class BatteryStatusInformationFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var batteryStatusInformationSettingsPrefCategory: PreferenceCategory? = null
    private var allowAllAppNotifications: Preference? = null
    private var notifyOverheatOvercool: SwitchPreferenceCompat? = null
    private var overheatDegrees: SeekBarPreference? = null
    private var overcoolDegrees: SeekBarPreference? = null
    private var notifyBatteryIsFullyCharged: SwitchPreferenceCompat? = null
    private var notifyFullChargeReminder: SwitchPreferenceCompat? = null
    private var fullChargeReminderFrequency: ListPreference? = null
    private var notifyBatteryIsCharged: SwitchPreferenceCompat? = null
    private var notifyBatteryIsChargedVoltage: SwitchPreferenceCompat? = null
    private var notifyChargingCurrent: SwitchPreferenceCompat? = null
    private var notifyDischargeCurrent: SwitchPreferenceCompat? = null
    private var batteryLevelNotifyCharged: SeekBarPreference? = null
    private var batteryNotifyChargedVoltage: SeekBarPreference? = null
    private var notifyBatteryIsDischarged: SwitchPreferenceCompat? = null
    private var notifyBatteryIsDischargedVoltage: SwitchPreferenceCompat? = null
    private var batteryLevelNotifyDischarged: SeekBarPreference? = null
    private var batteryNotifyDischargedVoltage: SeekBarPreference? = null
    private var chargingCurrentLevelNotify: SeekBarPreference? = null
    private var dischargeCurrentLevelNotify: SeekBarPreference? = null
    private var exportNotificationSounds: Preference? = null

    private var requestCode = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.battery_status_information_settings)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    when (requestCode) {

                        POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE -> {
                            allowAllAppNotifications?.isVisible = !it.value
                            batteryStatusInformationSettingsPrefCategory?.isEnabled = it.value
                        }
                    }
                }
            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(requestCode) {
                EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE -> if(it.resultCode == Activity.RESULT_OK)
                    exportNotificationSounds(it.data)
            }
        }

        batteryStatusInformationSettingsPrefCategory =
            findPreference("battery_status_information_settings_pref_category")
        allowAllAppNotifications = findPreference("allow_all_app_notifications")
        notifyOverheatOvercool = findPreference(IS_NOTIFY_OVERHEAT_OVERCOOL)
        overheatDegrees = findPreference(OVERHEAT_DEGREES)
        overcoolDegrees = findPreference(OVERCOOL_DEGREES)
        notifyBatteryIsFullyCharged = findPreference(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED)
        notifyFullChargeReminder = findPreference(IS_NOTIFY_FULL_CHARGE_REMINDER)
        fullChargeReminderFrequency = findPreference(FULL_CHARGE_REMINDER_FREQUENCY)
        notifyBatteryIsCharged = findPreference(IS_NOTIFY_BATTERY_IS_CHARGED)
        notifyBatteryIsChargedVoltage = findPreference(IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE)
        notifyChargingCurrent = findPreference(IS_NOTIFY_CHARGING_CURRENT)
        notifyDischargeCurrent = findPreference(IS_NOTIFY_DISCHARGE_CURRENT)
        batteryLevelNotifyCharged = findPreference(BATTERY_LEVEL_NOTIFY_CHARGED)
        batteryNotifyChargedVoltage = findPreference(BATTERY_NOTIFY_CHARGED_VOLTAGE)
        notifyBatteryIsDischarged = findPreference(IS_NOTIFY_BATTERY_IS_DISCHARGED)
        notifyBatteryIsDischargedVoltage = findPreference(IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE)
        batteryLevelNotifyDischarged = findPreference(BATTERY_LEVEL_NOTIFY_DISCHARGED)
        batteryNotifyDischargedVoltage = findPreference(BATTERY_NOTIFY_DISCHARGED_VOLTAGE)
        chargingCurrentLevelNotify = findPreference(CHARGING_CURRENT_LEVEL_NOTIFY)
        dischargeCurrentLevelNotify = findPreference(DISCHARGE_CURRENT_LEVEL_NOTIFY)
        exportNotificationSounds = findPreference("export_notification_sounds")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestCode = POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
            }
            batteryStatusInformationSettingsPrefCategory?.isEnabled = ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            allowAllAppNotifications?.apply {

                isVisible = ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED

                allowAllAppNotifications?.setOnPreferenceClickListener {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    })

                    true
                }
            }
        }

        overheatDegrees?.apply {
            summary = getOverheatDegreesSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                R.bool.is_notify_overheat_overcool))

            setOnPreferenceChangeListener { preference, newValue ->

                val temperature = (newValue as? Int) ?: resources.getInteger(R.integer
                    .overheat_degrees_default)

                preference.summary = getString(R.string.overheat_overcool_degrees, temperature,
                    DecimalFormat("#.#").format((temperature * 1.8) + 32.0))

                true
            }
        }

        overcoolDegrees?.apply {
            summary = getOvercoolDegreesSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                R.bool.is_notify_overheat_overcool))

            setOnPreferenceChangeListener { preference, newValue ->

                val temperature = (newValue as? Int) ?: resources.getInteger(R.integer
                    .overcool_degrees_default)

                preference.summary = getString(R.string.overheat_overcool_degrees, temperature,
                    DecimalFormat("#.#").format((temperature * 1.8) + 32.0))

                true
            }
        }

        notifyFullChargeReminder?.apply {

            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED,
                resources.getBoolean(R.bool.is_notify_battery_is_fully_charged))

            setOnPreferenceChangeListener { _, newValue ->

                val isChecked = (newValue as? Boolean) ?: false

                fullChargeReminderFrequency?.isEnabled = isChecked

                val fullChargeReminderFrequency = pref.getString(FULL_CHARGE_REMINDER_FREQUENCY,
                    "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}")?.toInt()

                if(isChecked && CapacityInfoService.instance?.isFull == true)
                    ServiceHelper.jobSchedule(requireContext(),
                        FullChargeReminderJobService::class.java,
                        Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID,
                        fullChargeReminderFrequency?.minutes?.inWholeMilliseconds ?: resources
                            .getInteger(R.integer.full_charge_reminder_frequency_default).minutes
                            .inWholeMilliseconds)
                else ServiceHelper.cancelJob(requireContext(),
                    Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)

                true
            }
        }

        batteryLevelNotifyCharged?.apply {
            summary = getBatteryLevelNotifyChargingSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))

            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = "${((newValue as? Int) ?: pref.getInt(
                    BATTERY_LEVEL_NOTIFY_CHARGED, 80))}%"

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

                true
            }
        }

        batteryNotifyChargedVoltage?.apply {
            summary = "${pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                .battery_notify_charged_voltage_min))}"
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE, resources.getBoolean(
                R.bool.is_notify_battery_is_charged_voltage))

            setOnPreferenceClickListener {

                changeBatteryNotifyChargedVoltage()

                true
            }

            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = "${((newValue as? Int) ?: pref.getInt(
                    BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                        .battery_notify_charged_voltage_min)))}"

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

                true
            }
        }

        batteryLevelNotifyDischarged?.apply {
            summary = getBatteryLevelNotifyDischargeSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_discharged))

            setOnPreferenceChangeListener { preference, newValue ->

                preference.summary = "${((newValue as? Int) ?: pref.getInt(
                    BATTERY_LEVEL_NOTIFY_DISCHARGED, 20))}%"

                NotificationInterface.notificationManager?.cancel(
                    NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

                true
            }
        }

        batteryNotifyDischargedVoltage?.apply {
            summary = "${pref.getInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(
                R.integer.battery_notify_discharged_voltage_min))}"
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE,
                resources.getBoolean(R.bool.is_notify_battery_is_discharged_voltage))

            setOnPreferenceClickListener {

                changeBatteryNotifyDischargedVoltage()

                true
            }
        }

        notifyOverheatOvercool?.setOnPreferenceChangeListener { _, newValue ->

            overheatDegrees?.isEnabled = newValue as? Boolean == true

            overcoolDegrees?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)

            true
        }

        notifyBatteryIsFullyCharged?.setOnPreferenceChangeListener { _, newValue ->

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_FULLY_CHARGED_ID)

            val isChecked = (newValue as? Boolean) ?: false

            notifyFullChargeReminder?.isEnabled = isChecked
            fullChargeReminderFrequency?.isEnabled = isChecked &&
                    notifyFullChargeReminder?.isChecked == true

            if(!isChecked) ServiceHelper.cancelJob(requireContext(),
                Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)

            true
        }

        fullChargeReminderFrequency?.apply {
            isEnabled = notifyBatteryIsFullyCharged?.isChecked == true &&
                    notifyFullChargeReminder?.isChecked == true
            summary = getFullChargeReminderFrequencySummary()
            setOnPreferenceChangeListener { preference, newValue ->

                ServiceHelper.cancelJob(requireContext(),
                    Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)

                val fullChargeReminderFrequencyPref = pref.getString(FULL_CHARGE_REMINDER_FREQUENCY,
                    "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}")

                if(notifyFullChargeReminder?.isChecked == true &&
                    CapacityInfoService.instance?.isFull == true)
                    ServiceHelper.jobSchedule(requireContext(),
                        FullChargeReminderJobService::class.java,
                        Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID,
                        fullChargeReminderFrequencyPref?.toInt()?.minutes
                            ?.inWholeMilliseconds ?: resources.getInteger(R.integer
                                .full_charge_reminder_frequency_default).minutes.inWholeMilliseconds)

                preference.summary = resources.getStringArray(R.array
                    .full_charge_reminder_frequency_list)[when((newValue as? String)?.toInt()) {
                        15 -> 0
                        30 -> 1
                        45 -> 2
                        60 -> 3
                        else -> resources.getInteger(
                            R.integer.full_charge_reminder_frequency_default_index)
                }]

                true
            }
        }

        notifyBatteryIsCharged?.setOnPreferenceChangeListener { _, newValue ->

            batteryLevelNotifyCharged?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyBatteryIsChargedVoltage?.setOnPreferenceChangeListener { _, newValue ->

            batteryNotifyChargedVoltage?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyChargingCurrent?.setOnPreferenceChangeListener { _, newValue ->

            chargingCurrentLevelNotify?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

            true
        }

        notifyDischargeCurrent?.setOnPreferenceChangeListener { _, newValue ->

            dischargeCurrentLevelNotify?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_DISCHARGE_CURRENT_ID)

            true
        }

        notifyBatteryIsDischarged?.setOnPreferenceChangeListener { _, newValue ->

            batteryLevelNotifyDischarged?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyBatteryIsDischargedVoltage?.setOnPreferenceChangeListener { _, newValue ->

            batteryNotifyDischargedVoltage?.isEnabled = newValue as? Boolean == true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        batteryNotifyDischargedVoltage?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = "${((newValue as? Int) ?: pref.getInt(
                BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_discharged_voltage_min)))}"

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        chargingCurrentLevelNotify?.setOnPreferenceClickListener {

            changeChargingCurrentNotifyLevel()

            true
        }

        chargingCurrentLevelNotify?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = getString(R.string.ma, ((newValue as? Int) ?: pref.getInt(
                CHARGING_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                    .charging_current_notify_level_min))))

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

            true
        }

        dischargeCurrentLevelNotify?.setOnPreferenceClickListener {

            changeDischargeCurrentNotifyLevel()

            true
        }

        dischargeCurrentLevelNotify?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = getString(R.string.ma, ((newValue as? Int) ?: pref.getInt(
                DISCHARGE_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                    .discharge_current_notify_level_min))))

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_DISCHARGE_CURRENT_ID)

            true
        }

        exportNotificationSounds?.setOnPreferenceClickListener {

            try {
                requestCode = EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE
                getResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
            }
            catch(e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), getString(R.string
                    .error_exporting_notification_sounds, e.message ?: e.toString()),
                    Toast.LENGTH_LONG).show()
            }

            true
        }
    }

    override fun onResume() {

        super.onResume()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            batteryStatusInformationSettingsPrefCategory?.isEnabled = ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            allowAllAppNotifications?.isVisible = ContextCompat
                .checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_DENIED
        }

        fullChargeReminderFrequency?.apply {
            isEnabled = notifyBatteryIsFullyCharged?.isChecked == true &&
                    notifyFullChargeReminder?.isChecked == true
            summary = getFullChargeReminderFrequencySummary()
        }

        overheatDegrees?.apply {
            summary = getOverheatDegreesSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                R.bool.is_notify_overheat_overcool))
        }

        overcoolDegrees?.apply {
            summary = getOvercoolDegreesSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                R.bool.is_notify_overheat_overcool))
        }

        notifyOverheatOvercool?.isChecked = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources
            .getBoolean(R.bool.is_notify_overheat_overcool))

        notifyBatteryIsFullyCharged?.isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED,
            resources.getBoolean(R.bool.is_notify_battery_is_fully_charged))

        notifyFullChargeReminder?.apply {

            isChecked = pref.getBoolean(IS_NOTIFY_FULL_CHARGE_REMINDER,
                resources.getBoolean(R.bool.is_notify_full_charge_reminder_default_value))

            isEnabled = notifyBatteryIsFullyCharged?.isChecked == true
        }

       notifyBatteryIsCharged?.isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources
           .getBoolean(R.bool.is_notify_battery_is_charged))

        notifyBatteryIsDischarged?.isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED,
            resources.getBoolean(R.bool.is_notify_battery_is_discharged))

        notifyBatteryIsDischargedVoltage?.isChecked = pref.getBoolean(
            IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE, resources.getBoolean(R.bool
                .is_notify_battery_is_discharged_voltage))

        notifyChargingCurrent?.isChecked = pref.getBoolean(IS_NOTIFY_CHARGING_CURRENT, resources
            .getBoolean(R.bool.is_notify_charging_current))

        notifyDischargeCurrent?.isChecked = pref.getBoolean(IS_NOTIFY_DISCHARGE_CURRENT, resources
            .getBoolean(R.bool.is_notify_discharge_current))

        batteryLevelNotifyCharged?.apply {

            summary = getBatteryLevelNotifyChargingSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))
        }

        batteryNotifyChargedVoltage?.apply {
            summary = getString(R.string.battery_charged_discharged_voltage_seekbar_summary, pref.getInt(
                BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min)))
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE, resources.getBoolean(
                R.bool.is_notify_battery_is_charged_voltage))
        }

        batteryLevelNotifyDischarged?.apply {

            summary = getBatteryLevelNotifyDischargeSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_discharged))
        }

        batteryNotifyDischargedVoltage?.apply {
            summary = "${pref.getInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(
                R.integer.battery_notify_discharged_voltage_min))}"
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE,
                resources.getBoolean(R.bool.is_notify_battery_is_discharged_voltage))
        }

        chargingCurrentLevelNotify?.apply {

            summary = getChargingCurrentLevelNotifySummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_CHARGING_CURRENT, resources.getBoolean(
                R.bool.is_notify_charging_current))
        }

        dischargeCurrentLevelNotify?.apply {

            summary = getDischargeCurrentLevelNotifySummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_DISCHARGE_CURRENT, resources.getBoolean(
                R.bool.is_notify_discharge_current))
        }
    }

    private fun getFullChargeReminderFrequencySummary(): String {
        if(pref.getString(FULL_CHARGE_REMINDER_FREQUENCY,
                "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}") !in
            resources.getStringArray(R.array.full_charge_reminder_frequency_values))
            pref.edit().putString(FULL_CHARGE_REMINDER_FREQUENCY,
                "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}").apply()

        val fullChargeReminderFrequencyPref = pref.getString(FULL_CHARGE_REMINDER_FREQUENCY,
            "${resources.getInteger(R.integer.full_charge_reminder_frequency_default)}")

        return resources.getStringArray(R.array.full_charge_reminder_frequency_list)[
           when(fullChargeReminderFrequencyPref?.toInt()) {
               15 -> 0
               30 -> 1
               45 -> 2
               60 -> 3
               else -> resources.getInteger(R.integer.full_charge_reminder_frequency_default_index)
           }]
    }

    private fun getOverheatDegreesSummary(): String {

        if(pref.getInt(OVERHEAT_DEGREES, resources.getInteger(R.integer.overheat_degrees_default))
            > resources.getInteger(R.integer.overheat_degrees_max) ||
            pref.getInt(OVERHEAT_DEGREES, resources.getInteger(R.integer.overheat_degrees_default))
            < resources.getInteger(R.integer.overheat_degrees_min))
            pref.edit().putInt(OVERHEAT_DEGREES, resources.getInteger(R
                .integer.overheat_degrees_default)).apply()

        val temperature = pref.getInt(OVERHEAT_DEGREES,
            resources.getInteger(R.integer.overheat_degrees_default))

        return getString(R.string.overheat_overcool_degrees, temperature, DecimalFormat(
            "#.#").format((temperature * 1.8) + 32.0))
    }

    private fun getOvercoolDegreesSummary(): String {

        if(pref.getInt(OVERCOOL_DEGREES, resources.getInteger(R.integer.overcool_degrees_default))
            > resources.getInteger(R.integer.overcool_degrees_max) ||
            pref.getInt(OVERCOOL_DEGREES, resources.getInteger(R.integer.overcool_degrees_default))
            < resources.getInteger(R.integer.overcool_degrees_min))
            pref.edit().putInt(OVERCOOL_DEGREES, resources.getInteger(R
                .integer.overcool_degrees_default)).apply()

        val temperature = pref.getInt(OVERCOOL_DEGREES,
            resources.getInteger(R.integer.overcool_degrees_default))

        return getString(R.string.overheat_overcool_degrees, temperature, DecimalFormat(
            "#.#").format((temperature * 1.8) + 32.0))
    }

    private fun getBatteryLevelNotifyChargingSummary(): String {

        if(pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED, 80) > 100 ||
                pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED, 80) < 1)
            pref.edit().putInt(BATTERY_LEVEL_NOTIFY_CHARGED, 80).apply()

        return "${pref.getInt(BATTERY_LEVEL_NOTIFY_CHARGED, 1)}%"
    }

    private fun getBatteryLevelNotifyDischargeSummary(): String {

        if(pref.getInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20) > 99 ||
            pref.getInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20) < 1)
            pref.edit().putInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20).apply()

        return "${pref.getInt(BATTERY_LEVEL_NOTIFY_DISCHARGED, 20)}%"
    }

    private fun getChargingCurrentLevelNotifySummary(): String {

        if(pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                .charging_current_notify_level_min)) > resources.getInteger(R.integer
                .charging_current_notify_level_max) || pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY,
                resources.getInteger(R.integer.charging_current_notify_level_min)) < resources
                .getInteger(R.integer.charging_current_notify_level_min))
                    pref.edit().putInt(CHARGING_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                .charging_current_notify_level_min)).apply()

        return getString(R.string.ma, pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY, resources
            .getInteger(R.integer.charging_current_notify_level_min)))
    }

    private fun getDischargeCurrentLevelNotifySummary(): String {

        if(pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                .discharge_current_notify_level_min)) > resources.getInteger(R.integer
                .discharge_current_notify_level_max) || pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY,
                resources.getInteger(R.integer.discharge_current_notify_level_min)) < resources
                .getInteger(R.integer.discharge_current_notify_level_min))
            pref.edit().putInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, resources.getInteger(R.integer
                .discharge_current_notify_level_min)).apply()

        return getString(R.string.ma, pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, resources
            .getInteger(R.integer.discharge_current_notify_level_min)))
    }

    private fun changeBatteryNotifyChargedVoltage() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val binding = ChangeBatteryIsChargedDischargedVoltageDialogBinding.inflate(
            LayoutInflater.from(context), null, false)

        dialog.setView(binding.root.rootView)

        binding.changeBatteryIsChargedDischargedMvEdit.setText(if(pref.getInt(
                BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min)) >= resources.getInteger(R.integer
                .battery_notify_charged_voltage_min) || pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE,
                resources.getInteger(R.integer.battery_notify_charged_voltage_min)) <= resources
                .getInteger(R.integer.battery_notify_charged_voltage_max))
            pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                .battery_notify_charged_voltage_min)).toString()

        else resources.getInteger(R.integer.battery_notify_charged_voltage_min).toString())

        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->

            pref.edit().putInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, binding
                .changeBatteryIsChargedDischargedMvEdit.text.toString().toInt()).apply()

            this.batteryNotifyChargedVoltage?.apply {

                summary = getString(R.string.battery_charged_discharged_voltage_seekbar_summary,
                    binding.changeBatteryIsChargedDischargedMvEdit.text.toString().toInt())

                value = pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeBatteryNotifyChargedVoltageDialogCreateShowListener(dialogCreate,
            binding.changeBatteryIsChargedDischargedMvEdit)

        dialogCreate.show()
    }

    private fun changeBatteryNotifyChargedVoltageDialogCreateShowListener(dialogCreate: AlertDialog,
                                                                         changeChargingCurrentLevel:
                                                                         TextInputEditText) {

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeChargingCurrentLevel.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE,
                            resources.getInteger(R.integer.battery_notify_charged_voltage_min)).toString()
                                && s.toString().toInt() >= resources.getInteger(R.integer
                            .battery_notify_charged_voltage_min) && s.toString().toInt() <= resources
                            .getInteger(R.integer.battery_notify_charged_voltage_max)
                }
            })
        }
    }

    private fun changeBatteryNotifyDischargedVoltage() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val binding = ChangeBatteryIsChargedDischargedVoltageDialogBinding.inflate(
            LayoutInflater.from(context), null, false)

        dialog.setView(binding.root.rootView)

        binding.changeBatteryIsChargedDischargedMvEdit.setText(if(pref.getInt(
                BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_discharged_voltage_min)) >= resources.getInteger(R.integer
                .battery_notify_discharged_voltage_min) || pref.getInt(
                BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_discharged_voltage_min)) <= resources.getInteger(R.integer
                .battery_notify_discharged_voltage_max)) pref.getInt(
            BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                .battery_notify_discharged_voltage_min)).toString()

        else resources.getInteger(R.integer.battery_notify_discharged_voltage_min).toString())

        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->

            pref.edit().putInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE,
                binding.changeBatteryIsChargedDischargedMvEdit.text.toString().toInt()).apply()

            this.batteryNotifyDischargedVoltage?.apply {

                summary = getString(R.string.battery_charged_discharged_voltage_seekbar_summary,
                    binding.changeBatteryIsChargedDischargedMvEdit.text.toString().toInt())

                value = pref.getInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(
                    R.integer.battery_notify_discharged_voltage_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeBatteryNotifyDischargedVoltageDialogCreateShowListener(dialogCreate,
            binding.changeBatteryIsChargedDischargedMvEdit)

        dialogCreate.show()
    }

    private fun changeBatteryNotifyDischargedVoltageDialogCreateShowListener(dialogCreate: AlertDialog,
                                                                          changeChargingCurrentLevel:
                                                                          TextInputEditText) {

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeChargingCurrentLevel.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(
                            BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                                .battery_notify_discharged_voltage_min)).toString()
                                && s.toString().toInt() >= resources.getInteger(R.integer
                            .battery_notify_discharged_voltage_min)
                                && s.toString().toInt() <= resources.getInteger(R.integer
                            .battery_notify_discharged_voltage_max)
                }
            })
        }
    }

    private fun changeChargingCurrentNotifyLevel() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val  binding = ChangeChargingCurrentNotifyLevelDialogBinding.inflate(
            LayoutInflater.from(context), null, false)

        dialog.setView(binding.root.rootView)

        binding.changeChargingCurrentLevelEdit.setText(if(pref.getInt(
                CHARGING_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                R.integer.charging_current_notify_level_min)) >= requireContext().resources
                .getInteger(R.integer.charging_current_notify_level_min) || pref.getInt(
                CHARGING_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                    R.integer.charging_current_notify_level_min)) <= requireContext().resources
                .getInteger(R.integer.charging_current_notify_level_max))
                    pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY, requireContext().resources
                        .getInteger(R.integer.charging_current_notify_level_min)).toString()

        else requireContext().resources.getInteger(R.integer.min_design_capacity).toString())

        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->

            pref.edit().putInt(CHARGING_CURRENT_LEVEL_NOTIFY, binding
                .changeChargingCurrentLevelEdit.text.toString().toInt()).apply()

            chargingCurrentLevelNotify?.apply {

                summary = getString(R.string.ma, binding.changeChargingCurrentLevelEdit.text
                    .toString().toInt())

                value = pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY,
                    resources.getInteger(R.integer.charging_current_notify_level_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeChargingCurrentNotifyLevelDialogCreateShowListener(dialogCreate,
            binding.changeChargingCurrentLevelEdit)

        dialogCreate.show()
    }

    private fun changeChargingCurrentNotifyLevelDialogCreateShowListener(dialogCreate: AlertDialog,
                                                                         changeChargingCurrentLevel:
                                                                         TextInputEditText) {

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeChargingCurrentLevel.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(
                            CHARGING_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                                R.integer.charging_current_notify_level_min)).toString() &&
                                s.toString().toInt() >= requireContext().resources.getInteger(
                            R.integer.charging_current_notify_level_min) && s.toString().toInt() <=
                                requireContext().resources.getInteger(
                                    R.integer.charging_current_notify_level_max)
                }
            })
        }
    }

    private fun changeDischargeCurrentNotifyLevel() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val binding = ChangeDischargeCurrentNotifyLevelDialogBinding.inflate(
            LayoutInflater.from(context), null, false)

        dialog.setView(binding.root.rootView)

        binding.changeDischargeCurrentLevelEdit.setText(if(pref.getInt(
                DISCHARGE_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                    R.integer.discharge_current_notify_level_min)) >= requireContext().resources
                .getInteger(R.integer.discharge_current_notify_level_min) || pref.getInt(
                DISCHARGE_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                    R.integer.discharge_current_notify_level_min)) <= requireContext().resources
                .getInteger(R.integer.discharge_current_notify_level_max))
            pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, requireContext().resources
                .getInteger(R.integer.discharge_current_notify_level_min)).toString()

        else requireContext().resources.getInteger(R.integer.discharge_current_notify_level_min)
            .toString())

        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, binding
                .changeDischargeCurrentLevelEdit.text.toString().toInt()).apply()

            dischargeCurrentLevelNotify?.apply {

                summary = getString(R.string.ma, binding.changeDischargeCurrentLevelEdit.text
                    .toString().toInt())

                value = pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY,
                    resources.getInteger(R.integer.discharge_current_notify_level_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeDischargeCurrentNotifyLevelDialogCreateShowListener(dialogCreate,
            binding.changeDischargeCurrentLevelEdit)

        dialogCreate.show()
    }

    private fun changeDischargeCurrentNotifyLevelDialogCreateShowListener(dialogCreate: AlertDialog,
                                                                         changeDischargeCurrentLevel:
                                                                         TextInputEditText) {

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeDischargeCurrentLevel.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(
                            DISCHARGE_CURRENT_LEVEL_NOTIFY, requireContext().resources.getInteger(
                                R.integer.discharge_current_notify_level_min)).toString() &&
                                s.toString().toInt() >= requireContext().resources.getInteger(
                            R.integer.discharge_current_notify_level_min) && s.toString().toInt() <=
                                requireContext().resources.getInteger(
                                    R.integer.discharge_current_notify_level_max)
                }
            })
        }
    }

    private fun exportNotificationSounds(intent: Intent?) {

        val notificationSoundsList = arrayListOf(R.raw.overheat_overcool,
            R.raw.battery_is_fully_charged, R.raw.battery_is_charged, R.raw.battery_is_discharged,
            R.raw.charging_current)

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    context?.let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                notificationSoundsList.forEach {

                    pickerDir?.findFile("${resources.getResourceEntryName(
                        it)}.mp3")?.delete()

                    val outputStream = pickerDir?.createFile("audio/mpeg",
                        resources.getResourceEntryName(it))?.uri?.let { it1 ->
                        context?.contentResolver?.openOutputStream(it1)
                    }

                    val fileInputStream = resources.openRawResource(it)
                    val buffer = byteArrayOf((1024 * 8).toByte())
                    var read: Int

                    while (true) {

                        read = fileInputStream.read(buffer)

                        if(read != -1)
                            outputStream?.write(buffer, 0, read)
                        else break
                    }

                    fileInputStream.close()
                    outputStream?.flush()
                    outputStream?.close()
                }

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context?.getString(R.string
                        .successful_export_of_notification_sounds), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context?.getString(R.string
                        .error_exporting_notification_sounds,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}