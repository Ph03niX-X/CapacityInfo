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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT

interface SettingsInterface {

    @RequiresApi(Build.VERSION_CODES.O)
    fun onOpenNotificationCategorySettings(context: Context, notificationId: String) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager
            .getNotificationChannel(notificationId)

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

        context.startActivity(intent)
    }

    fun getOnTextSizeSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_SIZE, "2") !in
            context.resources.getStringArray(R.array.text_size_values))
            pref.edit().putString(TEXT_SIZE, "2").apply()

        return context.resources.getStringArray(R.array.text_size_list)[
                (pref.getString(TEXT_SIZE, "2") ?: "2").toInt()]
    }

    fun getOnTextFontSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_FONT, "6") !in
            context.resources.getStringArray(R.array.fonts_values))
            pref.edit().putString(TEXT_FONT, "6").apply()

        return context.resources.getStringArray(R.array.fonts_list)[
                (pref.getString(TEXT_FONT, "6") ?: "6").toInt()]
    }

    fun getOnTextStyleSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_STYLE, "0") !in
            context.resources.getStringArray(R.array.text_style_values))
            pref.edit().putString(TEXT_STYLE, "0").apply()

        return context.resources.getStringArray(R.array.text_style_list)[
                (pref.getString(TEXT_STYLE, "0") ?: "0").toInt()]
    }

    fun getOnLanguageSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(LANGUAGE, null) !in
            context.resources.getStringArray(R.array.languages_codes))
            pref.edit().putString(LANGUAGE, null).apply()

        return when(pref.getString(LANGUAGE, null)) {

            "en" -> context.resources.getStringArray(R.array.languages_list)[0]

            "es" -> context.resources.getStringArray(R.array.languages_list)[1]

            "ro" -> context.resources.getStringArray(R.array.languages_list)[2]

            "be" -> context.resources.getStringArray(R.array.languages_list)[3]

            "ru" -> context.resources.getStringArray(R.array.languages_list)[4]

            "uk" -> context.resources.getStringArray(R.array.languages_list)[5]

            else -> defLang
        }
    }

    fun getOnTabOnApplicationLaunch(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") !in
            context.resources.getStringArray(R.array.tab_on_application_launch_values))
            pref.edit().putString(TAB_ON_APPLICATION_LAUNCH, "0").apply()

        return context.resources.getStringArray(R.array.tab_on_application_launch_list)[
                (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") ?: "0").toInt()]
    }

    fun getOnUnitOfChargeDischargeCurrentSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")
            !in context.resources.getStringArray(R.array.unit_of_charge_discharge_current_values))
            pref.edit().putString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA").apply()

        return when(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")) {

            "μA" -> context.resources.getStringArray(
                R.array.unit_of_charge_discharge_current_list)[0]

            "mA" -> context.resources.getStringArray(
                R.array.unit_of_charge_discharge_current_list)[1]

            else -> pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")
        }
    }

    fun getOnUnitOfMeasurementOfCurrentCapacitySummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
            !in context.resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_values))
            pref.edit().putString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh").apply()

        return when(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")) {

            "μAh" -> context.resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_list)[0]

            "mAh" -> context.resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_list)[1]

            else -> pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
        }
    }

    fun getOnVoltageUnitSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(VOLTAGE_UNIT, "mV")
            !in context.resources.getStringArray(
                R.array.voltage_unit_values))
            pref.edit().putString(VOLTAGE_UNIT, "mV").apply()

        return when(pref.getString(VOLTAGE_UNIT, "mV")) {

            "μV" -> context.resources.getStringArray(R.array.voltage_unit_list)[0]

            "mV" -> context.resources.getStringArray(R.array.voltage_unit_list)[1]

            else -> pref.getString(VOLTAGE_UNIT, "mV")
        }
    }

    fun onChangeLanguage(context: Context, language: String) {

        if(CapacityInfoService.instance != null)
            ServiceHelper.stopService(context, CapacityInfoService::class.java)

        if(OverlayService.instance != null)
            ServiceHelper.stopService(context, OverlayService::class.java)

        LocaleHelper.setLocale(context, language)

        (context as? MainActivity)?.recreate()
    }

    fun onChangeDesignCapacity(context: Context, designCapacity: Preference? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_design_capacity_dialog,
            null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<TextInputEditText>(R.id
            .change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(DESIGN_CAPACITY, context.resources.getInteger(
                R.integer.min_design_capacity)) >= context.resources.getInteger(
                R.integer.min_design_capacity)) pref.getInt(DESIGN_CAPACITY,
            context.resources.getInteger(R.integer.min_design_capacity)).toString()

        else context.resources.getInteger(R.integer.min_design_capacity).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DESIGN_CAPACITY, changeDesignCapacity.text.toString().toInt())
                .apply()

            designCapacity?.summary = changeDesignCapacity.text.toString()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeDesignCapacityDialogCreateShowListener(context, dialogCreate, changeDesignCapacity,
            pref)

        dialogCreate.show()
    }

    private fun changeDesignCapacityDialogCreateShowListener(context: Context,
                                                             dialogCreate: AlertDialog,
                                                             changeDesignCapacity: TextInputEditText,
                                                             pref: SharedPreferences) {

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changeDesignCapacity.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(DESIGN_CAPACITY,
                            context.resources.getInteger(R.integer.min_design_capacity)).toString()
                                && s.toString().toInt() >= context.resources.getInteger(
                            R.integer.min_design_capacity) && s.toString().toInt() <=
                                context.resources.getInteger(R.integer.max_design_capacity)
                }
            })
        }
    }
}