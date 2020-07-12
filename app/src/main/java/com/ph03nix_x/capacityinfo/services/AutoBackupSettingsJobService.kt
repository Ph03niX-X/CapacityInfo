package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Environment
import android.widget.Toast
import com.ph03nix_x.capacityinfo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception

class AutoBackupSettingsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            try {

                val backupPath =
                    "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

                if(!File(backupPath).exists()) File(backupPath).mkdirs()

                File("${applicationContext.filesDir?.parent}/shared_prefs/" +
                        "${applicationContext.packageName}_preferences.xml").copyTo(File(
                    "${backupPath}/${applicationContext.packageName}_preferences.xml"),
                    true)
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
}