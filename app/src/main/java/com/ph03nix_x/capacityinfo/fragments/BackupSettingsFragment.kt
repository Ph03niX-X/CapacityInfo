package com.ph03nix_x.capacityinfo.fragments

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.IntentCompat
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.AUTO_BACKUP_SETTINGS_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_BACKUP_SETTINGS
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlin.system.exitProcess

class BackupSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var backupPath: String
    private lateinit var pref: SharedPreferences

    private var autoBackupSettings: SwitchPreferenceCompat? = null
    private var createBackupSettings: Preference? = null
    private var restoreSettingsFromBackup: Preference? = null
    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.backup_settings)

        backupPath = "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        autoBackupSettings = findPreference(IS_AUTO_BACKUP_SETTINGS)

        createBackupSettings = findPreference("create_backup_settings")

        restoreSettingsFromBackup = findPreference("restore_settings_from_backup")

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources.getBoolean(
                R.bool.is_auto_backup_settings)) && checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        restoreSettingsFromBackup?.isEnabled = File("$backupPath/${requireContext()
            .packageName}_preferences.xml").exists() && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").length() > 0

        autoBackupSettings?.summary = getString(R.string.auto_backup_summary, backupPath)

        createBackupSettings?.summary = autoBackupSettings?.summary

        autoBackupSettings?.setOnPreferenceChangeListener { _, newValue ->

            if((newValue as? Boolean == true) && (checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED))
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)

            else if((newValue as? Boolean == true) && checkSelfPermission(requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                ServiceHelper.jobSchedule(requireContext(),
                    AutoBackupSettingsJobService::class.java, AUTO_BACKUP_SETTINGS_JOB_ID,
                    1 * 60 * 60 * 1000 /* 1 hour */)

            else ServiceHelper.cancelJob(requireContext(), AUTO_BACKUP_SETTINGS_JOB_ID)

            true
        }

        createBackupSettings?.setOnPreferenceClickListener {

            if(checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED || checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)

            else backupSettings()

            true
        }

        restoreSettingsFromBackup?.setOnPreferenceClickListener {

            restoreSettingsFromBackup()

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

        if(pref.getBoolean(IS_AUTO_BACKUP_SETTINGS, requireContext().resources.getBoolean(
                R.bool.is_auto_backup_settings)) && checkSelfPermission(requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED) {

            pref.edit().remove(IS_AUTO_BACKUP_SETTINGS).apply()

            autoBackupSettings?.isChecked = false
        }

        restoreSettingsFromBackup?.isEnabled = File("$backupPath/${requireContext()
            .packageName}_preferences.xml").exists() && File(
            "$backupPath/${requireContext().packageName}_preferences.xml").length() > 0
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {

        when(requestCode) {

            EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE ->
                if(grantResults.isNotEmpty() && autoBackupSettings?.isChecked == true) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        ServiceHelper.jobSchedule(requireContext(),
                            AutoBackupSettingsJobService::class.java, AUTO_BACKUP_SETTINGS_JOB_ID,
                            1 * 60 * 60 * 1000 /* 1 hour */)
                }

                else if(grantResults.isNotEmpty() && autoBackupSettings?.isChecked != true) {

                    backupSettings()
                }

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

    private fun backupSettings() {

        val backupPath =
            "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

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

                Toast.makeText(requireContext(), getString(R.string.error_backup_settings,
                    e.message ?: e.toString()), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun restoreSettingsFromBackup() {

        val prefArrays: HashMap<String, Any?> = hashMapOf()

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

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

                withContext(Dispatchers.Main) {

                    restartApp(prefArrays, true)
                }

            }
            catch(e: Exception) {

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

                    Toast.makeText(context, context?.getString(R.string.successful_export_of_settings,
                        prefName), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

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

                    restartApp(prefArrays)
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

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