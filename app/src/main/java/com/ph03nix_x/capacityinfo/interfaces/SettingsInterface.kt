package com.ph03nix_x.capacityinfo.interfaces

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R

interface SettingsInterface {

    @RequiresApi(Build.VERSION_CODES.O)
    fun openNotificationCategorySettings(context: Context) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager.getNotificationChannel("service_channel")

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

        context.startActivity(intent)
    }

    fun changeDesignCapacity(context: Context, pref: SharedPreferences, designCapacity: Preference) {

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_design_capacity, null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<EditText>(R.id.change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefKey, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) {

                pref.edit().putInt(Preferences.DesignCapacity.prefKey, changeDesignCapacity.text.toString().toInt()).apply()

                designCapacity.summary = changeDesignCapacity.text.toString()
            }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }
}