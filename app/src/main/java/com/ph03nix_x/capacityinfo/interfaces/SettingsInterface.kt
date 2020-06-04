package com.ph03nix_x.capacityinfo.interfaces

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.documentfile.provider.DocumentFile
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
import com.ph03nix_x.capacityinfo.utils.Constants.IMPORT_SETTINGS_EXTRA
import com.ph03nix_x.capacityinfo.utils.Constants.MAX_DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.Constants.MIN_DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_UNIT
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

    fun onGetTextSizeSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_SIZE, "2") !in
            context.resources.getStringArray(R.array.text_size_values))
            pref.edit().putString(TEXT_SIZE, "2").apply()

        return context.resources.getStringArray(R.array.text_size_list)[
                (pref.getString(TEXT_SIZE, "2") ?: "2").toInt()]
    }

    fun onGetTextFontSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_FONT, "6") !in
            context.resources.getStringArray(R.array.fonts_values))
            pref.edit().putString(TEXT_FONT, "6").apply()

        return context.resources.getStringArray(R.array.fonts_list)[
                (pref.getString(TEXT_FONT, "6") ?: "6").toInt()]
    }

    fun onGetTextStyleSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TEXT_STYLE, "0") !in
            context.resources.getStringArray(R.array.text_style_values))
            pref.edit().putString(TEXT_STYLE, "0").apply()

        return context.resources.getStringArray(R.array.text_style_list)[
                (pref.getString(TEXT_STYLE, "0") ?: "0").toInt()]
    }

    fun onGetLanguageSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(LANGUAGE, null) !in
            context.resources.getStringArray(R.array.languages_codes))
            pref.edit().putString(LANGUAGE, null).apply()

        return when(pref.getString(LANGUAGE, null)) {

            "en" -> context.resources.getStringArray(R.array.languages_list)[0]

            "ro" -> context.resources.getStringArray(R.array.languages_list)[1]

            "be" -> context.resources.getStringArray(R.array.languages_list)[2]

            "ru" -> context.resources.getStringArray(R.array.languages_list)[3]

            "uk" -> context.resources.getStringArray(R.array.languages_list)[4]

            else -> defLang
        }
    }

    fun onGetTabOnApplicationLaunch(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") !in
            context.resources.getStringArray(R.array.tab_on_application_launch_values))
            pref.edit().putString(TAB_ON_APPLICATION_LAUNCH, "0").apply()

        return context.resources.getStringArray(R.array.tab_on_application_launch_list)[
                (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") ?: "0").toInt()]
    }

    fun onGetUnitOfChargeDischargeCurrentSummary(context: Context): String? {

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

    fun onGetUnitOfMeasurementOfCurrentCapacitySummary(context: Context): String? {

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

    fun onGetVoltageUnitSummary(context: Context): String? {

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

        MainActivity.isLoadSettings = true

        (context as? MainActivity)?.recreate()
    }

    fun onExportSettings(context: Context, intent: Intent?) {

        val prefPath = "${context.filesDir.parent}/shared_prefs/" +
                "${context.packageName}_preferences.xml"
        val prefName = File(prefPath).name

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                val pickerDir = intent?.data?.let {
                    DocumentFile.fromTreeUri(context, it) }

                pickerDir?.findFile(prefName)?.delete()

                val outputStream = pickerDir?.createFile("text/xml",
                    prefName)?.uri?.let { context.contentResolver.openOutputStream(it)
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

                    Toast.makeText(context, context.getString(R.string.successful_export_of_settings,
                        prefName), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_exporting_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onImportSettings(context: Context, uri: Uri?) {

        val prefPath = "${context.filesDir.parent}/shared_prefs/" +
                "${context.packageName}_preferences.xml"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, R.string.import_settings_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        ServiceHelper.stopService(context, CapacityInfoService::class.java)

                    if(OverlayService.instance != null)
                        ServiceHelper.stopService(context, OverlayService::class.java)
                }

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                val prefArrays: HashMap<String, Any?> = hashMapOf()

                pref.all.forEach {

                    when(it.key) {

                        BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, DESIGN_CAPACITY, CAPACITY_ADDED,
                        LAST_CHARGE_TIME, PERCENT_ADDED, RESIDUAL_CAPACITY, IS_SUPPORTED,
                        IS_SHOW_NOT_SUPPORTED_DIALOG, IS_SHOW_INSTRUCTION ->
                            prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                if(File(prefPath).exists()) File(prefPath).delete()

                File(prefPath).createNewFile()

                val fileOutputStream = FileOutputStream(prefPath)
                val inputStream = uri?.let {
                    context.contentResolver.openInputStream(it) }

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

                    MaterialAlertDialogBuilder(context).apply {

                        setMessage(context.getString(R.string.import_restart_app_message))
                        setPositiveButton(context.getString(android.R.string.ok)) { _, _ ->
                            onRestartApp(context, prefArrays)
                        }
                        setCancelable(false)
                        show()
                    }
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_importing_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onRestartApp(context: Context, prefArrays: HashMap<String, Any?>) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE)
                as? AlarmManager

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        intent?.putExtra(IMPORT_SETTINGS_EXTRA, prefArrays)

        alarmManager?.set(AlarmManager.RTC, System.currentTimeMillis() + 1000L,
            PendingIntent.getActivity(context, 0, Intent(intent), intent!!.flags))

        Process.killProcess(Process.myPid())
    }

    fun onChangeDesignCapacity(context: Context, designCapacity: Preference? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_design_capacity_dialog,
            null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<TextInputEditText>(R.id
            .change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(DESIGN_CAPACITY, MIN_DESIGN_CAPACITY) >= 0)
            pref.getInt(DESIGN_CAPACITY, MIN_DESIGN_CAPACITY).toString()

        else (pref.getInt(DESIGN_CAPACITY, MIN_DESIGN_CAPACITY) / -1).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DESIGN_CAPACITY, changeDesignCapacity.text.toString().toInt())
                .apply()

            designCapacity?.summary = changeDesignCapacity.text.toString()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        val dialogCreate = dialog.create()

        changeDesignCapacityDialogCreateShowListener(dialogCreate, changeDesignCapacity, pref)

        dialogCreate.show()
    }

    private fun changeDesignCapacityDialogCreateShowListener(dialogCreate: AlertDialog,
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
                            MIN_DESIGN_CAPACITY).toString() && s.toString().toInt() >=
                                MIN_DESIGN_CAPACITY && s.toString().toInt() <=
                                MAX_DESIGN_CAPACITY
                }
            })
        }
    }
}