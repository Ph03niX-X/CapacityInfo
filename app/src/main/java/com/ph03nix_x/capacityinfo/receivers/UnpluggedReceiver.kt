package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.NotificationInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.capacityAdded
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isPowerConnected
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface.Companion.percentAdded
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STOP_THE_SERVICE_WHEN_THE_CD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES

class UnpluggedReceiver : BroadcastReceiver(), PremiumInterface, NavigationInterface {

    override fun onReceive(context: Context, intent: Intent) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        if(CapacityInfoService.instance != null && isPowerConnected)
            when(intent.action) {
            Intent.ACTION_POWER_DISCONNECTED -> {
                isPowerConnected = false
                CapacityInfoService.instance?.isPluggedOrUnplugged = true
                val isCheckedUpdateFromGooglePlay =
                    MainActivity.instance?.isCheckUpdateFromGooglePlay ?: false
                MainActivity.instance?.isCheckUpdateFromGooglePlay = !isCheckedUpdateFromGooglePlay
                val isPremium = PremiumInterface.isPremium
                val seconds = CapacityInfoService.instance?.seconds ?: 0
                val batteryLevel = CapacityInfoService.instance?.getBatteryLevel(context) ?: 0
                val batteryLevelWith = CapacityInfoService.instance?.batteryLevelWith ?: 0
                val numberOfCycles = if(batteryLevel == batteryLevelWith) pref.getFloat(
                    NUMBER_OF_CYCLES, 0f) + 0.01f else pref.getFloat(
                    NUMBER_OF_CYCLES, 0f) + (batteryLevel / 100f) - (
                        batteryLevelWith / 100f)
                pref.edit().apply {
                    if((CapacityInfoService.instance?.isFull != true) && seconds > 1) {
                        val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0)
                        putLong(NUMBER_OF_CHARGES, numberOfCharges + 1).apply()
                        putInt(LAST_CHARGE_TIME, seconds)
                        putInt(BATTERY_LEVEL_WITH, CapacityInfoService.instance
                            ?.batteryLevelWith ?: 0)
                        putInt(BATTERY_LEVEL_TO, batteryLevel)
                        if(CapacityInfoService.instance?.isSaveNumberOfCharges != false)
                            putFloat(NUMBER_OF_CYCLES, numberOfCycles)
                        if(capacityAdded > 0) putFloat(CAPACITY_ADDED, capacityAdded.toFloat())
                        if(percentAdded > 0) putInt(PERCENT_ADDED, percentAdded)
                        percentAdded = 0
                        capacityAdded = 0.0
                    }
                    apply()
                }
                batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                    .ACTION_BATTERY_CHANGED))
                CapacityInfoService.instance?.seconds = 0
                if(isPremium && (batteryLevel >= 90 || pref.getBoolean(
                        IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL, context.resources.getBoolean(
                            R.bool.is_reset_screen_time_at_any_charge_level))))
                    CapacityInfoService.instance?.screenTime = 0L
                BatteryInfoInterface.apply {
                    this.batteryLevel = 0
                    maxChargeCurrent = 0
                    averageChargeCurrent = 0
                    minChargeCurrent = 0
                    maxDischargeCurrent = 0
                    averageDischargeCurrent = 0
                    minDischargeCurrent = 0
                    maximumTemperature = 0.0
                    averageTemperature = 0.0
                    minimumTemperature = 0.0
                }
                CapacityInfoService.instance?.apply {
                    secondsFullCharge = 0
                    isFull = false
                }
                if(isPremium && pref.getBoolean(IS_STOP_THE_SERVICE_WHEN_THE_CD,
                        context.resources.getBoolean(R.bool.is_stop_the_service_when_the_cd)))
                    ServiceHelper.stopService(context, CapacityInfoService::class.java)
                NotificationInterface.apply {
                    notificationManager?.cancel(NOTIFICATION_FULLY_CHARGED_ID)
                    notificationManager?.cancel(NOTIFICATION_BATTERY_STATUS_ID)
                    notificationManager?.cancel(NOTIFICATION_BATTERY_OVERHEAT_OVERCOOL_ID)
                }
                ServiceHelper.cancelJob(context, Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)
                if(MainActivity.instance?.fragment != null) {
                    if(MainActivity.instance?.fragment is ChargeDischargeFragment)
                        MainActivity.instance?.toolbar?.title = context.getString(
                            R.string.discharge)
                    val chargeDischargeNavigation = MainActivity.instance?.navigation
                        ?.menu?.findItem(R.id.charge_discharge_navigation)
                    chargeDischargeNavigation?.title = context.getString(R.string.discharge)
                    chargeDischargeNavigation?.icon = MainActivity.instance
                        ?.getChargeDischargeNavigationIcon(false)?.let {
                            ContextCompat.getDrawable(context, it)
                        }
                }
                CapacityInfoService.instance?.apply {
                    isPluggedOrUnplugged = false
                    wakeLockRelease()
                }
            }
        }
    }
}