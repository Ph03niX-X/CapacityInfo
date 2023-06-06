package com.ph03nix_x.capacityinfo.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.microSDPath
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.BackupSettingsInterface
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.AUTO_BACKUP_SETTINGS_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_BACKUP_SETTINGS
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BACKUP_INFORMATION
import kotlinx.coroutines.*
import java.io.File

class BackupSettingsFragment : PreferenceFragmentCompat(), BackupSettingsInterface {

    private lateinit var backupPath: String
    private lateinit var pref: SharedPreferences
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var autoBackupSettings: SwitchPreferenceCompat? = null
    private var backupSettingsToMicroSD: SwitchPreferenceCompat? = null
    private var frequencyOfAutoBackupSettings: ListPreference? = null
    private var createBackupSettings: Preference? = null
    private var restoreSettingsFromBackup: Preference? = null
    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null
    private var exportHistory: Preference? = null
    private var importHistory: Preference? = null

    private var isRestoreSettingsFromBackup = false

    private var requestCode = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.backup_settings)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach {
                if(it.value) {
                    when(requestCode) {
                        EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->
                            if(autoBackupSettings?.isChecked == true && !isRestoreSettingsFromBackup) {

                                ServiceHelper.jobSchedule(requireContext(),
                                    AutoBackupSettingsJobService::class.java, AUTO_BACKUP_SETTINGS_JOB_ID,
                                    (pref.getString(FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                                        ?.toLong() ?: 1L) * 60L * 60L * 1000L)

                                frequencyOfAutoBackupSettings?.isEnabled = true

                                CoroutineScope(Dispatchers.Default).launch(Dispatchers.Main) {

                                    delay(250L)
                                    restoreSettingsFromBackup?.isEnabled = File(
                                        "$backupPath/${requireContext()
                                            .packageName}_preferences.xml").exists() && File(
                                        "$backupPath/${requireContext()
                                            .packageName}_preferences.xml").length() > 0
                                }
                            }
                            else if(autoBackupSettings?.isChecked != true && !isRestoreSettingsFromBackup) {
                                 onBackupSettings(restoreSettingsFromBackup)
                            }
                            else if(isRestoreSettingsFromBackup)
                                onRestoreSettingsFromBackup(backupPath)
                            else {

                                pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()
                                 autoBackupSettings?.isChecked = false

                                ServiceHelper.cancelJob(requireContext(), AUTO_BACKUP_SETTINGS_JOB_ID)
                            }
                    }
                }
            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(requestCode) {

                Constants.EXPORT_SETTINGS_REQUEST_CODE ->
                    if(it.resultCode == Activity.RESULT_OK) onExportSettings(it.data)

                Constants.IMPORT_SETTINGS_REQUEST_CODE ->
                    if(it.resultCode == Activity.RESULT_OK) onImportSettings(it.data?.data)

                Constants.EXPORT_HISTORY_REQUEST_CODE ->
                    if(it.resultCode == Activity.RESULT_OK) onExportHistory(it.data)

                Constants.IMPORT_HISTORY_REQUEST_CODE ->
                    if(it.resultCode == Activity.RESULT_OK) onImportHistory(it.data?.data,
                        exportHistory)
            }
        }

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

        exportHistory = findPreference("export_history")

        importHistory = findPreference("import_history")

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            MainApp.isInstalledGooglePlay = MainApp.isGooglePlay(requireContext())

            autoBackupSettings?.isEnabled = !MainApp.isInstalledGooglePlay

            backupSettingsToMicroSD?.isEnabled = !MainApp.isInstalledGooglePlay && isMicroSD()

            frequencyOfAutoBackupSettings?.isEnabled = !MainApp.isInstalledGooglePlay

            createBackupSettings?.isEnabled = !MainApp.isInstalledGooglePlay

            restoreSettingsFromBackup?.isEnabled = !MainApp.isInstalledGooglePlay

            exportSettings?.isVisible = MainApp.isInstalledGooglePlay

            importSettings?.isVisible = MainApp.isInstalledGooglePlay

            exportHistory?.apply {

                isVisible = MainApp.isInstalledGooglePlay
                isEnabled = HistoryHelper.isHistoryNotEmpty(context)
            }

            importHistory?.isVisible = MainApp.isInstalledGooglePlay

            if(MainApp.isInstalledGooglePlay && pref.getBoolean(IS_SHOW_BACKUP_INFORMATION,
                    resources.getBoolean(R.bool.is_show_backup_information)))
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setIcon(R.drawable.ic_instruction_not_supported_24dp)
                            setTitle(getString(R.string.information))
                            setMessage(getString(R.string.new_permission_is_required_dialog))
                            setPositiveButton(android.R.string.ok) { d, _ ->
                                pref.edit().putBoolean(IS_SHOW_BACKUP_INFORMATION, false).apply()
                                d.dismiss() }
                            setCancelable(false)
                            show()
                        }
        }

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources.getBoolean(
                R.bool.is_auto_backup_settings))
            && ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    !Environment.isExternalStorageManager()) ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                            !isExternalStoragePermission()))) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        frequencyOfAutoBackupSettings?.apply {

            isEnabled = pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && Environment.isExternalStorageManager()) || (Build.VERSION
                .SDK_INT < Build.VERSION_CODES.R && isExternalStoragePermission()))

            summary = onGetFrequencyOfAutoBackupSettingsSummary()
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
                !isExternalStoragePermission()) {
                requestCode = EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE))
            }

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
            else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                !isExternalStoragePermission()) {
                requestCode = EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                requestPermissionLauncher.launch(arrayOf(Manifest.permission
                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE))
            }
            else onBackupSettings(restoreSettingsFromBackup)
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
                requestCode =  EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE))
            }

            else onRestoreSettingsFromBackup(backupPath)

            true
        }

        exportSettings?.setOnPreferenceClickListener {

            try {
                requestCode = Constants.EXPORT_SETTINGS_REQUEST_CODE
                getResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
            }
            catch(e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), getString(R.string.error_exporting_settings,
                    e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }

            true
        }

        importSettings?.setOnPreferenceClickListener {

            try {
                requestCode = Constants.IMPORT_SETTINGS_REQUEST_CODE
                getResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/xml"
                })
            }
            catch(e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), getString(R.string.error_importing_settings,
                    e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }

            true
        }

        exportHistory?.setOnPreferenceClickListener {

            try {
                requestCode = Constants.EXPORT_HISTORY_REQUEST_CODE
                getResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE))
            }
            catch(e: ActivityNotFoundException) {
                Toast.makeText(requireContext(),e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        importHistory?.setOnPreferenceClickListener {

            try {
                requestCode = Constants.IMPORT_HISTORY_REQUEST_CODE
                getResult.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/octet-stream"
                })
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(),e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

    }

    override fun onResume() {

        super.onResume()

        MainApp.isInstalledGooglePlay = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && MainApp.isGooglePlay(requireContext())

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && !Environment.isExternalStorageManager()) || (Build.VERSION
                .SDK_INT < Build.VERSION_CODES.R && !isExternalStoragePermission()))) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        backupSettingsToMicroSD?.isEnabled = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !MainApp.isInstalledGooglePlay && isMicroSD()
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.R && isMicroSD())

        backupPath = if(pref.getBoolean(IS_BACKUP_SETTINGS_TO_MICROSD, requireContext().resources
                .getBoolean(R.bool.is_backup_settings_to_microsd)) && isMicroSD())
                    "$microSDPath/Capacity Info/Backup"
        else "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

        autoBackupSettings?.summary = backupPath

        createBackupSettings?.summary = autoBackupSettings?.summary

        frequencyOfAutoBackupSettings?.apply {

            isEnabled = pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources
                .getBoolean(R.bool.is_auto_backup_settings)) && ((Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.R && Environment.isExternalStorageManager()
                    && !MainApp.isInstalledGooglePlay) || (Build.VERSION.SDK_INT < Build
                .VERSION_CODES.R && isExternalStoragePermission()))

            summary = onGetFrequencyOfAutoBackupSettingsSummary()
        }

        restoreSettingsFromBackup?.isEnabled = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !MainApp.isInstalledGooglePlay && File("$backupPath/${requireContext()
            .packageName}_preferences.xml").exists() && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").length() > 0)
                || (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").exists()
                && File("$backupPath/${requireContext().packageName}_preferences.xml")
            .length() > 0)

        exportHistory?.isEnabled = HistoryHelper.isHistoryNotEmpty(requireContext())
    }
}