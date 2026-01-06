package com.ph03nix_x.capacityinfo.interfaces

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.utilities.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.kumaraswamy.autostart.Autostart
import xyz.kumaraswamy.autostart.Utils
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import androidx.core.net.toUri
import com.ph03nix_x.capacityinfo.interfaces.views.MenuInterface

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

interface ManufacturerInterface {

    fun MainActivity.checkManufacturer() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                delay(3.seconds)
                if(showXiaomiAutostartDialog == null && !isXiaomi()
                    && !Autostart.isAutoStartEnabled(this@checkManufacturer))
                    showXiaomiAutoStartDialog()
                else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                    isHuawei())
                    showHuaweiInfo()
            }
            catch (_: Exception) { return@launch }
        }
    }

    fun getManufacturer() = Build.MANUFACTURER.uppercase(Locale.getDefault())

    private fun isXiaomi() : Boolean {

        val xiaomiManufacturerList = arrayListOf("XIAOMI", "POCO", "REDMI", "BLACK SHARK")

        return getManufacturer() in xiaomiManufacturerList &&
                Utils.isOnMiui()
    }

    private fun isHuawei(): Boolean {

        val huaweiManufacturerList = arrayListOf("HUAWEI", "HONOR")

        return getManufacturer() in huaweiManufacturerList
    }

    private fun getXiaomiManufactures() = arrayListOf("XIAOMI", "POCO", "REDMI", "BLACK SHARK")

    private fun getHuaweiManufactures() = arrayListOf("HUAWEI", "HONOR")

    private fun getPixelManufactures() = arrayListOf("GOOGLE", "PIXEL")

    private fun getMotorolaManufactures() = arrayListOf("MOTOROLA", "MOTO")

    private fun getNokiaManufactures() = arrayListOf("NOKIA", "HMD", "HMD GLOBAL", "HMD GLOBAL OY")

    fun MenuInterface.getDontKillMyAppManufactures(): Uri {
        return when(getManufacturer()) {
            in getXiaomiManufactures() -> "${Constants.DONT_KILL_MY_APP_LINK}/xiaomi".toUri()
            in getHuaweiManufactures() -> "${Constants.DONT_KILL_MY_APP_LINK}/huawei".toUri()
            in getPixelManufactures() -> "${Constants.DONT_KILL_MY_APP_LINK}/google".toUri()
            "SAMSUNG" -> "${Constants.DONT_KILL_MY_APP_LINK}/samsung".toUri()
            "ONEPLUS" -> "${Constants.DONT_KILL_MY_APP_LINK}/oneplus".toUri()
            "OPPO" -> "${Constants.DONT_KILL_MY_APP_LINK}/oppo".toUri()
            "VIVO" -> "${Constants.DONT_KILL_MY_APP_LINK}/vivo".toUri()
            "REALME" -> "${Constants.DONT_KILL_MY_APP_LINK}/realme".toUri()
            "MEIZU" -> "${Constants.DONT_KILL_MY_APP_LINK}/meizu".toUri()
            "ASUS" -> "${Constants.DONT_KILL_MY_APP_LINK}/asus".toUri()
            "SONY" -> "${Constants.DONT_KILL_MY_APP_LINK}/sony".toUri()
            "LENOVO" -> "${Constants.DONT_KILL_MY_APP_LINK}/lenovo".toUri()
            in getMotorolaManufactures() -> "${Constants.DONT_KILL_MY_APP_LINK}/motorola".toUri()
            "HTC" -> "${Constants.DONT_KILL_MY_APP_LINK}/htc".toUri()
            "TECNO" -> "${Constants.DONT_KILL_MY_APP_LINK}/tecno".toUri()
            "BLACKVIEW" -> "${Constants.DONT_KILL_MY_APP_LINK}/blackview".toUri()
            "UNIHERTZ" -> "${Constants.DONT_KILL_MY_APP_LINK}/unihertz".toUri()
            "WIKO" -> "${Constants.DONT_KILL_MY_APP_LINK}/wiko".toUri()
            in getNokiaManufactures() -> "${Constants.DONT_KILL_MY_APP_LINK}/nokia".toUri()
            else -> "${Constants.DONT_KILL_MY_APP_LINK}/general".toUri()
        }
    }

    private fun MainActivity.showXiaomiAutoStartDialog() {
        if(showXiaomiAutostartDialog == null && !isXiaomi()
            && !Autostart.isAutoStartEnabled(this)) {
            isShowXiaomiBackgroundActivityControlDialog = true
            showXiaomiAutostartDialog = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.auto_start_xiaomi_dialog))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        startActivity(
                            Intent("miui.intent.action.OP_AUTO_START")
                            .addCategory(Intent.CATEGORY_DEFAULT))

                        showXiaomiBackgroundActivityControlDialog()
                    }
                    catch (_: ActivityNotFoundException) {
                        try {
                            startActivity(Intent().setClassName("com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity"))

                            showXiaomiBackgroundActivityControlDialog()
                        }
                        catch (_: ActivityNotFoundException) { showFailedOpenSecurityMIUIDialog() }
                    }
                    finally {
                        showXiaomiAutostartDialog = null
                    }
                }

                setCancelable(false)
                show()
            }
        }
    }

    private fun MainActivity.showXiaomiBackgroundActivityControlDialog() {
        if(isXiaomi()) {
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.background_activity_control_xiaomi_dialog))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        startActivity(Intent().apply {
                            setClassName("com.miui.powerkeeper",
                                "com.miui.powerkeeper.ui.HiddenAppsConfigActivity")
                            putExtra("package_name", packageName)
                            putExtra("package_label", getText(R.string.app_name))
                        })
                    }
                    catch (_: ActivityNotFoundException) { showFailedOpenSecurityMIUIDialog() }
                }
                show()
            }
        }
    }

    private fun MainActivity.showFailedOpenSecurityMIUIDialog() {
        if(isXiaomi())
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(R.string.error)
                setMessage(R.string.failed_open_security_miui)
                setPositiveButton(android.R.string.ok) { d, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            "${Constants.DONT_KILL_MY_APP_LINK}/xiaomi".toUri()))
                    }
                    catch (_: ActivityNotFoundException) { d.dismiss() }
                }
                setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
                show()
            }
    }

    private fun MainActivity.showHuaweiInfo() {
        if(isHuawei() && showHuaweiInformation == null)
            showHuaweiInformation = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.huawei_honor_information))
                setPositiveButton(android.R.string.ok) { d, _ ->
                    showHuaweiInformation = null
                    d.dismiss()
                }
                show()
            }
    }
}