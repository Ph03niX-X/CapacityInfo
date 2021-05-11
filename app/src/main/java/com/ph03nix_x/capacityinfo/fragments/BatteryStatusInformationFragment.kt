package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DISCHARGE_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_CHARGING_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class BatteryStatusInformationFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var notifyOverheatOvercool: SwitchPreferenceCompat? = null
    private var overheatDegrees: SeekBarPreference? = null
    private var overcoolDegrees: SeekBarPreference? = null
    private var notifyBatteryIsFullyCharged: SwitchPreferenceCompat? = null
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

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.battery_status_information_settings)

        notifyOverheatOvercool = findPreference(IS_NOTIFY_OVERHEAT_OVERCOOL)
        overheatDegrees = findPreference(OVERHEAT_DEGREES)
        overcoolDegrees = findPreference(OVERCOOL_DEGREES)
        notifyBatteryIsFullyCharged = findPreference(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED)
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

        batteryLevelNotifyCharged?.apply {

            summary = getBatteryLevelNotifyChargingSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))
        }

        batteryNotifyChargedVoltage?.apply {
            summary = "${pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                .battery_notify_charged_voltage_min))}"
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

        notifyOverheatOvercool?.setOnPreferenceChangeListener { _, newValue ->

            overheatDegrees?.isEnabled = newValue as? Boolean == true

            overcoolDegrees?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyOverheatOvercool = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)

            true
        }

        overheatDegrees?.setOnPreferenceChangeListener { preference, newValue ->

            val temperature = (newValue as? Int) ?: resources.getInteger(R.integer
                .overheat_degrees_default)

            preference.summary = getString(R.string.overheat_overcool_degrees, temperature,
                DecimalFormat("#.#").format((temperature * 1.8) + 32.0))

            true
        }

        overcoolDegrees?.setOnPreferenceChangeListener { preference, newValue ->

            val temperature = (newValue as? Int) ?: resources.getInteger(R.integer
                .overcool_degrees_default)

            preference.summary = getString(R.string.overheat_overcool_degrees, temperature,
                DecimalFormat("#.#").format((temperature * 1.8) + 32.0))

            true
        }

        notifyBatteryIsFullyCharged?.setOnPreferenceChangeListener { _, _ ->

            NotificationInterface.isNotifyBatteryFullyCharged = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyBatteryIsCharged?.setOnPreferenceChangeListener { _, newValue ->

            batteryLevelNotifyCharged?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyBatteryChargedVoltage = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyBatteryIsChargedVoltage?.setOnPreferenceChangeListener { _, newValue ->

            batteryNotifyChargedVoltage?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyBatteryChargedVoltage = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyChargingCurrent?.setOnPreferenceChangeListener { _, newValue ->

            chargingCurrentLevelNotify?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyChargingCurrent = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_CHARGING_CURRENT_ID)

            true
        }

        notifyDischargeCurrent?.setOnPreferenceChangeListener { _, newValue ->

            dischargeCurrentLevelNotify?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyDischargeCurrent = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_DISCHARGE_CURRENT_ID)

            true
        }

        batteryLevelNotifyCharged?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = "${((newValue as? Int) ?: pref.getInt(
                BATTERY_LEVEL_NOTIFY_CHARGED, 80))}%"

            NotificationInterface.isNotifyBatteryCharged = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        batteryNotifyChargedVoltage?.setOnPreferenceClickListener {

            changeBatteryNotifyChargedVoltage()

            true
        }

        batteryNotifyChargedVoltage?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = "${((newValue as? Int) ?: pref.getInt(
                BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min)))}"

            NotificationInterface.isNotifyBatteryChargedVoltage = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        notifyBatteryIsDischarged?.setOnPreferenceChangeListener { _, newValue ->

            batteryNotifyDischargedVoltage?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyBatteryDischarged = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        batteryLevelNotifyDischarged?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = "${((newValue as? Int) ?: pref.getInt(
                BATTERY_LEVEL_NOTIFY_DISCHARGED, 20))}%"

            NotificationInterface.isNotifyBatteryDischarged = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        batteryNotifyDischargedVoltage?.setOnPreferenceClickListener {

            changeBatteryNotifyDischargedVoltage()

            true
        }

        notifyBatteryIsDischargedVoltage?.setOnPreferenceChangeListener { _, newValue ->

            batteryNotifyDischargedVoltage?.isEnabled = newValue as? Boolean == true

            NotificationInterface.isNotifyBatteryDischargedVoltage = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

            true
        }

        batteryNotifyDischargedVoltage?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = "${((newValue as? Int) ?: pref.getInt(
                BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_discharged_voltage_min)))}"

            NotificationInterface.isNotifyBatteryDischargedVoltage = true

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

            NotificationInterface.isNotifyChargingCurrent = true

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

            NotificationInterface.isNotifyDischargeCurrent = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_DISCHARGE_CURRENT_ID)

            true
        }

        exportNotificationSounds?.setOnPreferenceClickListener {

            try {

                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    Constants. EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            Constants.EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) exportNotificationSounds(data)
        }
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

        val view = LayoutInflater.from(context).inflate(R.layout
            .change_battery_is_charged_discharged_voltage_dialog, null)

        dialog.setView(view)

        val batteryNotifyChargedVoltage = view.findViewById<TextInputEditText>(R.id
            .change_battery_is_charged_discharged_mv_edit)

        batteryNotifyChargedVoltage.setText(if(pref.getInt(
                BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min)) >= resources.getInteger(R.integer
                .battery_notify_charged_voltage_min) || pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE,
                resources.getInteger(R.integer.battery_notify_charged_voltage_min)) <= resources
                .getInteger(R.integer.battery_notify_charged_voltage_max))
            pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                .battery_notify_charged_voltage_min)).toString()

        else resources.getInteger(R.integer.battery_notify_charged_voltage_min).toString())

        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->

            pref.edit().putInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, batteryNotifyChargedVoltage.text.toString()
                .toInt()).apply()

            this.batteryNotifyChargedVoltage?.apply {

                summary = getString(R.string.battery_charged_discharged_voltage_seekbar_summary,
                    batteryNotifyChargedVoltage.text.toString().toInt())

                value = pref.getInt(BATTERY_NOTIFY_CHARGED_VOLTAGE, resources.getInteger(R.integer
                    .battery_notify_charged_voltage_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeBatteryNotifyChargedVoltageDialogCreateShowListener(dialogCreate,
            batteryNotifyChargedVoltage)

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

        val view = LayoutInflater.from(context).inflate(R.layout
            .change_battery_is_charged_discharged_voltage_dialog, null)

        dialog.setView(view)

        val batteryNotifyDischargedVoltage = view.findViewById<TextInputEditText>(R.id
            .change_battery_is_charged_discharged_mv_edit)

        batteryNotifyDischargedVoltage.setText(if(pref.getInt(
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
                batteryNotifyDischargedVoltage.text.toString().toInt()).apply()

            this.batteryNotifyDischargedVoltage?.apply {

                summary = getString(R.string.battery_charged_discharged_voltage_seekbar_summary,
                    batteryNotifyDischargedVoltage.text.toString().toInt())

                value = pref.getInt(BATTERY_NOTIFY_DISCHARGED_VOLTAGE, resources.getInteger(
                    R.integer.battery_notify_discharged_voltage_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeBatteryNotifyDischargedVoltageDialogCreateShowListener(dialogCreate,
            batteryNotifyDischargedVoltage)

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

        val view = LayoutInflater.from(context).inflate(R.layout
            .change_charging_current_notify_level_dialog, null)

        dialog.setView(view)

        val changeChargingCurrentLevel = view.findViewById<TextInputEditText>(R.id
            .change_charging_current_level_edit)

        changeChargingCurrentLevel.setText(if(pref.getInt(
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

            pref.edit().putInt(CHARGING_CURRENT_LEVEL_NOTIFY, changeChargingCurrentLevel.text
                .toString().toInt()).apply()

            chargingCurrentLevelNotify?.apply {

                summary = getString(R.string.ma, changeChargingCurrentLevel.text.toString().toInt())

                value = pref.getInt(CHARGING_CURRENT_LEVEL_NOTIFY,
                    resources.getInteger(R.integer.charging_current_notify_level_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeChargingCurrentNotifyLevelDialogCreateShowListener(dialogCreate,
            changeChargingCurrentLevel)

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

        val view = LayoutInflater.from(context).inflate(R.layout
            .change_discharge_current_notify_level_dialog, null)

        dialog.setView(view)

        val changeDischargeCurrentLevel = view.findViewById<TextInputEditText>(R.id
            .change_discharge_current_level_edit)

        changeDischargeCurrentLevel.setText(if(pref.getInt(
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

            pref.edit().putInt(DISCHARGE_CURRENT_LEVEL_NOTIFY, changeDischargeCurrentLevel.text
                .toString().toInt()).apply()

            dischargeCurrentLevelNotify?.apply {

                summary = getString(R.string.ma, changeDischargeCurrentLevel.text.toString().toInt())

                value = pref.getInt(DISCHARGE_CURRENT_LEVEL_NOTIFY,
                    resources.getInteger(R.integer.discharge_current_notify_level_min))
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeDischargeCurrentNotifyLevelDialogCreateShowListener(dialogCreate,
            changeDischargeCurrentLevel)

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