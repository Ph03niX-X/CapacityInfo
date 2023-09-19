package com.ph03nix_x.capacityinfo.interfaces

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import xyz.kumaraswamy.autostart.Autostart
import xyz.kumaraswamy.autostart.Utils
import java.util.Locale

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

interface ManufacturerInterface {

    fun MainActivity.checkManufacturer() {
        try {
            if(showXiaomiAutostartDialog == null && isXiaomi()
                && !Autostart.isAutoStartEnabled(this)) showXiaomiAutoStartDialog()
            else if(isHuawei()) showHuaweiInfo()
        }
        catch (_: Exception) { return }
    }

    private fun getManufacturer() = Build.MANUFACTURER.uppercase(Locale.getDefault())

    private fun isXiaomi() : Boolean {

        val xiaomiManufacturerList = arrayListOf("XIAOMI", "POCO", "REDMI", "BLACK SHARK")

        return getManufacturer() in xiaomiManufacturerList && Utils.isOnMiui()
    }

    private fun isHuawei(): Boolean {

        val huaweiManufacturerList = arrayListOf("HUAWEI", "HONOR")

        return getManufacturer() in huaweiManufacturerList
    }

    private fun MainActivity.showXiaomiAutoStartDialog() {
        if(showXiaomiAutostartDialog == null && isXiaomi()
            && !Autostart.isAutoStartEnabled(this)) {
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
                        startActivity(Intent().setClassName("com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"))

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