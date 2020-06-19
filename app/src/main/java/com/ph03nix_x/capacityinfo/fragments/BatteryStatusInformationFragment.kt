package com.ph03nix_x.capacityinfo.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL

class BatteryStatusInformationFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var notifyOverheatOvercool: SwitchPreferenceCompat? = null
    private var notifyBatteryIsFullyCharged: SwitchPreferenceCompat? = null
    private var notifyBatteryIsCharged: SwitchPreferenceCompat? = null
    private var batteryLevelNotifyCharged: SeekBarPreference? = null
    private var notifyBatteryIsDischarged: SwitchPreferenceCompat? = null
    private var batteryLevelNotifyDischarged: SeekBarPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.battery_status_information_settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        notifyOverheatOvercool = findPreference(IS_NOTIFY_OVERHEAT_OVERCOOL)
        notifyBatteryIsFullyCharged = findPreference(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED)
        notifyBatteryIsCharged = findPreference(IS_NOTIFY_BATTERY_IS_CHARGED)
        batteryLevelNotifyCharged = findPreference(BATTERY_LEVEL_NOTIFY_CHARGED)
        notifyBatteryIsDischarged = findPreference(IS_NOTIFY_BATTERY_IS_DISCHARGED)
        batteryLevelNotifyDischarged = findPreference(BATTERY_LEVEL_NOTIFY_DISCHARGED)

        batteryLevelNotifyCharged?.apply {

            summary = getBatteryLevelNotifyChargingSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))
        }

        batteryLevelNotifyDischarged?.apply {

            summary = getBatteryLevelNotifyDischargeSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_discharged))
        }

        notifyOverheatOvercool?.setOnPreferenceChangeListener { _, _ ->

            NotificationInterface.isNotifyOverheatOvercool = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)

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

            NotificationInterface.isNotifyBatteryCharged = true

            NotificationInterface.notificationManager?.cancel(
                NotificationInterface.NOTIFICATION_BATTERY_STATUS_ID)

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

        notifyBatteryIsDischarged?.setOnPreferenceChangeListener { _, newValue ->

            batteryLevelNotifyDischarged?.isEnabled = newValue as? Boolean == true

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
    }

    override fun onResume() {

        super.onResume()

        notifyOverheatOvercool?.apply {

            isChecked = pref.getBoolean(IS_NOTIFY_OVERHEAT_OVERCOOL, resources.getBoolean(
                R.bool.is_notify_overheat_overcool))
        }

        notifyBatteryIsFullyCharged?.apply {

            isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_fully_charged))
        }

       notifyBatteryIsCharged?.apply {

            isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))
        }

        notifyBatteryIsDischarged?.apply {

            isChecked = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_discharged))
        }

        batteryLevelNotifyCharged?.apply {

            summary = getBatteryLevelNotifyChargingSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_CHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_charged))
        }

        batteryLevelNotifyDischarged?.apply {

            summary = getBatteryLevelNotifyDischargeSummary()
            isEnabled = pref.getBoolean(IS_NOTIFY_BATTERY_IS_DISCHARGED, resources.getBoolean(
                R.bool.is_notify_battery_is_discharged))
        }
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
}