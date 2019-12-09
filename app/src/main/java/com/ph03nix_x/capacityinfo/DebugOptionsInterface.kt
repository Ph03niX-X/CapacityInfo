package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.activity.SettingsActivity
import java.lang.Exception

interface DebugOptionsInterface {

    fun changeSettingDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        Preferences.values().forEach {

            prefKeysArray.add(it.prefKey)
        }

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_pref_key, null)

        dialog.setView(view)

        val changePrefKey = view.findViewById<EditText>(R.id.change_pref_key_edit)

        val changePrefValue = view.findViewById<EditText>(R.id.change_pref_value_edit)

        var key = ""
        var value: Any = ""

        changePrefKey.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                key = s.toString()
                changePrefValue.isEnabled = key in prefKeysArray

                if(key in prefKeysArray) {

                    when(key) {

                        Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                        Preferences.ChargeCounter.prefKey, Preferences.PercentAdded.prefKey, Preferences.NumberOfCharges.prefKey ->
                            changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                        Preferences.CapacityAdded.prefKey -> changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")

                        else -> changePrefValue.keyListener = DigitsKeyListener.getInstance("01")
                    }
                }
            }
        })

        changePrefValue.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { value = s.toString() }
        })

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            if(key != "")
                try {

                    if(key in prefKeysArray) {

                        var isInt = false
                        var isLong = false
                        var isFloat = false
                        var isBoolean = false

                        when(key) {

                            Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                            Preferences.ChargeCounter.prefKey, Preferences.PercentAdded.prefKey -> isInt = true

                            Preferences.CapacityAdded.prefKey -> isFloat = true

                            Preferences.NumberOfCharges.prefKey -> isLong = true

                            else -> isBoolean = true
                        }

                        var toastMessage = context.getString(R.string.success_change_key, key)

                        if(isInt) changeKey(context, pref, key, value.toString().toInt())
                        else if(isLong) changeKey(context, pref, key, value.toString().toLong())
                        else if(isFloat) changeKey(context, pref, key, value.toString().toFloat())
                        else if(isBoolean) {

                            if(value.toString().length == 1) {

                                value = value == "1"

                                changeKey(context, pref, key, value.toString().toBoolean())

                            }

                            else toastMessage = context.getString(R.string.error_changing_key, key)
                        }

                        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                    }

                    else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()
                }

                catch (e: Exception) { Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show() }
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Int) {

        pref.edit().putInt(key, value).apply()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Long) {

        pref.edit().putLong(key, value).apply()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Float) {

        pref.edit().putFloat(key, value).apply()
    }

    fun changeKey(context: Context, pref: SharedPreferences, key: String, value: Boolean) {

        pref.edit().putBoolean(key, value).apply()

        if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

        (context as SettingsActivity).recreate()
    }

    fun resetSettingDialog(context: Context, pref: SharedPreferences) {

        val prefKeysArray = mutableListOf<String>()

        Preferences.values().forEach {

            prefKeysArray.add(it.prefKey)
        }

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.reset_pref_key, null)

        var key = ""

        dialog.setView(view)

        val resetPrefKey = view.findViewById<EditText>(R.id.reset_pref_key_edit)

        resetPrefKey.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { key = s.toString() }
        })

        dialog.setPositiveButton(context.getString(R.string.reset)) { _, _ ->

            if(key != "")
                if(key in prefKeysArray) {

                    pref.edit().remove(key).apply()

                    if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) MainActivity.instance?.recreate()

                    (context as SettingsActivity).recreate()

                } else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()

        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()

    }

    fun resetSettingsDialog(context: Context, pref: SharedPreferences) {

        MaterialAlertDialogBuilder(context).apply {

            setTitle(context.getString(R.string.reset_settings))
            setMessage(context.getString(R.string.are_you_sure))
            setPositiveButton(context.getString(R.string.reset)) { _, _ ->

                pref.edit().clear().apply()
                MainActivity.instance?.recreate()
                (context as SettingsActivity).recreate()

                Toast.makeText(context, context.getString(R.string.settings_reset_successfully), Toast.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }
}