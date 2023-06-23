package com.ph03nix_x.capacityinfo.interfaces

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import xyz.kumaraswamy.autostart.Autostart
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

interface ManufacturerInterface {

    fun MainActivity.checkManufacturer() {
        if(showXiaomiAutostartDialog == null && isXiaomi() &&
            Autostart(this).autoStartState == Autostart.State.DISABLED)
            showXiaomiAutoStartDialog() else if(isHuawei()) showHuaweiInfo()
    }

    private fun isXiaomi() =
        (Build.MANUFACTURER.uppercase(Locale.getDefault()) == "XIAOMI" ||
                Build.MANUFACTURER.uppercase(Locale.getDefault()) == "POCO" ||
                Build.MANUFACTURER.uppercase(Locale.getDefault()) == "REDMI" ||
                Build.MANUFACTURER.uppercase(Locale.getDefault()) == "BLACK SHARK") && isMIUI()

    private fun isMIUI(): Boolean {

        val propName = "ro.miui.ui.version.name"
        val line: String
        var input: BufferedReader? = null
        return try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
            line.isNotEmpty()
        } catch (ex: IOException) {
            false
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isHuawei() =
        Build.MANUFACTURER.uppercase(Locale.getDefault()) == "HUAWEI" ||
                Build.MANUFACTURER.uppercase(Locale.getDefault()) == "HONOR"

    private fun MainActivity.showXiaomiAutoStartDialog() {
        if(showXiaomiAutostartDialog == null && isXiaomi() &&
            Autostart(this).autoStartState == Autostart.State.DISABLED) {
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
                    catch (e: ActivityNotFoundException) {
                        startActivity(Intent().setComponent(ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity")))

                        showXiaomiBackgroundActivityControlDialog()
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
                            putExtra("package_name",
                                this@showXiaomiBackgroundActivityControlDialog.packageName)
                            putExtra("package_label", getText(R.string.app_name))
                        })
                    }
                    catch (e: ActivityNotFoundException) {
                        Toast.makeText(this@showXiaomiBackgroundActivityControlDialog,
                            e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                show()
            }
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