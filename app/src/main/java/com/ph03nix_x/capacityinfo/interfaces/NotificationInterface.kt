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
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.StopCapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.Constants.FULLY_CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.DISCHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.OVERHEAT_OVERCOOL_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BYPASS_DND
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_STOP_SERVICE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_IN_MV
import java.lang.RuntimeException
import java.text.DecimalFormat

interface NotificationInterface : BatteryInfoInterface {

    companion object {

        private lateinit var channelId: String
        private lateinit var stopService: PendingIntent

        const val NOTIFICATION_SERVICE_ID = 101
        const val NOTIFICATION_BATTERY_STATUS_ID = 102
        const val NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID = 103

        var notificationBuilder: NotificationCompat.Builder? = null
        var notificationManager: NotificationManager? = null
        var isNotifyOverheatOvercool = true
        var isNotifyBatteryFullyCharged = true
        var isNotifyBatteryCharged = true
        var isNotifyBatteryDischarged = true
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

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        notificationBuilder = NotificationCompat.Builder(context, channelId).apply {

            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_service_small_icon)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = ContextCompat.getColor(context, if(isSystemDarkMode(
                        context.resources.configuration)) R.color.red else R.color.blue)

            setContentIntent(openApp)

            if(pref.getBoolean(IS_SHOW_STOP_SERVICE, context.resources.getBoolean(
                    R.bool.is_show_stop_service)) && mActions.isEmpty())
                addAction(0, context.getString(R.string.stop_service), stopService)

            val remoteViewsServiceContent = RemoteViews(context.packageName,
                R.layout.notification_content)

            remoteViewsServiceContent.setTextViewText(R.id.notification_content_text,
                if(onGetCurrentCapacity(context) > 0.0) context.getString(
                    R.string.current_capacity, DecimalFormat("#.#").format(
                        onGetCurrentCapacity(context))) else "${context.getString(
                    R.string.battery_level, (onGetBatteryLevel(context) ?: 0).toString())}%")

            setCustomContentView(remoteViewsServiceContent)

            val isShowBigContent = pref.getBoolean(IS_SHOW_EXPANDED_NOTIFICATION, context
                .resources.getBoolean(R.bool.is_show_expanded_notification))

            if(isShowBigContent) {

                val remoteViewsServiceBigContent = RemoteViews(context.packageName,
                    R.layout.service_notification_big_content)

                remoteViewsServiceBigContent.setViewVisibility(R.id.voltage_service_notification,
                    if(mActions.isNullOrEmpty()) View.VISIBLE else View.GONE)

                onGetNotificationMessage(context, status, remoteViewsServiceBigContent)

                setCustomBigContentView(remoteViewsServiceBigContent)
            }

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                R.bool.is_service_time)))
        }

        (context as? CapacityInfoService)?.startForeground(NOTIFICATION_SERVICE_ID,
            notificationBuilder?.build())
    }

    @SuppressLint("RestrictedApi")
    fun onUpdateServiceNotification(context: Context) {

        try {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

            batteryIntent = context.registerReceiver(null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED))

            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

            notificationBuilder?.apply {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    color = ContextCompat.getColor(context.applicationContext, if(
                        isSystemDarkMode(context.resources.configuration)) R.color.red
                    else R.color.blue)

                if(pref.getBoolean(IS_SHOW_STOP_SERVICE, context.resources.getBoolean(
                        R.bool.is_show_stop_service)) && mActions.isEmpty())
                    addAction(0, context.getString(R.string.stop_service), stopService)
                else if(!pref.getBoolean(IS_SHOW_STOP_SERVICE, context.resources.getBoolean(
                        R.bool.is_show_stop_service)) &&
                    mActions.isNotEmpty()) mActions.clear()

                val remoteViewsServiceContent = RemoteViews(context.packageName,
                    R.layout.notification_content)

                remoteViewsServiceContent.setTextViewText(R.id.notification_content_text,
                    if(onGetCurrentCapacity(context) > 0.0) context.getString(
                        R.string.current_capacity, DecimalFormat("#.#").format(
                            onGetCurrentCapacity(context))) else "${context.getString(
                        R.string.battery_level, (onGetBatteryLevel(context) ?: 0).toString())}%")

                setCustomContentView(remoteViewsServiceContent)

                val isShowBigContent = pref.getBoolean(IS_SHOW_EXPANDED_NOTIFICATION,
                    context.resources.getBoolean(R.bool.is_show_expanded_notification))

                if(isShowBigContent) {

                    val remoteViewsServiceBigContent = RemoteViews(context.packageName,
                        R.layout.service_notification_big_content)

                    remoteViewsServiceBigContent.setViewVisibility(R.id
                        .voltage_service_notification, if(mActions.isNullOrEmpty()) View.VISIBLE
                    else View.GONE)

                    onGetNotificationMessage(context, status, remoteViewsServiceBigContent)

                    setCustomBigContentView(remoteViewsServiceBigContent)
                }

                setStyle(NotificationCompat.DecoratedCustomViewStyle())

                setShowWhen(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                    R.bool.is_service_time)))
            }

            notificationManager?.notify(NOTIFICATION_SERVICE_ID, notificationBuilder?.build())
        }
        catch(e: RuntimeException) {

            Toast.makeText(context, e.message ?: e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    fun onNotifyOverheatOvercool(context: Context, temperature: Double) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyOverheatOvercool = false

        val temperatureString = onGetTemperature(context)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, OVERHEAT_OVERCOOL_CHANNEL_ID) else ""

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        if(temperature >= 45.0)
            remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.battery_overheating_fahrenheit
                else R.string.battery_overheating_celsius, temperatureString))

        else
            remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.battery_overcooling_fahrenheit
                else R.string.battery_overcooling_celsius, temperatureString))

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)
            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_overheat_overcool_24)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = ContextCompat.getColor(context, R.color.overheat_overcool)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(true)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.overheat_overcool}"))
        }

        notificationManager?.notify(NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID,
            notificationBuilder.build())
    }
    
    fun onNotifyBatteryFullyCharged(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryFullyCharged = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            onCreateNotificationChannel(context, FULLY_CHARGED_CHANNEL_ID) else ""

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
            R.string.battery_is_fully_charged))

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)
            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_battery_is_fully_charged_24dp)
            
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = ContextCompat.getColor(context, R.color.battery_charged)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

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

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text,
            "${context.getString(R.string.battery_is_charged_notification, batteryLevel)}%")

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = ContextCompat.getColor(context, R.color.battery_charged)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

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

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text,
            "${context.getString(R.string.battery_is_discharged_notification,
                batteryLevel)}%")

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
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

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                color = ContextCompat.getColor(context, R.color.battery_discharged)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(true)

            setLights(Color.RED, 1000, 500)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.battery_is_discharged}"))
        }

        notificationManager?.notify(NOTIFICATION_BATTERY_STATUS_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreateNotificationChannel(context: Context, notificationChannelId: String):
            String {

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

            OVERHEAT_OVERCOOL_CHANNEL_ID -> {

                val channelName = context.getString(R.string.overheat_overcool)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.overheat_overcool}"),
                        soundAttributes.build())
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

    private fun onGetNotificationMessage(context: Context, status: Int?, remoteViews: RemoteViews) {

        when(status) {

            BatteryManager.BATTERY_STATUS_CHARGING -> onGetBatteryStatusCharging(context,
                batteryIntent, remoteViews)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> onGetBatteryStatusNotCharging(context,
            remoteViews)

            BatteryManager.BATTERY_STATUS_FULL -> onGetBatteryStatusFull(context, remoteViews)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> onGetBatteryStatusDischarging(context,
                remoteViews)

            else -> onGetBatteryStatusUnknown(context, remoteViews)
        }
    }

    private fun onGetBatteryStatusCharging(context: Context, batteryIntent: Intent?,
                                           remoteViews: RemoteViews) {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.number_of_cycles_service_notification, View.GONE)

            setViewVisibility(R.id.last_charge_time_service_notification, View.GONE)

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.source_of_power_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.capacity_added_service_notification,
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_capacity_added_in_notification))
                    && onGetCurrentCapacity(context) > 0.0) View.VISIBLE
                else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.charging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${onGetBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.charging_time_service_notification, onGetChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.source_of_power_service_notification, onGetSourceOfPower(context,
                batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                    -1) ?: -1))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(onGetCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, onGetCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                onGetResidualCapacity(context, true))

            setTextViewText(R.id.battery_wear_service_notification, onGetBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.charge_current, onGetChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.temperature_fahrenheit else
                    R.string.temperature_celsius, onGetTemperature(context)))

            setTextViewText(R.id.voltage_service_notification, context.getString(
                if(pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(
                        R.bool.voltage_in_mv))) R.string.voltage_mv else R.string.voltage,
                DecimalFormat("#.#").format(onGetVoltage(context))))
        }
    }

    private fun onGetBatteryStatusNotCharging(context: Context, remoteViews: RemoteViews) {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        
        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.last_charge_time_service_notification, View.GONE)

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.capacity_added_service_notification,
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_capacity_added_in_notification))
                    && onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.not_charging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${onGetBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, onGetChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(onGetCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, onGetCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                onGetResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, onGetBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, onGetChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.temperature_fahrenheit else
                    R.string.temperature_celsius, onGetTemperature(context)))

            setTextViewText(R.id.voltage_service_notification, context.getString(
                if(pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(
                        R.bool.voltage_in_mv))) R.string.voltage_mv else R.string.voltage,
                DecimalFormat("#.#").format(onGetVoltage(context))))
        }
    }

    private fun onGetBatteryStatusFull(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.last_charge_time_service_notification, View.GONE)

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.capacity_added_service_notification,
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_capacity_added_in_notification))
                    && onGetCurrentCapacity(context) > 0.0) View.VISIBLE
                else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.full)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${onGetBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, onGetChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(onGetCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, onGetCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                onGetResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, onGetBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, onGetChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.temperature_fahrenheit else
                    R.string.temperature_celsius, onGetTemperature(context)))

            setTextViewText(R.id.voltage_service_notification, context.getString(
                if(pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(R.bool.voltage_in_mv)))
                    R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
                    onGetVoltage(context))))
        }
    }

    private fun onGetBatteryStatusDischarging(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val batteryLevelWith = "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%"
        val batteryLevelTo = "${pref.getInt(BATTERY_LEVEL_TO, 0)}%"
        
        remoteViews.apply {

            setViewVisibility(R.id.charging_time_service_notification, View.GONE)

            setViewVisibility(R.id.last_charge_time_service_notification,
                if(pref.getBoolean(IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_last_charge_time_in_notification)) &&
                    pref.getInt(LAST_CHARGE_TIME, 0) > 0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.capacity_added_service_notification,
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_capacity_added_in_notification))
                    && onGetCurrentCapacity(context) > 0.0) View.VISIBLE
                else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.discharging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${onGetBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.last_charge_time_service_notification, context.getString(
                R.string.last_charge_time, onGetLastChargeTime(context), batteryLevelWith,
                batteryLevelTo))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(onGetCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, onGetCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                onGetResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, onGetBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, onGetChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.temperature_fahrenheit else
                    R.string.temperature_celsius, onGetTemperature(context)))

            setTextViewText(R.id.voltage_service_notification, context.getString(
                if(pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(R.bool.voltage_in_mv)))
                    R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
                    onGetVoltage(context))))
        }
    }

    private fun onGetBatteryStatusUnknown(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.last_charge_time_service_notification, View.GONE)

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.capacity_added_service_notification,
                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION, context.resources
                        .getBoolean(R.bool.is_show_capacity_added_in_notification))
                    && onGetCurrentCapacity(context) > 0.0) View.VISIBLE
                else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(onGetCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.unknown)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${onGetBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, onGetChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(onGetCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, onGetCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                onGetResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, onGetBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, onGetChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(
                if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit))) R.string.temperature_fahrenheit else
                    R.string.temperature_celsius, onGetTemperature(context)))

            setTextViewText(R.id.voltage_service_notification, context.getString(
                if(pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(
                        R.bool.voltage_in_mv))) R.string.voltage_mv else R.string.voltage,
                DecimalFormat("#.#").format(onGetVoltage(context))))
        }
    }
}