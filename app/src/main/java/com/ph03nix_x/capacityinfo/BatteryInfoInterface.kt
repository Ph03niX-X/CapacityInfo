package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Util.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.Util.Companion.hoursDefault
import com.ph03nix_x.capacityinfo.Util.Companion.percentAdded
import com.ph03nix_x.capacityinfo.Util.Companion.tempBatteryLevel
import com.ph03nix_x.capacityinfo.Util.Companion.tempCurrentCapacity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressWarnings("PrivateApi")
interface BatteryInfoInterface {

    fun getDesignCapacity(context: Context): Int {

        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(context)

        return (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()
    }

    fun getBatteryLevel(context: Context) = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    fun getChargingCurrent(context: Context): Int {

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if(chargingCurrent < 0) chargingCurrent /= -1

        return chargingCurrent / 1000
    }

    fun getTemperature(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var temp = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toDouble()

        temp /= 10

        var tempString = temp.toString()

        if(pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false))
            tempString = DecimalFormat("#.#").format((temp * 1.8) + 32)

        return tempString
    }

    fun getCurrentCapacity(context: Context): Double {

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        var currentCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

        if (currentCapacity < 0) currentCapacity /= -1

        return currentCapacity / 1000
    }

    fun getCapacityAdded(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            return when(intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> {

                percentAdded = getBatteryLevel(context) - tempBatteryLevel

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

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        var voltage = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()

        if(!pref.getBoolean(Preferences.VoltageInMv.prefKey, false))
            if(voltage >= 1000 && voltage < 1000000) voltage /= 1000 else if(voltage >= 1000000) voltage /= 1000000

        return voltage
    }

    fun getResidualCapacity(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        var residualCapacity = pref.getInt(Preferences.ChargeCounter.prefKey, 0).toDouble()

        if(residualCapacity < 0) residualCapacity /= -1

        residualCapacity /= 1000

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

        var capacity = pref.getInt(Preferences.ChargeCounter.prefKey, 0).toDouble()

        if(capacity < 0) capacity /= -1

        capacity /= 1000

        return context.getString(R.string.battery_wear,
            if(capacity > 0) "${DecimalFormat("#.#").format(100 - ((capacity / capacityDesign) * 100))}%"  else "0%")
    }

    fun getChargingTime(context: Context, seconds: Double): String {

        val secondsTime = TimeSpan.toSeconds(seconds)
        val minutes = TimeSpan.toMinutes(seconds)
        val hours = TimeSpan.toHours(seconds)

        var time = "$hours:$minutes:$secondsTime"

        var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

        var hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

        if(hoursDate > hoursDefault) {

            time = "${hours - 1}:$minutes:$secondsTime"

            dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

            hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

            if(hoursDefault != hoursDate) hoursDefault = hoursDate
        }

        return context.getString(R.string.charging_time,

            try { dateTime }

            catch (e: IllegalArgumentException) { seconds.toString() })
    }

    fun getLastChargeTime(context: Context): String {
        
        val secondsPref = PreferenceManager.getDefaultSharedPreferences(context).getInt(Preferences.LastChargeTime.prefKey, 0).toDouble()

        val seconds = TimeSpan.toSeconds(secondsPref)
        val minutes = TimeSpan.toMinutes(secondsPref)
        val hours = TimeSpan.toHours(secondsPref)

        var time = "$hours:$minutes:$seconds"

        var dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

        var hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

        if(hoursDate > hoursDefault) {

            time = "${hours - 1}:$minutes:$seconds"

            dateTime = DateFormat.format("HH:mm:ss", Date(SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).parse(time)!!.toString())).toString()

            hoursDate = dateTime.removeRange(2, dateTime.count()).toInt()

            if(hoursDefault != hoursDate) hoursDefault = hoursDate
        }

        return try { dateTime }

        catch (e: IllegalArgumentException) { secondsPref.toString() }
    }
}