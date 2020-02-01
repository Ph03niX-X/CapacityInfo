package com.ph03nix_x.capacityinfo.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.android.billingclient.api.BillingClient
import com.ph03nix_x.capacityinfo.utils.Constants.googlePlayPackageName

object Utils {

    lateinit var billingClient: BillingClient
    var isPowerConnected = false
    var isInstalledGooglePlay = true
    var tempCurrentCapacity = 0.0
    var capacityAdded = 0.0
    var tempBatteryLevelWith = 0
    var percentAdded = 0
    var batteryIntent: Intent? = null
    var orderId: String? = null

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

    fun launchActivity(context: Context, activity: Class<*>, intent: Intent) {

        context.startActivity(Intent(context, activity).apply {

            putExtras(intent)
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
        googlePlayPackageName == context.packageManager.getInstallerPackageName(context.packageName)

    fun isInstalledGooglePlay(context: Context): Boolean {

        return try {

            context.packageManager.getPackageInfo(googlePlayPackageName, 0)

            true
        }

        catch (e: PackageManager.NameNotFoundException) { false }
    }
}