package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.databinding.ChargeDischargeFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.TimeHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import kotlinx.coroutines.*
import java.text.DecimalFormat

class ChargeDischargeFragment : Fragment(R.layout.charge_discharge_fragment),
    BatteryInfoInterface {

    private lateinit var binding: ChargeDischargeFragmentBinding

    private lateinit var pref: SharedPreferences

    private var mainContext: MainActivity? = null
    private var job: Job? = null
    private var isJob = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = ChargeDischargeFragmentBinding.inflate(inflater, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        return binding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        super.onViewCreated(view, savedInstanceState)

        mainContext = context as? MainActivity

        binding.premiumButton.isVisible = !PremiumInterface.isPremium

        if(binding.premiumButton.isVisible)
            binding.premiumButton.setOnClickListener {
                MainActivity.instance?.showPremiumDialog()
            }

        updateTextAppearance()
    }

    override fun onResume() {

        super.onResume()

        binding.premiumButton.isVisible = !PremiumInterface.isPremium

        batteryIntent = requireContext().registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        isJob = true

        chargeDischargeInformationJob()
    }

    override fun onStop() {

        super.onStop()

        isJob = false
        job?.cancel()
        job = null
    }

    override fun onDestroy() {

        isJob = false
        job?.cancel()
        job = null

        super.onDestroy()
    }

    private fun chargeDischargeInformationJob() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    withContext(Dispatchers.Main) {

                        updateTextAppearance()
                    }

                    val status = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    withContext(Dispatchers.Main) {

                        mainContext?.toolbar?.title = getString(
                            if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                            else R.string.discharge)

                        val chargeDischargeNavigation = mainContext
                            ?.navigation?.menu?.findItem(R.id.charge_discharge_navigation)

                        chargeDischargeNavigation?.title = getString(if(status ==
                            BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                            R.string.discharge)

                        chargeDischargeNavigation?.icon = mainContext
                            ?.getChargeDischargeNavigationIcon(
                                status == BatteryManager.BATTERY_STATUS_CHARGING)?.let {
                                ContextCompat.getDrawable(requireContext(), it)
                        }

                        binding.batteryLevel.text = getString(R.string.battery_level,
                            "${getOnBatteryLevel(requireContext())}%")
                        if((CapacityInfoService.instance?.seconds ?: 0) > 1) {

                            binding.chargingTime.visibility = View.VISIBLE

                            binding.chargingTime.text = getOnChargingTime(requireContext(),
                                CapacityInfoService.instance?.seconds ?: 0)
                        }
                        else if(binding.chargingTime.visibility == View.VISIBLE)
                            binding.chargingTime.visibility = View.GONE

                        binding.lastChargeTime.text = getString(R.string.last_charge_time,
                            getOnLastChargeTime(requireContext()),
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_WITH, 0)}%",
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_TO, 0)}%")

                        if(sourceOfPower == BatteryManager.BATTERY_PLUGGED_AC
                            && status == BatteryManager.BATTERY_STATUS_CHARGING) {

                            if(binding.chargingTimeRemaining.visibility == View.GONE)
                                binding.chargingTimeRemaining.visibility = View.VISIBLE

                            if(binding.remainingBatteryTime.visibility == View.VISIBLE)
                                binding.remainingBatteryTime.visibility = View.GONE

                            binding.chargingTimeRemaining.text = getString(R.string.charging_time_remaining,
                                getOnChargingTimeRemaining(requireContext()))
                        }

                        else {

                            if(binding.chargingTimeRemaining.visibility == View.VISIBLE)
                                binding.chargingTimeRemaining.visibility = View.GONE

                            if(getOnCurrentCapacity(requireContext()) > 0.0) {

                                if(binding.remainingBatteryTime.visibility == View.GONE)
                                    binding.remainingBatteryTime.visibility = View.VISIBLE

                                binding.remainingBatteryTime.text = getString(
                                    R.string.remaining_battery_time, getOnRemainingBatteryTime(
                                        requireContext()))
                            }
                        }

                        binding.premiumButton.isVisible = !PremiumInterface.isPremium
                    }

                    withContext(Dispatchers.Main) {

                        this@ChargeDischargeFragment.binding.status.text = getString(R.string.status,
                            getOnStatus(requireContext(), status))

                        if(getOnSourceOfPower(requireContext(), sourceOfPower) != "N/A") {

                            if(this@ChargeDischargeFragment.binding.sourceOfPower.visibility == View.GONE)
                                this@ChargeDischargeFragment.binding.sourceOfPower.visibility = View.VISIBLE

                            this@ChargeDischargeFragment.binding.sourceOfPower.text =
                                getOnSourceOfPower(requireContext(), sourceOfPower)
                        }

                        else this@ChargeDischargeFragment.binding.sourceOfPower.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {

                        binding.temperature.text = getString(R.string.temperature, DecimalFormat()
                            .format(getOnTemperatureInCelsius(requireContext())), DecimalFormat()
                            .format(getOnTemperatureInFahrenheit(requireContext())))

                        binding.maximumTemperature.text =  getString(R.string.maximum_temperature,
                            DecimalFormat().format(BatteryInfoInterface.maximumTemperature),
                            DecimalFormat().format(getOnTemperatureInFahrenheit(
                                BatteryInfoInterface.maximumTemperature)))

                        binding.averageTemperature.text =  getString(R.string.average_temperature,
                            DecimalFormat().format(BatteryInfoInterface.averageTemperature),
                            DecimalFormat().format(getOnTemperatureInFahrenheit(
                                BatteryInfoInterface.averageTemperature)))

                        binding.minimumTemperature.text =  getString(R.string.minimum_temperature,
                            DecimalFormat().format(BatteryInfoInterface.minimumTemperature),
                            DecimalFormat().format(getOnTemperatureInFahrenheit(
                                BatteryInfoInterface.minimumTemperature)))

                        binding.voltage.text = getString(R.string.voltage, DecimalFormat("#.#")
                            .format(getOnVoltage(requireContext())))
                    }

                    if(getOnCurrentCapacity(requireContext()) > 0.0) {

                        if(binding.currentCapacityChargeDischarge.visibility == View.GONE)
                            withContext(Dispatchers.Main) {
                                binding.currentCapacityChargeDischarge.visibility = View.VISIBLE }

                        withContext(Dispatchers.Main) {

                            binding.currentCapacityChargeDischarge.text =
                                getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(getOnCurrentCapacity(
                                        requireContext())))

                            when {
                                getOnSourceOfPower(requireContext(), sourceOfPower) != "N/A" -> {

                                    if(binding.capacityAddedChargeDischarge.visibility == View.GONE)
                                        binding.capacityAddedChargeDischarge.visibility =
                                            View.VISIBLE

                                    binding.capacityAddedChargeDischarge.text =
                                        getOnCapacityAdded(requireContext())
                                }
                                getOnSourceOfPower(requireContext(), sourceOfPower) == "N/A" -> {

                                    if(binding.capacityAddedChargeDischarge.visibility == View.GONE)
                                        binding.capacityAddedChargeDischarge.visibility =
                                            View.VISIBLE

                                    binding.capacityAddedChargeDischarge.text =
                                        getOnCapacityAdded(requireContext())
                                }
                            }
                        }
                    }

                    else {

                        if(binding.currentCapacityChargeDischarge.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) {
                                binding.currentCapacityChargeDischarge.visibility = View.GONE }

                        if(binding.capacityAddedChargeDischarge.visibility == View.GONE
                            && pref.getFloat(PreferencesKeys.CAPACITY_ADDED, 0f) > 0f)
                            withContext(Dispatchers.Main) {
                                binding.capacityAddedChargeDischarge.visibility = View.VISIBLE }

                        else withContext(Dispatchers.Main) {
                            binding.capacityAddedChargeDischarge.visibility = View.GONE }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING -> {

                            if(binding.chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    binding.chargeCurrent.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                binding.chargeCurrent.text = getString(R.string.charge_current,
                                    getOnChargeDischargeCurrent(requireContext()).toString())
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager
                            .BATTERY_STATUS_FULL, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                            if(binding.chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    binding.chargeCurrent.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                binding.chargeCurrent.text = getString(R.string.discharge_current,
                                    getOnChargeDischargeCurrent(requireContext()).toString())
                            }
                        }

                        else -> {

                            if(binding.chargeCurrent.visibility == View.VISIBLE)
                                withContext(Dispatchers.Main) {
                                    binding.chargeCurrent.visibility = View.GONE }
                        }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL
                        -> {

                            if(binding.fastCharge.visibility == View.GONE) {
                                withContext(Dispatchers.Main) {
                                    binding.fastCharge.visibility = View.VISIBLE }
                            }

                            withContext(Dispatchers.Main) {
                                binding.fastCharge.text = getOnFastCharge(requireContext())
                            }

                            withContext(Dispatchers.Main) {

                                if(binding.maxChargeDischargeCurrent.visibility == View.GONE)
                                    binding.maxChargeDischargeCurrent.visibility = View.VISIBLE

                                if(binding.averageChargeDischargeCurrent.visibility == View.GONE)
                                    binding.averageChargeDischargeCurrent.visibility = View.VISIBLE

                                if(binding.minChargeDischargeCurrent.visibility == View.GONE)
                                    binding.minChargeDischargeCurrent.visibility = View.VISIBLE

                                binding.maxChargeDischargeCurrent.text = getString(R.string
                                    .max_charge_current, BatteryInfoInterface.maxChargeCurrent)

                                binding.averageChargeDischargeCurrent.text = getString(R.string
                                    .average_charge_current,
                                    BatteryInfoInterface.averageChargeCurrent)

                                binding.minChargeDischargeCurrent.text = getString(R.string
                                    .min_charge_current, BatteryInfoInterface.minChargeCurrent)
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager
                            .BATTERY_STATUS_NOT_CHARGING -> withContext(Dispatchers.Main) {

                            if(binding.fastCharge.visibility == View.VISIBLE)
                                binding.fastCharge.visibility = View.GONE

                            if(binding.maxChargeDischargeCurrent.visibility == View.GONE)
                                binding.maxChargeDischargeCurrent.visibility = View.VISIBLE

                            if(binding.averageChargeDischargeCurrent.visibility == View.GONE)
                                binding.averageChargeDischargeCurrent.visibility = View.VISIBLE

                            if(binding.minChargeDischargeCurrent.visibility == View.GONE)
                                binding.minChargeDischargeCurrent.visibility = View.VISIBLE

                            binding.maxChargeDischargeCurrent.text = getString(
                                R.string.max_discharge_current,
                                BatteryInfoInterface.maxDischargeCurrent)

                            binding.averageChargeDischargeCurrent.text = getString(
                                R.string.average_discharge_current,
                                BatteryInfoInterface.averageDischargeCurrent)

                            binding.minChargeDischargeCurrent.text = getString(
                                R.string.min_discharge_current,
                                BatteryInfoInterface.minDischargeCurrent)
                        }

                        else -> {

                            withContext(Dispatchers.Main) {

                                if(binding.maxChargeDischargeCurrent.visibility == View.VISIBLE)
                                    binding.maxChargeDischargeCurrent.visibility = View.GONE

                                if(binding.averageChargeDischargeCurrent.visibility == View.VISIBLE)
                                    binding.averageChargeDischargeCurrent.visibility = View.GONE

                                if(binding.minChargeDischargeCurrent.visibility == View.VISIBLE)
                                    binding.minChargeDischargeCurrent.visibility = View.GONE
                            }
                        }
                    }

                    val chargingCurrentLimit = getOnChargingCurrentLimit(requireContext())

                    withContext(Dispatchers.Main) {

                        if(chargingCurrentLimit != null && chargingCurrentLimit.toInt() > 0) {

                            if(this@ChargeDischargeFragment.binding.chargingCurrentLimit
                                    .visibility == View.GONE) this@ChargeDischargeFragment
                                .binding.chargingCurrentLimit.visibility = View.VISIBLE

                            this@ChargeDischargeFragment
                                .binding.chargingCurrentLimit.text = requireContext().getString(R.string
                                .charging_current_limit, chargingCurrentLimit)
                        }

                        else if(this@ChargeDischargeFragment.binding.chargingCurrentLimit
                                .visibility == View.VISIBLE) this@ChargeDischargeFragment
                            .binding.chargingCurrentLimit.visibility = View.GONE

                        binding.screenTime.text = getString(R.string.screen_time, TimeHelper
                            .getTime(CapacityInfoService.instance
                                ?.screenTime ?: 0L))
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if(getOnCurrentCapacity(requireContext()) > 0.0) 974L else 981L)

                        else -> delay(1500L)
                    }
                }
            }
    }

    private fun updateTextAppearance() {

        TextAppearanceHelper.setTextAppearance(requireContext(), binding.batteryLevel,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.chargingTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.chargingTimeRemaining,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.remainingBatteryTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.screenTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(),
            binding.currentCapacityChargeDischarge, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(),
            binding.capacityAddedChargeDischarge, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.status,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.sourceOfPower,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.chargeCurrent,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.maxChargeDischargeCurrent,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(),
            binding.averageChargeDischargeCurrent, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.minChargeDischargeCurrent,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.chargingCurrentLimit,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.temperature,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.maximumTemperature,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.averageTemperature,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.minimumTemperature,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.voltage,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.lastChargeTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))
    }
}