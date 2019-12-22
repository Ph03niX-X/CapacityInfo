package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.SharedPreferences
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.activity.DebugActivity
import com.ph03nix_x.capacityinfo.activity.MainActivity
import java.lang.Exception

interface DebugOptionsInterface {

    fun changeSettingDialog(context: Context, pref: SharedPreferences) = createDialog(context)

    private fun createDialog(context: Context) {

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
                        Preferences.ResidualCapacity.prefKey, Preferences.PercentAdded.prefKey, Preferences.NumberOfCharges.prefKey ->
                            changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                        Preferences.CapacityAdded.prefKey -> changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")

                        else -> changePrefValue.keyListener = DigitsKeyListener.getInstance("01")
                    }
                }

                else changePrefValue.setText("")
            }
        })

        changePrefValue.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { value = s.toString() }
        })

        dialog.apply {

            setPositiveButton(context.getString(R.string.change)) { _, _ -> positiveButton(context, key, value, prefKeysArray) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }

    private fun positiveButton(context: Context, key: String, value: Any, prefKeysArray: MutableList<String>) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(key != "")
            try {

                if(key in prefKeysArray) {

                    var toastMessage = context.getString(R.string.success_change_key, key)

                    when(key) {

                        Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                        Preferences.ResidualCapacity.prefKey, Preferences.PercentAdded.prefKey -> changeSetting(context, pref, key, value.toString().toInt())

                        Preferences.CapacityAdded.prefKey -> changeSetting(context, pref, key, value.toString().toFloat())

                        Preferences.NumberOfCharges.prefKey -> changeSetting(context, pref, key, value.toString().toLong())

                        else -> {

                            if(value.toString().length == 1) changeSetting(context, pref, key, value = value == "1")

                            else toastMessage = context.getString(R.string.error_changing_key, key)
                        }
                    }

                    Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show()
                }

                else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()
            }

            catch (e: Exception) { Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show() }
    }

    fun changeSetting(context: Context, pref: SharedPreferences, key: String, value: Int) {

        pref.edit().putInt(key, value).apply()
    }

    fun changeSetting(context: Context, pref: SharedPreferences, key: String, value: Long) {

        pref.edit().putLong(key, value).apply()
    }

    fun changeSetting(context: Context, pref: SharedPreferences, key: String, value: Float) {

        pref.edit().putFloat(key, value).apply()
    }

    fun changeSetting(context: Context, pref: SharedPreferences, key: String, value: Boolean) {

        pref.edit().putBoolean(key, value).apply()

        if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) {

            MainActivity.instance?.recreate()

            (context as DebugActivity).recreate()
        }
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

                    if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey) {

                        MainActivity.instance?.recreate()

                        (context as DebugActivity).recreate()
                    }

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
                (context as DebugActivity).recreate()

                Toast.makeText(context, context.getString(R.string.settings_reset_successfully), Toast.LENGTH_LONG).show()
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }
}