package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utils.Utils
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import kotlinx.coroutines.*
import java.text.DecimalFormat

class ChargeDischargeFragment : Fragment(), BatteryInfoInterface {

    private lateinit var pref: SharedPreferences

    private lateinit var batteryLevel: AppCompatTextView
    private lateinit var chargingTime: AppCompatTextView
    private lateinit var currentCapacity: AppCompatTextView
    private lateinit var capacityAdded: AppCompatTextView
    private lateinit var status: AppCompatTextView
    private lateinit var sourceOfPower: AppCompatTextView
    private lateinit var chargeCurrent: AppCompatTextView
    private lateinit var maxChargeDischargeCurrent: AppCompatTextView
    private lateinit var averageChargeDischargeCurrent: AppCompatTextView
    private lateinit var minChargeDischargeCurrent: AppCompatTextView
    private lateinit var temperature: AppCompatTextView
    private lateinit var voltage: AppCompatTextView
    private lateinit var lastChargeTime: AppCompatTextView
    private var isJob = false
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.charge_discharge_fragment, container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        batteryLevel = view.findViewById(R.id.battery_level)
        chargingTime = view.findViewById(R.id.charging_time)
        currentCapacity = view.findViewById(R.id.current_capacity_charge_discharge)
        capacityAdded = view.findViewById(R.id.capacity_added_charge_discharge)
        status = view.findViewById(R.id.status)
        sourceOfPower = view.findViewById(R.id.source_of_power)
        chargeCurrent = view.findViewById(R.id.charge_current)
        maxChargeDischargeCurrent = view.findViewById(R.id.max_charge_discharge_current)
        averageChargeDischargeCurrent = view.findViewById(R.id.average_charge_discharge_current)
        minChargeDischargeCurrent = view.findViewById(R.id.min_charge_discharge_current)
        temperature = view.findViewById(R.id.temperature)
        voltage = view.findViewById(R.id.voltage)
        lastChargeTime = view.findViewById(R.id.last_charge_time)

        updateTextAppearance()
    }

    override fun onResume() {

        super.onResume()

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

                    val status = Utils.batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = Utils.batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    withContext(Dispatchers.Main) {

                        (context as? MainActivity)?.toolbar?.title = getString(
                            if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                            else R.string.discharge)

                        val chargeDischargeNavigation = (context as? MainActivity)
                            ?.navigation?.menu?.findItem(R.id.charge_discharge_navigation)

                        chargeDischargeNavigation?.title = getString(if(status ==
                            BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                            R.string.discharge)

                        chargeDischargeNavigation?.icon = requireContext().getDrawable(
                            if(status == BatteryManager.BATTERY_STATUS_CHARGING)
                                R.drawable.ic_charge_navigation_24dp
                            else R.drawable.ic_discharge_navigation_24dp)

                        batteryLevel.text = getString(R.string.battery_level,
                            "${onGetBatteryLevel(
                                context ?: batteryLevel.context)}%")
                        if(CapacityInfoService.instance?.seconds ?: 0 > 0) {

                            chargingTime.visibility = View.VISIBLE

                            chargingTime.text = onGetChargingTime(
                                context ?: chargingTime.context,
                                CapacityInfoService.instance?.seconds ?: 0)
                        }
                        else if(chargingTime.visibility == View.VISIBLE)
                            chargingTime.visibility = View.GONE

                        lastChargeTime.text = getString(R.string.last_charge_time,
                            onGetLastChargeTime(context ?: lastChargeTime.context),
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_WITH, 0)}%",
                            "${pref.getInt(PreferencesKeys.BATTERY_LEVEL_TO, 0)}%")
                    }

                    withContext(Dispatchers.Main) {

                        this@ChargeDischargeFragment.status.text = getString(R.string.status,
                            onGetStatus(
                                context ?: this@ChargeDischargeFragment.status.context,
                                status))

                        if(onGetSourceOfPower(
                                context ?: this@ChargeDischargeFragment.sourceOfPower
                                    .context, sourceOfPower) != "N/A") {

                            if(this@ChargeDischargeFragment.sourceOfPower.visibility == View.GONE)
                                this@ChargeDischargeFragment.sourceOfPower.visibility = View.VISIBLE

                            this@ChargeDischargeFragment.sourceOfPower.text = onGetSourceOfPower(
                                context ?: this@ChargeDischargeFragment
                                    .sourceOfPower.context, sourceOfPower)
                        }

                        else this@ChargeDischargeFragment.sourceOfPower.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {

                        temperature.text =
                            if(!pref.getBoolean(PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT,
                                    false)) getString(R.string.temperature_celsius,
                                onGetTemperature(context ?: temperature.context))
                            else getString(R.string.temperature_fahrenheit,
                                onGetTemperature(context ?: temperature.context))

                        voltage.text = getString(if(pref.getBoolean(PreferencesKeys.VOLTAGE_IN_MV,
                                false)) R.string.voltage_mv else R.string.voltage,
                            DecimalFormat("#.#").format(onGetVoltage(
                                context ?: voltage.context)))
                    }

                    if(pref.getBoolean(PreferencesKeys.IS_SUPPORTED, true)) {

                        if(onGetCurrentCapacity(context ?: currentCapacity.context) > 0) {

                            if(currentCapacity.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    currentCapacity.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(onGetCurrentCapacity(
                                        context ?: currentCapacity.context)))

                                when {
                                    onGetSourceOfPower(
                                        context ?: this@ChargeDischargeFragment
                                            .sourceOfPower.context, sourceOfPower) != "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = onGetCapacityAdded(
                                            context ?: currentCapacity.context)
                                    }
                                    onGetSourceOfPower(
                                        context ?: this@ChargeDischargeFragment
                                            .sourceOfPower.context, sourceOfPower) == "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = onGetCapacityAdded(
                                            context ?: currentCapacity.context)
                                    }
                                    capacityAdded.visibility == View.VISIBLE ->
                                        capacityAdded.visibility = View.GONE
                                }
                            }
                        }

                        else {

                            if(currentCapacity.visibility == View.VISIBLE)
                                withContext(Dispatchers.Main) {
                                    currentCapacity.visibility = View.GONE }

                            if(capacityAdded.visibility == View.GONE
                                && pref.getFloat(PreferencesKeys.CAPACITY_ADDED, 0f) > 0f)
                                withContext(Dispatchers.Main) {
                                    capacityAdded.visibility = View.VISIBLE }

                            else withContext(Dispatchers.Main) {
                                capacityAdded.visibility = View.GONE }
                        }
                    }

                    else {

                        if(capacityAdded.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) { capacityAdded.visibility = View.GONE }

                        if(pref.contains(PreferencesKeys.CAPACITY_ADDED)) pref.edit().remove(
                            PreferencesKeys.CAPACITY_ADDED
                        ).apply()

                        if(pref.contains(PreferencesKeys.PERCENT_ADDED)) pref.edit().remove(
                            PreferencesKeys.PERCENT_ADDED
                        ).apply()
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING -> {

                            if(chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    chargeCurrent.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                chargeCurrent.text = getString(R.string.charge_current,
                                    onGetChargeDischargeCurrent(
                                        context ?: chargeCurrent.context).toString())
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_FULL,
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                            if(chargeCurrent.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    chargeCurrent.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                chargeCurrent.text = getString(R.string.discharge_current,
                                    onGetChargeDischargeCurrent(
                                        context ?: chargeCurrent.context).toString())
                            }
                        }

                        else -> {

                            if(chargeCurrent.visibility == View.VISIBLE)
                                withContext(Dispatchers.Main) {
                                    chargeCurrent.visibility = View.GONE }
                        }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL,
                        BatteryManager.BATTERY_STATUS_NOT_CHARGING ->

                            withContext(Dispatchers.Main) {

                                if(maxChargeDischargeCurrent.visibility ==  View.GONE)
                                    maxChargeDischargeCurrent.visibility = View.VISIBLE

                                if(averageChargeDischargeCurrent.visibility ==  View.GONE)
                                    averageChargeDischargeCurrent.visibility = View.VISIBLE

                                if(minChargeDischargeCurrent.visibility ==  View.GONE)
                                    minChargeDischargeCurrent.visibility = View.VISIBLE

                                maxChargeDischargeCurrent.text =getString(R.string.max_charge_current,
                                    BatteryInfoInterface.maxChargeCurrent)

                                averageChargeDischargeCurrent.text = getString(
                                    R.string.average_charge_current,
                                    BatteryInfoInterface.averageChargeCurrent)

                                minChargeDischargeCurrent.text = getString(
                                    R.string.min_charge_current,
                                    BatteryInfoInterface.minChargeCurrent)
                            }

                        BatteryManager.BATTERY_STATUS_DISCHARGING -> withContext(Dispatchers.Main) {

                            if(maxChargeDischargeCurrent.visibility ==  View.GONE)
                                maxChargeDischargeCurrent.visibility = View.VISIBLE

                            if(averageChargeDischargeCurrent.visibility ==  View.GONE)
                                averageChargeDischargeCurrent.visibility = View.VISIBLE

                            if(minChargeDischargeCurrent.visibility ==  View.GONE)
                                minChargeDischargeCurrent.visibility = View.VISIBLE

                            maxChargeDischargeCurrent.text = getString(R.string.max_discharge_current,
                                BatteryInfoInterface.maxDischargeCurrent)

                            averageChargeDischargeCurrent.text = getString(
                                R.string.average_discharge_current,
                                BatteryInfoInterface.averageDischargeCurrent)

                            minChargeDischargeCurrent.text = getString(
                                R.string.min_discharge_current,
                                BatteryInfoInterface.minDischargeCurrent)
                        }

                        else -> {

                            withContext(Dispatchers.Main) {

                                if(maxChargeDischargeCurrent.visibility ==  View.VISIBLE)
                                    maxChargeDischargeCurrent.visibility = View.GONE

                                if(averageChargeDischargeCurrent.visibility ==  View.VISIBLE)
                                    averageChargeDischargeCurrent.visibility = View.GONE

                                if(minChargeDischargeCurrent.visibility ==  View.VISIBLE)
                                    minChargeDischargeCurrent.visibility = View.GONE
                            }
                        }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if(onGetCurrentCapacity(
                                    context ?: currentCapacity.context) > 0L) 974L
                            else 981L)

                        else -> delay(3000L)
                    }
                }
            }
    }

    private fun updateTextAppearance() {

        TextAppearanceHelper.setTextAppearance(batteryLevel,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(chargingTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(currentCapacity,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(capacityAdded,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
//        TextAppearanceHelper.setTextAppearance(technology,
//            pref.getString(TEXT_STYLE, "0"),
//            pref.getString(TEXT_FONT, "6"),
//            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(status,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(sourceOfPower,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(chargeCurrent,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(
            maxChargeDischargeCurrent, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(
            averageChargeDischargeCurrent, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(
            minChargeDischargeCurrent, pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(temperature,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(voltage,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
        TextAppearanceHelper.setTextAppearance(lastChargeTime,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "1"))
    }
}