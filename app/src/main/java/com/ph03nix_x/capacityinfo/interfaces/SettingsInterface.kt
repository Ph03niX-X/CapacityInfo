package com.ph03nix_x.capacityinfo.interfaces

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
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
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.Constants.MAX_DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.MAIN_SCREEN_TEXT_FONT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.MAIN_SCREEN_TEXT_STYLE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_CHARGE_DISCHARGE_CURRENT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_UNIT
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

interface SettingsInterface : ServiceInterface {

    @RequiresApi(Build.VERSION_CODES.O)
    fun onOpenNotificationCategorySettings(context: Context) {

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager
            .getNotificationChannel("service_channel")

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }

        context.startActivity(intent)
    }

    fun onGetMainScreenTextFontSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(MAIN_SCREEN_TEXT_FONT, "6") !in
            context.resources.getStringArray(R.array.fonts_values))
            pref.edit().putString(MAIN_SCREEN_TEXT_FONT, "6").apply()

        return context.resources.getStringArray(R.array.fonts_list)[
                (pref.getString(MAIN_SCREEN_TEXT_FONT, "6") ?: "6").toInt()]
    }

    fun onGetMainScreenTextStyleSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(MAIN_SCREEN_TEXT_STYLE, "0") !in
            context.resources.getStringArray(R.array.text_style_values))
            pref.edit().putString(MAIN_SCREEN_TEXT_STYLE, "0").apply()

        return context.resources.getStringArray(R.array.text_style_list)[
                (pref.getString(MAIN_SCREEN_TEXT_STYLE, "0") ?: "0").toInt()]
    }

    fun onGetLanguageSummary(context: Context): String? {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(LANGUAGE, null) !in
            context.resources.getStringArray(R.array.languages_codes))
            pref.edit().putString(LANGUAGE, defLang).apply()

        return when(defLang) {

            "en" -> context.resources.getStringArray(R.array.languages_list)[0]

            "ro" -> context.resources.getStringArray(R.array.languages_list)[1]

            "be" -> context.resources.getStringArray(R.array.languages_list)[2]

            "ru" -> context.resources.getStringArray(R.array.languages_list)[3]

            "uk" -> context.resources.getStringArray(R.array.languages_list)[4]

            else -> defLang
        }
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

        if(pref.getString(VOLTAGE_UNIT, "μV")
            !in context.resources.getStringArray(
                R.array.voltage_unit_values))
            pref.edit().putString(VOLTAGE_UNIT, "μV").apply()

        return when(pref.getString(VOLTAGE_UNIT, "μV")) {

            "μV" -> context.resources.getStringArray(R.array.voltage_unit_list)[0]

            "mV" -> context.resources.getStringArray(R.array.voltage_unit_list)[1]

            else -> pref.getString(VOLTAGE_UNIT, "μV")
        }
    }

    fun onChangeLanguage(context: Context, language: String) {

        if(CapacityInfoService.instance != null)
            onStopService(context, CapacityInfoService::class.java)

        if(OverlayService.instance != null)
            onStopService(context, OverlayService::class.java)

        LocaleHelper.setLocale(context, language)

        MainActivity.instance?.recreate()

        (context as? SettingsActivity)?.recreate()
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

                    Toast.makeText(context, context.getString(R.string.import_settings_3dots), Toast.LENGTH_LONG).show()
                }

                if(CapacityInfoService.instance != null)
                    onStopService(context, CapacityInfoService::class.java)

                if(OverlayService.instance != null)
                    onStopService(context, OverlayService::class.java)

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

                delay(1500L)
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

                    SettingsActivity.instance = null

                    MainActivity.instance?.finish()

                    launchActivity(context, MainActivity::class.java,
                        arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK),
                        Intent().putExtra("pref_arrays", prefArrays))

                    Runtime.getRuntime().exit(0)
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

    fun onChangeDesignCapacity(context: Context, designCapacity: Preference? = null) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val dialog = MaterialAlertDialogBuilder(context)

        val view = LayoutInflater.from(context).inflate(R.layout.change_design_capacity_dialog,
            null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<TextInputEditText>(R.id
            .change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(DESIGN_CAPACITY, 0) >= 0)
            pref.getInt(DESIGN_CAPACITY, 0).toString()

        else (pref.getInt(DESIGN_CAPACITY, 0) / -1).toString())

        dialog.setPositiveButton(context.getString(R.string.change)) { _, _ ->

            pref.edit().putInt(DESIGN_CAPACITY, changeDesignCapacity.text.toString().toInt())
                .apply()

            designCapacity?.summary = changeDesignCapacity.text.toString()

            (context as? MainActivity)?.designCapacity?.text = context.getString(
                R.string.design_capacity, changeDesignCapacity.text.toString())
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

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                    dialogCreate.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
                        s.isNotEmpty() && s.toString() != pref.getInt(DESIGN_CAPACITY, 0)
                            .toString() && s.count() >= 4 && s.toString().toInt() <=
                                MAX_DESIGN_CAPACITY && s.toString().toInt() > 0 && s.first() != '0'
                }
            })
        }
    }
}