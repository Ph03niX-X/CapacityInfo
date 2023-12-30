package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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

    fun getDesignCapacity(context: Context): Int {

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

    fun getBatteryLevel(context: Context) = try {

        (context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager)?.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    catch (e: RuntimeException) {

        val batteryIntent = try { context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED)) } catch (e: RuntimeException) { null }

        batteryIntent?.getStringExtra(BatteryManager.EXTRA_LEVEL)?.toInt() ?: 0
    }

    fun getChargeDischargeCurrent(context: Context): Int {

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

            getMaxAverageMinChargeDischargeCurrent(status, chargeCurrent)
            
            chargeCurrent
        }

        catch (e: RuntimeException) {

            val status = batteryIntent?.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN
            )

            getMaxAverageMinChargeDischargeCurrent(status, 0)

            0
        }
    }

    fun getChargeDischargeCurrentInWatt(chargeDischargeCurrent: Int, isCharging: Boolean = false)
    = (chargeDischargeCurrent.toDouble() * if(isCharging) CHARGING_VOLTAGE_WATT
    else NOMINAL_BATTERY_VOLTAGE) / 1000.0

    fun getFastCharge(context: Context): String {
        return if(isFastCharge(context))
            context.resources.getString(R.string.fast_charge_yes, getFastChargeWatt())
        else context.resources.getString(R.string.fast_charge_no)
    }

    fun getFastChargeOverlay(context: Context): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                R.bool.is_only_values_overlay))) getFastCharge(context)
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

    private fun getFastChargeWatt() = DecimalFormat("#.#").format(
        (maxChargeCurrent.toDouble() * CHARGING_VOLTAGE_WATT) / 1000.0)
    
    fun getMaxAverageMinChargeDischargeCurrent(status: Int?, chargeCurrent: Int) {
        
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

    fun getChargingCurrentLimit(context: Context): String? {

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

    fun getTemperatureInCelsius(context: Context): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0

        return temperatureInCelsius / 10.0
    }

    fun getTemperatureInFahrenheit(context: Context) =
        (getTemperatureInCelsius(context) * 1.8) + 32.0

    fun getTemperatureInFahrenheit(temperatureInCelsius: Double) =
        (temperatureInCelsius * 1.8) + 32.0

    fun getMaximumTemperature(context: Context, temperature: Double): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = (batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0) / 10.0

        return if(temperatureInCelsius >= temperature || temperature == 0.0) temperatureInCelsius
        else temperature
    }

    fun getAverageTemperature(context: Context, temperatureMax: Double, temperatureMin: Double) =
        (temperatureMax + temperatureMin) / 2.0

    fun getMinimumTemperature(context: Context, temperature: Double): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val temperatureInCelsius = (batteryIntent?.getIntExtra(BatteryManager
            .EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0) / 10.0

        return if(temperatureInCelsius <= temperature || temperature == 0.0) temperatureInCelsius
        else temperature
    }

    fun getCurrentCapacity(context: Context) =

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

    fun getCapacityInWh(capacity: Double) =
        (capacity * NOMINAL_BATTERY_VOLTAGE) / 1000.0

    fun getCapacityAdded(context: Context, isOverlay: Boolean = false,
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
                percentAdded = (getBatteryLevel(context) ?: 0) - tempBatteryLevelWith

                if (percentAdded < 0) percentAdded = 0

                capacityAdded = getCurrentCapacity(context) - tempCurrentCapacity

                if (capacityAdded < 0) capacityAdded /= -1

                if(isCapacityInWh)
                context.getString(if(!isOverlay || !isOnlyValues)
                    R.string.capacity_added_wh else
                        R.string.capacity_added_wh_overlay_only_values, DecimalFormat("#.#")
                    .format(getCapacityInWh(capacityAdded)), "$percentAdded%")

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
                        .format(getCapacityInWh(capacityAddedPref)),
                        "$percentAddedPref%")

            else context.getString(if(!isOverlay || !isOnlyValues)
                R.string.capacity_added else
                    R.string.capacity_added_overlay_only_values, DecimalFormat("#.#")
                    .format(capacityAddedPref), "$percentAddedPref%")
            }
        }
    }

    fun getVoltage(context: Context): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            ?.toDouble() ?: 0.0

        if(pref.getString(VOLTAGE_UNIT, "mV") == "μV") voltage /= 1000.0.pow(2.0)

        return voltage
    }

    fun getBatteryHealth(context: Context): Int? {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val designCapacity = pref.getInt(DESIGN_CAPACITY,
            context.resources.getInteger(R.integer.min_design_capacity)).toDouble()
        val residualCapacity = pref.getInt(RESIDUAL_CAPACITY, 0).toDouble() / if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0 else 100.0
        return if(residualCapacity > 0 && residualCapacity <= designCapacity) {
            when((100.0 - ((residualCapacity / designCapacity) * 100.0)).toInt()) {
                in 0..9 -> R.string.battery_health_great
                in 10..19 -> R.string.battery_health_very_good
                in 20..29 -> R.string.battery_health_good
                in 30..39 -> R.string.battery_health_bad
                in 40..59 -> R.string.battery_health_very_bad
                else -> R.string.battery_health_replacement_required
            }
        }
        else if(residualCapacity > designCapacity) R.string.battery_health_great
        else if(residualCapacity < 0) R.string.battery_health_replacement_required else null
    }

    fun getResidualCapacity(context: Context, isOverlay: Boolean = false,
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
            DecimalFormat("#.#").format(getCapacityInWh(residualCapacity)),
            "${DecimalFormat("#.#").format(
                (residualCapacity / designCapacity) * 100.0)}%")

        else context.getString(if(!isOverlay || !isOnlyValues) R.string.residual_capacity else
            R.string.residual_capacity_overlay_only_values, DecimalFormat("#.#").format(
            residualCapacity), "${DecimalFormat("#.#").format((
                residualCapacity / designCapacity) * 100.0)}%")
    }

    fun getStatus(context: Context, extraStatus: Int): String {

        return when(extraStatus) {

            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.discharging)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.not_charging)
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.charging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.full)
            else -> context.getString(R.string.unknown)
        }
    }

    fun getSourceOfPower(context: Context, extraPlugged: Int, isOverlay: Boolean = false,
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
    
    fun getBatteryWear(context: Context, isOverlay: Boolean = false,
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
                        getCapacityInWh(designCapacity - residualCapacity)) else "0")
        else context.getString(if(!isOverlay || !isOnlyValues) R.string.battery_wear else R.string
                .battery_wear_overlay_only_values, if (residualCapacity > 0 &&
            residualCapacity < designCapacity) "${DecimalFormat("#.#")
            .format(100 - ((residualCapacity / designCapacity) * 100))}%" else "0%",
            if(residualCapacity > 0 && residualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - residualCapacity) else "0")
    }

    fun getChargingTime(context: Context, seconds: Int, isOverlay: Boolean = false,
                          isOnlyValues: Boolean = false): String {

        return context.getString(
            if (!isOverlay || !isOnlyValues) R.string.charging_time else
                R.string.charging_time_overlay_only_values, TimeHelper.getTime(seconds.toLong()))
    }

    fun getChargingTimeRemaining(context: Context): String {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val chargingTimeRemaining = batteryManager.computeChargeTimeRemaining() / 1000
            return TimeHelper.getTime(chargingTimeRemaining)
        }

        var chargingTimeRemaining: Double

        val batteryLevel = getBatteryLevel(context) ?: 0

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val currentCapacity = getCurrentCapacity(context)

        val residualCapacity = if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                "μAh") == "μAh") pref.getInt(RESIDUAL_CAPACITY, 0)
            .toDouble() / 1000.0 else pref.getInt(RESIDUAL_CAPACITY, 0).toDouble() / 100.0

        val chargeDischargeCurrent = getChargeDischargeCurrent(context).toDouble()

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

    fun getRemainingBatteryTime(context: Context): String {

        val currentCapacity = getCurrentCapacity(context)

        return if(averageDischargeCurrent > 0.0) {

            val remainingBatteryTime = ((currentCapacity / averageDischargeCurrent) * 3600.0).toLong()

            TimeHelper.getTime(remainingBatteryTime)
        }

        else context.getString(R.string.unknown)
    }

    fun getLastChargeTime(context: Context): String {
        
        val seconds = PreferenceManager.getDefaultSharedPreferences(context).getInt(
            LAST_CHARGE_TIME, 0).toLong()

        return TimeHelper.getTime(seconds)
    }
}