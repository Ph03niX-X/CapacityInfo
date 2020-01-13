package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.activity.DebugActivity
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

interface DebugOptionsInterface : ServiceInterface{

    fun changeSettingDialog(context: Context, pref: SharedPreferences) = createDialog(context, pref)

    private fun createDialog(context: Context, pref: SharedPreferences) {

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

            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                key = s.toString()
                changePrefValue.isEnabled = key in prefKeysArray

                if(key in prefKeysArray) {

                    when(key) {

                        Preferences.Language.prefKey, Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey,
                        Preferences.UnitOfChargeDischargeCurrent.prefKey, Preferences.VoltageUnit.prefKey -> {

                            changePrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                            changePrefValue.setText(pref.all.getValue(key).toString())
                        }

                        Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                        Preferences.ResidualCapacity.prefKey, Preferences.PercentAdded.prefKey, Preferences.NumberOfCharges.prefKey -> {

                            changePrefValue.filters = arrayOf(InputFilter.LengthFilter(Long.MAX_VALUE.toString().count()))

                            changePrefValue.setText(pref.all.getValue(key).toString())

                            changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                            changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                        }

                        Preferences.CapacityAdded.prefKey -> {

                            changePrefValue.filters = arrayOf(InputFilter.LengthFilter(10))

                            changePrefValue.setText(pref.all.getValue(key).toString())

                            changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL

                            if(changePrefValue.text.toString().contains("."))
                                changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                            else changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")
                        }

                        else -> {

                            changePrefValue.filters = arrayOf(InputFilter.LengthFilter(1))

                            changePrefValue.setText(if(pref.all.getValue(key).toString() == "true") "1" else "0")

                            changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                            changePrefValue.keyListener = DigitsKeyListener.getInstance("01")
                        }
                    }
                }

                else changePrefValue.setText("")
            }
        })

        changePrefValue.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                if(changePrefValue.isEnabled && s.isNotEmpty() && changePrefKey.text.toString() == Preferences.CapacityAdded.prefKey) {

                    if(s.first() == '.') changePrefValue.setText("")

                    if(s.contains(".") && changePrefValue.keyListener == DigitsKeyListener.getInstance("0123456789."))
                        changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")

                    else if(changePrefValue.keyListener == DigitsKeyListener.getInstance("0123456789"))
                        changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789.")
                }

                value = s.toString()
            }
        })

        dialog.apply {

            setPositiveButton(context.getString(R.string.change)) { _, _ -> positiveButton(context, key, value, prefKeysArray) }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }

    private fun positiveButton(context: Context, key: String, value: Any, prefKeysArray: MutableList<String>) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(key != "" && value != "")
            try {

                if(key in prefKeysArray) {

                    var toastMessage = context.getString(R.string.success_change_key, key)

                    when(key) {

                        Preferences.Language.prefKey, Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey,
                        Preferences.UnitOfChargeDischargeCurrent.prefKey, Preferences.VoltageUnit.prefKey ->
                            changeSetting(context, pref, key, value.toString())

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

    fun changeSetting(context: Context, pref: SharedPreferences, key: String, value: String) {

        pref.edit().putString(key, value).apply()

        if(key == Preferences.Language.prefKey) {

            changeLanguage(context, value)

            MainActivity.instance?.recreate()

            (context as DebugActivity).recreate()
        }
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

                    if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey
                        || key == Preferences.Language.prefKey) {

                        MainActivity.instance?.recreate()

                        (context as DebugActivity).recreate()
                    }

                    Toast.makeText(context, context.getString(R.string.settings_reset_successfully),
                        Toast.LENGTH_LONG).show()
                } else Toast.makeText(context, context.getString(R.string.key_not_found, key), Toast.LENGTH_LONG).show()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    fun resetSettingsDialog(context: Context, pref: SharedPreferences) {

        MaterialAlertDialogBuilder(context).apply {

            setIcon(R.drawable.ic_help_outline_dialog_24dp)
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

    fun exportSettings(context: Context, intent: Intent, prefPath: String, prefName: String) {

        GlobalScope.launch(Dispatchers.IO) {

            try {

                val pickerDir = DocumentFile.fromTreeUri(context, intent.data!!)!!

                if(pickerDir.findFile(prefName) != null) pickerDir.findFile(prefName)!!.delete()

                val outputStream = context.contentResolver.openOutputStream(pickerDir.createFile("text/xml", prefName)!!.uri)!!
                val fileInputStream = FileInputStream(prefPath)
                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = fileInputStream.read(buffer)

                    if (read != -1)
                        outputStream.write(buffer, 0, read)
                    else break
                }

                fileInputStream.close()
                outputStream.flush()
                outputStream.close()

                launch(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.successful_export_of_settings, prefName), Toast.LENGTH_LONG).show()
                }

            }

            catch(e: Exception) {

                launch(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_exporting_settings, e.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun importSettings(context: Context, uri: Uri, prefPath: String, prefName: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        GlobalScope.launch(Dispatchers.IO) {

            try {

                launch(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.import_settings_3dots), Toast.LENGTH_LONG).show()
                }

                if(CapacityInfoService.instance != null)
                    context.stopService(Intent(context, CapacityInfoService::class.java))

                if(File(prefPath).exists()) File(prefPath).delete()

                File(prefPath).createNewFile()

                val fileOutputStream = FileOutputStream(prefPath)
                val inputStream = context.contentResolver.openInputStream(uri)!!
                val buffer = byteArrayOf((1024 * 8).toByte())
                var read: Int

                while (true) {

                    read = inputStream.read(buffer)

                    if (read != -1)
                        fileOutputStream.write(buffer, 0, read)
                    else break
                }

                inputStream.close()
                fileOutputStream.flush()
                fileOutputStream.close()

                launch(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.settings_imported_successfully), Toast.LENGTH_LONG).show()
                }

                if(pref.getBoolean(Preferences.IsEnableService.prefKey, true))
                    startService(context)

                delay(3000)
                System.exit(0)
            }

            catch(e: Exception) {

                launch(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_importing_settings, e.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun changeLanguage(context: Context, newValue: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(CapacityInfoService.instance != null)
            context.stopService(Intent(context, CapacityInfoService::class.java))

        LocaleHelper.setLocale(context, newValue)

        MainActivity.instance?.recreate()

        (context as DebugActivity).recreate()

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)) startService(context)
    }
}