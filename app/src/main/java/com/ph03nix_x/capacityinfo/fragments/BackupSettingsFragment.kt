package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupSettingsFragment : PreferenceFragmentCompat() {

    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.backup_settings)

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        exportSettings?.setOnPreferenceClickListener {

            try {

                startActivityForResult(
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    Constants.EXPORT_SETTINGS_REQUEST_CODE
                )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            Constants.EXPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) exportSettings(data)

            Constants.IMPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) importSettings(data?.data)
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

                    context?.let {
                        MaterialAlertDialogBuilder(it).apply {

                            setMessage(context.getString(R.string.import_restart_app_message))
                            setPositiveButton(context.getString(android.R.string.ok)) { _, _ ->
                                restartApp(prefArrays)
                            }

                            setCancelable(false)

                            show()
                        }
                    }
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

    private fun restartApp(prefArrays: HashMap<String, Any?>) {

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE)
                as? AlarmManager

        val intent = context?.packageName?.let { context?.packageManager?.getLaunchIntentForPackage(
            it)
        }

        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        intent?.putExtra(Constants.IMPORT_SETTINGS_EXTRA, prefArrays)

        alarmManager?.set(AlarmManager.RTC, System.currentTimeMillis() + 1000L,
            intent?.flags?.let { PendingIntent.getActivity(context, 0, Intent(intent),
                it) })

        Process.killProcess(Process.myPid())
    }
}