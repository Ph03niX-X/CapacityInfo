package com.ph03nix_x.capacityinfo.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.android.billingclient.api.BillingClient
import com.ph03nix_x.capacityinfo.utils.Constants.GOOGLE_PLAY_PACKAGE_NAME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_VOLTAGE_OVERLAY

object Utils {

    var billingClient: BillingClient? = null
    var isPowerConnected = false
    var isInstalledGooglePlay = true
    var isDonated = false
    var isStartedService = false
    var tempCurrentCapacity = 0.0
    var capacityAdded = 0.0
    var tempBatteryLevelWith = 0
    var percentAdded = 0
    var batteryIntent: Intent? = null

    fun launchActivity(context: Context, activity: Class<*>) {

        context.startActivity(Intent(context, activity))
    }

    fun launchActivity(context: Context, activity: Class<*>, flags: ArrayList<Int>) {

        context.startActivity(Intent(context, activity).apply {

            flags.forEach {

                this.addFlags(it)
            }
        })
    }

    fun launchActivity(context: Context, activity: Class<*>, flags: ArrayList<Int>, intent: Intent) {

        context.startActivity(Intent(context, activity).apply {

            flags.forEach {

                this.addFlags(it)
            }
            putExtras(intent)
        })
    }

    fun isGooglePlay(context: Context) =
        GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallerPackageName(context.packageName)

    fun isInstalledGooglePlay(context: Context): Boolean {

        return try {

            context.packageManager.getPackageInfo(GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch (e: PackageManager.NameNotFoundException) { false }
    }

    fun isEnabledOverlay(context: Context): Boolean {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        with(pref) {

            return when {

                getBoolean(IS_BATTERY_LEVEL_OVERLAY, false) || getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false)
                        || getBoolean(IS_BATTERY_HEALTH_OVERLAY, false) || getBoolean(IS_STATUS_OVERLAY, false)
                        || getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false) || getBoolean(
                    IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false) || getBoolean(
                    IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, false) || getBoolean(
                    IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false) || getBoolean(
                    IS_TEMPERATURE_OVERLAY, false) || getBoolean(IS_VOLTAGE_OVERLAY, false) -> true

                else -> false
            }
        }
    }
}