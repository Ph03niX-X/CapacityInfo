package com.ph03nix_x.capacityinfo.fragments

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.databinding.LastChargeFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.helpers.TimeHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_TIME_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FAST_CHARGE_WATTS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.SOURCE_OF_POWER_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.STATUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_LAST_CHARGE
import java.text.DecimalFormat

/**
 * Created by Ph03niX-X on 06.02.2024
 * Ph03niX-X@outlook.com
 */
class LastChargeFragment : Fragment(R.layout.last_charge_fragment), BatteryInfoInterface {
    private lateinit var binding: LastChargeFragmentBinding
    private lateinit var pref: SharedPreferences

    private var isResume = false

    companion object {
        var instance: LastChargeFragment? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if(!isInstalledFromGooglePlay())
            throw RuntimeException("Application not installed from Google Play")
        binding = LastChargeFragmentBinding.inflate(inflater, container, false)
        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        instance = this
        return binding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTextAppearance()
        lastCharge()
    }

    override fun onStop() {
        isResume = true
        super.onStop()
    }

    override fun onResume() {
        if(isResume) {
            lastCharge()
            isResume = false
        }
        super.onResume()
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    private fun updateTextAppearance() {
        with(binding) {
            val textViewArrayList = arrayListOf(batteryLevelLastCharge, chargingTimeLastCharge,
                currentCapacityLastCharge, capacityAddedLastCharge, statusLastCharge,
                sourceOfPowerLastCharge, fastChargeLastCharge, maxChargeCurrentLastCharge,
                averageChargeCurrentLastCharge, minChargeCurrentLastCharge,
                maximumTemperatureLastCharge, averageTemperatureLastCharge,
                minimumTemperatureLastCharge, voltageLastCharge, lastChargeTime)
            TextAppearanceHelper.setTextAppearance(requireContext(), textViewArrayList,
                pref.getString(PreferencesKeys.TEXT_STYLE, "0"),
                pref.getString(PreferencesKeys.TEXT_FONT, "6"),
                pref.getString(PreferencesKeys.TEXT_SIZE, "2"))
        }
    }

    fun lastCharge() {
        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            resources.getBoolean(R.bool.is_capacity_in_wh))
        with(pref) {
            binding.apply {
                batteryLevelLastCharge.text = getString(R.string.battery_level,
                    "${pref.getInt(BATTERY_LEVEL_LAST_CHARGE, 0)}%")
                chargingTimeLastCharge.text = getString(R.string.charging_time,
                    TimeHelper.getTime(pref.getInt(CHARGING_TIME_LAST_CHARGE, 0).toLong()))
                currentCapacityLastCharge.text = getString(if(isCapacityInWh)
                    R.string.current_capacity_wh else R.string.current_capacity,
                    DecimalFormat("#.#").format(if(isCapacityInWh) getCapacityInWh(
                        getCurrentCapacityLastCharge(requireContext())) else
                            getCurrentCapacityLastCharge(requireContext())))
                capacityAddedLastCharge.text = getCapacityAddedLastCharge()
                statusLastCharge.text = getString(R.string.status,
                    getString(STATUS_LAST_CHARGE, getString(R.string.unknown)))
                sourceOfPowerLastCharge.text = getString(R.string.source_of_power,
                    getString(SOURCE_OF_POWER_LAST_CHARGE, "N/A"))
                fastChargeLastCharge.text =
                    if(getBoolean(IS_FAST_CHARGE_LAST_CHARGE, false))
                        getString( R.string.fast_charge_yes, DecimalFormat("#.#").format(
                            getFloat(FAST_CHARGE_WATTS_LAST_CHARGE, 0f))) else
                                getString(R.string.fast_charge_no)
                maxChargeCurrentLastCharge.text = getMaxChargeCurrentLastCharge()
                averageChargeCurrentLastCharge.text = getAverageChargeCurrentLastCharge()
                minChargeCurrentLastCharge.text = getMinChargeCurrentLastCharge()
                maximumTemperatureLastCharge.text = getMaxTemperatureLastCharge()
                averageTemperatureLastCharge.text = getAverageTemperatureLastCharge()
                minimumTemperatureLastCharge.text = getMinTemperatureLastCharge()
                voltageLastCharge.text = getString(R.string.voltage,
                    "${getFloat(VOLTAGE_LAST_CHARGE, 0f)}")
                lastChargeTime.text = getString(R.string.last_charge_time,
                    getLastChargeTime(requireContext()),
                    "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_WITH, 0)}%",
                    "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_TO, 0)}%")
            }
        }
    }

    private fun getCapacityAddedLastCharge(): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            requireContext().resources.getBoolean(R.bool.is_capacity_in_wh))
        val capacityAddedPref = pref.getFloat(PreferencesKeys.CAPACITY_ADDED_LAST_CHARGE,
            0f).toDouble()
        val percentAddedPref = pref.getInt(PreferencesKeys.PERCENT_ADDED_LAST_CHARGE, 0)
        return if(isCapacityInWh)
            requireContext().getString(R.string.capacity_added_wh, DecimalFormat("#.#")
                .format(getCapacityInWh(capacityAddedPref)), "$percentAddedPref%")
        else requireContext().getString(R.string.capacity_added, DecimalFormat("#.#")
            .format(capacityAddedPref), "$percentAddedPref%")
        }

    private fun getMaxChargeCurrentLastCharge(): String {
        val isChargingDischargeCurrentInWatt = pref.getBoolean(
            PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
            resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
        return if(isChargingDischargeCurrentInWatt)
            getString(R.string.max_charge_current_watt,
                DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                    pref.getInt(MAX_CHARGE_LAST_CHARGE, 0))))
        else getString(R.string.max_charge_current, pref.getInt(MAX_CHARGE_LAST_CHARGE, 0))
    }

    private fun getAverageChargeCurrentLastCharge(): String {
        val isChargingDischargeCurrentInWatt = pref.getBoolean(
            PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
            resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
        return if(isChargingDischargeCurrentInWatt)
            getString(R.string.average_charge_current_watt,
                DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                    pref.getInt(AVERAGE_CHARGE_LAST_CHARGE, 0))))
        else getString(R.string.average_charge_current,
            pref.getInt(AVERAGE_CHARGE_LAST_CHARGE, 0))
    }

    private fun getMinChargeCurrentLastCharge(): String {
        val isChargingDischargeCurrentInWatt = pref.getBoolean(
            PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
            resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
        return if(isChargingDischargeCurrentInWatt)
            getString(R.string.min_charge_current_watt,
                DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                    pref.getInt(MIN_CHARGE_LAST_CHARGE, 0))))
        else getString(R.string.min_charge_current, pref.getInt(MIN_CHARGE_LAST_CHARGE, 0))
    }

    private fun getMaxTemperatureLastCharge() =
        getString(R.string.maximum_temperature,
            DecimalFormat("#.#").format(pref.getFloat(
                MAX_TEMP_CELSIUS_LAST_CHARGE, 0f)),
            DecimalFormat("#.#").format(pref.getFloat(
                MAX_TEMP_FAHRENHEIT_LAST_CHARGE, 0f)))

    private fun getAverageTemperatureLastCharge() =
        getString(R.string.average_temperature,
            DecimalFormat("#.#").format(pref.getFloat(
                AVERAGE_TEMP_CELSIUS_LAST_CHARGE, 0f)),
            DecimalFormat("#.#").format(pref.getFloat(
                AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE, 0f)))

    private fun getMinTemperatureLastCharge() =
        getString(R.string.minimum_temperature,
            DecimalFormat("#.#").format(pref.getFloat(
                MIN_TEMP_CELSIUS_LAST_CHARGE, 0f)),
            DecimalFormat("#.#").format(pref.getFloat(
                MIN_TEMP_FAHRENHEIT_LAST_CHARGE, 0f)))

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay() =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
                .getInstallSourceInfo(requireContext().packageName).installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
            .getInstallerPackageName(requireContext().packageName)

}