package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.DebugActivity
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AUTO_DARK_MODE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DARK_MODE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_UNIT
import java.lang.Exception

interface DebugOptionsInterface : ServiceInterface {

    fun addSettingDialog(context: Context, pref: SharedPreferences) =
        addSettingCreateDialog(context, pref)

    fun changeSettingDialog(context: Context, pref: SharedPreferences) =
        changeSettingCreateDialog(context, pref)

    private fun addSettingCreateDialog(context: Context, pref: SharedPreferences) {

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.add_pref_key, null)

        dialog.setView(view)

        val addPrefKey = view.findViewById<EditText>(R.id.add_pref_key_edit)

        val addPrefType = view.findViewById<Spinner>(R.id.add_pref_spinner)

        val addPrefValue = view.findViewById<EditText>(R.id.add_pref_value_edit)

        val prefValueInputTypeDef = addPrefValue.inputType

        val prefValueKeyListenerDef = addPrefValue.keyListener

        dialog.apply {

            setPositiveButton(context.getString(R.string.add)) { _, _ ->

                try {

                    when(addPrefType.selectedItemPosition) {

                        0 -> addChangeSetting(context, pref, addPrefKey.text.toString(), addPrefValue.text.toString())

                        1 -> addChangeSetting(context, pref, addPrefKey.text.toString(), addPrefValue.text.toString().toInt())

                        2 -> addChangeSetting(context, pref, addPrefKey.text.toString(), addPrefValue.text.toString().toLong())

                        3 -> addChangeSetting(context, pref, addPrefKey.text.toString(), addPrefValue.text.toString().toFloat())

                        4 -> addChangeSetting(context, pref, addPrefKey.text.toString(), addPrefValue.text.toString().toBoolean())
                    }

                    Toast.makeText(context, context.getString(R.string.setting_added_successfully, addPrefKey.text.toString()), Toast.LENGTH_LONG).show()
                }
                catch(e: Exception) {

                    Toast.makeText(context, context.getString(R.string.error_adding_settings, addPrefKey.text.toString(), e.message),
                        Toast.LENGTH_LONG).show()
                }


            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }
        
        val dialogCreate = dialog.create()
        
        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false
            
            addPrefKey.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    if(addPrefValue.text.isNotEmpty()) addPrefValue.text.clear()

                    addPrefValue.isEnabled = s.isNotEmpty() && !pref.contains(addPrefKey.text.toString())
                }
            })

            addPrefType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    when(position) {

                        0 -> {

                            addPrefValue.text.clear()

                            addPrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                            addPrefValue.inputType = prefValueInputTypeDef

                            addPrefValue.keyListener = prefValueKeyListenerDef
                        }

                        1, 2 -> {

                            addPrefValue.text.clear()

                            addPrefValue.filters = arrayOf(InputFilter.LengthFilter(if(position == 1) Int.MAX_VALUE.toString().count() else Long.MAX_VALUE.toString().count()))

                            addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                            addPrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                        }

                        3 -> {

                            addPrefValue.text.clear()

                            addPrefValue.filters = arrayOf(InputFilter.LengthFilter(10))

                            addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
                        }

                        4 -> {

                            addPrefValue.text.clear()

                            addPrefValue.filters = arrayOf(InputFilter.LengthFilter(1))

                            addPrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                            addPrefValue.keyListener = DigitsKeyListener.getInstance("01")
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }

            addPrefValue.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {}

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    if(addPrefValue.isEnabled && s.isNotEmpty() && addPrefType.selectedItemPosition == 3) {

                        dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.first() != '.' && s.last() != '.'

                        if(s.first() == '.') addPrefValue.text.clear()

                        if(s.contains(".") && addPrefValue.keyListener == DigitsKeyListener.getInstance("0123456789."))
                            addPrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                        else if(addPrefValue.keyListener == DigitsKeyListener.getInstance("0123456789"))
                            addPrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")
                    }
                    else dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = addPrefValue.isEnabled && s.isNotEmpty()
                }
            })
        }

        dialogCreate.show()
    }

    private fun changeSettingCreateDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        prefKeysArray.addAll(pref.all.keys)

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_pref_key, null)

        dialog.setView(view)

        val changePrefKey = view.findViewById<EditText>(R.id.change_pref_key_edit)

        val changePrefValue = view.findViewById<EditText>(R.id.change_pref_value_edit)

        var key = ""
        var value: Any = ""
        var valueType = ""

        dialog.apply {

            setPositiveButton(context.getString(R.string.change)) { _, _ -> changeSettingPositiveButton(context, key, value) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        }

        val dialogCreate = dialog.create()

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            changePrefKey.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    key = s.toString()
                    changePrefValue.isEnabled = key in prefKeysArray

                    if(key in prefKeysArray) {

                        when(key) {

                            LANGUAGE, UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                            UNIT_OF_CHARGE_DISCHARGE_CURRENT, VOLTAGE_UNIT -> {

                                valueType = "string"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                                changePrefValue.setText(pref.all.getValue(key).toString())
                            }

                            DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                            RESIDUAL_CAPACITY, PERCENT_ADDED, NUMBER_OF_CHARGES -> {

                                valueType = "int|long"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(Long.MAX_VALUE.toString().count()))

                                changePrefValue.setText(pref.all.getValue(key).toString())

                                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                                changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                            }

                            CAPACITY_ADDED, NUMBER_OF_CYCLES -> {

                                valueType = "float"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(10))

                                changePrefValue.setText(pref.all.getValue(key).toString())

                                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

                                if(changePrefValue.text.toString().contains("."))
                                    changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                                else changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")
                            }

                            else -> {

                                valueType = "boolean"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(1))

                                changePrefValue.setText(if(pref.all.getValue(key).toString() == "true") "1" else "0")

                                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                                changePrefValue.keyListener = DigitsKeyListener.getInstance("01")
                            }
                        }
                    }

                    else changePrefValue.text.clear()
                }
            })

            changePrefValue.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    if(changePrefValue.isEnabled && s.isNotEmpty() &&
                        (changePrefKey.text.toString() == CAPACITY_ADDED || changePrefKey.text.toString() == NUMBER_OF_CYCLES)) {

                        if(s.first() == '.') changePrefValue.text.clear()

                        if(s.contains(".") && changePrefValue.keyListener == DigitsKeyListener.getInstance("0123456789."))
                            changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                        else if(changePrefValue.keyListener == DigitsKeyListener.getInstance("0123456789"))
                            changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")
                    }

                    when(key) {

                        LANGUAGE -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.languages_codes)

                        UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.unit_of_measurement_of_current_capacity_values)

                        UNIT_OF_CHARGE_DISCHARGE_CURRENT -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.unit_of_charge_discharge_current_values)

                        VOLTAGE_UNIT -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.voltage_unit_values)

                        else -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                    }

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty() && when(valueType) {

                        "string" -> s.toString() != pref.getString(key, "")

                        "int|long" -> {

                            if(key != NUMBER_OF_CHARGES)
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
            })
        }

        dialogCreate.show()
    }

    private fun changeSettingPositiveButton(context: Context, key: String, value: Any) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        try {

            when(key) {

                LANGUAGE, UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY,
                UNIT_OF_CHARGE_DISCHARGE_CURRENT, VOLTAGE_UNIT -> addChangeSetting(context, pref, key, value.toString())

                DESIGN_CAPACITY, LAST_CHARGE_TIME, BATTERY_LEVEL_WITH, BATTERY_LEVEL_TO,
                RESIDUAL_CAPACITY, PERCENT_ADDED -> addChangeSetting(context, pref, key, value.toString().toInt())

                CAPACITY_ADDED, NUMBER_OF_CYCLES -> addChangeSetting(context, pref, key, value.toString().toFloat())

                NUMBER_OF_CHARGES -> addChangeSetting(context, pref, key, value.toString().toLong())

                else -> addChangeSetting(context, pref, key, value = value == "1")
            }

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()
        }

        catch (e: Exception) {

            Toast.makeText(context, context.getString(R.string.error_changing_key, key, e.message), Toast.LENGTH_LONG).show()
        }
    }

    fun addChangeSetting(context: Context, pref: SharedPreferences, key: String, value: String) {

        pref.edit().putString(key, value).apply()

        if(key == LANGUAGE) {

            LocaleHelper.setLocale(context, value)

            MainActivity.instance?.recreate()

            (context as? DebugActivity)?.recreate()
        }
    }

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String, value: Int) {

        pref.edit().putInt(key, value).apply()
    }

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String, value: Long) {

        pref.edit().putLong(key, value).apply()
    }

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String, value: Float) {

        pref.edit().putFloat(key, value).apply()
    }

    private fun addChangeSetting(context: Context, pref: SharedPreferences, key: String, value: Boolean) {

        pref.edit().putBoolean(key, value).apply()

        if(key == IS_AUTO_DARK_MODE || key == IS_DARK_MODE) {

            MainActivity.instance?.recreate()

            (context as? DebugActivity)?.recreate()
        }
    }

    fun resetSettingDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        prefKeysArray.addAll(pref.all.keys)

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.reset_pref_key, null)

        var key = ""

        dialog.setView(view)

        val resetPrefKey = view.findViewById<EditText>(R.id.reset_pref_key_edit)

        dialog.setPositiveButton(context.getString(R.string.reset)) { _, _ ->

            pref.edit().remove(key).apply()

            if(key == IS_AUTO_DARK_MODE || key == IS_DARK_MODE
                || key == LANGUAGE) {

                MainActivity.instance?.recreate()

                (context as? DebugActivity)?.recreate()
            }

            Toast.makeText(context, context.getString(R.string.key_successfully_reset, key), Toast.LENGTH_LONG).show()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        dialogCreate.setOnShowListener {

            dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = false

            resetPrefKey.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.toString() in prefKeysArray

                    key = s.toString()
                }
            })
        }

        dialogCreate.show()
    }

    fun resetSettingsDialog(context: Context, pref: SharedPreferences) {

        MaterialAlertDialogBuilder(context).apply {

            setIcon(R.drawable.ic_faq_question_24dp)
            setTitle(context.getString(R.string.reset_settings))
            setMessage(context.getString(R.string.are_you_sure))
            setPositiveButton(context.getString(R.string.reset)) { _, _ ->

                pref.edit().clear().apply()
                MainActivity.instance?.recreate()
                (context as? DebugActivity)?.recreate()

                Toast.makeText(context, context.getString(R.string.settings_reset_successfully), Toast.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }
}