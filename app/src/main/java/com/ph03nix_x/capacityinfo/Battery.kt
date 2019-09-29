package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressWarnings("PrivateApi")
class Battery(var context: Context) {

    fun getDesignCapacity(): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(context)

        var capacity = (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()

        if(capacity >= 100000) capacity /= 1000

        return capacity
    }

    fun getBatteryLevel() = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    fun getChargingCurrent(): Int {

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if(chargingCurrent < 0) chargingCurrent /= -1

        if(chargingCurrent >= 10000) chargingCurrent /= 1000

        return chargingCurrent
    }

    fun getTemperature(): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toDouble()

        if(temp >= 100) temp /= 10

        var tempString = temp.toString()

        if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) tempString = toDecimalFormat((temp * 1.8) + 32)

        return tempString
    }

    fun getCurrentCapacity(): Double {

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

        if (currentCapacity < 0) currentCapacity /= -1

        if (currentCapacity >= 100000) currentCapacity /= 1000

        return currentCapacity
    }

    fun getVoltage(): Double {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()

        if(!pref.getBoolean(Preferences.VoltageInMv.prefKey, false))
            if(voltage >= 1000 && voltage < 1000000) voltage /= 1000 else if(voltage >= 1000000) voltage /= 1000000

        return voltage
    }

    fun getResidualCapacity(): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val residualCapacity = pref.getInt(Preferences.ChargeCounter.prefKey, 0).toDouble()

        return context.getString(R.string.residual_capacity, toDecimalFormat(residualCapacity), "${DecimalFormat("#.#").format(
            if (residualCapacity >= 100000) ((residualCapacity / 1000) / pref.getInt(
                Preferences.DesignCapacity.prefKey, 0).toDouble()) * 100

            else (residualCapacity / pref.getInt(Preferences.DesignCapacity.prefKey, 0).toDouble()) * 100)}%")
    }

    fun getStatus(extraStatus: Int): String {

        return when(extraStatus) {

            BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.status, context.getString(R.string.discharging))
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> context.getString(R.string.status, context.getString(R.string.not_charging))
            BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.status, context.getString(R.string.charging))
            BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.status, context.getString(R.string.full))
            BatteryManager.BATTERY_STATUS_UNKNOWN -> context.getString(R.string.status, context.getString(R.string.unknown))
            else -> "N/A"
        }
    }

    fun getPlugged(extraPlugged: Int): String {

        return when(extraPlugged) {

            BatteryManager.BATTERY_PLUGGED_AC -> context.getString(R.string.plugged, context.getString(R.string.plugged_ac))
            BatteryManager.BATTERY_PLUGGED_USB -> context.getString(R.string.plugged, context.getString(R.string.plugged_usb))
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> context.getString(R.string.plugged, context.getString(R.string.plugged_wireless))
            else -> "N/A"
        }
    }
    
    fun getBatteryWear(): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityDesign = pref.getInt(Preferences.DesignCapacity.prefKey, 0).toDouble()

        var capacity = pref.getInt(Preferences.ChargeCounter.prefKey, 0).toDouble()

        if(capacity >= 100000) capacity /= 1000

        return context.getString(R.string.battery_wear,
            if(capacity > 0) "${DecimalFormat("#.#").format(100 - ((capacity / capacityDesign) * 100))}%"  else "0%")
    }

    fun toDecimalFormat(number: Double) = if(number >= 100000) DecimalFormat("#.#").format(number / 1000) else DecimalFormat("#.#").format(number)

    fun getChargingTime(seconds: Double): String {

        val secondsTime = TimeSpan.toSeconds(seconds)
        val minutes = TimeSpan.toMinutes(seconds)
        val hours = TimeSpan.toHours(seconds)

        val time = "$hours:$minutes:$secondsTime"

        return context.getString(R.string.charging_time,
            DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString())
    }

    fun getLastChargeTime(): String { 
        
        val secondsPref = PreferenceManager.getDefaultSharedPreferences(context).getInt(Preferences.LastChargeTime.prefKey, 0).toDouble()

        val seconds = TimeSpan.toSeconds(secondsPref)
        val minutes = TimeSpan.toMinutes(secondsPref)
        val hours = TimeSpan.toHours(secondsPref)

        val time = "$hours:$minutes:$seconds"

        return DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()
    }
}