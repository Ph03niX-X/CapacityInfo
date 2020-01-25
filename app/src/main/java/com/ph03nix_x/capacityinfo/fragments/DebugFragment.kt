package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import com.ph03nix_x.capacityinfo.utils.Constants.exportSettingsRequestCode
import com.ph03nix_x.capacityinfo.utils.Constants.importSettingsRequestCode
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.isGooglePlay
import java.io.File

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface, ServiceInterface {

    private lateinit var pref: SharedPreferences
    private lateinit var prefPath: String
    private lateinit var prefName: String

    private var forciblyShowRateTheApp: SwitchPreferenceCompat? = null
    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null
    private var exportSettings: Preference? = null
    private var importSettings: Preference? = null
    private var openSettings: Preference? = null
    private var restartService: Preference? = null
    private var selectLanguage: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        prefPath = "${requireContext().filesDir.parent}/shared_prefs/${requireContext().packageName}_preferences.xml"
        prefName = File(prefPath).name

        addPreferencesFromResource(R.xml.debug)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        openSettings = findPreference("open_settings")

        restartService = findPreference("restart_service")

        selectLanguage = findPreference(LANGUAGE)

        forciblyShowRateTheApp?.isVisible = !isGooglePlay(requireContext())

        if(pref.getString(LANGUAGE, null) !in resources.getStringArray(R.array.languages_codes))
            selectLanguage?.value = defLang

        selectLanguage?.summary = selectLanguage?.entry

        changeSetting?.setOnPreferenceClickListener {

            changeSettingDialog(requireContext(), pref)

            true
        }

        resetSetting?.setOnPreferenceClickListener {

            resetSettingDialog(requireContext(), pref)

            true
        }

        resetSettings?.setOnPreferenceClickListener {

            resetSettingsDialog(requireContext(), pref)

            true
        }

        exportSettings?.setOnPreferenceClickListener {

            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), exportSettingsRequestCode)

            true
        }

        importSettings?.setOnPreferenceClickListener {

            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
            }, importSettingsRequestCode)

            true
        }

        openSettings?.setOnPreferenceClickListener {

            launchActivity(requireContext(), SettingsActivity::class.java, arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK))

            true
        }

        restartService?.setOnPreferenceClickListener {

            restartService(requireContext())

            it.isVisible = CapacityInfoService.instance != null

            true
        }

        selectLanguage?.setOnPreferenceChangeListener { _, newValue ->

            changeLanguage(requireContext(), newValue as String)

            true
        }
    }

    override fun onResume() {

        super.onResume()

        exportSettings?.isVisible = File(prefPath).exists()

        restartService?.isVisible = CapacityInfoService.instance != null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            exportSettingsRequestCode ->
                if(resultCode == RESULT_OK) exportSettings(requireContext(), data!!, prefPath, prefName)

            importSettingsRequestCode ->
                if(resultCode == RESULT_OK) importSettings(requireContext(), data!!.data!!, prefPath, prefName)
        }
    }
}