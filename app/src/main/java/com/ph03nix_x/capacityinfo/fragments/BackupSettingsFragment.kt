package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.interfaces.BackupSettingsInterface
import com.ph03nix_x.capacityinfo.utilities.Constants

class BackupSettingsFragment : PreferenceFragmentCompat(), BackupSettingsInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null
    private var exportHistory: Preference? = null
    private var importHistory: Preference? = null

    private var isResume = false

    private var requestCode = 0

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.backup_settings)

        if(!isInstalledFromGooglePlay(requireContext()))
            throw RuntimeException("Application not installed from Google Play")

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

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

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        exportHistory = findPreference("export_history")

        importHistory = findPreference("import_history")

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

        exportHistory?.apply {
            isEnabled = HistoryHelper.isHistoryNotEmpty(requireContext())
            setOnPreferenceClickListener {
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
        if(isResume) exportHistory?.isEnabled = HistoryHelper.isHistoryNotEmpty(requireContext())
        else isResume = true
    }

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay(context: Context) =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager.getInstallSourceInfo(
                context.packageName).installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == context.packageManager
            .getInstallerPackageName(context.packageName)
}