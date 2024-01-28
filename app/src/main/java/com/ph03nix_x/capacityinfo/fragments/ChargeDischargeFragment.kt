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
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
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
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UPDATE_TEMP_SCREEN_TIME
import kotlinx.coroutines.*
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.seconds

class ChargeDischargeFragment : Fragment(R.layout.charge_discharge_fragment),
    BatteryInfoInterface, NavigationInterface, PremiumInterface {

    private lateinit var binding: ChargeDischargeFragmentBinding

    private lateinit var pref: SharedPreferences

    private var mainContext: MainActivity? = null
    private var job: Job? = null
    private var isJob = false
    private var isChargingDischargeCurrentInWatt = false

    private var chargingTime = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = ChargeDischargeFragmentBinding.inflate(inflater, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        return binding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        
        super.onViewCreated(view, savedInstanceState)

        mainContext = context as? MainActivity

        updateTextAppearance()
    }

    override fun onResume() {

        super.onResume()

        batteryIntent = requireContext().registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        isJob = true

        isChargingDischargeCurrentInWatt = pref.getBoolean(IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
            resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))

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
                            "${getBatteryLevel(requireContext())}%")
                        if(chargingTime == 0)
                            chargingTime = CapacityInfoService.instance?.seconds ?: -1
                        if(chargingTime > 0) {
                            binding.chargingTime.apply {
                                visibility = View.VISIBLE
                                text = getChargingTime(requireContext(), chargingTime)
                            }
                        }
                        else if(binding.chargingTime.visibility == View.VISIBLE)
                            binding.chargingTime.visibility = View.GONE

                        binding.lastChargeTime.text = getString(R.string.last_charge_time,
                            getLastChargeTime(requireContext()),
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_WITH, 0)}%",
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_TO, 0)}%")

                        if(sourceOfPower == BatteryManager.BATTERY_PLUGGED_AC
                            && status == BatteryManager.BATTERY_STATUS_CHARGING) {
                            binding.apply {
                                if(chargingTimeRemaining.visibility == View.GONE)
                                    chargingTimeRemaining.visibility = View.VISIBLE
                                if(remainingBatteryTime.visibility == View.VISIBLE)
                                    remainingBatteryTime.visibility = View.GONE
                                chargingTimeRemaining.text =
                                    getString(R.string.charging_time_remaining,
                                        getChargingTimeRemaining(requireContext()))
                            }
                        }
                        else {
                            if(binding.chargingTimeRemaining.visibility == View.VISIBLE)
                                binding.chargingTimeRemaining.visibility = View.GONE

                            if(getCurrentCapacity(requireContext()) > 0.0) {
                                binding.remainingBatteryTime.apply {
                                    if(visibility == View.GONE) visibility = View.VISIBLE

                                    text = getString(R.string.remaining_battery_time,
                                        getRemainingBatteryTime(requireContext()))
                                }
                                return@withContext
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        
                        binding.apply {
                            this.status.text = getString(R.string.status,
                                getStatus(requireContext(), status))
                            
                            if(getSourceOfPower(requireContext(), sourceOfPower) != "N/A") {

                                if(this.sourceOfPower.visibility == View.GONE)
                                    this.sourceOfPower.visibility = View.VISIBLE

                                this.sourceOfPower.text =
                                    getSourceOfPower(requireContext(), sourceOfPower)
                            }
                            else this.sourceOfPower.visibility = View.GONE
                        }
                    }

                    withContext(Dispatchers.Main) {

                        binding.apply {
                            temperature.text = getString(R.string.temperature,
                                DecimalFormat("#.#").format(getTemperatureInCelsius(
                                    requireContext())), DecimalFormat("#.#")
                                    .format(getTemperatureInFahrenheit(requireContext())))

                            maximumTemperature.text =  getString(R.string.maximum_temperature,
                                DecimalFormat("#.#").format(
                                    BatteryInfoInterface.maximumTemperature),
                                DecimalFormat("#.#").format(getTemperatureInFahrenheit(
                                    BatteryInfoInterface.maximumTemperature)))

                            averageTemperature.text =  getString(R.string.average_temperature,
                                DecimalFormat("#.#").format(BatteryInfoInterface
                                    .averageTemperature), DecimalFormat("#.#").format(
                                    getTemperatureInFahrenheit(BatteryInfoInterface
                                        .averageTemperature)))

                            minimumTemperature.text =  getString(R.string.minimum_temperature,
                                DecimalFormat("#.#").format(BatteryInfoInterface
                                    .minimumTemperature), DecimalFormat("#.#").format(
                                    getTemperatureInFahrenheit(BatteryInfoInterface
                                        .minimumTemperature)))

                            voltage.text = getString(R.string.voltage, DecimalFormat("#.#")
                                .format(getVoltage(requireContext())))
                        }
                    }

                    if(getCurrentCapacity(requireContext()) > 0.0) {

                        if(binding.currentCapacityChargeDischarge.visibility == View.GONE)
                            withContext(Dispatchers.Main) {
                                binding.currentCapacityChargeDischarge.visibility = View.VISIBLE }

                        withContext(Dispatchers.Main) {

                            val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
                                resources.getBoolean(R.bool.is_capacity_in_wh))

                            binding.currentCapacityChargeDischarge.text =
                                getString(if(isCapacityInWh) R.string.current_capacity_wh
                                else R.string.current_capacity, DecimalFormat("#.#")
                                    .format(if(isCapacityInWh) getCapacityInWh(
                                        getCurrentCapacity(requireContext()))
                                    else getCurrentCapacity(requireContext())))

                            when {
                                getSourceOfPower(requireContext(), sourceOfPower) != "N/A" -> {

                                    if(binding.capacityAddedChargeDischarge.visibility == View.GONE)
                                        binding.capacityAddedChargeDischarge.visibility =
                                            View.VISIBLE

                                    binding.capacityAddedChargeDischarge.text =
                                        getCapacityAdded(requireContext())
                                }
                                getSourceOfPower(requireContext(), sourceOfPower) == "N/A" -> {
                                    if(chargingTime > 0) chargingTime = 0
                                    if(binding.capacityAddedChargeDischarge.visibility == View.GONE)
                                        binding.capacityAddedChargeDischarge.visibility =
                                            View.VISIBLE

                                    binding.capacityAddedChargeDischarge.text =
                                        getCapacityAdded(requireContext())
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
                            chargingTime++
                            if(binding.chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    binding.chargeCurrent.visibility = View.VISIBLE }
                            withContext(Dispatchers.Main) {
                                binding.chargeCurrent.text = if(isChargingDischargeCurrentInWatt)
                                    getString(R.string.charge_current_watt,
                                        DecimalFormat("#.##").format(
                                            getChargeDischargeCurrentInWatt(
                                                getChargeDischargeCurrent(requireContext()),
                                                true)))
                                else getString(R.string.charge_current,
                                    "${getChargeDischargeCurrent(requireContext())}")
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager
                            .BATTERY_STATUS_FULL, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                            if(binding.chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    binding.chargeCurrent.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                binding.chargeCurrent.text = if(isChargingDischargeCurrentInWatt)
                                    getString(R.string.discharge_current_watt,
                                        DecimalFormat("#.##").format(
                                            getChargeDischargeCurrentInWatt(
                                                getChargeDischargeCurrent(requireContext()))))
                                else getString(R.string.discharge_current,
                                    "${getChargeDischargeCurrent(requireContext())}")
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
                                binding.fastCharge.text = getFastCharge(requireContext())
                            }

                            withContext(Dispatchers.Main) {
                                
                                binding.apply {
                                    if(maxChargeDischargeCurrent.visibility == View.GONE)
                                        maxChargeDischargeCurrent.visibility = View.VISIBLE

                                    if(averageChargeDischargeCurrent.visibility == View.GONE)
                                        averageChargeDischargeCurrent.visibility = View.VISIBLE

                                    if(minChargeDischargeCurrent.visibility == View.GONE)
                                        minChargeDischargeCurrent.visibility = View.VISIBLE

                                    maxChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.max_charge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.maxChargeCurrent,
                                                        true)))
                                        else getString(R.string.max_charge_current,
                                            BatteryInfoInterface.maxChargeCurrent)

                                    averageChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.average_charge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.averageChargeCurrent,
                                                        true)))
                                        else getString(R.string.average_charge_current,
                                            BatteryInfoInterface.averageChargeCurrent)

                                    minChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.min_charge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.minChargeCurrent,
                                                        true)))
                                        else getString(R.string.min_charge_current,
                                            BatteryInfoInterface.minChargeCurrent)   
                                }
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager
                            .BATTERY_STATUS_NOT_CHARGING -> withContext(Dispatchers.Main) {
                                
                                binding.apply {
                                    if(fastCharge.visibility == View.VISIBLE)
                                        fastCharge.visibility = View.GONE

                                    if(maxChargeDischargeCurrent.visibility == View.GONE)
                                        maxChargeDischargeCurrent.visibility = View.VISIBLE

                                    if(averageChargeDischargeCurrent.visibility == View.GONE)
                                        averageChargeDischargeCurrent.visibility = View.VISIBLE

                                    if(minChargeDischargeCurrent.visibility == View.GONE)
                                        minChargeDischargeCurrent.visibility = View.VISIBLE

                                    maxChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.max_discharge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.maxDischargeCurrent)))
                                        else getString(R.string.max_discharge_current,
                                            BatteryInfoInterface.maxDischargeCurrent)

                                    averageChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.average_discharge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.averageDischargeCurrent)))
                                        else getString(R.string.average_discharge_current,
                                            BatteryInfoInterface.averageDischargeCurrent)

                                    minChargeDischargeCurrent.text =
                                        if(isChargingDischargeCurrentInWatt)
                                            getString(R.string.min_discharge_current_watt,
                                                DecimalFormat("#.##").format(
                                                    getChargeDischargeCurrentInWatt(
                                                        BatteryInfoInterface.minDischargeCurrent)))
                                        else getString(R.string.min_discharge_current,
                                            BatteryInfoInterface.minDischargeCurrent)   
                                }
                        }

                        else -> {

                            withContext(Dispatchers.Main) {
                                
                                binding.apply {
                                    if(maxChargeDischargeCurrent.visibility == View.VISIBLE)
                                        maxChargeDischargeCurrent.visibility = View.GONE

                                    if(averageChargeDischargeCurrent.visibility == View.VISIBLE)
                                        averageChargeDischargeCurrent.visibility = View.GONE

                                    if(minChargeDischargeCurrent.visibility == View.VISIBLE)
                                        minChargeDischargeCurrent.visibility = View.GONE   
                                }
                            }
                        }
                    }

                    val chargingCurrentLimit = getChargingCurrentLimit(requireContext())

                    withContext(Dispatchers.Main) {

                        if(chargingCurrentLimit != null && chargingCurrentLimit.toInt() > 0) {

                            if(binding.chargingCurrentLimit
                                    .visibility == View.GONE) this@ChargeDischargeFragment
                                .binding.chargingCurrentLimit.visibility = View.VISIBLE

                            if(isChargingDischargeCurrentInWatt)
                                binding.chargingCurrentLimit.text = getString(
                                    R.string.charging_current_limit_watt,
                                    DecimalFormat("#.##").format(
                                        getChargeDischargeCurrentInWatt(
                                            chargingCurrentLimit.toInt(), true)))
                            else binding.chargingCurrentLimit.text = getString(
                                R.string.charging_current_limit, chargingCurrentLimit)
                        }

                        else if(binding.chargingCurrentLimit
                                .visibility == View.VISIBLE) this@ChargeDischargeFragment
                            .binding.chargingCurrentLimit.visibility = View.GONE

                        binding.screenTime.text = getString(R.string.screen_time, TimeHelper
                            .getTime(CapacityInfoService.instance
                                ?.screenTime ?: if(MainApp.tempScreenTime > 0) MainApp.tempScreenTime
                            else if(MainApp.isUpdateApp)
                                pref.getLong(UPDATE_TEMP_SCREEN_TIME, 0L) else 0L))
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if(getCurrentCapacity(requireContext()) > 0.0) 0.972.seconds
                            else 0.979.seconds)

                        else -> delay(1.5.seconds)
                    }
                }
            }
    }

    private fun updateTextAppearance() {
        with(binding) {
            val textViewArrayList = arrayListOf(batteryLevel, chargingTime, chargingTimeRemaining,
                remainingBatteryTime, screenTime, currentCapacityChargeDischarge,
                capacityAddedChargeDischarge, status, sourceOfPower, chargeCurrent, fastCharge,
                maxChargeDischargeCurrent, averageChargeDischargeCurrent, minChargeDischargeCurrent,
                chargingCurrentLimit, temperature, maximumTemperature, averageTemperature,
                minimumTemperature, voltage, lastChargeTime)

            TextAppearanceHelper.setTextAppearance(requireContext(), textViewArrayList,
                pref.getString(TEXT_STYLE, "0"),
                pref.getString(TEXT_FONT, "6"), pref.getString(TEXT_SIZE, "2"))   
        }
    }
}