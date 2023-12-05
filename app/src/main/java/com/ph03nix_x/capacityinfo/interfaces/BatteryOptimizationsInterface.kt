package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity

/**
 * Created by Ph03niX-X on 05.12.2023
 * Ph03niX-X@outlook.com
 */

interface BatteryOptimizationsInterface {

    fun MainActivity.isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
    }

    fun MainActivity.showRequestIgnoringBatteryOptimizationsDialog() {
        showRequestIgnoringBatteryOptimizationsDialog =
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(R.string.information)
                setMessage(R.string.ignoring_battery_optimizations_dialog_message)
                setPositiveButton(android.R.string.ok) {_, _ ->
                    requestIgnoringBatteryOptimizations()
                }
                show()
        }
    }

    @SuppressLint("BatteryLife")
    private fun MainActivity.requestIgnoringBatteryOptimizations() {
        try {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
                startActivity(this)
            }
        }
        catch (_: ActivityNotFoundException) {}
    }
}