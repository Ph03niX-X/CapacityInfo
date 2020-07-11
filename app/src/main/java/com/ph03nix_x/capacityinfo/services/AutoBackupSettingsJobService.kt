package com.ph03nix_x.capacityinfo.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AutoBackupSettingsJobService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {

        val backupPath =
            "${Environment.getExternalStorageDirectory().absolutePath}/Capacity Info/Backup"

        CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

            if(!File(backupPath).exists()) File(backupPath).mkdirs()

            File("${applicationContext.filesDir?.parent}/shared_prefs/" +
                    "${applicationContext.packageName}_preferences.xml").copyTo(File(
                "${backupPath}/${applicationContext.packageName}_preferences.xml"),
                true)
        }

        return true
    }

    override fun onStopJob(params: JobParameters?) = true
}