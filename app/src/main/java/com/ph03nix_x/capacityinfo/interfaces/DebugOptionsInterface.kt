package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.content.DialogInterface
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
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import com.ph03nix_x.capacityinfo.activities.DebugActivity
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception

interface DebugOptionsInterface : ServiceInterface {

    fun changeSettingDialog(context: Context, pref: SharedPreferences) =
        changeSettingCreateDialog(context, pref)

    private fun changeSettingCreateDialog(context: Context, pref: SharedPreferences) {

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

                            Preferences.Language.prefKey, Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey,
                            Preferences.UnitOfChargeDischargeCurrent.prefKey, Preferences.VoltageUnit.prefKey -> {

                                valueType = "string"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(3))

                                changePrefValue.setText(pref.all.getValue(key).toString())
                            }

                            Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                            Preferences.ResidualCapacity.prefKey, Preferences.PercentAdded.prefKey, Preferences.NumberOfCharges.prefKey -> {

                                valueType = "int|long"

                                changePrefValue.filters = arrayOf(InputFilter.LengthFilter(Long.MAX_VALUE.toString().count()))

                                changePrefValue.setText(pref.all.getValue(key).toString())

                                changePrefValue.inputType = InputType.TYPE_CLASS_NUMBER

                                changePrefValue.keyListener = DigitsKeyListener.getInstance("0123456789")
                            }

                            Preferences.CapacityAdded.prefKey -> {

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

                    when(key) {

                        Preferences.Language.prefKey -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.languages_codes)

                        Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.unit_of_measurement_of_current_capacity_values)

                        Preferences.UnitOfChargeDischargeCurrent.prefKey -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.unit_of_charge_discharge_current_values)

                        Preferences.VoltageUnit.prefKey -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                            s.toString() in context.resources.getStringArray(R.array.voltage_unit_values)

                        else -> dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty()
                    }

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = s.isNotEmpty() && when(valueType) {

                        "string" -> s.toString() != pref.getString(key, "")

                        "int|long" -> {

                            if(key != Preferences.NumberOfCharges.prefKey)
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

                Preferences.Language.prefKey, Preferences.UnitOfMeasurementOfCurrentCapacity.prefKey,
                Preferences.UnitOfChargeDischargeCurrent.prefKey, Preferences.VoltageUnit.prefKey ->
                    changeSetting(context, pref, key, value.toString())

                Preferences.DesignCapacity.prefKey, Preferences.LastChargeTime.prefKey, Preferences.BatteryLevelWith.prefKey, Preferences.BatteryLevelTo.prefKey,
                Preferences.ResidualCapacity.prefKey, Preferences.PercentAdded.prefKey -> changeSetting(context, pref, key, value.toString().toInt())

                Preferences.CapacityAdded.prefKey -> changeSetting(context, pref, key, value.toString().toFloat())

                Preferences.NumberOfCharges.prefKey -> changeSetting(context, pref, key, value.toString().toLong())

                else -> changeSetting(context, pref, key, value = value == "1")
            }

            Toast.makeText(context, context.getString(R.string.success_change_key, key), Toast.LENGTH_LONG).show()
        }

        catch (e: Exception) {

            Toast.makeText(context, context.getString(R.string.error_changing_key, key, e.message), Toast.LENGTH_LONG).show()
        }
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

        else if(key == Preferences.IsEnableService.prefKey && !value
            && CapacityInfoService.instance != null)
            context.stopService(Intent(context, CapacityInfoService::class.java))
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

        dialog.setPositiveButton(context.getString(R.string.reset)) { _, _ ->

            pref.edit().remove(key).apply()

            if(key == Preferences.IsAutoDarkMode.prefKey || key == Preferences.IsDarkMode.prefKey
                || key == Preferences.Language.prefKey) {

                MainActivity.instance?.recreate()

                (context as DebugActivity).recreate()
            }

            Toast.makeText(context, context.getString(R.string.settings_reset_successfully), Toast.LENGTH_LONG).show()
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

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

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

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.successful_export_of_settings, prefName), Toast.LENGTH_LONG).show()
                }

            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_exporting_settings, e.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun importSettings(context: Context, uri: Uri, prefPath: String, prefName: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.import_settings_3dots), Toast.LENGTH_LONG).show()
                }

                if(CapacityInfoService.instance != null)
                    context.stopService(Intent(context, CapacityInfoService::class.java))

                delay(1500)
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

                if(pref.getBoolean(Preferences.IsEnableService.prefKey, true))
                    startService(context)

                launchActivity(context, MainActivity::class.java, arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK),
                    Intent().putExtra("is_import_settings", true))

                System.exit(0)
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

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