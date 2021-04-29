package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.microSDPath
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BACKUP_SETTINGS_TO_MICROSD
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception

class AutoBackupSettingsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

                val backupPath = if(pref.getBoolean(IS_BACKUP_SETTINGS_TO_MICROSD,
                        applicationContext.resources.getBoolean(R.bool
                            .is_backup_settings_to_microsd)) && isMicroSD())
                    "$microSDPath/Capacity Info/Backup" else "${Environment
                    .getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

                if(!File(backupPath).exists()) File(backupPath).mkdirs()

                File("${applicationContext.filesDir?.parent}/shared_prefs/" +
                        "${applicationContext.packageName}_preferences.xml").copyTo(File(
                    "${backupPath}/${applicationContext.packageName}_preferences.xml"),
                    true)

                delay(1000)
                if(HistoryHelper.getHistoryCount(applicationContext) > 0)
                    File("${applicationContext.filesDir.parent}/databases/History.db")
                        .copyTo(File("${backupPath}/History.db"), true)
            }

            catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    Toast.makeText(applicationContext, "Capacity Info\n\n${getString(
                        R.string.error_backup_settings, e.message ?: e.toString())}",
                        Toast.LENGTH_LONG).show()
                }
            }
        }

        return false
    }

    override fun onStopJob(params: JobParameters?) = true

    private fun isMicroSD(): Boolean {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val files = ContextCompat.getExternalFilesDirs(applicationContext, null)

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
}