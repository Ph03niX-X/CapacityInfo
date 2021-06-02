package com.ph03nix_x.capacityinfo.interfaces

import android.Manifest
import android.content.Context
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

    fun onExportSettings(context: Context, intent: Intent?) {

        val prefPath = "${context.filesDir?.parent}/shared_prefs/" +
                "${context.packageName}_preferences.xml"
        val prefName = File(prefPath).name

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    context.let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                pickerDir?.findFile(prefName)?.delete()

                val outputStream = pickerDir?.createFile("text/xml",
                    prefName)?.uri?.let {
                    context.contentResolver?.openOutputStream(it)
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

                    Toast.makeText(context, context.getString(R.string.successful_export_of_settings,
                        prefName), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context.getString(R.string.error_exporting_settings, 
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onImportSettings(context: Context, uri: Uri?) {

        val prefPath = "${context.filesDir?.parent}/shared_prefs/" +
                "${context.packageName}_preferences.xml"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, R.string.import_settings_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        context.let {
                            ServiceHelper.stopService(it, CapacityInfoService::class.java)
                        }

                    if(OverlayService.instance != null)
                        context.let { ServiceHelper.stopService(it, OverlayService::class.java) }
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
                    context.contentResolver?.openInputStream(it) }

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

                    MainApp.restartApp(context, prefArrays)
                }
            }

            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, context.getString(R.string.error_importing_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

   fun isMicroSD(context: Context): Boolean {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val files = ContextCompat.getExternalFilesDirs(context, null)

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

    fun isExternalStoragePermission(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun onGetFrequencyOfAutoBackupSettingsSummary(context: Context): CharSequence {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if(pref.getString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1") !in
            context.resources.getStringArray(R.array.frequency_of_auto_backup_settings_values))
            pref.edit().putString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1").apply()

        return context.resources.getStringArray(R.array.frequency_of_auto_backup_settings_list)[
                (pref.getString(PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS,
                    "1")?.toInt() ?: 1) - 1]
    }

    fun onBackupSettings(context: Context, restoreSettingsFromBackupPref: Preference?) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        var backupPath = ""

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                backupPath = if(pref.getBoolean(PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD,
                        context.resources.getBoolean(R.bool.is_backup_settings_to_microsd)) &&
                    isMicroSD(context)) "${MainApp.microSDPath}/Capacity Info/Backup"
                else "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

                if(!File(backupPath).exists()) File(backupPath).mkdirs()

                File("${context.filesDir?.parent}/shared_prefs/" +
                        "${context.packageName}_preferences.xml").copyTo(
                    File(
                    "${backupPath}/${context.packageName}_preferences.xml"),
                    true)

                delay(1000L)
                if(HistoryHelper.isHistoryNotEmpty(context))
                    File("${context.filesDir?.parent}/databases/History.db")
                        .copyTo(File("${backupPath}/History.db"), true)

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string
                        .settings_backup_successfully_created), Toast.LENGTH_LONG).show()
                }
            }
            catch(e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string.error_backup_settings,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }

            finally {

                withContext(Dispatchers.Main) {

                    restoreSettingsFromBackupPref?.isEnabled = File(
                        "$backupPath/${context.packageName}_preferences.xml")
                        .exists() && File("$backupPath/${context
                        .packageName}_preferences.xml").length() > 0
                }
            }
        }
    }

    fun onRestoreSettingsFromBackup(context: Context, backupPath: String) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val prefArrays: HashMap<String, Any?> = hashMapOf()

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, R.string.restore_settings_from_backup_3dots,
                        Toast.LENGTH_LONG).show()

                    if(CapacityInfoService.instance != null)
                        context.let { ServiceHelper.stopService(it, CapacityInfoService::class.java)

                        }

                    if(OverlayService.instance != null)
                        context.let { ServiceHelper.stopService(it, OverlayService::class.java) }
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
                File("${backupPath}/${context.packageName}_preferences.xml").copyTo(
                    File(
                    "${context.filesDir?.parent}/shared_prefs/${context.packageName}" +
                            "_preferences.xml"), true)

                if(File("${backupPath}/History.db").exists())
                    File("${backupPath}/History.db").copyTo(
                        File(
                        "${context.filesDir?.parent}/databases/History.db"),
                        true)

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    MainApp.restartApp(context, prefArrays, true)
                }

            }
            catch(e: Exception) {

                MainActivity.isOnBackPressed = true

                withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(
                        R.string.error_restoring_settings_from_backup,
                        e.message ?: e.toString()), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onExportHistory(context: Context, intent: Intent?) {

        val dbPath = "${context.filesDir?.parent}/databases/History.db"
        val dbName = "History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                if(HistoryHelper.isHistoryEmpty(context))
                    throw IOException (context.getString(R.string.history_is_empty))

                MainActivity.isOnBackPressed = false

                val pickerDir = intent?.data?.let {
                    context.let { it1 -> DocumentFile.fromTreeUri(it1, it) }
                }

                delay(1000L)
                pickerDir?.findFile(dbName)?.delete()
                val outputStream = pickerDir?.createFile("application/vnd.sqlite3",
                    dbName)?.uri?.let {
                    context.contentResolver?.openOutputStream(it)
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

                    Toast.makeText(context, context.getString(
                        R.string.history_exported_successfully), Toast.LENGTH_LONG).show()
                }

                MainActivity.isOnBackPressed = true
            }

            catch(e: java.lang.Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, "${context.getString(R.string
                        .error_exporting_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onImportHistory(context: Context, uri: Uri?, exportHistoryPref: Preference?) {

        val dbPath = "${context.filesDir?.parent}/databases/History.db"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                MainActivity.isOnBackPressed = false

                delay(1000L)
                File(dbPath).deleteOnExit()
                File("$dbPath-journal").deleteOnExit()

                File(dbPath).createNewFile()

                val fileOutputStream = FileOutputStream(dbPath)
                val inputStream = uri?.let {
                    context.contentResolver?.openInputStream(it) }

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

                val isHistoryNotEmpty = HistoryHelper.isHistoryNotEmpty(context)

                withContext(Dispatchers.Main) {

                    MainActivity.instance?.navigation?.menu?.findItem(R.id.history_navigation)
                        ?.isVisible = isHistoryNotEmpty
                    exportHistoryPref?.isEnabled = isHistoryNotEmpty && !HistoryHelper
                        .isHistoryMax(context)
                }

                if(!isHistoryNotEmpty)
                    throw IOException(context.getString(R.string.history_is_empty))

                else withContext(Dispatchers.Main) {

                    Toast.makeText(context, context.getString(R.string
                        .history_imported_successfully), Toast.LENGTH_LONG).show()
                }
            }

            catch(e: java.lang.Exception) {

                withContext(Dispatchers.Main) {

                    MainActivity.isOnBackPressed = true

                    Toast.makeText(context, "${context.getString(R.string
                        .error_importing_history)}\n${e.message ?: e.toString()}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}