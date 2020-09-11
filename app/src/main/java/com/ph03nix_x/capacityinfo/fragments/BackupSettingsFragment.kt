package com.ph03nix_x.capacityinfo.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.documentfile.provider.DocumentFile
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.microSDPath
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.AUTO_BACKUP_SETTINGS_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess

class BackupSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var backupPath: String
    private lateinit var pref: SharedPreferences

    private var autoBackupSettings: SwitchPreferenceCompat? = null
    private var backupSettingsToMicroSD: SwitchPreferenceCompat? = null
    private var frequencyOfAutoBackupSettings: ListPreference? = null
    private var createBackupSettings: Preference? = null
    private var restoreSettingsFromBackup: Preference? = null
    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null

    private var isRestoreSettingsFromBackup = false

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.backup_settings)

        backupPath = if(pref.getBoolean(IS_BACKUP_SETTINGS_TO_MICROSD, requireContext()
                .resources.getBoolean(R.bool.is_backup_settings_to_microsd)) &&
            isMicroSD()) "$microSDPath/Capacity Info/Backup"
        else "${Environment.getExternalStorageDirectory()
            .absolutePath}/Capacity Info/Backup"

        autoBackupSettings = findPreference(IS_AUTO_BACKUP_SETTINGS)

        backupSettingsToMicroSD = findPreference(IS_BACKUP_SETTINGS_TO_MICROSD)

        frequencyOfAutoBackupSettings = findPreference(FREQUENCY_OF_AUTO_BACKUP_SETTINGS)

        createBackupSettings = findPreference("create_backup_settings")

        restoreSettingsFromBackup = findPreference("restore_settings_from_backup")

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources.getBoolean(
                R.bool.is_auto_backup_settings))
            && ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()) ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                            !isExternalStoragePermission()))) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        backupSettingsToMicroSD?.isEnabled = isMicroSD()

        frequencyOfAutoBackupSettings?.apply {

            isEnabled = pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && Environment.isExternalStorageManager()) || (Build.VERSION
                .SDK_INT < Build.VERSION_CODES.R && isExternalStoragePermission()))

            summary = getFrequencyOfAutoBackupSettingsSummary()
        }

        restoreSettingsFromBackup?.isEnabled = File("$backupPath/${requireContext()
            .packageName}_preferences.xml").exists() && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").length() > 0

        autoBackupSettings?.summary = getString(R.string.auto_backup_summary, backupPath)

        createBackupSettings?.summary = autoBackupSettings?.summary

        autoBackupSettings?.setOnPreferenceChangeListener { it, newValue ->

           val isAutoBackup = newValue as? Boolean

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && isAutoBackup == true &&
                !Environment.isExternalStorageManager()) {

                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(R.string.access_memory)
                    setPositiveButton(android.R.string.ok) { _, _ ->

                        (it as? SwitchPreferenceCompat)?.isChecked = false

                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${requireContext().packageName}"))

                        startActivity(intent)
                    }

                    setCancelable(false)

                    show()
                }
            }

            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R && isAutoBackup == true &&
                !isExternalStoragePermission())
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)

            else if((newValue as? Boolean == true) &&
                ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        Environment.isExternalStorageManager()) || (Build.VERSION.SDK_INT < Build
                    .VERSION_CODES.R && isExternalStoragePermission()))) {

                ServiceHelper.jobSchedule(requireContext(),
                    AutoBackupSettingsJobService::class.java, AUTO_BACKUP_SETTINGS_JOB_ID,
                    (pref.getString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                        ?.toLong() ?: 1L) * 60L * 60L * 1000L)

                frequencyOfAutoBackupSettings?.isEnabled = true

                CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

                    delay(250L)
                    restoreSettingsFromBackup?.isEnabled = File(
                        "$backupPath/${requireContext().packageName}_preferences.xml")
                        .exists() && File("$backupPath/${requireContext()
                        .packageName}_preferences.xml").length() > 0
                }
            }

            else {

                frequencyOfAutoBackupSettings?.isEnabled = false

                ServiceHelper.cancelJob(requireContext(), AUTO_BACKUP_SETTINGS_JOB_ID)
            }

            true
        }

        backupSettingsToMicroSD?.setOnPreferenceChangeListener { it, newValue ->

            if((newValue as? Boolean == true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !Environment.isExternalStorageManager()) {

                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(R.string.access_memory_card)
                    setPositiveButton(android.R.string.ok) { _, _ ->

                        (it as? SwitchPreferenceCompat)?.isChecked = false

                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${requireContext().packageName}"))

                        startActivity(intent)
                    }

                    setCancelable(false)

                    show()
                }
            }

            else if((newValue as? Boolean == true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && Environment.isExternalStorageManager()) {

                if(microSDPath == null) isMicroSD()

                backupPath = "$microSDPath/Capacity Info/Backup"

                autoBackupSettings?.summary = getString(R.string.auto_backup_summary, backupPath)

                createBackupSettings?.summary = autoBackupSettings?.summary
            }

            else if((newValue as? Boolean != true)) {

                backupPath = "${Environment.getExternalStorageDirectory()
                    .absolutePath}/Capacity Info/Backup"

                autoBackupSettings?.summary = getString(R.string.auto_backup_summary, backupPath)

                createBackupSettings?.summary = autoBackupSettings?.summary
            }

            true
        }

        frequencyOfAutoBackupSettings?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(R.array
                .frequency_of_auto_backup_settings_list)[((newValue as? String)?.toInt() ?: 1) - 1]

            ServiceHelper.rescheduleJob(requireContext(), AutoBackupSettingsJobService::class.java,
                AUTO_BACKUP_SETTINGS_JOB_ID,
                ((newValue as? String)?.toLong() ?: 1) * 60 * 60 * 1000)

            true
        }

        createBackupSettings?.setOnPreferenceClickListener {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()) {

                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(R.string.access_memory)
                    setPositiveButton(android.R.string.ok) { _, _ ->

                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${requireContext().packageName}"))

                        startActivity(intent)
                    }

                    setCancelable(false)

                    show()
                }
            }

            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !isExternalStoragePermission())
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)

            else backupSettings()

            true
        }

        restoreSettingsFromBackup?.setOnPreferenceClickListener {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                !Environment.isExternalStorageManager()) {

                MaterialAlertDialogBuilder(requireContext()).apply {

                    setMessage(R.string.access_memory)
                    setPositiveButton(android.R.string.ok) { _, _ ->

                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:${requireContext().packageName}"))

                        startActivity(intent)
                    }

                    setCancelable(false)

                    show()
                }
            }

            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                !isExternalStoragePermission()) {

                isRestoreSettingsFromBackup = !isRestoreSettingsFromBackup

                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
            }

            else restoreSettingsFromBackup()

            true
        }

        exportSettings?.setOnPreferenceClickListener {

            try {

                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    Constants.EXPORT_SETTINGS_REQUEST_CODE)
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_exporting_settings,
                    e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }

            true
        }

        importSettings?.setOnPreferenceClickListener {

            try {

                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/xml"
                }, Constants.IMPORT_SETTINGS_REQUEST_CODE)
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_importing_settings,
                    e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }

            true
        }

    }

    override fun onResume() {

        super.onResume()

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && !Environment.isExternalStorageManager()) || (Build.VERSION
                .SDK_INT < Build.VERSION_CODES.R && !isExternalStoragePermission()))) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        backupSettingsToMicroSD?.isEnabled = isMicroSD()

        val backupPath =
            if(pref.getBoolean(IS_BACKUP_SETTINGS_TO_MICROSD, requireContext()
                    .resources.getBoolean(R.bool.is_backup_settings_to_microsd)) &&
                        isMicroSD()) "$microSDPath/Capacity Info/Backup"
            else "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

        autoBackupSettings?.summary = backupPath

        createBackupSettings?.summary = autoBackupSettings?.summary

        frequencyOfAutoBackupSettings?.apply {

            isEnabled = pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && Environment.isExternalStorageManager()) || (Build.VERSION
                .SDK_INT < Build.VERSION_CODES.R && isExternalStoragePermission()))

            summary = getFrequencyOfAutoBackupSettingsSummary()
        }

        restoreSettingsFromBackup?.isEnabled = File("$backupPath/${requireContext()
            .packageName}_preferences.xml").exists() && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").length() > 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        when(requestCode) {

            EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->
                if(grantResults.isNotEmpty() && autoBackupSettings?.isChecked == true
                    && !isRestoreSettingsFromBackup) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                        ServiceHelper.jobSchedule(requireContext(),
                            AutoBackupSettingsJobService::class.java, AUTO_BACKUP_SETTINGS_JOB_ID,
                            (pref.getString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                                ?.toLong() ?: 1L) * 60L * 60L * 1000L)

                        frequencyOfAutoBackupSettings?.isEnabled = true

                        CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

                            delay(250)
                            restoreSettingsFromBackup?.isEnabled = File(
                                "$backupPath/${requireContext()
                                    .packageName}_preferences.xml").exists() && File(
                                "$backupPath/${requireContext()
                                    .packageName}_preferences.xml").length() > 0
                        }
                    }
                }

                else if(grantResults.isNotEmpty() && autoBackupSettings?.isChecked != true
                    && !isRestoreSettingsFromBackup) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED) backupSettings()
                }

                else if(grantResults.isNotEmpty() && isRestoreSettingsFromBackup)
                        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            restoreSettingsFromBackup()

                else {

                    pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

                    autoBackupSettings?.isChecked = false

                    ServiceHelper.cancelJob(requireContext(), AUTO_BACKUP_SETTINGS_JOB_ID)
                }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            Constants.EXPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) exportSettings(data)

            Constants.IMPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) importSettings(data?.data)
        }
    }

    private fun isMicroSD(): Boolean {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val files = ContextCompat.getExternalFilesDirs(requireContext(), null)

            for (m: File? in files) {

                if(m?.listFiles() != null)
                    if (m.path.contains("-", true)) {

                        microSDPath = m.parentFile?.parentFile?.parentFile?.parentFile?.absolutePath

                        return true
                    }
            }
        }

        microSDPath = null

        return false
    }

    private fun isExternalStoragePermission() =
        checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun getFrequencyOfAutoBackupSettingsSummary(): CharSequence? {

        if(pref.getString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1") !in
            resources.getStringArray(R.array.frequency_of_auto_backup_settings_values))
            pref.edit().putString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1").apply()

        return resources.getStringArray(R.array.frequency_of_auto_backup_settings_list)[
                (pref.getString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")?.toInt() ?: 1) - 1]

    }

    private fun backupSettings() {

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                val backupPath =
                    if(pref.getBoolean(IS_BACKUP_SETTINGS_TO_MICROSD, requireContext()
                            .resources.getBoolean(R.bool.is_backup_settings_to_microsd)) &&
                                isMicroSD()) "$microSDPath/Capacity Info/Backup"
                    else "${Environment.getExternalStorageDirectory()
                        .absolutePath}/Capacity Info/Backup"

                if(!File(backupPath).exists()) File(backupPath).mkdirs()

                File("${context?.filesDir?.parent}/shared_prefs/" +
                        "${context?.packageName}_preferences.xml").copyTo(File(
                    "${backupPath}/${context?.packageName}_preferences.xml"),
                    true)

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(
                        R.string.settings_backup_successfully_created), Toast.LENGTH_LONG).show()
                }
            }
            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(R.string.error_backup_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }

            finally {

                withContext(Dispatchers.Main) {

                    restoreSettingsFromBackup?.isEnabled = File(
                        "$backupPath/${requireContext().packageName}_preferences.xml")
                        .exists() && File("$backupPath/${requireContext()
                        .packageName}_preferences.xml").length() > 0
                }
            }
        }
    }

    private fun restoreSettingsFromBackup() {

        val prefArrays: HashMap<String, Any?> = hashMapOf()

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, R.string.restore_settings_from_backup_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        context?.let { ServiceHelper.stopService(it, CapacityInfoService::class.java)

                        }

                    if(OverlayService.instance != null)
                        context?.let { ServiceHelper.stopService(it, OverlayService::class.java) }
                }

                pref.all.forEach {

                    when(it.key) {

                        PreferencesKeys.BATTERY_LEVEL_TO, PreferencesKeys.BATTERY_LEVEL_WITH,
                        PreferencesKeys.DESIGN_CAPACITY, PreferencesKeys.CAPACITY_ADDED,
                        PreferencesKeys.LAST_CHARGE_TIME, PreferencesKeys.PERCENT_ADDED,
                        PreferencesKeys.RESIDUAL_CAPACITY, PreferencesKeys.IS_SUPPORTED,
                        PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG,
                        PreferencesKeys.IS_SHOW_INSTRUCTION -> prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                File("${backupPath}/${context?.packageName}_preferences.xml").copyTo(File(
                    "${context?.filesDir?.parent}/shared_prefs/${context?.packageName}" +
                            "_preferences.xml"), true)

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    restartApp(prefArrays, true)
                }

            }
            catch(e: Exception) {

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, getString(R.string.error_restoring_settings_from_backup,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

   private fun exportSettings(intent: Intent?) {

        val prefPath = "${context?.filesDir?.parent}/shared_prefs/" +
                "${context?.packageName}_preferences.xml"
        val prefName = File(prefPath).name

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    context?.let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                pickerDir?.findFile(prefName)?.delete()

                val outputStream = pickerDir?.createFile("text/xml",
                    prefName)?.uri?.let {
                    context?.contentResolver?.openOutputStream(it)
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

                    Toast.makeText(context, context?.getString(R.string.successful_export_of_settings,
                        prefName), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context?.getString(R.string.error_exporting_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

   private fun importSettings(uri: Uri?) {

        val prefPath = "${context?.filesDir?.parent}/shared_prefs/" +
                "${context?.packageName}_preferences.xml"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, R.string.import_settings_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        context?.let { ServiceHelper.stopService(it, CapacityInfoService::class.java)

                        }

                    if(OverlayService.instance != null)
                        context?.let { ServiceHelper.stopService(it, OverlayService::class.java) }
                }

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                val prefArrays: HashMap<String, Any?> = hashMapOf()

                pref.all.forEach {

                    when(it.key) {

                        PreferencesKeys.BATTERY_LEVEL_TO, PreferencesKeys.BATTERY_LEVEL_WITH,
                        PreferencesKeys.DESIGN_CAPACITY, PreferencesKeys.CAPACITY_ADDED,
                        PreferencesKeys.LAST_CHARGE_TIME, PreferencesKeys.PERCENT_ADDED,
                        PreferencesKeys.RESIDUAL_CAPACITY, PreferencesKeys.IS_SUPPORTED,
                        PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG,
                        PreferencesKeys.IS_SHOW_INSTRUCTION -> prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                if(File(prefPath).exists()) File(prefPath).delete()

                File(prefPath).createNewFile()

                val fileOutputStream = FileOutputStream(prefPath)
                val inputStream = uri?.let {
                    context?.contentResolver?.openInputStream(it) }

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

                    restartApp(prefArrays)
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context?.getString(R.string.error_importing_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun restartApp(prefArrays: HashMap<String, Any?>, isRestore: Boolean = false) {

        val packageManager = requireContext().packageManager
        
        val componentName = packageManager.getLaunchIntentForPackage(
            requireContext().packageName)?.component

        val intent = Intent.makeRestartActivityTask(componentName)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        intent?.putExtra(Constants.IMPORT_RESTORE_SETTINGS_EXTRA, prefArrays)

        if(isRestore) intent?.putExtra(Constants.IS_RESTORE_SETTINGS_EXTRA, true)

        requireContext().startActivity(intent)

        exitProcess(0)
    }
}