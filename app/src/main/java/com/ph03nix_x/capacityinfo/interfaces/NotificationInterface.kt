package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
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
import com.ph03nix_x.capacityinfo.services.StopCapacityInfoService
import com.ph03nix_x.capacityinfo.utils.Constants.FULLY_CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utils.Constants.CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utils.Constants.DISCHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utils.Constants.SERVICE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BYPASS_DND
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_STOP_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import java.lang.RuntimeException
import java.text.DecimalFormat

interface NotificationInterface : BatteryInfoInterface {

    companion object {

        const val NOTIFICATION_SERVICE_ID = 101
        const val NOTIFICATION_BATTERY_STATUS_ID = 102
        var isNotifyBatteryFullyCharged = true
        var isNotifyBatteryCharged = true
        var isNotifyBatteryDischarged = true
        var notificationBuilder: NotificationCompat.Builder? = null
        var notificationManager: NotificationManager? = null
        private lateinit var channelId: String
        private lateinit var stopService: PendingIntent
    }

    @SuppressLint("RestrictedApi")
    fun onCreateServiceNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, SERVICE_CHANNEL_ID) else ""

        val openApp = PendingIntent.getActivity(context, 0, Intent(context,
            MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        stopService = PendingIntent.getService(context, 1, Intent(context,
            StopCapacityInfoService::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder = NotificationCompat.Builder(context, channelId).apply {

            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_service_small_icon)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                color = ContextCompat.getColor(context, if(isSystemDarkMode(
                        context.resources.configuration)) R.color.red else R.color.blue)

            setContentIntent(openApp)

            if(pref.getBoolean(IS_SHOW_STOP_SERVICE, false) && mActions.isEmpty())
                addAction(0, context.getString(R.string.stop_service), stopService)

            setContentText(context.getString(R.string.current_capacity,
                DecimalFormat("#.#").format(onGetCurrentCapacity(context))))

            setStyle(NotificationCompat.BigTextStyle().bigText(onGetNotificationMessage(context)))

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, false))
        }

        (context as? CapacityInfoService)?.startForeground(NOTIFICATION_SERVICE_ID,
            notificationBuilder?.build())
    }
    
    fun onNotifyBatteryFullyCharged(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryFullyCharged = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, FULLY_CHARGED_CHANNEL_ID) else ""

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, true))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)
            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_battery_is_fully_charged_24dp)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                color = ContextCompat.getColor(context, R.color.green)

            setContentTitle(context.getString(R.string.battery_status_information))

            setContentText(context.getString(R.string.battery_is_fully_charged))

            setShowWhen(true)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.battery_is_fully_charged}"))
        }

        notificationManager?.notify(NOTIFICATION_BATTERY_STATUS_ID, notificationBuilder.build())
    }

    fun onNotifyBatteryCharged(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryCharged = false

        val batteryLevel = onGetBatteryLevel(context)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, CHARGED_CHANNEL_ID) else ""

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, true))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)
            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(when(batteryLevel) {

                in 0..29 -> R.drawable.ic_battery_is_charged_20_24dp
                in 30..49 -> R.drawable.ic_battery_is_charged_30_24dp
                in 50..59 -> R.drawable.ic_battery_is_charged_50_24dp
                in 60..79 -> R.drawable.ic_battery_is_charged_60_24dp
                in 80..89 -> R.drawable.ic_battery_is_charged_80_24dp
                in 90..95 -> R.drawable.ic_battery_is_charged_90_24dp
                else -> R.drawable.ic_battery_is_fully_charged_24dp
            })

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                color = ContextCompat.getColor(context, R.color.green)

            setContentTitle(context.getString(R.string.battery_status_information))

            setContentText("${context.getString(R.string.battery_is_charged_notification,
                batteryLevel)}%")

            setShowWhen(true)

            setLights(Color.GREEN, 1500, 500)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.battery_is_charged}"))
        }

        notificationManager?.notify(NOTIFICATION_BATTERY_STATUS_ID, notificationBuilder.build())
    }

    fun onNotifyBatteryDischarged(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryDischarged = false

        val batteryLevel = onGetBatteryLevel(context) ?: 0

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, DISCHARGED_CHANNEL_ID) else ""

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, true))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)
            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(when(batteryLevel) {

                in 0..9 -> R.drawable.ic_battery_discharged_9_24dp
                in 10..29 -> R.drawable.ic_battery_is_discharged_20_24dp
                in 30..49 -> R.drawable.ic_battery_is_discharged_30_24dp
                in 50..59 -> R.drawable.ic_battery_is_discharged_50_24dp
                in 60..79 -> R.drawable.ic_battery_is_discharged_60_24dp
                in 80..89 -> R.drawable.ic_battery_is_discharged_80_24dp
                in 90..99 -> R.drawable.ic_battery_is_discharged_90_24dp
                else -> R.drawable.ic_battery_discharged_9_24dp
            })

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                color = ContextCompat.getColor(context, R.color.red)

            setContentTitle(context.getString(R.string.battery_status_information))

            setContentText("${context.getString(R.string.battery_is_discharged_notification,
                batteryLevel)}%")

            setShowWhen(true)

            setLights(Color.RED, 1000, 500)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.battery_is_discharged}"))
        }

        notificationManager?.notify(NOTIFICATION_BATTERY_STATUS_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    fun onUpdateServiceNotification(context: Context) {

        try {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            notificationBuilder?.apply {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    color = ContextCompat.getColor(context.applicationContext, if(
                        isSystemDarkMode(context.resources.configuration)) R.color.red
                    else R.color.blue)

                setContentText(context.getString(R.string.current_capacity,
                    DecimalFormat("#.#").format(onGetCurrentCapacity(context))))

                setStyle(NotificationCompat.BigTextStyle().bigText(onGetNotificationMessage(
                    context)))

                if(pref.getBoolean(IS_SHOW_STOP_SERVICE, false) && mActions.isEmpty())
                    addAction(0, context.getString(R.string.stop_service), stopService)
                else if(!pref.getBoolean(IS_SHOW_STOP_SERVICE, false) &&
                    mActions.isNotEmpty()) mActions.clear()

                setShowWhen(pref.getBoolean(IS_SERVICE_TIME, false))
            }

            notificationManager?.notify(NOTIFICATION_SERVICE_ID, notificationBuilder?.build())
        }
        catch(e: RuntimeException) { return }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateNotificationChannel(context: Context, notificationChannelId: String): String {

        val notificationService =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        val soundAttributes = AudioAttributes.Builder().apply {

            setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)

            setUsage(AudioAttributes.USAGE_NOTIFICATION)
        }

        when (notificationChannelId) {

            SERVICE_CHANNEL_ID -> {

                val channelName = context.getString(R.string.service)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {

                    setShowBadge(false)
                })
            }

            FULLY_CHARGED_CHANNEL_ID -> {

                val channelName = context.getString(R.string.fully_charged)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.battery_is_fully_charged}"),
                        soundAttributes.build())
                })
            }

            CHARGED_CHANNEL_ID -> {

                val channelName = context.getString(R.string.charged)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    enableLights(true)

                    lightColor = Color.GREEN

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.battery_is_charged}"),
                        soundAttributes.build())
                })
            }

            DISCHARGED_CHANNEL_ID -> {

                val channelName = context.getString(R.string.discharged)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    enableLights(true)

                    lightColor = Color.RED

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.battery_is_discharged}"),
                        soundAttributes.build())
                })
            }
        }

        return notificationChannelId
    }

    private fun onGetNotificationMessage(context: Context): String {

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        return when(batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN)) {

            BatteryManager.BATTERY_STATUS_CHARGING -> onGetBatteryStatusCharging(context)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> onGetBatteryStatusNotCharging(context)

            BatteryManager.BATTERY_STATUS_FULL -> onGetBatteryStatusFull(context)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> onGetBatteryStatusDischarging(context)

            else -> onGetBatteryStatusUnknown(context)
        }
    }

    private fun onGetBatteryStatusCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val charging = context.getString(R.string.status, context.getString(
            R.string.charging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${onGetBatteryLevel(context)}%" }
        catch (e: RuntimeException)  { R.string.unknown })

        val sourceOfPower = onGetSourceOfPower(context, batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_PLUGGED, -1) ?: -1)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat(
            "#.#").format(onGetCurrentCapacity(context)))
        val capacityAdded = onGetCapacityAdded(context)
        val chargeCurrent = context.getString(R.string.charge_current,
            onGetChargeDischargeCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                false)) R.string.temperature_fahrenheit else R.string.temperature_celsius,
            onGetTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
            R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
            onGetVoltage(context)))

        return if(onGetCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                "$charging\n$batteryLevel\n$sourceOfPower\n${onGetChargingTime(context, (context
                        as? CapacityInfoService)?.seconds ?: 0)}\n$currentCapacity" +
                        "\n$capacityAdded\n${onGetResidualCapacity(context, true)}\n${
                        onGetBatteryWear(context)}\n$chargeCurrent\n$temperature\n$voltage"
            else "$charging\n$batteryLevel\n$sourceOfPower\n${onGetChargingTime(context, (context
                    as? CapacityInfoService)?.seconds ?: 0)}\n$currentCapacity\n${
            onGetResidualCapacity(context, true)}\n${onGetBatteryWear(context)}\n" +
                    "$chargeCurrent\n$temperature\n$voltage"

        else "$charging\n$batteryLevel\n$sourceOfPower\n${onGetChargingTime(context, (context
                as? CapacityInfoService)?.seconds ?: 0)}\n$chargeCurrent\n$temperature\n$voltage"
    }

    private fun onGetBatteryStatusNotCharging(context: Context): String {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val notCharging = context.getString(R.string.status, context.getString(
            R.string.not_charging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${onGetBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val numberOfCycles = context.getString(R.string.number_of_cycles,
            DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat(
            "#.#").format(onGetCurrentCapacity(context)))
        val capacityAdded = onGetCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current,
            onGetChargeDischargeCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                false)) R.string.temperature_fahrenheit else R.string.temperature_celsius,
            onGetTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
            R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
            onGetVoltage(context)))

        return if(onGetCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                "$notCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
                    (context as? CapacityInfoService)?.seconds ?: 0)}\n$currentCapacity" +
                        "\n$capacityAdded\n$dischargeCurrent\n$temperature\n$voltage"
            else "$notCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
                (context as? CapacityInfoService)?.seconds ?: 0)}\n$currentCapacity" +
                    "\n$dischargeCurrent\n$temperature\n$voltage"

        else "$notCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
            (context as? CapacityInfoService)?.seconds ?: 0)}\n$dischargeCurrent\n" +
                "$temperature\n$voltage"
    }

    private fun onGetBatteryStatusFull(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val fullCharging = context.getString(R.string.status, context.getString(
            R.string.full))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${onGetBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val numberOfCycles = context.getString(R.string.number_of_cycles,
            DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat(
            "#.#").format(onGetCurrentCapacity(context)))
        val capacityAdded = onGetCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current,
            onGetChargeDischargeCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                false)) R.string.temperature_fahrenheit else R.string.temperature_celsius,
            onGetTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
            R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
            onGetVoltage(context)))

        return if(pref.getBoolean(IS_SUPPORTED, true)) {

            if(onGetCurrentCapacity(context) > 0)
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                    "$fullCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
                        (context as? CapacityInfoService)?.seconds ?: 0)}\n" +
                            "$currentCapacity\n$capacityAdded\n${onGetResidualCapacity(context)}\n" +
                            "${onGetBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"
                else "$fullCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
                    (context as? CapacityInfoService)?.seconds ?: 0)}\n$currentCapacity" +
                        "\n${onGetResidualCapacity(context)}\n${onGetBatteryWear(context)}\n" +
                        "$dischargeCurrent\n$temperature\n$voltage"

            else "$fullCharging\n$batteryLevel\n${onGetChargingTime(context, (context
                    as? CapacityInfoService)?.seconds ?: 0)}\n${onGetResidualCapacity(context)}" +
                    "\n${onGetBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"
        }

        else "$fullCharging\n$batteryLevel\n$numberOfCycles\n${onGetChargingTime(context, 
            (context as? CapacityInfoService)?.seconds ?: 0)}\n$dischargeCurrent" +
                "\n$temperature"
    }

    private fun onGetBatteryStatusDischarging(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryLevelWith = "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%"
        val batteryLevelTo = "${pref.getInt(BATTERY_LEVEL_TO, 0)}%"

        val discharging = context.getString(R.string.status, context.getString(
            R.string.discharging))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${onGetBatteryLevel(context)}%"
        }
        catch (e: RuntimeException)  { R.string.unknown })

        val numberOfCycles = context.getString(R.string.number_of_cycles,
            DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))
        val lastChargingTime = context.getString(R.string.last_charge_time,
            onGetLastChargeTime(context), batteryLevelWith, batteryLevelTo)
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat(
            "#.#").format(onGetCurrentCapacity(context)))
        val capacityAdded = onGetCapacityAdded(context)
        val dischargeCurrent = context.getString(R.string.discharge_current,
            onGetChargeDischargeCurrent(context).toString())
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                false)) R.string.temperature_fahrenheit else R.string.temperature_celsius,
            onGetTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
            R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
            onGetVoltage(context)))

        return if(onGetCurrentCapacity(context) > 0) {

            if(pref.getInt(LAST_CHARGE_TIME, 0) > 0 && pref.getBoolean(
                    IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION, true))

                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                    "$discharging\n$batteryLevel\n$numberOfCycles\n$lastChargingTime\n" +
                            "$currentCapacity\n$capacityAdded\n${onGetResidualCapacity(context)}" +
                            "\n${onGetBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$numberOfCycles\n$lastChargingTime\n" +
                        "$currentCapacity\n${onGetResidualCapacity(context)}" +
                        "\n${onGetBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"

            else {

                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                    "$discharging\n$batteryLevel\n$numberOfCycles\n$currentCapacity\n" +
                            "$capacityAdded\n${onGetResidualCapacity(context)}\n" +
                            "${onGetBatteryWear(context)}\n$dischargeCurrent\n$temperature\n$voltage"
                else "$discharging\n$batteryLevel\n$numberOfCycles\n$currentCapacity\n" +
                        "${onGetResidualCapacity(context)}\n${onGetBatteryWear(context)}" +
                        "\n$dischargeCurrent\n$temperature\n$voltage"
            }
        }

        else {

            if(pref.getInt(LAST_CHARGE_TIME, 0) > 0 && pref.getBoolean(
                    IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION, true))
                "$discharging\n$batteryLevel\n$numberOfCycles\n$lastChargingTime\n" +
                        "$dischargeCurrent\n$temperature\n$voltage"

            else "$discharging\n$batteryLevel\n$numberOfCycles\n$dischargeCurrent\n$temperature" +
                    "\n$voltage"
        }
    }

    private fun onGetBatteryStatusUnknown(context: Context): String {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val discharging = context.getString(R.string.status, context.getString(
                R.string.unknown
            ))
        val batteryLevel = context.getString(R.string.battery_level, try {
            "${onGetBatteryLevel(context)}%" }
        catch (e: RuntimeException)  { R.string.unknown })

        val numberOfCycles = context.getString(R.string.number_of_cycles,
            DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))
        val currentCapacity = context.getString(R.string.current_capacity, DecimalFormat(
            "#.#").format(onGetCurrentCapacity(context)))
        val capacityAdded = onGetCapacityAdded(context)
        val temperature = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                false)) R.string.temperature_fahrenheit else R.string.temperature_celsius,
            onGetTemperature(context))

        val voltage = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
            R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
            onGetVoltage(context)))

        return if(onGetCurrentCapacity(context) > 0)
            if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, true))
                "$discharging\n$batteryLevel\n$numberOfCycles\n$currentCapacity\n$capacityAdded" +
                        "\n$temperature\n$voltage" else "$discharging\n$batteryLevel\n" +
                    "$numberOfCycles\n$currentCapacity\n$temperature\n$voltage"

        else "$discharging\n$batteryLevel\n$numberOfCycles\n$temperature\n$voltage"
    }
}