package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.TimeSpan.toHours
import com.ph03nix_x.capacityinfo.helpers.TimeSpan.toMinutes
import com.ph03nix_x.capacityinfo.helpers.TimeSpan.toSeconds
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_UNIT
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.utils.Utils.capacityAdded
import com.ph03nix_x.capacityinfo.utils.Utils.percentAdded
import com.ph03nix_x.capacityinfo.utils.Utils.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.utils.Utils.tempCurrentCapacity
import java.lang.RuntimeException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

@SuppressWarnings("PrivateApi")
interface BatteryInfoInterface {

    companion object {

        var residualCapacity = 0.0
        var batteryLevel = 0
        var maxChargeCurrent = 0
        var averageChargeCurrent = 0
        var minChargeCurrent = 0
        var maxDischargeCurrent = 0
        var averageDischargeCurrent = 0
        var minDischargeCurrent = 0
    }

    fun getDesignCapacity(context: Context): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(context)

        return (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()
    }

    fun getBatteryLevel(context: Context) = try {

        (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    catch (e: RuntimeException) {

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        batteryIntent?.getStringExtra(BatteryManager.EXTRA_LEVEL)?.toInt() ?: 0
    }

    fun getChargeDischargeCurrent(context: Context): Int {

        return try {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

            var chargeCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

            if(chargeCurrent < 0) chargeCurrent /= -1

            if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA") == "μA") chargeCurrent /= 1000

            getMaxAverageMinChargeDischargeCurrent(status, chargeCurrent)
            
            chargeCurrent
        }

        catch (e: RuntimeException) {

            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

            getMaxAverageMinChargeDischargeCurrent(status, 0)

            0
        }
    }
    
    fun getMaxAverageMinChargeDischargeCurrent(status: Int?, chargeCurrent: Int) {
        
        when(status) {
            
            BatteryManager.BATTERY_STATUS_CHARGING -> {

                maxDischargeCurrent = 0
                averageDischargeCurrent = 0
                minDischargeCurrent = 0
                
                if(chargeCurrent > maxChargeCurrent) maxChargeCurrent = chargeCurrent

                if(chargeCurrent < minChargeCurrent && chargeCurrent < maxChargeCurrent)
                    minChargeCurrent = chargeCurrent

                else if(minChargeCurrent == 0 && chargeCurrent < maxChargeCurrent) minChargeCurrent = chargeCurrent

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

                else if(minDischargeCurrent == 0 && chargeCurrent < maxDischargeCurrent) minDischargeCurrent = chargeCurrent

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

    fun getTemperature(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)?.toDouble() ?: 0.0

        temp /= 10

        var tempString = temp.toString()

        if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false))
            tempString = DecimalFormat("#.#").format((temp * 1.8) + 32)

        return tempString
    }

    fun getCurrentCapacity(context: Context): Double {

      return try {

          val pref = PreferenceManager.getDefaultSharedPreferences(context)

          val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

          var currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

          if (currentCapacity < 0) currentCapacity /= -1

          if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
              currentCapacity / 1000 else currentCapacity
      }

      catch (e: RuntimeException) { 0.0 }
    }

    fun getCapacityAdded(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                percentAdded = getBatteryLevel(context) - tempBatteryLevelWith

                capacityAdded = getCurrentCapacity(context) - tempCurrentCapacity

                if(capacityAdded < 0) capacityAdded /= -1

                context.getString(R.string.capacity_added, DecimalFormat("#.#").format(capacityAdded), "$percentAdded%")
            }

            else -> context.getString(R.string.capacity_added_last_charge,
                DecimalFormat("#.#").format(pref.getFloat(CAPACITY_ADDED, 0f).toDouble()),
                "${pref.getInt(PERCENT_ADDED, 0)}%")
        }
    }

    fun getVoltage(context: Context): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)?.toDouble() ?: 0.0

        if(!pref.getBoolean(VOLTAGE_IN_MV, false)) {

            if(pref.getString(VOLTAGE_UNIT, "mV") == "μV")
                voltage /= 1000.0.pow(2.0)
            else voltage /= 1000
        }

        else if(pref.getString(VOLTAGE_UNIT, "mV") == "μV") voltage /= 1000

        return voltage
    }

    fun getBatteryHealth(context: Context): String {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)) {

            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.battery_health_overheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.battery_health_over_voltage)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.battery_health_unspecified_failure)
            else -> context.getString(R.string.unknown)
        }
    }

    fun getResidualCapacity(context: Context, isCharging: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(isCharging && batteryLevel < getBatteryLevel(context)) {

            batteryLevel = getBatteryLevel(context)

            residualCapacity = getCurrentCapacity(context) / if(batteryLevel > 1) (batteryLevel / 100.0) else 1.0
        }

        else if(isCharging && batteryLevel == 100) residualCapacity = getCurrentCapacity(context)

        else if(!isCharging) {

            residualCapacity = pref.getInt(RESIDUAL_CAPACITY, 0).toDouble()

            if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                residualCapacity /= 1000
        }

        if(residualCapacity < 0) residualCapacity /= -1

        return context.getString(
            R.string.residual_capacity, DecimalFormat("#.#").format(residualCapacity),
            "${DecimalFormat("#.#").format(
                (residualCapacity / pref.getInt(DESIGN_CAPACITY, 0).toDouble()) * 100)}%")
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

    fun getPlugged(context: Context, extraPlugged: Int): String {

        return when(extraPlugged) {

            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.plugged, context.getString(R.string.plugged_ac))
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.plugged, context.getString(R.string.plugged_usb))
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.plugged,
                context.getString(R.string.plugged_wireless))
            else -> "N/A"
        }
    }
    
    fun getBatteryWear(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val designCapacity = pref.getInt(DESIGN_CAPACITY, 0).toDouble()

        return context.getString(
            R.string.battery_wear,
            if(residualCapacity > 0 && residualCapacity < designCapacity)
                "${DecimalFormat("#.#").format(100 - ((residualCapacity / designCapacity) * 100))}%" else "0%",
            if (residualCapacity > 0 && residualCapacity < designCapacity)
                DecimalFormat("#.#").format(designCapacity - residualCapacity) else "0")
    }

    fun getChargingTime(context: Context, seconds: Int): String {

        val secondsTime = toSeconds(seconds)
        val minutes = toMinutes(seconds)
        val hours = toHours(seconds)

        var time = "$hours:$minutes:$secondsTime"

        return context.getString(
            R.string.charging_time,

            try {

                var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)?.toString())).toString()

                val hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

                if(hoursDate > hours) {

                    time = "${hours - 1}:$minutes:$secondsTime"

                    dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)?.toString())).toString()
                }

                dateTime
            }

            catch (e: java.lang.IllegalArgumentException) { "${seconds}s" })
    }

    fun getLastChargeTime(context: Context): String {
        
        val secondsPref = PreferenceManager.getDefaultSharedPreferences(context).getInt(LAST_CHARGE_TIME, 0)

        val seconds = toSeconds(secondsPref)
        val minutes = toMinutes(secondsPref)
        val hours = toHours(secondsPref)

        var time = "$hours:$minutes:$seconds"

        return try {

            var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)?.toString())).toString()

            val hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

            if(hoursDate > hours) {

                time = "${hours - 1}:$minutes:$seconds"

                dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)?.toString())).toString()
            }

            dateTime
        }

        catch (e: IllegalArgumentException) { "${secondsPref}s" }
    }
}