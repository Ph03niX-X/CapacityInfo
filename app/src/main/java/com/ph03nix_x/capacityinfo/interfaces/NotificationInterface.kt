package com.ph03nix_x.capacityinfo.interfaces

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import java.lang.RuntimeException
import java.text.DecimalFormat

interface NotificationInterface : BatteryInfoInterface {

    companion object {

        private lateinit var notificationBuilder: NotificationCompat.Builder
        private lateinit var channelId: String
        private const val notificationId = 101
    }

    fun createNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(context) else ""

        val openApp = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder = NotificationCompat.Builder(context, channelId).apply {

            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.service_small_icon)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                color = ContextCompat.getColor(context.applicationContext,
                    if(isSystemDarkMode(context.resources.configuration)) R.color.red else R.color.blue)

            setContentIntent(openApp)
            setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, false))
        }

        (context as CapacityInfoService).startForeground(notificationId, notificationBuilder.build())
    }

    fun updateNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationBuilder.apply {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            color = ContextCompat.getColor(context.applicationContext, if(isSystemDarkMode(context.resources.configuration)) R.color.red else R.color.blue)

            setStyle(NotificationCompat.BigTextStyle().bigText(getNotificationMessage(context)))

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, false))
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context): String {
        val channelId = "service_channel"
        val channelName = context.getString(R.string.service)
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        chan.setShowBadge(false)
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun getNotificationMessage(context: Context): String {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> getBatteryStatusCharging(context)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getBatteryStatusNotCharging(context)

            BatteryManager.BATTERY_STATUS_FULL -> getBatteryStatusFull(context)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> getBatteryStatusDischarging(context)

            BatteryManager.BATTERY_STATUS_UNKNOWN -> getBatteryStatusUnknown(context)

            else -> "N/A"
        }
    }

    private fun getBatteryStatusCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val charging = context.getString(
            R.string.status, context.getString(
                R.string.charging
            ))
        val batteryLevel = context.getString(
            R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  {
            R.string.unknown
        })

        val plugged = getPlugged(context, batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val chargeCurrent = context.getString(R.string.charge_current, getChargeDischargeCurrent(context).toString())
        val maxChargeCurrent = context.getString(R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)
        val averageChargeCurrent = context.getString(R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)
        val minChargeCurrent = context.getString(R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                        "\n$capacityAdded\n${getResidualCapacity(context, true)}\n${getBatteryWear(context)}\n$chargeCurrent\n$maxChargeCurrent" +
                        "\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
            else "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                    "\n${getResidualCapacity(context, true)}\n${getBatteryWear(context)}\n$chargeCurrent\n$maxChargeCurrent\n$averageChargeCurrent" +
                    "\n$minChargeCurrent\n$temperature\n$voltage"

        else "$charging\n$batteryLevel\n$plugged\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$chargeCurrent\n$maxChargeCurrent" +
                "\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
    }

    private fun getBatteryStatusNotCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val notCharging = context.getString(
            R.string.status, context.getString(
                R.string.not_charging
            ))
        val batteryLevel = context.getString(
            R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  {
            R.string.unknown
        })

        val numberOfCharges = context.getString(R.string.number_of_charges,
            pref.getLong(NUMBER_OF_CHARGES, 0))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current, getChargeDischargeCurrent(context).toString())
        val maxChargeCurrent = context.getString(R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)
        val averageChargeCurrent = context.getString(R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)
        val minChargeCurrent = context.getString(R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))
        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION, true))
                "$notCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                        "\n$capacityAdded\n$dischargeCurrent\n$maxChargeCurrent\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
            else "$notCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                    "\n$dischargeCurrent\n$maxChargeCurrent\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"

        else "$notCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$dischargeCurrent\n$maxChargeCurrent" +
                "\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
    }

    private fun getBatteryStatusFull(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val fullCharging = context.getString(
            R.string.status, context.getString(
                R.string.full
            ))
        val batteryLevel = context.getString(
            R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  {
            R.string.unknown
        })

        val numberOfCharges = context.getString(
            R.string.number_of_charges, pref.getLong(
                NUMBER_OF_CHARGES, 0))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current, getChargeDischargeCurrent(context).toString())
        val maxChargeCurrent = context.getString(R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)
        val averageChargeCurrent = context.getString(R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)
        val minChargeCurrent = context.getString(R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(pref.getBoolean(IS_SUPPORTED, true)) {

            if(getCurrentCapacity(context) > 0)
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION, true))
                    "$fullCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                            "\n$capacityAdded\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargeCurrent\n$maxChargeCurrent" +
                            "\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
                else "$fullCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$currentCapacity" +
                        "\n${getResidualCapacity(context)}\n${getBatteryWear(context)}\n$dischargeCurrent\n$maxChargeCurrent\n$averageChargeCurrent" +
                        "\n$minChargeCurrent\n$temperature\n$voltage"

            else "$fullCharging\n$batteryLevel\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n${getResidualCapacity(context)}" +
                    "\n${getBatteryWear(context)}\n$dischargeCurrent\n$maxChargeCurrent\n$averageChargeCurrent\n$minChargeCurrent\n$temperature\n$voltage"
        }

        else "$fullCharging\n$batteryLevel\n$numberOfCharges\n${getChargingTime(context, (context as CapacityInfoService).seconds)}\n$dischargeCurrent" +
                "\n$maxChargeCurrent\n$averageChargeCurrent\n$minChargeCurrent\n$temperature"
    }

    private fun getBatteryStatusDischarging(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryLevelWith = "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%"
        val batteryLevelTo = "${pref.getInt(BATTERY_LEVEL_TO, 0)}%"

        val discharging = context.getString(
            R.string.status, context.getString(
                R.string.discharging
            ))
        val batteryLevel = context.getString(
            R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  {
            R.string.unknown
        })

        val numberOfCharges = context.getString(
            R.string.number_of_charges, pref.getLong(
                NUMBER_OF_CHARGES, 0))
        val lastChargingTime = context.getString(R.string.last_charge_time, getLastChargeTime(context), batteryLevelWith, batteryLevelTo)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current, getChargeDischargeCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0) {

            if(pref.getInt(LAST_CHARGE_TIME, 0) > 0 && pref.getBoolean(
                    IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION, true))

                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION, true))
                    "$discharging\n$batteryLevel\n$numberOfCharges\n$lastChargingTime\n$currentCapacity\n$capacityAdded\n${getResidualCapacity(context)}" +
                            "\n${getBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$numberOfCharges\n$lastChargingTime\n$currentCapacity\n${getResidualCapacity(context)}" +
                        "\n${getBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"

            else {

                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION, true))
                    "$discharging\n$batteryLevel\n$numberOfCharges\n$currentCapacity\n$capacityAdded\n${getResidualCapacity(context)}\n${getBatteryWear(context)}" +
                            "\n$dischargeCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$numberOfCharges\n$currentCapacity\n${getResidualCapacity(context)}\n${getBatteryWear(context)}" +
                        "\n$dischargeCurrent\n$temperature\n$voltage"
            }
        }

        else {

            if(pref.getInt(LAST_CHARGE_TIME, 0) > 0 && pref.getBoolean(
                    IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION, true))
                "$discharging\n$batteryLevel\n$numberOfCharges\n$lastChargingTime\n$dischargeCurrent\n$temperature\n$voltage"

            else "$discharging\n$batteryLevel\n$numberOfCharges\n$dischargeCurrent\n$temperature\n$voltage"
        }
    }

    private fun getBatteryStatusUnknown(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val discharging = context.getString(
            R.string.status, context.getString(
                R.string.unknown
            ))
        val batteryLevel = context.getString(
            R.string.battery_level, try {
            "${getBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  {
            R.string.unknown
        })

        val numberOfCharges = context.getString(
            R.string.number_of_charges, pref.getLong(
                NUMBER_OF_CHARGES, 0))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))
        val capacityAdded = getCapacityAdded(context)
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
        else R.string.temperature_celsius, getTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
            DecimalFormat("#.#").format(getVoltage(context)))

        return if(getCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_NOTIFICATION, true))
                "$discharging\n$batteryLevel\n$numberOfCharges\n$currentCapacity\n$capacityAdded\n$temperature\n$voltage"
            else "$discharging\n$batteryLevel\n$numberOfCharges\n$currentCapacity\n$temperature\n$voltage"

        else "$discharging\n$batteryLevel\n$numberOfCharges\n$temperature\n$voltage"
    }
}