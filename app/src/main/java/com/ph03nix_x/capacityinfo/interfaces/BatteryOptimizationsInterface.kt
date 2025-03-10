package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.utilities.Constants
import androidx.core.net.toUri

/**
 * Created by Ph03niX-X on 05.12.2023
 * Ph03niX-X@outlook.com
 */

interface BatteryOptimizationsInterface {

    fun MainActivity.isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(packageName) == true
    }

    fun MainActivity.showRequestIgnoringBatteryOptimizationsDialog() {
        showRequestIgnoringBatteryOptimizationsDialog =
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(R.string.information)
                setMessage(R.string.ignoring_battery_optimizations_dialog_message)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    isShowRequestIgnoringBatteryOptimizationsDialog = false
                    requestIgnoringBatteryOptimizations()
                }
                show()
        }
    }

    @SuppressLint("BatteryLife")
    private fun MainActivity.requestIgnoringBatteryOptimizations() {
        try {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
                startActivity(this)
            }
        }
        catch (_: ActivityNotFoundException) {
            if(showFailedRequestIgnoringBatteryOptimizationsDialog == null)
                showFailedRequestIgnoringBatteryOptimizationsDialog()
        }
    }

    private fun MainActivity.showFailedRequestIgnoringBatteryOptimizationsDialog() {
        showFailedRequestIgnoringBatteryOptimizationsDialog =
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(R.string.error)
                setMessage(R.string.failed_request_permission)
                setPositiveButton(android.R.string.ok) { d, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            Constants.DONT_KILL_MY_APP_LINK.toUri()))
                    }
                    catch (_: ActivityNotFoundException) { d.dismiss() }
                    finally {
                        showFailedRequestIgnoringBatteryOptimizationsDialog = null
                    }
                }
                setNegativeButton(android.R.string.cancel) { _, _ ->
                    showFailedRequestIgnoringBatteryOptimizationsDialog = null
                }
                show()
            }
    }
}