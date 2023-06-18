package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.TimeHelper
import com.ph03nix_x.capacityinfo.utilities.Constants.CHARGING_VOLTAGE_WATT
import com.ph03nix_x.capacityinfo.utilities.Constants.NOMINAL_BATTERY_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ONLY_VALUES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.pow

@SuppressWarnings("PrivateApi")
interface BatteryInfoInterface {

    companion object {

        var tempCurrentCapacity = 0.0
        var capacityAdded = 0.0
        var tempBatteryLevelWith = 0
        var percentAdded = 0
        var residualCapacity = 0.0
        var batteryLevel = 0
        var tempBatteryLevel = 0
        var maxChargeCurrent = 0
        var averageChargeCurrent = 0
        var minChargeCurrent = 0
        var maxDischargeCurrent = 0
        var averageDischargeCurrent = 0
        var minDischargeCurrent = 0
        var maximumTemperature = 0.0
        var averageTemperature = 0.0
        var minimumTemperature = 0.0
    }

    fun getOnDesignCapacity(context: Context): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(
            Context::class.java
        ).newInstance(context)

        val designCapacity = (Class.forName(powerProfileClass).getMethod(
            "getBatteryCapacity"
        ).invoke(mPowerProfile) as Double).toInt()

        return when {

            designCapacity == 0 || designCapacity < context.resources.getInteger(
                R.integer.min_design_capacity) || designCapacity > context.resources.getInteger(
                R.integer.max_design_capacity) -> context.resources.getInteger(
                R.integer.min_design_capacity)
            designCapacity < 0 -> designCapacity / -1
            else -> designCapacity
        }
    }

    fun getOnBatteryLevel(context: Context) = try {

        (context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager)?.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    catch (e: RuntimeException) {

        val batteryIntent = try { context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED)) } catch (e: RuntimeException) { null }

        batteryIntent?.getStringExtra(BatteryManager.EXTRA_LEVEL)?.toInt() ?: 0
    }

    fun getOnChargeDischargeCurrent(context: Context): Int {

        return try {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE)
                    as BatteryManager

            var chargeCurrent = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
            )
            
            val status = batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )

            if(chargeCurrent < 0) chargeCurrent /= -1

            if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA") == "μA")
                chargeCurrent /= 1000

            getOnMaxAverageMinChargeDischargeCurrent(status, chargeCurrent)
            
            chargeCurrent
        }

        catch (e: RuntimeException) {

            val status = batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )

            getOnMaxAverageMinChargeDischargeCurrent(status, 0)

            0
        }
    }

    fun getOnChargeDischargeCurrentInWatt(chargeDischargeCurrent: Int, isCharging: Boolean = false)
    = (chargeDischargeCurrent.toDouble() * if(isCharging) CHARGING_VOLTAGE_WATT
    else NOMINAL_BATTERY_VOLTAGE) / 1000.0

    fun getOnFastCharge(context: Context): String {
        return if(isFastCharge(context))
            context.resources.getString(R.string.fast_charge_yes, getOnFastChargeWatt())
        else context.resources.getString(R.string.fast_charge_no)
    }

    fun getOnFastChargeOverlay(context: Context): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                R.bool.is_only_values_overlay))) getOnFastCharge(context)
        else if (isFastCharge(context))
            context.getString(R.string.fast_charge_yes_overlay_only_values)
        else context.resources.getString(R.string.fast_charge_no_overlay_only_values)
    }

    private fun isFastCharge(context: Context) =
        maxChargeCurrent >= context.resources.getInteger(R.integer.fast_charge_min)

    fun isTurboCharge(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return maxChargeCurrent >= pref.getInt(DESIGN_CAPACITY,
            context.resources.getInteger(R.integer.min_design_capacity) - 250)
    }

    private fun getOnFastChargeWatt() = DecimalFormat("#.#").format(
        (maxChargeCurrent.toDouble() * CHARGING_VOLTAGE_WATT) / 1000.0)
    
    fun getOnMaxAverageMinChargeDischargeCurrent(status: Int?, chargeCurrent: Int) {
        
        when(status) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                maxDischargeCurrent = 0
                averageDischargeCurrent = 0
                minDischargeCurrent = 0

                if (chargeCurrent > maxChargeCurrent) maxChargeCurrent = chargeCurrent

                if (chargeCurrent < minChargeCurrent && chargeCurrent < maxChargeCurrent)
                    minChargeCurrent = chargeCurrent
                else if (minChargeCurrent == 0 && chargeCurrent < maxChargeCurrent)
                    minChargeCurrent = chargeCurrent

                if (maxChargeCurrent > 0 && minChargeCurrent > 0)
                    averageChargeCurrent = (maxChargeCurrent + minChargeCurrent) / 2
            }

            BatteryManager.BATTERY_STATUS_DISCHARGING -> {

                maxChargeCurrent = 0
                averageChargeCurrent = 0
                minChargeCurrent = 0

                if (chargeCurrent > maxDischargeCurrent) maxDischargeCurrent = chargeCurrent

                if (chargeCurrent < minDischargeCurrent && chargeCurrent < maxDischargeCurrent)
                    minDischargeCurrent = chargeCurrent
                else if (minDischargeCurrent == 0 && chargeCurrent < maxDischargeCurrent)
                    minDischargeCurrent = chargeCurrent

                if (maxDischargeCurrent > 0 && minDischargeCurrent > 0)
                    averageDischargeCurrent = (maxDischargeCurrent + minDischargeCurrent) / 2
            }

            BatteryManager.BATTERY_STATUS_UNKNOWN -> {

                maxChargeCurrent = 0
                averageChargeCurrent = 0
                minChargeCurrent = 0
                maxDischargeCurrent = 0
                averageDischargeCurrent = 0
                minDischargeCurrent = 0
            }
        }
        
    }

    fun getOnChargingCurrentLimit(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val constantChargeCurrentMax = File(
            "/sys/class/power_supply/battery/constant_charge_current_max")

        var chargingCurrentLimit: String? = null

       return if(constantChargeCurrentMax.exists()) {

            try {

                val bufferReader = BufferedReader(FileReader(constantChargeCurrentMax))

                chargingCurrentLimit = bufferReader.readLine()

                bufferReader.close()

                if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA") == "μA")
                    chargingCurrentLimit = ((chargingCurrentLimit?.toInt() ?: 0) / 1000).toString()

                chargingCurrentLimit
            }

            catch (e: IOException) { chargingCurrentLimit }
        }

        else null
    }

    fun getOnTemperatureInCelsius(context: Context): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0

        return temperatureInCelsius / 10.0
    }

    fun getOnTemperatureInFahrenheit(context: Context) =
        (getOnTemperatureInCelsius(context) * 1.8) + 32.0

    fun getOnTemperatureInFahrenheit(temperatureInCelsius: Double) =
        (temperatureInCelsius * 1.8) + 32.0

    fun getOnMaximumTemperature(context: Context, temperature: Double): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = (batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0) / 10.0

        return if(temperatureInCelsius >= temperature || temperature == 0.0) temperatureInCelsius
        else temperature
    }

    fun getOnAverageTemperature(context: Context, temperatureMax: Double, temperatureMin: Double) =
        (temperatureMax + temperatureMin) / 2.0

    fun getOnMinimumTemperature(context: Context, temperature: Double): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = (batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0) / 10.0

        return if(temperatureInCelsius <= temperature || temperature == 0.0) temperatureInCelsius
        else temperature
    }

    fun getOnCurrentCapacity(context: Context) =

        try {

          val pref = PreferenceManager.getDefaultSharedPreferences(context)

          val batteryManager = context.getSystemService(Context.BATTERY_SERVICE)
                  as BatteryManager

          val currentCapacity = batteryManager.getIntProperty(
              BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

          when {

              currentCapacity < 0 -> 0.001

              pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh" ->
                  currentCapacity / 1000.0

              else -> currentCapacity
          }
      }

      catch (e: RuntimeException) { 0.001 }

    fun getOnCapacityInWh(capacity: Double) =
        (capacity * NOMINAL_BATTERY_VOLTAGE) / 1000.0

    fun getOnCapacityAdded(context: Context, isOverlay: Boolean = false,
                           isOnlyValues: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))

        val capacityAddedPref = pref.getFloat(CAPACITY_ADDED, 0f).toDouble()

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))
        return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {
                percentAdded = (getOnBatteryLevel(context) ?: 0) - tempBatteryLevelWith

                if (percentAdded < 0) percentAdded = 0

                capacityAdded = getOnCurrentCapacity(context) - tempCurrentCapacity

                if (capacityAdded < 0) capacityAdded /= -1

                if(isCapacityInWh)
                context.getString(if(!isOverlay || !isOnlyValues)
                    R.string.capacity_added_wh else
                        R.string.capacity_added_wh_overlay_only_values, DecimalFormat("#.#")
                    .format(getOnCapacityInWh(capacityAdded)), "$percentAdded%")

                else context.getString(if(!isOverlay || !isOnlyValues)
                    R.string.capacity_added else
                        R.string.capacity_added_overlay_only_values, DecimalFormat("#.#")
                    .format(capacityAdded), "$percentAdded%")
            }

            else -> {
                val percentAddedPref = pref.getInt(PERCENT_ADDED, 0)
                if(isCapacityInWh)
                    context.getString(if(!isOverlay || !isOnlyValues)
                        R.string.capacity_added_wh else
                        R.string.capacity_added_wh_overlay_only_values, DecimalFormat("#.#")
                        .format(getOnCapacityInWh(capacityAddedPref)),
                        "$percentAddedPref%")

            else context.getString(if(!isOverlay || !isOnlyValues)
                R.string.capacity_added else
                    R.string.capacity_added_overlay_only_values, DecimalFormat("#.#")
                    .format(capacityAddedPref), "$percentAddedPref%")
            }
        }
    }

    fun getOnVoltage(context: Context): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            ?.toDouble() ?: 0.0

        if(pref.getString(VOLTAGE_UNIT, "mV") == "μV") voltage /= 1000.0.pow(2.0)

        return voltage
    }

    fun getOnBatteryHealth(context: Context): String {

        batteryIntent = context.registerReceiver(
            null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        return when(batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        )) {

            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(
                R.string.battery_health_overheat
            )
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(
                R.string.battery_health_over_voltage
            )
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(
                R.string.battery_health_unspecified_failure
            )
            else -> context.getString(R.string.unknown)
        }
    }

    fun getOnResidualCapacity(context: Context, isOverlay: Boolean = false,
                              isOnlyValues: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))

        val designCapacity = pref.getInt(DESIGN_CAPACITY, context.resources
            .getInteger(R.integer.min_design_capacity)).toDouble()

        residualCapacity = pref.getInt(RESIDUAL_CAPACITY, 0).toDouble()

        residualCapacity /= if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0 else 100.0

        if(residualCapacity < 0.0) residualCapacity /= -1.0

        return if(isCapacityInWh) context.getString(if(!isOverlay || !isOnlyValues)
            R.string.residual_capacity_wh else R.string.residual_capacity_wh_overlay_only_values,
            DecimalFormat("#.#").format(getOnCapacityInWh(residualCapacity)),
            "${DecimalFormat("#.#").format(
                (residualCapacity / designCapacity) * 100.0)}%")

        else context.getString(if(!isOverlay || !isOnlyValues) R.string.residual_capacity else
            R.string.residual_capacity_overlay_only_values, DecimalFormat("#.#").format(
            residualCapacity), "${DecimalFormat("#.#").format((
                residualCapacity / designCapacity) * 100.0)}%")
    }

    fun getOnStatus(context: Context, extraStatus: Int): String {

        return when(extraStatus) {

            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.discharging)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.not_charging)
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.charging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.full)
            else -> context.getString(R.string.unknown)
        }
    }

    fun getOnSourceOfPower(context: Context, extraPlugged: Int, isOverlay: Boolean = false,
                           isOnlyValues: Boolean = false): String {

        return when(extraPlugged) {

            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(
                if (!isOverlay || !isOnlyValues)
                    R.string.source_of_power else R.string.source_of_power_overlay_only_values,
                context.getString(R.string.source_of_power_ac)
            )
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(
                if (!isOverlay || !isOnlyValues)
                    R.string.source_of_power else R.string.source_of_power_overlay_only_values,
                context.getString(R.string.source_of_power_usb)
            )
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(
                if (!isOverlay || !isOnlyValues)
                    R.string.source_of_power else R.string.source_of_power_overlay_only_values,
                context.getString(R.string.source_of_power_wireless)
            )
            else -> "N/A"
        }
    }
    
    fun getOnBatteryWear(context: Context, isOverlay: Boolean = false,
                         isOnlyValues: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val designCapacity = pref.getInt(DESIGN_CAPACITY, context.resources
            .getInteger(R.integer.min_design_capacity)).toDouble()

        val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))

        return if(isCapacityInWh)
            context.getString(
                if(!isOverlay || !isOnlyValues) R.string.battery_wear_wh else R.string
                    .battery_wear_wh_overlay_only_values, if (residualCapacity > 0 &&
                    residualCapacity < designCapacity) "${DecimalFormat("#.#")
                    .format(100 - ((residualCapacity / designCapacity) * 100))}%"
                else "0%", if(residualCapacity > 0 && residualCapacity < designCapacity)
                    DecimalFormat("#.#").format(
                        getOnCapacityInWh(designCapacity - residualCapacity)) else "0")
        else context.getString(if(!isOverlay || !isOnlyValues) R.string.battery_wear else R.string
                .battery_wear_overlay_only_values, if (residualCapacity > 0 &&
            residualCapacity < designCapacity) "${DecimalFormat("#.#")
            .format(100 - ((residualCapacity / designCapacity) * 100))}%" else "0%",
            if(residualCapacity > 0 && residualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - residualCapacity) else "0")
    }

    fun getOnChargingTime(context: Context, seconds: Int, isOverlay: Boolean = false,
                          isOnlyValues: Boolean = false): String {

        return context.getString(
            if (!isOverlay || !isOnlyValues) R.string.charging_time else
                R.string.charging_time_overlay_only_values, TimeHelper.getTime(seconds.toLong())
        )
    }

    fun getOnChargingTimeRemaining(context: Context): String {

        var chargingTimeRemaining: Double

        val batteryLevel = getOnBatteryLevel(context) ?: 0

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val currentCapacity = getOnCurrentCapacity(context)

        val residualCapacity = if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                "μAh") == "μAh") pref.getInt(RESIDUAL_CAPACITY, 0)
            .toDouble() / 1000.0 else pref.getInt(RESIDUAL_CAPACITY, 0).toDouble() / 100.0

        val chargeDischargeCurrent = getOnChargeDischargeCurrent(context).toDouble()

        return if(currentCapacity > 0.0 && currentCapacity < residualCapacity) {

            val capacity = residualCapacity - currentCapacity

            if(chargeDischargeCurrent > 0.0) {

                chargingTimeRemaining = if(chargeDischargeCurrent > 500.0) {
                    (capacity / chargeDischargeCurrent) * if(isTurboCharge(context)) {
                        if(chargeDischargeCurrent >= 1500) 1.2
                        else if(chargeDischargeCurrent >= 1000) 0.8 else 0.4
                    }
                    else 1.1
                }
                else capacity / chargeDischargeCurrent

                chargingTimeRemaining *= 3600.0

                TimeHelper.getTime(chargingTimeRemaining.toLong())
            }

            else context.getString(R.string.unknown)
        }

        else if(currentCapacity > 0.0 && (currentCapacity >= residualCapacity
                    || currentCapacity >= pref.getInt(
                DESIGN_CAPACITY, context.resources.getInteger(
                    R.integer.min_design_capacity
                )
            ).toDouble()) && residualCapacity > 0.0)
        context.getString(R.string.unknown)

        else if(currentCapacity > 0.0 && residualCapacity == 0.0) {

            val capacity = pref.getInt(
                DESIGN_CAPACITY, context.resources.getInteger(
                    R.integer.min_design_capacity
                )
            ) - currentCapacity

            if(chargeDischargeCurrent > 0.0) {

                chargingTimeRemaining = if(chargeDischargeCurrent > 500.0)
                    (capacity / chargeDischargeCurrent) * 1.1 else capacity / chargeDischargeCurrent

                chargingTimeRemaining *= 3600.0

                TimeHelper.getTime(chargingTimeRemaining.toLong())
            }

            else context.getString(R.string.unknown)
        }

        else {

            val designCapacity = pref.getInt(
                DESIGN_CAPACITY, context.resources.getInteger(
                    R.integer.min_design_capacity
                )
            ).toDouble()

            val capacity = designCapacity - (designCapacity * (
                    batteryLevel.toDouble() / 100.0))

            if(chargeDischargeCurrent > 0.0) {

                chargingTimeRemaining = if(chargeDischargeCurrent > 500.0)
                    (capacity / chargeDischargeCurrent) * 1.1
                else capacity / chargeDischargeCurrent

                chargingTimeRemaining *= 3600.0

                TimeHelper.getTime(chargingTimeRemaining.toLong())
            }

            else context.getString(R.string.unknown)
        }
    }

    fun getOnRemainingBatteryTime(context: Context): String {

        val currentCapacity = getOnCurrentCapacity(context)

        return if(averageDischargeCurrent > 0.0) {

            val remainingBatteryTime = ((currentCapacity / averageDischargeCurrent) * 3600.0).toLong()

            TimeHelper.getTime(remainingBatteryTime)
        }

        else context.getString(R.string.unknown)
    }

    fun getOnLastChargeTime(context: Context): String {
        
        val seconds = PreferenceManager.getDefaultSharedPreferences(context).getInt(
            LAST_CHARGE_TIME, 0).toLong()

        return TimeHelper.getTime(seconds)
    }
}