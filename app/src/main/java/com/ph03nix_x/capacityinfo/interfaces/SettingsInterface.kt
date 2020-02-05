package com.ph03nix_x.capacityinfo.interfaces

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY

interface SettingsInterface : ServiceInterface {

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

        changeDesignCapacity.setText(if(pref.getInt(DESIGN_CAPACITY, 0) >= 0) pref.getInt(
            DESIGN_CAPACITY, 0).toString()

        else (pref.getInt(DESIGN_CAPACITY, 0) / -1).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DESIGN_CAPACITY, changeDesignCapacity.text.toString().toInt()).apply()

            designCapacity.summary = changeDesignCapacity.text.toString()

        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeDesignCapacity.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                            && s.toString() != pref.getInt(DESIGN_CAPACITY, 0).toString()
                            && s.count() >= 4 && s.toString().toInt() <= 10500
                }
            })
        }

        dialogCreate.show()
    }

    fun changeLanguage(context: Context, language: String) {

        if(CapacityInfoService.instance != null)
            context.stopService(Intent(context, CapacityInfoService::class.java))

        LocaleHelper.setLocale(context, language)

        MainActivity.instance?.recreate()

        (context as SettingsActivity).recreate()

        startService(context)
    }
}