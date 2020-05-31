package com.ph03nix_x.capacityinfo.utils

import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.utils.Constants.GOOGLE_PLAY_PACKAGE_NAME

object Utils {

    var isPowerConnected = false
    var isInstalledGooglePlay = true
    var isStartedService = false
    var tempCurrentCapacity = 0.0
    var capacityAdded = 0.0
    var tempBatteryLevelWith = 0
    var percentAdded = 0
    var batteryIntent: Intent? = null

    fun isGooglePlay(context: Context) =
        GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallerPackageName(
            context.packageName)
}