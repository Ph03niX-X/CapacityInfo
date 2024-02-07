package com.ph03nix_x.capacityinfo

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumContext
import com.ph03nix_x.capacityinfo.utilities.Constants
import java.io.Serializable
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class MainApp : Application(), PremiumInterface {

    companion object {

        var batteryIntent: Intent? = null
        var isRequestPurchasePremium = true
        var isPowerConnected = false
        var isUpdateApp = false
        var isInstalledGooglePlay = true

        var currentTheme = -1

        var tempScreenTime = 0L

        @Suppress("DEPRECATION")
        fun isGooglePlay(context: Context) =

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallSourceInfo(
                    context.packageName).installingPackageName

            else Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager
                .getInstallerPackageName(context.packageName)

        fun restartApp(context: Context, prefArrays: HashMap<String, Any?>) {

            val packageManager = context.packageManager

            val componentName = packageManager.getLaunchIntentForPackage(
                context.packageName)?.component

            val intent = Intent.makeRestartActivityTask(componentName)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

            intent?.putExtra(Constants.IMPORT_RESTORE_SETTINGS_EXTRA, prefArrays)

            context.startActivity(intent)

            exitProcess(0)
        }

        fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T?
        {
            return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                activity.intent.getSerializableExtra(name, clazz)
            else {
                @Suppress("DEPRECATION", "UNCHECKED_CAST")
                activity.intent.getSerializableExtra(name) as T
            }
        }
    }

    override fun onCreate() {

        super.onCreate()

        premiumContext = this

        isInstalledGooglePlay = isInstalledGooglePlay()

        ThemeHelper.setTheme(this)

        if(isInstalledGooglePlay) checkPremium()

        currentTheme = ThemeHelper.currentTheme(resources.configuration)

        if(isInstalledGooglePlay) ServiceHelper.checkPremiumJobSchedule(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newTheme = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_NO

        if(newTheme != currentTheme) {
            MainActivity.apply {
                tempFragment = instance?.fragment

               isRecreate = true

               instance?.recreate()
            }
        }
    }

    private fun isInstalledGooglePlay(): Boolean {

        return try {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0))
            else packageManager.getPackageInfo(Constants.GOOGLE_PLAY_PACKAGE_NAME, 0)

            true
        }

        catch(e: PackageManager.NameNotFoundException) { false }
    }
}