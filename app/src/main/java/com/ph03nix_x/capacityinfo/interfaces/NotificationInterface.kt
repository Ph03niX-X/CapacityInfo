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
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper.isSystemDarkMode
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.CloseNotificationBatteryStatusInformationService
import com.ph03nix_x.capacityinfo.services.DisableNotificationBatteryStatusInformationService
import com.ph03nix_x.capacityinfo.utilities.Constants.FULLY_CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.CHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.CHARGED_CHANNEL_VOLTAGE_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.CHARGING_CURRENT_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.Constants.DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.Constants.DISCHARGED_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.DISCHARGED_VOLTAGE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.DISCHARGE_CURRENT_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.OPEN_APP_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.Constants.OVERHEAT_OVERCOOL_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.SERVICE_CHANNEL_ID
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BYPASS_DND
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SERVICE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERCOOL_DEGREES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERHEAT_DEGREES
import java.lang.RuntimeException
import java.text.DecimalFormat

@SuppressLint("StaticFieldLeak")
interface NotificationInterface : BatteryInfoInterface {

    companion object {

        const val NOTIFICATION_SERVICE_ID = 101
        const val NOTIFICATION_BATTERY_STATUS_ID = 102
        const val NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID = 103
        const val NOTIFICATION_CHARGING_CURRENT_ID = 104
        const val NOTIFICATION_DISCHARGE_CURRENT_ID = 105

        private lateinit var channelId: String

        var notificationBuilder: NotificationCompat.Builder? = null
        var notificationManager: NotificationManager? = null
        var isNotifyOverheatOvercool = true
        var isNotifyBatteryFullyCharged = true
        var isNotifyBatteryCharged = true
        var isNotifyBatteryChargedVoltage = true
        var isNotifyBatteryDischarged = true
        var isNotifyBatteryDischargedVoltage = true
        var isNotifyChargingCurrent = true
        var isNotifyDischargeCurrent = true
        var isOverheatOvercool = false
        var isBatteryFullyCharged = false
        var isBatteryCharged = false
        var isBatteryChargedVoltage = false
        var isBatteryDischarged = false
        var isBatteryDischargedVoltage = false
        var isChargingCurrent = false
        var isDischargeCurrent = false
    }

    @SuppressLint("RestrictedApi")
    fun onCreateServiceNotification(context: Context) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        channelId = onCreateNotificationChannel(context, SERVICE_CHANNEL_ID)

        val openApp = PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, Intent(context,
            MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)

        batteryIntent = context.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        notificationBuilder = NotificationCompat.Builder(context, channelId).apply {

            setOngoing(true)
            setCategory(Notification.CATEGORY_SERVICE)
            setSmallIcon(R.drawable.ic_service_small_icon)

            color = ContextCompat.getColor(context, if(isSystemDarkMode(
                        context.resources.configuration)) R.color.red else R.color.blue)

            setContentIntent(openApp)

            val remoteViewsServiceContent = RemoteViews(context.packageName,
                R.layout.notification_content)

            remoteViewsServiceContent.setTextViewText(R.id.notification_content_text,
                if(getOnCurrentCapacity(context) > 0.0) context.getString(
                    R.string.current_capacity, DecimalFormat("#.#").format(
                        getOnCurrentCapacity(context))) else "${context.getString(
                    R.string.battery_level, (getOnBatteryLevel(context) ?: 0).toString())}%")

            setCustomContentView(remoteViewsServiceContent)

            val isShowBigContent = pref.getBoolean(IS_SHOW_EXPANDED_NOTIFICATION, context
                .resources.getBoolean(R.bool.is_show_expanded_notification))

            if(isShowBigContent) {

                val remoteViewsServiceBigContent = RemoteViews(context.packageName,
                    R.layout.service_notification_big_content)

                remoteViewsServiceBigContent.setViewVisibility(R.id
                    .voltage_service_notification, if(getOnCurrentCapacity(context) == 0.0
                    || mActions.isNullOrEmpty()) View.VISIBLE else View.GONE)

                getOnNotificationMessage(context, status, remoteViewsServiceBigContent)

                setCustomBigContentView(remoteViewsServiceBigContent)
            }

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                R.bool.is_service_time)))

            setUsesChronometer(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                R.bool.is_service_time)))
        }

        (context as? CapacityInfoService)?.startForeground(NOTIFICATION_SERVICE_ID,
            notificationBuilder?.build())
    }

    @SuppressLint("RestrictedApi")
    fun onUpdateServiceNotification(context: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        batteryIntent = context.applicationContext.registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        notificationBuilder?.apply {

            color = ContextCompat.getColor(context.applicationContext, if(
                isSystemDarkMode(context.resources.configuration)) R.color.red
            else R.color.blue)

            val remoteViewsServiceContent = RemoteViews(context.packageName,
                R.layout.notification_content)

            remoteViewsServiceContent.setTextViewText(R.id.notification_content_text,
                if(getOnCurrentCapacity(context) > 0.0) context.getString(
                    R.string.current_capacity, DecimalFormat("#.#").format(
                        getOnCurrentCapacity(context))) else "${context.getString(
                    R.string.battery_level, (getOnBatteryLevel(context) ?: 0).toString())}%")

            setCustomContentView(remoteViewsServiceContent)

            val isShowBigContent = pref.getBoolean(IS_SHOW_EXPANDED_NOTIFICATION,
                context.resources.getBoolean(R.bool.is_show_expanded_notification))

            if(isShowBigContent) {

                val remoteViewsServiceBigContent = RemoteViews(context.packageName,
                    R.layout.service_notification_big_content)

                remoteViewsServiceBigContent.setViewVisibility(R.id
                    .voltage_service_notification, if(getOnCurrentCapacity(context) == 0.0
                    || mActions.isNullOrEmpty()) View.VISIBLE else View.GONE)

                getOnNotificationMessage(context, status, remoteViewsServiceBigContent)

                setCustomBigContentView(remoteViewsServiceBigContent)
            }

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                R.bool.is_service_time)))

            setUsesChronometer(pref.getBoolean(IS_SERVICE_TIME, context.resources.getBoolean(
                R.bool.is_service_time)))
        }

        notificationManager?.notify(NOTIFICATION_SERVICE_ID, notificationBuilder?.build())
    }

    fun onNotifyOverheatOvercool(context: Context, temperature: Double) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val temperatureInFahrenheit = getOnTemperatureInFahrenheit(context)

        isNotifyOverheatOvercool = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, OVERHEAT_OVERCOOL_CHANNEL_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        when {

            temperature >= pref.getInt(OVERHEAT_DEGREES, context.resources.getInteger(
                R.integer.overheat_degrees_default)) ->
                remoteViewsContent.setTextViewText(R.id.notification_content_text,
                    context.getString(R.string.battery_overheating, DecimalFormat().format(
                        temperature), DecimalFormat().format(temperatureInFahrenheit)))

            temperature <= pref.getInt(OVERCOOL_DEGREES, context.resources.getInteger(
                R.integer.overcool_degrees_default)) ->
                remoteViewsContent.setTextViewText(R.id.notification_content_text,
                    context.getString(R.string.battery_overcooling, DecimalFormat().format(
                        temperature), DecimalFormat().format(temperatureInFahrenheit)))

            else -> return
        }

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
            CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
            DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = true
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_overheat_overcool_24)

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

        val channelId = onCreateNotificationChannel(context, FULLY_CHARGED_CHANNEL_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
            R.string.battery_is_fully_charged))

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
            CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
            DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = true
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_battery_is_fully_charged_24dp)
            
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

        val batteryLevel = getOnBatteryLevel(context)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, CHARGED_CHANNEL_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text,
            "${context.getString(R.string.battery_is_charged_notification, batteryLevel)}%")

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = true
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

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

    fun onNotifyBatteryChargedVoltage(context: Context, voltage: Int) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryChargedVoltage = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, CHARGED_CHANNEL_VOLTAGE_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
            R.string.battery_is_charged_notification_voltage, voltage))

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = true
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(when(voltage) {

                in 2800..3399 -> R.drawable.ic_battery_is_charged_20_24dp
                in 3400..3599 -> R.drawable.ic_battery_is_charged_30_24dp
                in 3600..3799 -> R.drawable.ic_battery_is_charged_50_24dp
                in 3800..3999 -> R.drawable.ic_battery_is_charged_60_24dp
                in 4000..4199 -> R.drawable.ic_battery_is_charged_80_24dp
                in 4200..4299 -> R.drawable.ic_battery_is_charged_90_24dp
                else -> R.drawable.ic_battery_is_fully_charged_24dp
            })

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

        val batteryLevel = getOnBatteryLevel(context) ?: 0

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, DISCHARGED_CHANNEL_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text,
            "${context.getString(R.string.battery_is_discharged_notification,
                batteryLevel)}%")

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = true
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

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

    fun onNotifyBatteryDischargedVoltage(context: Context, voltage: Int) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyBatteryDischargedVoltage = false

        val batteryLevel = getOnBatteryLevel(context) ?: 0

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, DISCHARGED_VOLTAGE_CHANNEL_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
            R.string.battery_is_discharged_notification_voltage, voltage))

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = true
        isChargingCurrent = false
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(when(batteryLevel) {

                in 2800..3399 -> R.drawable.ic_battery_discharged_9_24dp
                in 3400..3599 -> R.drawable.ic_battery_is_discharged_20_24dp
                in 3600..3799 -> R.drawable.ic_battery_is_discharged_30_24dp
                in 3800..3999 -> R.drawable.ic_battery_is_discharged_50_24dp
                in 4000..4099 -> R.drawable.ic_battery_is_discharged_60_24dp
                in 4100..4199 -> R.drawable.ic_battery_is_discharged_80_24dp
                in 4200..4399 -> R.drawable.ic_battery_is_discharged_90_24dp
                else -> R.drawable.ic_battery_discharged_9_24dp
            })

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

    fun onNotifyChargingCurrent(context: Context, chargingCurrent: Int) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyChargingCurrent = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, CHARGING_CURRENT_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
                R.string.charging_current_ma, chargingCurrent))

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = true
        isDischargeCurrent = false

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_charging_current_notification_24)

            color = ContextCompat.getColor(context, R.color.charging_current_notification)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(true)

            setLights(Color.BLUE, 1500, 500)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.charging_current}"))
        }

        notificationManager?.notify(NOTIFICATION_CHARGING_CURRENT_ID, notificationBuilder.build())
    }

    fun onNotifyDischargeCurrent(context: Context, dischargeCurrent: Int) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        isNotifyDischargeCurrent = false

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager

        val channelId = onCreateNotificationChannel(context, CHARGING_CURRENT_ID)

        val remoteViewsContent = RemoteViews(context.packageName, R.layout.notification_content)

        remoteViewsContent.setTextViewText(R.id.notification_content_text, context.getString(
            R.string.discharge_current_ma, dischargeCurrent))

        val close = PendingIntent.getService(context,
            CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                CloseNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        val disable = PendingIntent.getService(context,
            DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE, Intent(context,
                DisableNotificationBatteryStatusInformationService::class.java),
            PendingIntent.FLAG_IMMUTABLE)

        isOverheatOvercool = false
        isBatteryFullyCharged = false
        isBatteryCharged = false
        isBatteryChargedVoltage = false
        isBatteryDischarged = false
        isBatteryDischargedVoltage = false
        isChargingCurrent = false
        isDischargeCurrent = true

        val notificationBuilder = NotificationCompat.Builder(
            context, channelId).apply {

            if(pref.getBoolean(IS_BYPASS_DND, context.resources.getBoolean(
                    R.bool.is_bypass_dnd_mode)))
                setCategory(NotificationCompat.CATEGORY_ALARM)

            setAutoCancel(true)
            setOngoing(false)

            addAction(0, context.getString(R.string.close), close)
            addAction(0, context.getString(R.string.disable), disable)

            priority = NotificationCompat.PRIORITY_MAX

            setSmallIcon(R.drawable.ic_charging_current_notification_24)

            color = ContextCompat.getColor(context, R.color.charging_current_notification)

            setContentTitle(context.getString(R.string.battery_status_information))

            setCustomContentView(remoteViewsContent)

            setStyle(NotificationCompat.DecoratedCustomViewStyle())

            setShowWhen(true)

            setLights(Color.BLUE, 1500, 500)

            setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                    "${context.packageName}/${R.raw.charging_current}"))
        }

        notificationManager?.notify(NOTIFICATION_DISCHARGE_CURRENT_ID, notificationBuilder.build())
    }

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

            CHARGED_CHANNEL_VOLTAGE_ID -> {

                val channelName = context.getString(R.string.charged_voltage)

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

            DISCHARGED_VOLTAGE_CHANNEL_ID -> {

                val channelName = context.getString(R.string.discharged_voltage)

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

            CHARGING_CURRENT_ID -> {

                val channelName = context.getString(R.string.charging_current)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    enableLights(true)

                    lightColor = Color.BLUE

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.charging_current}"),
                        soundAttributes.build())
                })
            }

            DISCHARGE_CURRENT_ID -> {

                val channelName = context.getString(R.string.discharge_current_settings_channel)

                notificationService?.createNotificationChannel(NotificationChannel(
                    notificationChannelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {

                    setShowBadge(true)

                    enableLights(true)

                    lightColor = Color.BLUE

                    setSound(Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://" +
                            "${context.packageName}/${R.raw.charging_current}"),
                        soundAttributes.build())
                })
            }
        }

        return notificationChannelId
    }

    private fun getOnNotificationMessage(context: Context, status: Int?, remoteViews: RemoteViews) {

        when(status) {

            BatteryManager.BATTERY_STATUS_CHARGING -> getOnBatteryStatusCharging(context,
                batteryIntent, remoteViews)

            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> getOnBatteryStatusNotCharging(context,
            remoteViews)

            BatteryManager.BATTERY_STATUS_FULL -> getOnBatteryStatusFull(context, remoteViews)

            BatteryManager.BATTERY_STATUS_DISCHARGING -> getOnBatteryStatusDischarging(context,
                remoteViews)

            else -> getOnBatteryStatusUnknown(context, remoteViews)
        }
    }

    private fun getOnBatteryStatusCharging(context: Context, batteryIntent: Intent?,
                                           remoteViews: RemoteViews) {

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.number_of_cycles_service_notification, View.GONE)

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.source_of_power_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.charging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${getOnBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.charging_time_service_notification, getOnChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.source_of_power_service_notification, getOnSourceOfPower(context,
                batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                    -1) ?: -1))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(getOnCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, getOnCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                getOnResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, getOnBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.charge_current, getOnChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(R.string
                .temperature, DecimalFormat().format(getOnTemperatureInCelsius(context)),
                DecimalFormat().format(getOnTemperatureInFahrenheit(context))))

            setTextViewText(R.id.voltage_service_notification, context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getOnVoltage(context))))
        }
    }

    private fun getOnBatteryStatusNotCharging(context: Context, remoteViews: RemoteViews) {
        
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        
        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.not_charging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${getOnBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, getOnChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(getOnCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, getOnCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                getOnResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, getOnBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, getOnChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(R.string
                .temperature, DecimalFormat().format(getOnTemperatureInCelsius(context)),
                DecimalFormat().format(getOnTemperatureInFahrenheit(context))))

            setTextViewText(R.id.voltage_service_notification, context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getOnVoltage(context))))
        }
    }

    private fun getOnBatteryStatusFull(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.full)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${getOnBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, getOnChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(getOnCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, getOnCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                getOnResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, getOnBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, getOnChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(R.string
                .temperature, DecimalFormat().format(getOnTemperatureInCelsius(context)),
                DecimalFormat().format(getOnTemperatureInFahrenheit(context))))

            setTextViewText(R.id.voltage_service_notification, context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getOnVoltage(context))))
        }
    }

    private fun getOnBatteryStatusDischarging(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        
        remoteViews.apply {

            setViewVisibility(R.id.current_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.discharging)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${getOnBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(getOnCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, getOnCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                getOnResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, getOnBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, getOnChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(R.string
                .temperature, DecimalFormat().format(getOnTemperatureInCelsius(context)),
                DecimalFormat().format(getOnTemperatureInFahrenheit(context))))

            setTextViewText(R.id.voltage_service_notification, context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getOnVoltage(context))))
        }
    }

    private fun getOnBatteryStatusUnknown(context: Context, remoteViews: RemoteViews) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val capacityInfoServiceContext = context as? CapacityInfoService

        remoteViews.apply {

            setViewVisibility(R.id.charging_time_service_notification, View.VISIBLE)

            setViewVisibility(R.id.current_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.residual_capacity_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setViewVisibility(R.id.battery_wear_service_notification,
                if(getOnCurrentCapacity(context) > 0.0) View.VISIBLE else View.GONE)

            setTextViewText(R.id.status_service_notification, context.getString(
                R.string.status, context.getString(R.string.unknown)))

            setTextViewText(R.id.battery_level_service_notification, context.getString(
                R.string.battery_level, try {
                    "${getOnBatteryLevel(context)}%"
                }
                catch(e: RuntimeException)  { R.string.unknown }))

            setTextViewText(R.id.number_of_cycles_service_notification, context.getString(
                R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(
                    NUMBER_OF_CYCLES, 0f))))

            setTextViewText(R.id.charging_time_service_notification, getOnChargingTime(context,
                    capacityInfoServiceContext?.seconds ?: 0))

            setTextViewText(R.id.current_capacity_service_notification, context.getString(
                R.string.current_capacity, DecimalFormat("#.#").format(getOnCurrentCapacity(
                    context))))

            setTextViewText(R.id.capacity_added_service_notification, getOnCapacityAdded(
                context))

            setTextViewText(R.id.residual_capacity_service_notification,
                getOnResidualCapacity(context))

            setTextViewText(R.id.battery_wear_service_notification, getOnBatteryWear(
                context))

            setTextViewText(R.id.charge_discharge_current_service_notification,
                context.getString(R.string.discharge_current, getOnChargeDischargeCurrent(context)
                    .toString()))

            setTextViewText(R.id.temperature_service_notification, context.getString(R.string
                .temperature, DecimalFormat().format(getOnTemperatureInCelsius(context)),
                DecimalFormat().format(getOnTemperatureInFahrenheit(context))))

            setTextViewText(R.id.voltage_service_notification, context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getOnVoltage(context))))
        }
    }
}