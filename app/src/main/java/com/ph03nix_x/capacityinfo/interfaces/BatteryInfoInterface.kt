package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ChargingTimeHelper.getHours
import com.ph03nix_x.capacityinfo.helpers.ChargingTimeHelper.getMinutes
import com.ph03nix_x.capacityinfo.helpers.ChargingTimeHelper.getSeconds
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import java.lang.RuntimeException
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
        var maxChargeCurrent = 0
        var averageChargeCurrent = 0
        var minChargeCurrent = 0
        var maxDischargeCurrent = 0
        var averageDischargeCurrent = 0
        var minDischargeCurrent = 0
    }

    fun onGetDesignCapacity(context: Context): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(
            Context::class.java).newInstance(context)

        val designCapacity = (Class.forName(powerProfileClass).getMethod(
            "getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()

        return when {

            designCapacity == 0 -> context.resources.getInteger(R.integer.min_design_capacity)
            designCapacity < 0 -> designCapacity / -1
            else -> designCapacity
        }
    }

    fun onGetBatteryLevel(context: Context) = try {

        (context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager)?.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    catch(e: RuntimeException) {

        val batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        batteryIntent?.getStringExtra(BatteryManager.EXTRA_LEVEL)?.toInt() ?: 0
    }

    fun onGetChargeDischargeCurrent(context: Context): Int {

        return try {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE)
                    as BatteryManager

            var chargeCurrent = batteryManager.getIntProperty(
                BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN)

            if(chargeCurrent < 0) chargeCurrent /= -1

            if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA") == "μA")
                chargeCurrent /= 1000

            onGetMaxAverageMinChargeDischargeCurrent(status, chargeCurrent)
            
            chargeCurrent
        }

        catch(e: RuntimeException) {

            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN)

            onGetMaxAverageMinChargeDischargeCurrent(status, 0)

            0
        }
    }
    
    fun onGetMaxAverageMinChargeDischargeCurrent(status: Int?, chargeCurrent: Int) {
        
        when(status) {
            
            BatteryManager.BATTERY_STATUS_CHARGING -> {

                maxDischargeCurrent = 0
                averageDischargeCurrent = 0
                minDischargeCurrent = 0
                
                if(chargeCurrent > maxChargeCurrent) maxChargeCurrent = chargeCurrent

                if(chargeCurrent < minChargeCurrent && chargeCurrent < maxChargeCurrent)
                    minChargeCurrent = chargeCurrent

                else if(minChargeCurrent == 0 && chargeCurrent < maxChargeCurrent)
                    minChargeCurrent = chargeCurrent

                if(maxChargeCurrent > 0 && minChargeCurrent > 0)
                    averageChargeCurrent = (maxChargeCurrent + minChargeCurrent) / 2
            }
            
            BatteryManager.BATTERY_STATUS_DISCHARGING -> {

                maxChargeCurrent = 0
                averageChargeCurrent = 0
                minChargeCurrent = 0

                if(chargeCurrent > maxDischargeCurrent) maxDischargeCurrent = chargeCurrent

                if(chargeCurrent < minDischargeCurrent && chargeCurrent < maxDischargeCurrent)
                    minDischargeCurrent = chargeCurrent

                else if(minDischargeCurrent == 0 && chargeCurrent < maxDischargeCurrent)
                    minDischargeCurrent = chargeCurrent

                if(maxDischargeCurrent > 0 && minDischargeCurrent > 0)
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

    fun onGetTemperature(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            ?.toDouble() ?: 0.0

        temp /= 10.0

        return DecimalFormat("#.#").format(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                context.resources.getBoolean(R.bool.temperature_in_fahrenheit)))
            (temp * 1.8) + 32.0 else temp)
    }

    fun onGetTemperatureInDouble(context: Context): Double {

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            ?.toDouble() ?: 0.0

        return temp / 10.0
    }

    fun onGetCurrentCapacity(context: Context): Double {

      return try {

          val pref = PreferenceManager.getDefaultSharedPreferences(context)

          val batteryManager = context.getSystemService(Context.BATTERY_SERVICE)
                  as BatteryManager

          val currentCapacity = batteryManager.getIntProperty(BatteryManager
              .BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

          when {

              currentCapacity < 0 -> 0.001

              pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh" ->
                  currentCapacity / 1000

              else -> currentCapacity
          }
      }

      catch(e: RuntimeException) { 0.001 }
    }

    fun onGetCapacityAdded(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

            return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                percentAdded = (onGetBatteryLevel(context) ?: 0) - tempBatteryLevelWith

                if(percentAdded < 0) percentAdded = 0

                capacityAdded = onGetCurrentCapacity(context) - tempCurrentCapacity

                if(capacityAdded < 0) capacityAdded /= -1

                context.getString(R.string.capacity_added, DecimalFormat("#.#")
                    .format(capacityAdded), "$percentAdded%")
            }

            else -> context.getString(R.string.capacity_added, DecimalFormat("#.#").format(
                pref.getFloat(CAPACITY_ADDED, 0f).toDouble()), "${pref.getInt(PERCENT_ADDED, 
                0)}%")
        }
    }

    fun onGetVoltage(context: Context): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            ?.toDouble() ?: 0.0

        if(!pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(R.bool.voltage_in_mv))) {

            if(pref.getString(VOLTAGE_UNIT, "mV") == "μV")
                voltage /= 1000.0.pow(2.0)
            else voltage /= 1000
        }

        else if(pref.getString(VOLTAGE_UNIT, "mV") == "μV") voltage /= 1000

        return voltage
    }

    fun onGetBatteryHealth(context: Context): String {

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN)) {

            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(
                R.string.battery_health_overheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(
                R.string.battery_health_over_voltage)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(
                R.string.battery_health_unspecified_failure)
            else -> context.getString(R.string.unknown)
        }
    }

    fun onGetResidualCapacity(context: Context, isCharging: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(isCharging && batteryLevel < (onGetBatteryLevel(context) ?: 0)) {

            batteryLevel = onGetBatteryLevel(context) ?: 0

            residualCapacity = onGetCurrentCapacity(context) / if(batteryLevel > 1)
                (batteryLevel / 100.0) else 1.0
        }

        else if(isCharging && batteryLevel == 100) residualCapacity = onGetCurrentCapacity(
            context)

        else if(!isCharging) {

            residualCapacity = pref.getInt(RESIDUAL_CAPACITY, 0).toDouble()

            if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                residualCapacity /= 1000
        }

        if(residualCapacity < 0) residualCapacity /= -1

        return context.getString(
            R.string.residual_capacity, DecimalFormat("#.#").format(residualCapacity),
            "${DecimalFormat("#.#").format((residualCapacity / pref.getInt(
                DESIGN_CAPACITY, context.resources.getInteger(R.integer.min_design_capacity))
                .toDouble()) * 100)}%")
    }

    fun onGetStatus(context: Context, extraStatus: Int): String {

        return when(extraStatus) {

            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.discharging)
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.not_charging)
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.charging)
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.full)
            else -> context.getString(R.string.unknown)
        }
    }

    fun onGetSourceOfPower(context: Context, extraPlugged: Int): String {

        return when(extraPlugged) {

            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.source_of_power,
                context.getString(R.string.source_of_power_ac))
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.source_of_power,
                context.getString(R.string.source_of_power_usb))
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.source_of_power,
                context.getString(R.string.source_of_power_wireless))
            else -> "N/A"
        }
    }
    
    fun onGetBatteryWear(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val designCapacity = pref.getInt(DESIGN_CAPACITY, context.resources.getInteger(
            R.integer.min_design_capacity)).toDouble()

        return context.getString(R.string.battery_wear, if(residualCapacity > 0
            && residualCapacity < designCapacity) "${DecimalFormat("#.#").format(
            100 - ((residualCapacity / designCapacity) * 100))}%" else "0%",
            if(residualCapacity > 0 && residualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - residualCapacity) else "0")
    }

    fun onGetChargingTime(context: Context, seconds: Int): String {

        val hours = getHours(seconds.toLong())
        val minutes = getMinutes(seconds.toLong())
        val secondsTime = getSeconds(seconds.toLong())

        return context.getString(R.string.charging_time, "$hours:$minutes:$secondsTime")
    }

    fun onGetLastChargeTime(context: Context): String {
        
        val secondsPref = PreferenceManager.getDefaultSharedPreferences(context).getInt(
            LAST_CHARGE_TIME, 0).toLong()

        val hours = getHours(secondsPref)
        val minutes = getMinutes(secondsPref)
        val seconds = getSeconds(secondsPref)

        return "$hours:$minutes:$seconds"
    }
}