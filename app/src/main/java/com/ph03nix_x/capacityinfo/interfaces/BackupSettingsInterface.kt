package com.ph03nix_x.capacityinfo.interfaces

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.BackupSettingsFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Ph03niX-X on 15.05.2021
 * Ph03niX-X@outlook.com
 */

interface BackupSettingsInterface {

    fun BackupSettingsFragment.onExportSettings(intent: Intent?) {

        val prefPath = "${requireContext().filesDir?.parent}/shared_prefs/" +
                "${requireContext().packageName}_preferences.xml"
        val prefName = File(prefPath).name

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    requireContext().let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                pickerDir?.findFile(prefName)?.delete()

                val outputStream = pickerDir?.createFile("text/xml",
                    prefName)?.uri?.let {
                    requireContext().contentResolver?.openOutputStream(it)
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

                    Toast.makeText(requireContext(),
                        getString(R.string.successful_export_of_settings, prefName),
                        Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), getString(R.string.error_exporting_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun BackupSettingsFragment.onImportSettings(uri: Uri?) {

        val prefPath = "${requireContext().filesDir?.parent}/shared_prefs/" +
                "${requireContext().packageName}_preferences.xml"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), R.string.import_settings_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it, CapacityInfoService::class.java)
                        }

                    if(OverlayService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it, OverlayService::class.java)
                        }
                }

                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

                val prefArrays: HashMap<String, Any?> = hashMapOf()

                pref.all.forEach {

                    when(it.key) {

                        PreferencesKeys.BATTERY_LEVEL_TO, PreferencesKeys.BATTERY_LEVEL_WITH,
                        PreferencesKeys.DESIGN_CAPACITY, PreferencesKeys.CAPACITY_ADDED,
                        PreferencesKeys.LAST_CHARGE_TIME, PreferencesKeys.PERCENT_ADDED,
                        PreferencesKeys.RESIDUAL_CAPACITY, PreferencesKeys.IS_SHOW_INSTRUCTION ->
                            prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                if(File(prefPath).exists()) File(prefPath).delete()

                File(prefPath).createNewFile()

                val fileOutputStream = FileOutputStream(prefPath)
                val inputStream = uri?.let {
                    requireContext().contentResolver?.openInputStream(it) }

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

                    MainApp.restartApp(requireContext(), prefArrays)
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), getString(
                        R.string.error_importing_settings, e.message ?: e.toString()),
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

   fun BackupSettingsFragment.isMicroSD(): Boolean {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val files = ContextCompat.getExternalFilesDirs(requireContext(), null)

            for (m: File? in files) {

                if(m?.listFiles() != null)
                    if (m.path.contains("-", true)) {

                        MainApp.microSDPath = m.parentFile?.parentFile?.parentFile?.parentFile
                            ?.absolutePath

                        return true
                    }
            }
        }

        MainApp.microSDPath = null

        return false
    }

    fun BackupSettingsFragment.isExternalStoragePermission() =
        ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun BackupSettingsFragment.onGetFrequencyOfAutoBackupSettingsSummary(): CharSequence {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(pref.getString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1") !in
            requireContext().resources.getStringArray(R.array.frequency_of_auto_backup_settings_values))
            pref.edit().putString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1").apply()

        return requireContext().resources.getStringArray(R.array.frequency_of_auto_backup_settings_list)[
                (pref.getString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS,
                    "1")?.toInt() ?: 1) - 1]
    }

    fun BackupSettingsFragment.onBackupSettings(restoreSettingsFromBackupPref: Preference?) {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        var backupPath = ""

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                backupPath = if(pref.getBoolean(PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD,
                        requireContext().resources.getBoolean(R.bool.is_backup_settings_to_microsd))
                    && isMicroSD()) "${MainApp.microSDPath}/Capacity Info/Backup"
                else "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

                if(!File(backupPath).exists()) File(backupPath).mkdirs()

                File("${requireContext().filesDir?.parent}/shared_prefs/" +
                        "${requireContext().packageName}_preferences.xml").copyTo(
                    File(
                    "${backupPath}/${requireContext().packageName}_preferences.xml"),
                    true)

                delay(1000L)
                if(HistoryHelper.isHistoryNotEmpty(requireContext()))
                    File("${requireContext().filesDir?.parent}/databases/History.db")
                        .copyTo(File("${backupPath}/History.db"), true)

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(R.string
                        .settings_backup_successfully_created), Toast.LENGTH_LONG).show()
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

                    restoreSettingsFromBackupPref?.isEnabled = File(
                        "$backupPath/${requireContext().packageName}_preferences.xml")
                        .exists() && File(
                        "$backupPath/${requireContext().packageName}_preferences.xml")
                        .length() > 0
                }
            }
        }
    }

    fun BackupSettingsFragment.onRestoreSettingsFromBackup(backupPath: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val prefArrays: HashMap<String, Any?> = hashMapOf()

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), R.string.restore_settings_from_backup_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it, CapacityInfoService::class.java)

                        }

                    if(OverlayService.instance != null)
                        requireContext().let {
                            ServiceHelper.stopService(it, OverlayService::class.java)
                        }
                }

                pref.all.forEach {

                    when(it.key) {

                        PreferencesKeys.BATTERY_LEVEL_TO, PreferencesKeys.BATTERY_LEVEL_WITH,
                        PreferencesKeys.DESIGN_CAPACITY, PreferencesKeys.CAPACITY_ADDED,
                        PreferencesKeys.LAST_CHARGE_TIME, PreferencesKeys.PERCENT_ADDED,
                        PreferencesKeys.RESIDUAL_CAPACITY, PreferencesKeys.IS_SHOW_INSTRUCTION ->
                            prefArrays[it.key] = it.value
                    }
                }

                delay(2000L)
                File("${backupPath}/${requireContext().packageName}_preferences.xml")
                    .copyTo(File(
                        "${requireContext().filesDir?.parent}/shared_prefs/${
                            requireContext().packageName}_preferences.xml"), true)

                if(File("${backupPath}/History.db").exists())
                    File("${backupPath}/History.db").copyTo(File(
                        "${requireContext().filesDir?.parent}/databases/History.db"),
                        true)

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    MainApp.restartApp(requireContext(), prefArrays, true)
                }

            }
            catch(e: Exception) {

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(
                        R.string.error_restoring_settings_from_backup,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun BackupSettingsFragment.onExportHistory(intent: Intent?) {

        val dbPath = "${requireContext().filesDir?.parent}/databases/History.db"
        val dbName = "History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                if(HistoryHelper.isHistoryEmpty(requireContext()))
                    throw IOException (getString(R.string.history_is_empty))

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    requireContext().let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                delay(1000L)
                pickerDir?.findFile(dbName)?.delete()
                val outputStream = pickerDir?.createFile("application/vnd.sqlite3",
                    dbName)?.uri?.let {
                    requireContext().contentResolver?.openOutputStream(it)
                }

                val fileInputStream = FileInputStream(dbPath)
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

                    Toast.makeText(requireContext(), getString(
                        R.string.history_exported_successfully), Toast.LENGTH_LONG).show()
                }

                MainActivity.isOnBackPressed = true
            }

            catch(e: java.lang.Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), "${getString(R.string
                        .error_exporting_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun BackupSettingsFragment.onImportHistory(uri: Uri?, exportHistoryPref: Preference?) {

        val dbPath = "${requireContext().filesDir?.parent}/databases/History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                delay(1000L)
                File(dbPath).deleteOnExit()
                File("$dbPath-journal").deleteOnExit()

                File(dbPath).createNewFile()

                val fileOutputStream = FileOutputStream(dbPath)
                val inputStream = uri?.let {
                    requireContext().contentResolver?.openInputStream(it) }

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

                MainActivity.isOnBackPressed = true

                val isHistoryNotEmpty = HistoryHelper.isHistoryNotEmpty(requireContext())

                withContext(Dispatchers.Main) {

                    MainActivity.instance?.navigation?.menu?.findItem(R.id.history_navigation)
                        ?.isVisible = isHistoryNotEmpty
                    exportHistoryPref?.isEnabled = isHistoryNotEmpty && !HistoryHelper
                        .isHistoryMax(requireContext())
                }

                if(!isHistoryNotEmpty)
                    throw IOException(getString(R.string.history_is_empty))

                else withContext(Dispatchers.Main) {

                    Toast.makeText(requireContext(), getString(R.string
                        .history_imported_successfully), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: java.lang.Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(requireContext(), "${getString(R.string
                        .error_importing_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}