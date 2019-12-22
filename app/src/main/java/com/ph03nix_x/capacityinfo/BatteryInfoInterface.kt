package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Util.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevelWith
import com.ph03nix_x.capacityinfo.Util.Companion.tempCurrentCapacity
import java.lang.RuntimeException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressWarnings("PrivateApi")
interface BatteryInfoInterface : TimeSpanInterface {

    companion object {

        var residualCapacity = 0.0
        var batteryLevel = 0
    }

    fun getDesignCapacity(context: Context): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(context)

        return (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()
    }

    fun getBatteryLevel(context: Context) = try {

        (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    catch (e: RuntimeException) { 0 }

    fun getChargingCurrent(context: Context): Int {

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if(chargingCurrent < 0) chargingCurrent /= -1

        return chargingCurrent / 1000
    }

    fun getTemperature(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toDouble()

        temp /= 10

        var tempString = temp.toString()

        if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false))
            tempString = DecimalFormat("#.#").format((temp * 1.8) + 32)

        return tempString
    }

    fun getCurrentCapacity(context: Context): Double {

      return try {

          val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

          var currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

          if (currentCapacity < 0) currentCapacity /= -1

          currentCapacity / 1000
      }

      catch (e: RuntimeException) { 0.0 }
    }

    fun getCapacityAdded(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                percentAdded = getBatteryLevel(context) - tempBatteryLevelWith

                capacityAdded = getCurrentCapacity(context) - tempCurrentCapacity

                if(capacityAdded < 0) capacityAdded /= -1

                context.getString(R.string.capacity_added, DecimalFormat("#.#").format(capacityAdded), "$percentAdded%")
            }

            else -> context.getString(R.string.capacity_added_last_charge,
                DecimalFormat("#.#").format(pref.getFloat(Preferences.CapacityAdded.prefKey, 0f).toDouble()),
                "${pref.getInt(Preferences.PercentAdded.prefKey, 0)}%")
        }
    }

    fun getVoltage(context: Context): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryIntent!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()

        if(!pref.getBoolean(Preferences.VoltageInMv.prefKey, false))
            if(voltage >= 1000 && voltage < 1000000) voltage /= 1000 else if(voltage >= 1000000) voltage /= 1000000

        return voltage
    }

    fun getResidualCapacity(context: Context, isCharging: Boolean = false): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(isCharging && batteryLevel < getBatteryLevel(context)) {

            batteryLevel = getBatteryLevel(context)

            residualCapacity = getCurrentCapacity(context) / if(batteryLevel > 1) (batteryLevel / 100.0) else 1.0
        }

        else if(isCharging && batteryLevel == 100) residualCapacity = getCurrentCapacity(context)

        else if(!isCharging) {

            residualCapacity = pref.getInt(Preferences.ResidualCapacity.prefKey, 0).toDouble()

            residualCapacity /= 1000
        }

        if(residualCapacity < 0) residualCapacity /= -1

        return context.getString(R.string.residual_capacity, DecimalFormat("#.#").format(residualCapacity),
            "${DecimalFormat("#.#").format((residualCapacity / pref.getInt(Preferences.DesignCapacity.prefKey, 0).toDouble()) * 100)}%")
    }

    fun getStatus(context: Context, extraStatus: Int): String {

        return when(extraStatus) {

            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.status, context.getString(R.string.discharging))
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.status, context.getString(R.string.not_charging))
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.status, context.getString(R.string.charging))
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.status, context.getString(R.string.full))
            BatteryManager.BATTERY_STATUS_UNKNOWN -> context.getString(R.string.status, context.getString(R.string.unknown))
            else -> "N/A"
        }
    }

    fun getPlugged(context: Context, extraPlugged: Int): String {

        return when(extraPlugged) {

            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.plugged, context.getString(R.string.plugged_ac))
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.plugged, context.getString(R.string.plugged_usb))
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.plugged, context.getString(R.string.plugged_wireless))
            else -> "N/A"
        }
    }
    
    fun getBatteryWear(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityDesign = pref.getInt(Preferences.DesignCapacity.prefKey, 0).toDouble()

        return context.getString(R.string.battery_wear,
            if(residualCapacity > 0 && residualCapacity < getDesignCapacity(context))
                "${DecimalFormat("#.#").format(100 - ((residualCapacity / capacityDesign) * 100))}%" else "0%",
            if (residualCapacity > 0 && residualCapacity < getDesignCapacity(context))
                DecimalFormat("#.#").format(capacityDesign - residualCapacity) else "0")
    }

    fun getChargingTime(context: Context, seconds: Int): String {

        val secondsTime = toSeconds(seconds)
        val minutes = toMinutes(seconds)
        val hours = toHours(seconds)

        var time = "$hours:$minutes:$secondsTime"

        return context.getString(R.string.charging_time,

            try {

                var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

                val hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

                if(hoursDate > hours) {

                    time = "${hours - 1}:$minutes:$secondsTime"

                    dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()
                }

                dateTime
            }

            catch (e: java.lang.IllegalArgumentException) { "${seconds}s" })
    }

    fun getLastChargeTime(context: Context): String {
        
        val secondsPref = PreferenceManager.getDefaultSharedPreferences(context).getInt(Preferences.LastChargeTime.prefKey, 0)

        val seconds = toSeconds(secondsPref)
        val minutes = toMinutes(secondsPref)
        val hours = toHours(secondsPref)

        var time = "$hours:$minutes:$seconds"

        return try {

            var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

            val hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

            if(hoursDate > hours) {

                time = "${hours - 1}:$minutes:$seconds"

                dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()
            }

            dateTime
        }

        catch (e: IllegalArgumentException) { "${secondsPref}s" }
    }
}