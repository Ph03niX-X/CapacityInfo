package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
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
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper.setLocale
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_NOTIFY_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_CHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_NOTIFY_DISCHARGED_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CHARGING_CURRENT_LEVEL_NOTIFY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FAKE_BATTERY_WEAR_VALUE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_UNIT
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

interface DebugOptionsInterface {

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

        val view = LayoutInflater.from(context).inflate(R.layout.add_pref_key_dialog,
            null)

        dialog.setView(view)

        val addPrefKey = view.findViewById<TextInputEditText>(R.id.add_pref_key_edit)

        val addPrefType = view.findViewById<Spinner>(R.id.add_pref_spinner)

        val addPrefValue = view.findViewById<TextInputEditText>(
            R.id.add_pref_value_edit)

        dialog.apply {

            setPositiveButton(context.getString(R.string.add)) { _, _ ->
                addSettingCreateDialogPositiveButton(context, pref, addPrefKey, addPrefType,
                    addPrefValue) }

            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }

        val dialogCreate = dialog.create()

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            addPrefKey.addTextChangedListener(addPrefKeyTextChangedListener(addPrefKey,
                addPrefValue, pref))

            addPrefType.onItemSelectedListener = addPrefTypeOnItemSelectedListener(addPrefValue)

            addPrefValue.addTextChangedListener(addPrefValueTextChangedListener(addPrefValue,
                addPrefType, dialogCreate))
        }

        dialogCreate.show()
    }

    private fun addSettingCreateDialogPositiveButton(context: Context, pref: SharedPreferences,
                                                     addPrefKey: TextInputEditText,
                                                     addPrefType: Spinner,
                                                     addPrefValue: TextInputEditText) {

        try {

            when(addPrefType.selectedItemPosition) {

                0 -> addChangeSetting(context, pref, addPrefKey.text.toString(),
                    addPrefValue.text.toString())

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

                if(addPrefValue.text?.isNotEmpty() == true) addPrefValue.text?.clear()

                addPrefValue.isEnabled = s.isNotEmpty() && !pref.contains(addPrefKey.text.toString())
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

                when(position) {

                    0 -> {

                        addPrefValue.text?.clear()

                        addPrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                        addPrefValue.inputType = prefValueInputTypeDef

                        addPrefValue.keyListener = prefValueKeyListenerDef
                    }

                    1, 2 -> {

                        addPrefValue.text?.clear()

                        addPrefValue.filters = arrayOf(InputFilter.LengthFilter(if(position == 1)
                            Int.MAX_VALUE.toString().count()
                        else Long.MAX_VALUE.toString().count()))

                        addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                        addPrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                    }

                    3 -> {

                        addPrefValue.text?.clear()

                        addPrefValue.filters = arrayOf(InputFilter.LengthFilter(10))

                        addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER +
                                InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }

                    4 -> {

                        addPrefValue.text?.clear()

                        addPrefValue.filters = arrayOf(InputFilter.LengthFilter(1))

                        addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                        addPrefValue.keyListener = DigitsKeyListener.getInstance("01")
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

        val view = LayoutInflater.from(context).inflate(R.layout.change_pref_key_dialog,
            null)

        dialog.setView(view)

        val changePrefKey = view.
        findViewById<TextInputEditText>(R.id.change_pref_key_edit)

        val changePrefValue = view.
        findViewById<TextInputEditText>(R.id.change_pref_value_edit)

        key = ""
        value = ""
        valueType = ""

        dialog.apply {

            setPositiveButton(context.getString(R.string.change)) { _, _ ->
                changeSettingPositiveButton(context, key, value) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }

        val dialogCreate = dialog.create()

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changePrefKey.addTextChangedListener(changePrefKeyTextChangedListener(changePrefValue,
                pref, prefKeysArray))

            changePrefValue.addTextChangedListener(changePrefValueTextChangedListener(context,
                dialogCreate, changePrefKey, changePrefValue, pref))
        }

        dialogCreate.show()
    }

    private fun changeSettingPositiveButton(context: Context, key: String, value: Any) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        try {

            when(key) {

                LANGUAGE, UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, UNIT_OF_CHARGE_DISCHARGE_CURRENT,
                VOLTAGE_UNIT, OVERLAY_SIZE, OVERLAY_FONT, TEXT_SIZE, TEXT_FONT,
                OVERLAY_TEXT_STYLE, TEXT_STYLE, TAB_ON_APPLICATION_LAUNCH,
                FREQUENCY_OF_AUTO_BACKUP_SETTINGS -> addChangeSetting(
                    context, pref, key, value.toString())

                DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                RESIDUAL_CAPACITY, PERCENT_ADDED, BATTERY_LEVEL_NOTIFY_CHARGED,
                BATTERY_NOTIFY_CHARGED_VOLTAGE, BATTERY_LEVEL_NOTIFY_DISCHARGED,
                BATTERY_NOTIFY_DISCHARGED_VOLTAGE, CHARGING_CURRENT_LEVEL_NOTIFY,
                FAKE_BATTERY_WEAR_VALUE -> addChangeSetting(pref, key, value.toString().toInt())

                CAPACITY_ADDED, NUMBER_OF_CYCLES -> addChangeSetting(pref, key,
                    value.toString().toFloat())

                NUMBER_OF_CHARGES, NUMBER_OF_FULL_CHARGES -> addChangeSetting(pref, key,
                    value.toString().toLong())

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

                        LANGUAGE, UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                        UNIT_OF_CHARGE_DISCHARGE_CURRENT, VOLTAGE_UNIT, OVERLAY_SIZE,
                        OVERLAY_TEXT_STYLE, TEXT_SIZE, TEXT_STYLE, TAB_ON_APPLICATION_LAUNCH,
                        FREQUENCY_OF_AUTO_BACKUP_SETTINGS -> setValueType("string",
                            changePrefValue, pref, prefValueInputTypeDef, prefValueKeyListenerDef)

                        DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                        RESIDUAL_CAPACITY, PERCENT_ADDED, NUMBER_OF_CHARGES, NUMBER_OF_FULL_CHARGES,
                        OVERLAY_OPACITY, BATTERY_LEVEL_NOTIFY_CHARGED,
                        BATTERY_NOTIFY_CHARGED_VOLTAGE, BATTERY_NOTIFY_DISCHARGED_VOLTAGE,
                        BATTERY_LEVEL_NOTIFY_DISCHARGED, CHARGING_CURRENT_LEVEL_NOTIFY,
                        FAKE_BATTERY_WEAR_VALUE -> setValueType("int|long",
                            changePrefValue, pref, prefValueInputTypeDef, prefValueKeyListenerDef)

                        CAPACITY_ADDED, NUMBER_OF_CYCLES ->
                            setValueType("float", changePrefValue, pref,
                                prefValueInputTypeDef, prefValueKeyListenerDef)

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

                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                changePrefValue.setText(pref.all.getValue(key).toString())

                when(key) {

                    OVERLAY_SIZE, OVERLAY_TEXT_STYLE, OVERLAY_FONT, TEXT_SIZE, TEXT_FONT,
                    TEXT_STYLE, TAB_ON_APPLICATION_LAUNCH, FREQUENCY_OF_AUTO_BACKUP_SETTINGS -> {
                        changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                        changePrefValue.keyListener = DigitsKeyListener.getInstance(
                            "0123456789")
                    }

                    else -> {
                        changePrefValue.inputType = prefValueInputTypeDef

                        changePrefValue.keyListener = prefValueKeyListenerDef
                    }
                }
            }

            "int|long" -> {

                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(Long.MAX_VALUE.toString()
                    .count()))

                changePrefValue.setText(pref.all.getValue(key).toString())

                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
            }

            "float" -> {

                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(10))

                changePrefValue.setText(pref.all.getValue(key).toString())

                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER +
                        InputType.TYPE_NUMBER_FLAG_DECIMAL

                if(changePrefValue.text.toString().contains("."))
                    changePrefValue.keyListener =
                        DigitsKeyListener.getInstance("0123456789")
                else changePrefValue.keyListener =
                    DigitsKeyListener.getInstance("0123456789.")
            }

            "boolean" -> {

                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(1))

                changePrefValue.setText(if(pref.all.getValue(key).toString() == "true") "1"
                else "0")

                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                changePrefValue.keyListener = DigitsKeyListener.getInstance("01")
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

                            LANGUAGE -> s.toString() != pref.getString(key, null)
                                    && s.toString() in context.resources.getStringArray(
                                R.array.languages_codes)

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

                            FREQUENCY_OF_AUTO_BACKUP_SETTINGS -> s.toString() != pref.getString(key,
                                "1") && s.toString() in context.resources.getStringArray(
                                R.array.frequency_of_auto_backup_settings_values)

                            else -> s.isNotEmpty() && s.toString() !=
                                    pref.getString(key, null)
                        }
                    }

                    "int|long" -> {

                        if(key != NUMBER_OF_CHARGES && key != NUMBER_OF_FULL_CHARGES)
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

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String,
                                 value: String) {

        pref.edit().putString(key, value).apply()

        if(key == LANGUAGE) {

            MainActivity.isRecreate = !MainActivity.isRecreate

            MainActivity.tempFragment = MainActivity.instance?.fragment

            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                context.setLocale(value)

            (context as? MainActivity)?.recreate()
        }
    }

    private fun addChangeSetting(pref: SharedPreferences, key: String,
                                 value: Int) {

        pref.edit().putInt(key, value).apply()
    }

    private fun addChangeSetting(pref: SharedPreferences, key: String,
                                 value: Long) {

        pref.edit().putLong(key, value).apply()
    }

    private fun addChangeSetting(pref: SharedPreferences, key: String,
                                 value: Float) {

        pref.edit().putFloat(key, value).apply()
    }

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String,
                                 value: Boolean) {

        pref.edit().putBoolean(key, value).apply()

        if(key == IS_AUTO_DARK_MODE || key == IS_DARK_MODE)
            ThemeHelper.setTheme(context)

        else if(key == IS_FORCIBLY_SHOW_RATE_THE_APP) {

            MainActivity.tempFragment = MainActivity.instance?.fragment

            MainActivity.isRecreate = !MainActivity.isRecreate

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

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.reset_pref_key_dialog,
            null)

        var key = ""

        dialog.setView(view)

        val resetPrefKey = view.findViewById<TextInputEditText>(
            R.id.reset_pref_key_edit)

        dialog.setPositiveButton(getString(R.string.reset)) { _, _ ->

            pref.edit().remove(key).apply()

            when (key) {

                IS_AUTO_DARK_MODE, IS_DARK_MODE -> ThemeHelper.setTheme(requireContext())
                LANGUAGE -> {

                    MainActivity.isRecreate = !MainActivity.isRecreate

                    MainActivity.tempFragment = MainActivity.instance?.fragment

                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                        requireContext().setLocale(MainApp.defLang)

                    (context as? MainActivity)?.recreate()
                }

                IS_FORCIBLY_SHOW_RATE_THE_APP -> {

                    MainActivity.tempFragment = MainActivity.instance?.fragment

                    MainActivity.isRecreate = !MainActivity.isRecreate

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

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            resetPrefKey.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int)
                { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.toString() in prefKeysArray

                    key = s.toString()
                }
            })
        }

        dialogCreate.show()
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

    fun DebugFragment.onExportHistory(intent: Intent?) {

        val dbPath = "${requireContext().filesDir?.parent}/databases/History.db"
        val dbName = "History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                if(HistoryHelper.isHistoryEmpty(requireContext()))
                    throw IOException (getString(R.string.history_is_empty))

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    requireContext().let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                delay(1000L)
                pickerDir?.findFile(dbName)?.delete()
                val outputStream = pickerDir?.createFile("application/vnd.sqlite3",
                    dbName)?.uri?.let {
                    requireContext().contentResolver?.openOutputStream(it)
                    }

                val fileInputStream = FileInputStream(dbPath)
                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = fileInputStream.read(buffer)

                    if(read != -1)
                        outputStream?.write(buffer, 0, read)
                    else break
                }

                fileInputStream.close()
                outputStream?.flush()
                outputStream?.close()

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(
                        R.string.history_exported_successfully), Toast.LENGTH_LONG).show()
                }

                MainActivity.isOnBackPressed = true
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), "${getString(R.string
                        .error_exporting_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun DebugFragment.onImportHistory(uri: Uri?, preferencesList: ArrayList<Preference?>) {

        val dbPath = "${requireContext().filesDir?.parent}/databases/History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                delay(1000L)
                File(dbPath).deleteOnExit()
                File("$dbPath-journal").deleteOnExit()

                File(dbPath).createNewFile()

                val fileOutputStream = FileOutputStream(dbPath)
                val inputStream = uri?.let {
                    requireContext().contentResolver?.openInputStream(it) }

                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = inputStream?.read(buffer) ?: -1

                    if(read != -1)
                        fileOutputStream.write(buffer, 0, read)
                    else break
                }

                inputStream?.close()
                fileOutputStream.flush()
                fileOutputStream.close()

                MainActivity.isOnBackPressed = true

                val isHistoryNotEmpty = HistoryHelper.isHistoryNotEmpty(requireContext())

                withContext(Dispatchers.Main) {

                    MainActivity.instance?.navigation?.menu?.findItem(R.id.history_navigation)
                        ?.isVisible = isHistoryNotEmpty

                    preferencesList.forEach {

                        if(it?.key == "add_history" || it?.key == "add_ten_history"
                            || it?.key == "add_fifty_history")
                            it.isEnabled = isHistoryNotEmpty &&
                                    !HistoryHelper.isHistoryMax(requireContext())
                        else it?.isEnabled = isHistoryNotEmpty
                    }
                }

                if(!isHistoryNotEmpty)
                    throw IOException(getString(R.string.history_is_empty))

                else withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(
                        R.string.history_imported_successfully), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), "${getString(R.string
                        .error_importing_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun DebugFragment.onExportSettings(intent: Intent?) {

        val prefPath = "${requireContext().filesDir?.parent}/shared_prefs/" +
                "${requireContext().packageName}_preferences.xml"
        val prefName = File(prefPath).name

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    requireContext().let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                pickerDir?.findFile(prefName)?.delete()

                val outputStream = pickerDir?.createFile("text/xml",
                    prefName)?.uri?.let {
                    requireContext().contentResolver?.openOutputStream(it)
                }

                val fileInputStream = FileInputStream(prefPath)
                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = fileInputStream.read(buffer)

                    if(read != -1)
                        outputStream?.write(buffer, 0, read)
                    else break
                }

                fileInputStream.close()
                outputStream?.flush()
                outputStream?.close()

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), getString(
                        R.string.successful_export_of_settings, prefName), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), getString(R.string.error_exporting_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun DebugFragment.onImportSettings(uri: Uri?) {

        val prefPath = "${requireContext().filesDir?.parent}/shared_prefs/" +
                "${requireContext().packageName}_preferences.xml"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), R.string.import_settings_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it,CapacityInfoService::class.java)
                        }

                    if(OverlayService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it, OverlayService::class.java)
                        }
                }

                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

                val prefArrays: HashMap<String, Any?> = hashMapOf()

                pref.all.forEach {

                    when(it.key) {

                        BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, DESIGN_CAPACITY, CAPACITY_ADDED,
                        LAST_CHARGE_TIME, PERCENT_ADDED, RESIDUAL_CAPACITY, IS_SHOW_INSTRUCTION ->
                            prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                if(File(prefPath).exists()) File(prefPath).delete()

                File(prefPath).createNewFile()

                val fileOutputStream = FileOutputStream(prefPath)
                val inputStream = uri?.let {
                    requireContext().contentResolver?.openInputStream(it) }

                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = inputStream?.read(buffer) ?: -1

                    if(read != -1)
                        fileOutputStream.write(buffer, 0, read)
                    else break
                }

                inputStream?.close()
                fileOutputStream.flush()
                fileOutputStream.close()

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    MainApp.restartApp(requireContext(), prefArrays)
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), getString(R.string.error_importing_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}