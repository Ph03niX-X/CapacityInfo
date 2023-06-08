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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.databinding.ChangeDesignCapacityDialogBinding
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.fragments.WearFragment
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper.getSystemLocale
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper.setLocale
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import java.lang.NumberFormatException

interface SettingsInterface {

    fun SettingsFragment.onOpenNotificationCategorySettings(notificationId: String) {

        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager
            .getNotificationChannel(notificationId)

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
        }

        requireContext().startActivity(intent)
    }

    fun SettingsFragment.getOnTextSizeSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(TEXT_SIZE, "2") !in
            resources.getStringArray(R.array.text_size_values))
            pref.edit().putString(TEXT_SIZE, "2").apply()

        return resources.getStringArray(R.array.text_size_list)[
                (pref.getString(TEXT_SIZE, "2") ?: "2").toInt()]
    }

    fun SettingsFragment.getOnTextFontSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(TEXT_FONT, "6") !in
            resources.getStringArray(R.array.fonts_values))
            pref.edit().putString(TEXT_FONT, "6").apply()

        return resources.getStringArray(R.array.fonts_list)[
                (pref.getString(TEXT_FONT, "6") ?: "6").toInt()]
    }

    fun SettingsFragment.getOnTextStyleSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(TEXT_STYLE, "0") !in
            resources.getStringArray(R.array.text_style_values))
            pref.edit().putString(TEXT_STYLE, "0").apply()

        return resources.getStringArray(R.array.text_style_list)[
                (pref.getString(TEXT_STYLE, "0") ?: "0").toInt()]
    }

    fun SettingsFragment.getOnLanguageSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(LANGUAGE, null) !in
            resources.getStringArray(R.array.languages_codes))
            pref.edit().putString(LANGUAGE, null).apply()

        return when(pref.getString(LANGUAGE, null)) {

            "en" -> resources.getStringArray(R.array.languages_list)[0]

            "de" -> resources.getStringArray(R.array.languages_list)[1]

            "es" -> resources.getStringArray(R.array.languages_list)[2]

            "pl" -> resources.getStringArray(R.array.languages_list)[3]

            "ro" -> resources.getStringArray(R.array.languages_list)[4]

            "be" -> resources.getStringArray(R.array.languages_list)[5]

            "bg" -> resources.getStringArray(R.array.languages_list)[6]

            "kk" -> resources.getStringArray(R.array.languages_list)[7]

            "ru" -> resources.getStringArray(R.array.languages_list)[8]

            "uk" -> resources.getStringArray(R.array.languages_list)[9]

            else -> defLang
        }
    }

    fun SettingsFragment.getOnChangeAppLanguageSummary(): String? {

        return when(requireContext().resources.configuration.getSystemLocale()) {

            "en" -> resources.getStringArray(R.array.languages_list)[0]

            "de" -> resources.getStringArray(R.array.languages_list)[1]

            "es" -> resources.getStringArray(R.array.languages_list)[2]

            "pl" -> resources.getStringArray(R.array.languages_list)[3]

            "ro" -> resources.getStringArray(R.array.languages_list)[4]

            "be" -> resources.getStringArray(R.array.languages_list)[5]

            "bg" -> resources.getStringArray(R.array.languages_list)[6]

            "kk" -> resources.getStringArray(R.array.languages_list)[7]

            "ru" -> resources.getStringArray(R.array.languages_list)[8]

            "uk" -> resources.getStringArray(R.array.languages_list)[9]

            else -> defLang
        }
    }

    fun SettingsFragment.getOnTabOnApplicationLaunch(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") !in
            resources.getStringArray(R.array.tab_on_application_launch_values))
            pref.edit().putString(TAB_ON_APPLICATION_LAUNCH, "0").apply()

        return resources.getStringArray(R.array.tab_on_application_launch_list)[
                (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") ?: "0").toInt()]
    }

    fun SettingsFragment.getOnUnitOfChargeDischargeCurrentSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")
            !in resources.getStringArray(R.array.unit_of_charge_discharge_current_values))
            pref.edit().putString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA").apply()

        return when(pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")) {

            "μA" -> resources.getStringArray(
                R.array.unit_of_charge_discharge_current_list)[0]

            "mA" -> resources.getStringArray(
                R.array.unit_of_charge_discharge_current_list)[1]

            else -> pref.getString(UNIT_OF_CHARGE_DISCHARGE_CURRENT, "μA")
        }
    }

    fun SettingsFragment.getOnUnitOfMeasurementOfCurrentCapacitySummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
            !in resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_values))
            pref.edit().putString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh").apply()

        return when(pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")) {

            "μAh" -> resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_list)[0]

            "mAh" -> resources.getStringArray(
                R.array.unit_of_measurement_of_current_capacity_list)[1]

            else -> pref.getString(UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh")
        }
    }

    fun SettingsFragment.getOnVoltageUnitSummary(): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(VOLTAGE_UNIT, "mV")
            !in resources.getStringArray(
                R.array.voltage_unit_values))
            pref.edit().putString(VOLTAGE_UNIT, "mV").apply()

        return when(pref.getString(VOLTAGE_UNIT, "mV")) {

            "μV" -> resources.getStringArray(R.array.voltage_unit_list)[0]

            "mV" -> resources.getStringArray(R.array.voltage_unit_list)[1]

            else -> pref.getString(VOLTAGE_UNIT, "mV")
        }
    }

    fun SettingsFragment.onChangeLanguage(language: String) {

        if(CapacityInfoService.instance != null)
            ServiceHelper.stopService(requireContext(), CapacityInfoService::class.java)

        if(OverlayService.instance != null)
            ServiceHelper.stopService(requireContext(), OverlayService::class.java)

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            requireContext().setLocale(language)

        (context as? MainActivity)?.recreate()
    }

    fun SettingsFragment.onChangeDesignCapacity(designCapacity: Preference? = null) {

        onChangeDesignCapacity(requireContext(), designCapacity)
    }

    fun WearFragment.onChangeDesignCapacity(designCapacity: Preference? = null) {
        
        onChangeDesignCapacity(requireContext(), designCapacity)
    }
    
    private fun onChangeDesignCapacity(context: Context, designCapacity: Preference? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val dialog = MaterialAlertDialogBuilder(context)

        val binding = ChangeDesignCapacityDialogBinding.inflate(LayoutInflater.from(context),
            null, false)

        dialog.setView(binding.root.rootView)

        binding.changeDesignCapacityEdit.setText(if(pref.getInt(DESIGN_CAPACITY,
                context.resources.getInteger(R.integer.min_design_capacity)) >=
            context.resources.getInteger(R.integer.min_design_capacity))
            pref.getInt(DESIGN_CAPACITY, context.resources.getInteger(
                R.integer.min_design_capacity)).toString() else
                    context.resources.getInteger(R.integer.min_design_capacity).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DESIGN_CAPACITY, binding.changeDesignCapacityEdit.text.toString()
                .toInt()).apply()

            designCapacity?.summary = binding.changeDesignCapacityEdit.text.toString()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeDesignCapacityDialogCreateShowListener(context, dialogCreate,
            binding.changeDesignCapacityEdit, pref)

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

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                        s.isNotEmpty() && s.toString() != pref.getInt(DESIGN_CAPACITY,
                            context.resources.getInteger(R.integer.min_design_capacity)).toString()
                                && s.toString().toInt() >= context.resources.getInteger(
                            R.integer.min_design_capacity) && s.toString().toInt() <=
                                context.resources.getInteger(R.integer.max_design_capacity)
                    }
                    catch (e: NumberFormatException) {
                        Toast.makeText(context, e.message ?: e.toString(),
                            Toast.LENGTH_LONG).show()
                        false
                    }

                }
            })
        }
    }
}