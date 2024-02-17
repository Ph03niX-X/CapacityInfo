package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.services.CapacityInfoService.Companion.NOMINAL_BATTERY_VOLTAGE
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.TOKEN_PREF
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.databinding.AddCustomHistoryDialogBinding
import com.ph03nix_x.capacityinfo.databinding.AddNumberOfCyclesDialogBinding
import com.ph03nix_x.capacityinfo.databinding.AddPrefKeyDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ChangeNominalBatteryVoltageDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ChangePrefKeyDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ChangeScreenTimeDialogBinding
import com.ph03nix_x.capacityinfo.databinding.ResetPrefKeyDialogBinding
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_TIME_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CURRENT_CAPACITY_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FAST_CHARGE_WATTS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FULL_CHARGE_REMINDER_FREQUENCY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MAX_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_CHARGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_TEMP_CELSIUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.MIN_TEMP_FAHRENHEIT_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NOMINAL_BATTERY_VOLTAGE_PREF
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_LOCATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_COLOR
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.SOURCE_OF_POWER_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.STATUS_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UPDATE_TEMP_SCREEN_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_LAST_CHARGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import kotlinx.coroutines.*
import java.lang.NumberFormatException
import java.text.DecimalFormat

interface DebugOptionsInterface: BatteryInfoInterface {

    companion object {

        private lateinit var key: String
        private lateinit var value: Any
        private lateinit var valueType: String
    }

    fun DebugFragment.addSettingDialog(pref: SharedPreferences) =
        addSettingCreateDialog(requireContext(), pref)

    fun DebugFragment.changeSettingDialog(pref: SharedPreferences) =
        changeSettingCreateDialog(requireContext(), pref)

    private fun addSettingCreateDialog(context: Context, pref: SharedPreferences) {
        val dialog = MaterialAlertDialogBuilder(context)
        val binding = AddPrefKeyDialogBinding.inflate(LayoutInflater.from(context),
            null, false)
        dialog.setView(binding.root.rootView)
        dialog.apply {
            setPositiveButton(context.getString(R.string.add)) { _, _ ->
                addSettingCreateDialogPositiveButton(context, pref, binding.addPrefKeyEdit,
                    binding.addPrefSpinner, binding.addPrefValueEdit) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }
        val dialogCreate = dialog.create()
        dialogCreate.setOnShowListener {
            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            binding.apply {
                addPrefKeyEdit.addTextChangedListener(addPrefKeyTextChangedListener(
                    addPrefKeyEdit, addPrefValueEdit, pref))
                addPrefSpinner.onItemSelectedListener = addPrefTypeOnItemSelectedListener(
                    addPrefValueEdit)
                addPrefValueEdit.addTextChangedListener(addPrefValueTextChangedListener(
                    addPrefValueEdit, addPrefSpinner, dialogCreate))
            }
        }
        dialogCreate.show()
    }

    private fun addSettingCreateDialogPositiveButton(context: Context, pref: SharedPreferences,
                                                     addPrefKey: TextInputEditText,
                                                     addPrefType: Spinner,
                                                     addPrefValue: TextInputEditText) {
        try {
            when(addPrefType.selectedItemPosition) {
                0 -> addChangeSetting(pref, addPrefKey.text.toString(), addPrefValue.text.toString())
                1 -> addChangeSetting(pref, addPrefKey.text.toString(),
                    addPrefValue.text.toString().toInt())
                2 -> addChangeSetting(pref, addPrefKey.text.toString(),
                    addPrefValue.text.toString().toLong())
                3 -> addChangeSetting(pref, addPrefKey.text.toString(),
                    addPrefValue.text.toString().toFloat())
                4 -> addChangeSetting(context, pref, addPrefKey.text.toString(),
                    addPrefValue.text.toString().toBoolean())
            }
            Toast.makeText(context, context.getString(R.string.setting_added_successfully,
                addPrefKey.text.toString()), Toast.LENGTH_LONG).show()
        }
        catch(e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_adding_settings,
                addPrefKey.text.toString(), e.message ?: e.toString()), Toast.LENGTH_LONG).show()
        }
    }

    private fun addPrefKeyTextChangedListener(addPrefKey: TextInputEditText,
                                              addPrefValue: TextInputEditText,
                                              pref: SharedPreferences): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                addPrefValue.apply {
                    if(text?.isNotEmpty() == true) text?.clear()
                    isEnabled = s.isNotEmpty() && !pref.contains(addPrefKey.text.toString())
                }
            }
        }
    }

    private fun addPrefTypeOnItemSelectedListener(addPrefValue: TextInputEditText):
            AdapterView.OnItemSelectedListener {
        val prefValueInputTypeDef = addPrefValue.inputType
        val prefValueKeyListenerDef = addPrefValue.keyListener
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                with(addPrefValue) {
                    when(position) {
                        0 -> {
                            text?.clear()
                            filters = arrayOf(InputFilter.LengthFilter(3))
                            inputType = prefValueInputTypeDef
                            keyListener = prefValueKeyListenerDef
                        }
                        1, 2 -> {
                            text?.clear()
                            filters = arrayOf(InputFilter.LengthFilter(if(position == 1)
                                Int.MAX_VALUE.toString().count()
                            else Long.MAX_VALUE.toString().count()))
                            inputType = InputType.TYPE_CLASS_NUMBER
                            keyListener = DigitsKeyListener.getInstance("0123456789")
                        }
                        3 -> {
                            text?.clear()
                            filters = arrayOf(InputFilter.LengthFilter(10))
                            inputType = InputType.TYPE_CLASS_NUMBER +
                                    InputType.TYPE_NUMBER_FLAG_DECIMAL
                        }
                        4 -> {
                            text?.clear()
                            filters = arrayOf(InputFilter.LengthFilter(1))
                            inputType = InputType.TYPE_CLASS_NUMBER
                            keyListener = DigitsKeyListener.getInstance("01")
                        }
                    }   
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
    }

    private fun addPrefValueTextChangedListener(addPrefValue: TextInputEditText,
                                                addPrefType: Spinner, dialogCreate: AlertDialog):
            TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(addPrefValue.isEnabled && s.isNotEmpty()
                    && addPrefType.selectedItemPosition == 3) {
                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.first() != '.' && s.last() != '.'
                    if(s.first() == '.') addPrefValue.text?.clear()
                    if(s.contains(".") && addPrefValue.keyListener ==
                        DigitsKeyListener.getInstance("0123456789."))
                        addPrefValue.keyListener =
                            DigitsKeyListener.getInstance("0123456789")
                    else if(addPrefValue.keyListener ==
                        DigitsKeyListener.getInstance("0123456789"))
                        addPrefValue.keyListener =
                            DigitsKeyListener.getInstance("0123456789.")
                }
                else dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                    addPrefValue.isEnabled && s.isNotEmpty()
            }
        }
    }

    private fun changeSettingCreateDialog(context: Context, pref: SharedPreferences) {
        val prefKeysArray = mutableListOf<String>()
        prefKeysArray.addAll(pref.all.keys)
        val dialog = MaterialAlertDialogBuilder(context)
        val binding = ChangePrefKeyDialogBinding.inflate(LayoutInflater.from(context), null,
            false)
        dialog.setView(binding.root.rootView)
        key = ""
        value = ""
        valueType = ""
        dialog.apply {
            setPositiveButton(context.getString(R.string.change)) { _, _ ->
                changeSettingPositiveButton(context, key, value) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }
        val dialogCreate = dialog.create()
        dialogCreate.apply {
            setOnShowListener {
                getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
                binding.apply {
                    changePrefKeyEdit.addTextChangedListener(
                        changePrefKeyTextChangedListener(changePrefValueEdit, pref, prefKeysArray))
                    changePrefValueEdit.addTextChangedListener(changePrefValueTextChangedListener(
                        context, dialogCreate, changePrefKeyEdit, changePrefValueEdit, pref))
                }

            }
            show()
        }
    }

    private fun changeSettingPositiveButton(context: Context, key: String, value: Any) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        try {
            when(key) {
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, UNIT_OF_CHARGE_DISCHARGE_CURRENT,
                VOLTAGE_UNIT, OVERLAY_LOCATION, OVERLAY_SIZE, OVERLAY_FONT, TEXT_SIZE, TEXT_FONT,
                OVERLAY_TEXT_STYLE, TEXT_STYLE, TAB_ON_APPLICATION_LAUNCH,
                FULL_CHARGE_REMINDER_FREQUENCY, STATUS_LAST_CHARGE, SOURCE_OF_POWER_LAST_CHARGE ->
                    addChangeSetting(pref, key, value.toString())

                DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                RESIDUAL_CAPACITY, PERCENT_ADDED, BATTERY_LEVEL_NOTIFY_CHARGED,
                BATTERY_LEVEL_NOTIFY_DISCHARGED, BATTERY_LEVEL_LAST_CHARGE,
                CHARGING_TIME_LAST_CHARGE, PERCENT_ADDED_LAST_CHARGE, CURRENT_CAPACITY_LAST_CHARGE,
                MAX_CHARGE_LAST_CHARGE, AVERAGE_CHARGE_LAST_CHARGE, MIN_CHARGE_LAST_CHARGE ->
                    addChangeSetting(pref, key, value.toString().toInt())

                CAPACITY_ADDED, NUMBER_OF_CYCLES, FAST_CHARGE_WATTS_LAST_CHARGE,
                CAPACITY_ADDED_LAST_CHARGE, VOLTAGE_LAST_CHARGE, MAX_TEMP_CELSIUS_LAST_CHARGE,
                MAX_TEMP_FAHRENHEIT_LAST_CHARGE, AVERAGE_TEMP_CELSIUS_LAST_CHARGE,
                AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE, MIN_TEMP_CELSIUS_LAST_CHARGE,
                MIN_TEMP_FAHRENHEIT_LAST_CHARGE -> addChangeSetting(pref, key,
                    value.toString().toFloat())

                NUMBER_OF_CHARGES, NUMBER_OF_FULL_CHARGES, UPDATE_TEMP_SCREEN_TIME ->
                    addChangeSetting(pref, key, value.toString().toLong())

                else -> addChangeSetting(context, pref, key, value = value == "1")
            }
            Toast.makeText(context, context.getString(R.string.success_change_key, key),
                Toast.LENGTH_LONG).show()
        }
        catch(e: Exception) {
            Toast.makeText(context, context.getString(R.string.error_changing_key, key,
                e.message ?: e.toString()), Toast.LENGTH_LONG).show()
        }
    }

    private fun changePrefKeyTextChangedListener(changePrefValue: TextInputEditText,
                                                 pref: SharedPreferences,
                                                 prefKeysArray: MutableList<String>): TextWatcher {
        val prefValueInputTypeDef = changePrefValue.inputType
        val prefValueKeyListenerDef = changePrefValue.keyListener
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                key = s.toString()
                changePrefValue.isEnabled = key in prefKeysArray
                if(key in prefKeysArray) {
                    when(key) {
                        UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                        UNIT_OF_CHARGE_DISCHARGE_CURRENT, VOLTAGE_UNIT, OVERLAY_LOCATION,
                        OVERLAY_SIZE, OVERLAY_TEXT_STYLE, TEXT_SIZE, TEXT_STYLE,
                        TAB_ON_APPLICATION_LAUNCH, FULL_CHARGE_REMINDER_FREQUENCY,
                        STATUS_LAST_CHARGE, SOURCE_OF_POWER_LAST_CHARGE ->
                            setValueType("string", changePrefValue, pref,
                                prefValueInputTypeDef, prefValueKeyListenerDef)

                        DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                        RESIDUAL_CAPACITY, PERCENT_ADDED, NUMBER_OF_CHARGES, NUMBER_OF_FULL_CHARGES,
                        OVERLAY_OPACITY, OVERLAY_TEXT_COLOR, BATTERY_LEVEL_NOTIFY_CHARGED,
                        BATTERY_LEVEL_NOTIFY_DISCHARGED, UPDATE_TEMP_SCREEN_TIME,
                        BATTERY_LEVEL_LAST_CHARGE, CHARGING_TIME_LAST_CHARGE,
                        PERCENT_ADDED_LAST_CHARGE, CURRENT_CAPACITY_LAST_CHARGE,
                        MAX_CHARGE_LAST_CHARGE, AVERAGE_CHARGE_LAST_CHARGE, MIN_CHARGE_LAST_CHARGE
                        -> setValueType("int|long", changePrefValue, pref,
                            prefValueInputTypeDef, prefValueKeyListenerDef)

                        CAPACITY_ADDED, NUMBER_OF_CYCLES, FAST_CHARGE_WATTS_LAST_CHARGE,
                        CAPACITY_ADDED_LAST_CHARGE, VOLTAGE_LAST_CHARGE, MAX_TEMP_CELSIUS_LAST_CHARGE,
                        MAX_TEMP_FAHRENHEIT_LAST_CHARGE, AVERAGE_TEMP_CELSIUS_LAST_CHARGE,
                        AVERAGE_TEMP_FAHRENHEIT_LAST_CHARGE, MIN_TEMP_CELSIUS_LAST_CHARGE,
                        MIN_TEMP_FAHRENHEIT_LAST_CHARGE -> setValueType("float",
                            changePrefValue, pref, prefValueInputTypeDef, prefValueKeyListenerDef)

                        TOKEN_PREF -> return

                        else -> setValueType("boolean", changePrefValue, pref,
                            prefValueInputTypeDef, prefValueKeyListenerDef)
                    }
                }
                else changePrefValue.text?.clear()
            }
        }
    }

    private fun setValueType(valueType: String, changePrefValue: TextInputEditText,
                             pref: SharedPreferences, prefValueInputTypeDef: Int,
                             prefValueKeyListenerDef: KeyListener) {
        Companion.valueType = valueType
        when(valueType) {
            "string" -> {
                changePrefValue.apply {
                    filters = arrayOf(InputFilter.LengthFilter(3))
                    setText(pref.all.getValue(key).toString())
                }
                when(key) {
                    OVERLAY_LOCATION, OVERLAY_SIZE, OVERLAY_TEXT_STYLE, OVERLAY_FONT, TEXT_SIZE,
                    TEXT_FONT, TEXT_STYLE, TAB_ON_APPLICATION_LAUNCH,
                    FULL_CHARGE_REMINDER_FREQUENCY -> {
                        changePrefValue.apply {
                            inputType = InputType.TYPE_CLASS_NUMBER
                            keyListener = DigitsKeyListener.getInstance("0123456789")
                        }
                    }
                    else -> {
                        changePrefValue.inputType = prefValueInputTypeDef

                        changePrefValue.keyListener = prefValueKeyListenerDef
                    }
                }
            }
            "int|long" -> {
                changePrefValue.apply {
                    filters = arrayOf(InputFilter.LengthFilter(Long.MAX_VALUE.toString().count()))
                    setText(pref.all.getValue(key).toString())
                    inputType = InputType.TYPE_CLASS_NUMBER
                    keyListener = DigitsKeyListener.getInstance("0123456789")   
                }
            }
            "float" -> {
                changePrefValue.apply {
                    filters = arrayOf(InputFilter.LengthFilter(10))
                    setText(pref.all.getValue(key).toString())
                    inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                    keyListener = if(text.toString().contains("."))
                        DigitsKeyListener.getInstance("0123456789") else
                            DigitsKeyListener.getInstance("0123456789.")
                }
            }
            "boolean" -> {
                changePrefValue.apply {
                    filters = arrayOf(InputFilter.LengthFilter(1))
                    setText(if(pref.all.getValue(key).toString() == "true") "1" else "0")
                    inputType = InputType.TYPE_CLASS_NUMBER
                    keyListener = DigitsKeyListener.getInstance("01")
                }
            }
        }
    }

    private fun changePrefValueTextChangedListener(context: Context, dialogCreate: AlertDialog,
                                                   changePrefKey: TextInputEditText,
                                                   changePrefValue: TextInputEditText,
                                                   pref: SharedPreferences): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if(changePrefValue.isEnabled && s.isNotEmpty() &&
                    (changePrefKey.text.toString() == CAPACITY_ADDED
                            || changePrefKey.text.toString() == NUMBER_OF_CYCLES)) {
                    if(s.first() == '.') changePrefValue.setText(pref.all.getValue(key).toString())
                    else if(s.contains(".") && changePrefValue.keyListener ==
                        DigitsKeyListener.getInstance("0123456789."))
                        changePrefValue.keyListener =
                            DigitsKeyListener.getInstance("0123456789")
                    else if(changePrefValue.keyListener ==
                        DigitsKeyListener.getInstance("0123456789"))
                        changePrefValue.keyListener =
                            DigitsKeyListener.getInstance("0123456789.")
                }
                dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                        && s.last() != '.' && when(valueType) {
                    "string" -> {
                        when(key) {
                            UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY ->
                                s.toString() != pref.getString(key, "μAh") &&
                                        s.toString() in context.resources.getStringArray(R.array
                                            .unit_of_measurement_of_current_capacity_values)

                            UNIT_OF_CHARGE_DISCHARGE_CURRENT ->
                                s.toString() != pref.getString(key, "μA") &&
                                        s.toString() in context.resources.getStringArray(R.array
                                            .unit_of_charge_discharge_current_values)

                            VOLTAGE_UNIT -> s.toString() != pref.getString(key, "mV") &&
                                    s.toString() in context.resources.getStringArray(R.array
                                .voltage_unit_values)

                            OVERLAY_LOCATION -> s.toString() != pref.getString(key,
                                "${context.resources.getInteger(
                                    R.integer.overlay_location_default)}") && s.toString() in
                                    context.resources.getStringArray(R.array.overlay_location_values)

                            OVERLAY_SIZE -> s.toString() != pref.getString(key, "2") &&
                                    s.toString() in context.resources.getStringArray(R.array
                                .text_size_values)

                            OVERLAY_FONT -> s.toString() != pref.getString(key, "6")
                                    && s.toString() in context.resources.getStringArray(R.array
                                .fonts_values)

                            OVERLAY_TEXT_STYLE -> s.toString() != pref.getString(key, "0")
                                    && s.toString() in context.resources.getStringArray(R.array
                                .text_style_values)

                            TEXT_SIZE -> s.toString() != pref.getString(
                                key, "2") && s.toString() in context.resources
                                .getStringArray(R.array.text_size_values)

                            TEXT_FONT -> s.toString() != pref.getString(
                                key, "6") && s.toString() in context.resources
                                .getStringArray(R.array.fonts_values)

                            TEXT_STYLE -> s.toString() != pref.getString(
                                key, "0") && s.toString() in context.resources
                                .getStringArray(R.array.fonts_values)

                            TAB_ON_APPLICATION_LAUNCH -> s.toString() != pref.getString(
                                key, "0") && s.toString() in context.resources
                                .getStringArray(R.array.tab_on_application_launch_values)

                            FULL_CHARGE_REMINDER_FREQUENCY -> s.toString() != pref.getString(
                                key,"${context.resources.getInteger(
                                    R.integer.full_charge_reminder_frequency_default)}") &&
                                    s.toString() in context.resources.getStringArray(
                                R.array.full_charge_reminder_frequency_values)

                            else -> s.isNotEmpty() && s.toString() !=
                                    pref.getString(key, null)
                        }
                    }
                    "int|long" -> {
                        if(key != NUMBER_OF_CHARGES && key != NUMBER_OF_FULL_CHARGES
                            && key != UPDATE_TEMP_SCREEN_TIME)
                            s.toString().toInt() != pref.getInt(key, 0)
                        else s.toString().toLong() != pref.getLong(key, 0)
                    }
                    "float" -> s.toString().toFloat() != pref.getFloat(key, 0f)
                    "boolean" -> {
                        val b = s.toString() == "1"
                        b != pref.getBoolean(key, false)
                    }
                    else -> false
                }
                value = s.toString()
            }
        }
    }

    private fun addChangeSetting(pref: SharedPreferences, key: String, value: String) =
        pref.edit().putString(key, value).apply()

    private fun addChangeSetting(pref: SharedPreferences, key: String, value: Int) =
        pref.edit().putInt(key, value).apply()

    private fun addChangeSetting(pref: SharedPreferences, key: String, value: Long) =
        pref.edit().putLong(key, value).apply()

    private fun addChangeSetting(pref: SharedPreferences, key: String, value: Float) =
        pref.edit().putFloat(key, value).apply()

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String,
                                 value: Boolean) {
        pref.edit().putBoolean(key, value).apply()
        if(key == IS_AUTO_DARK_MODE || key == IS_DARK_MODE)
            ThemeHelper.setTheme(context)
        else if(key == IS_FORCIBLY_SHOW_RATE_THE_APP) {
            MainActivity.apply {
                tempFragment = instance?.fragment
                isRecreate = !isRecreate
            }
            (context as? MainActivity)?.recreate()
        }
        else if(key == IS_ENABLED_DEBUG_OPTIONS && !value) {
            val mainContext = context as? MainActivity
            mainContext?.backPressed()
        }
        else if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(context))
            ServiceHelper.startService(context, OverlayService::class.java)
    }

    fun DebugFragment.resetSettingDialog(pref: SharedPreferences) {
        val prefKeysArray = mutableListOf<String>()
        prefKeysArray.addAll(pref.all.keys)
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val binding = ResetPrefKeyDialogBinding.inflate(LayoutInflater.from(context), null,
            false)
        var key = ""
        dialog.setView(binding.root.rootView)
        dialog.setPositiveButton(getString(R.string.reset)) { _, _ ->
            pref.edit().remove(key).apply()
            when (key) {
                IS_AUTO_DARK_MODE, IS_DARK_MODE -> ThemeHelper.setTheme(requireContext())
                IS_FORCIBLY_SHOW_RATE_THE_APP -> {
                    MainActivity.apply {
                        tempFragment = instance?.fragment
                        isRecreate = !isRecreate
                    }
                    (context as? MainActivity)?.recreate()
                }
                IS_ENABLED_DEBUG_OPTIONS -> {
                    val mainContext = context as? MainActivity
                    mainContext?.backPressed()
                }
            }
            Toast.makeText(requireContext(), getString(R.string.key_successfully_reset, key),
                Toast.LENGTH_LONG).show()
        }
        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        val dialogCreate = dialog.create()
        with(dialogCreate) {
            setOnShowListener {
                getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
                binding.resetPrefKeyEdit.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) { }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                                   after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in prefKeysArray
                        key = s.toString()
                    }
                })
            }
            show()
        }
    }

    fun DebugFragment.resetSettingsDialog(pref: SharedPreferences) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setIcon(R.drawable.ic_faq_question_24dp)
            setTitle(getString(R.string.reset_settings))
            setMessage(getString(R.string.are_you_sure))
            setPositiveButton(getString(R.string.reset)) { _, _ ->
                pref.edit().clear().apply()
                Toast.makeText(context, R.string.settings_reset_successfully,
                    Toast.LENGTH_LONG).show()
                (context as? MainActivity)?.recreate()
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }
    fun DebugFragment.addNumberOfCyclesDialog() {
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val numberOfCyclesPref = pref.getFloat(NUMBER_OF_CYCLES, 0f)
        val numberOfCycles = (numberOfCyclesPref * 100.0f).toInt()
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val binding = AddNumberOfCyclesDialogBinding.inflate(LayoutInflater.from(context),
            null, false)
        dialog.setView(binding.root.rootView)
        binding.addNumberOfCyclesEdit.setText("$numberOfCycles")
        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->
            pref.edit().putFloat(NUMBER_OF_CYCLES,
                binding.addNumberOfCyclesEdit.text.toString().toFloat() / 100.0f).apply()
        }
        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        val dialogCreate = dialog.create()
        addNumberOfCyclesCreateDialogCreateShowListener(requireContext(), dialogCreate,
            binding.addNumberOfCyclesEdit, pref)
        dialogCreate.show()
    }

    private fun addNumberOfCyclesCreateDialogCreateShowListener(context: Context,
                                                             dialogCreate: AlertDialog,
                                                             addNumberOfCycles: TextInputEditText,
                                                             pref: SharedPreferences) {
        dialogCreate.setOnShowListener {
            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            addNumberOfCycles.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                        s.isNotEmpty() && (s.toString().toFloat() / 100.0f) !=
                                pref.getFloat(NUMBER_OF_CYCLES, 0f)
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
    
    fun DebugFragment.onChangeScreenTime() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val binding = ChangeScreenTimeDialogBinding.inflate(LayoutInflater.from(requireContext()),
            null, false)
        dialog.setView(binding.root.rootView)
        binding.changeScreenTimeEdit.setText((CapacityInfoService.instance?.screenTime ?: 0)
            .toString())
        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->
            CapacityInfoService.instance?.screenTime =
                binding.changeScreenTimeEdit.text.toString().toLong()
        }
        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        val dialogCreate = dialog.create()
        changeScreenTimeDialogCreateShowListener(requireContext(), dialogCreate,
            binding.changeScreenTimeEdit)
        dialogCreate.show()
    }

    private fun changeScreenTimeDialogCreateShowListener(context: Context, dialogCreate: AlertDialog,
                                                         changeScreeTime: TextInputEditText) {
        dialogCreate.setOnShowListener {
            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            changeScreeTime.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                        s.isNotEmpty() && s.toString().toLong() >= 0L &&
                                s.toString().toLong() < Long.MAX_VALUE
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

    fun DebugFragment.onChangeNominalBatteryVoltage() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val binding = ChangeNominalBatteryVoltageDialogBinding.inflate(LayoutInflater.from(
            requireContext()), null, false)
        dialog.setView(binding.root.rootView)
        binding.changeNominalBatteryVoltageEdit.setText("${pref.getInt(NOMINAL_BATTERY_VOLTAGE_PREF,
            resources.getInteger(R.integer.nominal_battery_voltage_default))}")
        dialog.setPositiveButton(requireContext().getString(R.string.change)) { _, _ ->
            pref.edit().putInt(NOMINAL_BATTERY_VOLTAGE_PREF,
                binding.changeNominalBatteryVoltageEdit.text.toString().toInt()).apply()
            NOMINAL_BATTERY_VOLTAGE =
                binding.changeNominalBatteryVoltageEdit.text.toString().toDouble() / 100.0
            Toast.makeText(requireContext(), "$NOMINAL_BATTERY_VOLTAGE",
                Toast.LENGTH_LONG).show()
        }
        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        val dialogCreate = dialog.create()
        changeNominalBatteryVoltageDialogCreateShowListener(requireContext(), dialogCreate,
            binding.changeNominalBatteryVoltageEdit, pref)
        dialogCreate.show()
    }

    private fun changeNominalBatteryVoltageDialogCreateShowListener(context: Context,
                                                              dialogCreate: AlertDialog,
                                                              nominalBatteryVoltage: TextInputEditText,
                                                                    pref: SharedPreferences) {
        dialogCreate.setOnShowListener {
            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            nominalBatteryVoltage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                               after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                        s.isNotEmpty() && s.toString().toInt() !=
                                pref.getInt(NOMINAL_BATTERY_VOLTAGE_PREF, context.resources
                                    .getInteger(R.integer.nominal_battery_voltage_default)) &&
                                s.toString().toInt() >= context.resources.getInteger(
                            R.integer.nominal_battery_voltage_min) && s.toString().toInt() <=
                                context.resources.getInteger(R.integer.nominal_battery_voltage_max)
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

    fun DebugFragment.getBatteryWearNew(): String {
        val historyList = HistoryDB(requireContext()).readDB()
        return if(historyList.count() >= 5) {
            var residualCapacity = 0
            for(i in historyList.lastIndex downTo historyList.lastIndex -
                    Constants.BATTERY_WEAR_NEW_COUNT + 1) {
                residualCapacity += historyList[i].residualCapacity
            }
            residualCapacity /= Constants.BATTERY_WEAR_NEW_COUNT
            getBatteryWearNew(requireContext(), residualCapacity)
        }
        else getBatteryWearPref(requireContext())
    }

    private fun getBatteryWearNew(context: Context, residualCapacity: Int): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val designCapacity = pref.getInt(DESIGN_CAPACITY, context.resources
            .getInteger(R.integer.min_design_capacity)).toDouble()
        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))
        var newResidualCapacity = residualCapacity.toDouble() / if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0
        if(newResidualCapacity < 0.0) newResidualCapacity /= -1.0
        return if(isCapacityInWh) context.getString(R.string.battery_wear_new_wh_summary,
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                "${DecimalFormat("#.#").format(
                    100 - (newResidualCapacity / designCapacity) * 100)}%" else "0%",
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                DecimalFormat("#.#").format(
                    getCapacityInWh(designCapacity - newResidualCapacity)) else "0")
        else context.getString(R.string.battery_wear_new_summary, if (newResidualCapacity > 0 &&
            newResidualCapacity < designCapacity) "${DecimalFormat("#.#").format(
            100 - ((newResidualCapacity / designCapacity) * 100))}%" else "0%",
            if (newResidualCapacity > 0 && newResidualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - newResidualCapacity) else "0"
        )
    }

    private fun getBatteryWearPref(context: Context): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val designCapacity = pref.getInt(DESIGN_CAPACITY, context.resources
            .getInteger(R.integer.min_design_capacity)).toDouble()
        val residualCapacity = pref.getInt(RESIDUAL_CAPACITY, 0)
        val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
            context.resources.getBoolean(R.bool.is_capacity_in_wh))
        var newResidualCapacity = residualCapacity.toDouble() / if(pref.getString(
                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") 1000.0
        else 100.0
        if(newResidualCapacity < 0.0) newResidualCapacity /= -1.0
        return if(isCapacityInWh) context.getString(R.string.battery_wear_new_wh_summary,
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                "${DecimalFormat("#.#").format(
                    100 - (newResidualCapacity / designCapacity) * 100)}%" else "0%",
            if(newResidualCapacity > 0 && newResidualCapacity < designCapacity)
                DecimalFormat("#.#").format(
                    getCapacityInWh(designCapacity - newResidualCapacity)) else "0")
        else context.getString(R.string.battery_wear_new_summary, if (newResidualCapacity > 0 &&
            newResidualCapacity < designCapacity) "${DecimalFormat("#.#").format(
            100 - ((newResidualCapacity / designCapacity) * 100))}%" else "0%",
            if (newResidualCapacity > 0 && newResidualCapacity < designCapacity) DecimalFormat(
                "#.#").format(designCapacity - newResidualCapacity) else "0"
        )
    }

    fun DebugFragment.onAddCustomHistory(pref: SharedPreferences,
                                         addHistoryList: ArrayList<Preference?>,
                                         historyCount: Preference? = null) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
        val binding = AddCustomHistoryDialogBinding.inflate(LayoutInflater.from(requireContext()),
            null, false)
        dialog.apply {
            setView(binding.root.rootView)
            binding.historyCountEdit.setText("${HistoryHelper.getHistoryCount(requireContext())}")
            setPositiveButton(requireContext().getString(R.string.add)) { _, _ ->
                val oldHistoryCount = HistoryHelper.getHistoryCount(requireContext())
                var addHistoryCount = 0L
                CoroutineScope(Dispatchers.Default).launch {
                    for(count in 1..(
                            binding.historyCountEdit.text?.toString()?.toInt() ?: 1)) {
                        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                            R.integer.min_design_capacity))
                        val date =  DateHelper.getDate((1..31).random(),
                            (1..12).random(), DateHelper.getCurrentYear())
                        val residualCapacity = if(pref.getString(
                                UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
                                ((designCapacity * 0.01).toInt() * 1000..(designCapacity + (
                                (designCapacity / 1000) * 5)) * 1000).random()
                        else ((designCapacity * 0.01).toInt() * 100..(designCapacity + (
                                (designCapacity / 1000) * 5)) * 100).random()
                        HistoryHelper.addHistory(requireContext(), date, residualCapacity)
                        addHistoryCount =
                            HistoryHelper.getHistoryCount(requireContext()) - oldHistoryCount
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "$addHistoryCount",
                            Toast.LENGTH_LONG).show()
                        addHistoryList.forEach {
                            it?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                        }
                        historyCount?.summary = "${HistoryHelper.getHistoryCount(requireContext())}"
                    }
                }
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            val dialogCreate = dialog.create()
            addHistoryDialogCreateShowListener(requireContext(), dialogCreate,
                binding.historyCountEdit)
            dialogCreate.show()
        }
    }

    private fun addHistoryDialogCreateShowListener(context: Context,
                                                             dialogCreate: AlertDialog,
                                                             historyCount: TextInputEditText) {
        with(dialogCreate) {
            setOnShowListener {
                getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                    historyCount.text?.toString()!!.toInt() > 0 && !HistoryHelper.isHistoryMax(context)
                }
                catch (e: NumberFormatException) { false }
                catch (e: Exception) { Toast.makeText(context, e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
                    getButton(DialogInterface.BUTTON_POSITIVE).isEnabled
                }
                historyCount.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable) {}

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                                   after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                               count: Int) {
                        getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = try {
                            (HistoryHelper.isHistoryEmpty(context) && s.isNotEmpty() &&
                                    s.toString().toInt() > 0 && s.toString().toInt() <=
                                    Constants.HISTORY_COUNT_MAX) || (!HistoryHelper
                                        .isHistoryEmpty(context) && s.isNotEmpty() &&
                                    s.toString().toInt() > 0 && HistoryHelper
                                        .getHistoryCount(context) + s.toString().toInt() <=
                                    Constants.HISTORY_COUNT_MAX)
                        }
                        catch (e: NumberFormatException) { false }
                        catch (e: Exception) { Toast.makeText(context,
                            e.message ?: e.toString(), Toast.LENGTH_LONG).show()
                            getButton(DialogInterface.BUTTON_POSITIVE).isEnabled
                        }
                    }
                })
            }   
        }
    }
}