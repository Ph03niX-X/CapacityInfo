package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity.RESULT_OK
import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.preference.*
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import com.ph03nix_x.capacityinfo.utils.Constants.EXPORT_SETTINGS_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utils.Constants.IMPORT_SETTINGS_REQUEST_CODE
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.isGooglePlay
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay
import java.io.File

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface, ServiceInterface, BillingInterface {

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
    private var orderId: Preference? = null
    private var selectLanguage: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        prefPath = "${requireContext().filesDir.parent}/shared_prefs/${requireContext().packageName}_preferences.xml"
        prefName = File(prefPath).name

        addPreferencesFromResource(R.xml.debug)

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        exportSettings = findPreference("export_settings")

        importSettings = findPreference("import_settings")

        openSettings = findPreference("open_settings")

        restartService = findPreference("restart_service")

        orderId = findPreference("order_id")

        orderId?.isVisible = getOrderId(requireContext()) != null

        if(orderId!!.isVisible) orderId?.summary = getOrderId(requireContext())

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

            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), EXPORT_SETTINGS_REQUEST_CODE)

            true
        }

        importSettings?.setOnPreferenceClickListener {

            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {

                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/xml"
            }, IMPORT_SETTINGS_REQUEST_CODE)

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

        orderId?.setOnPreferenceClickListener {

            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("order_id", getOrderId(requireContext()))
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), getString(R.string.order_id_successfully_copied), Toast.LENGTH_LONG).show()

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

        if(isInstalledGooglePlay) {

           if(billingClient == null)
               billingClient = onBillingClientBuilder(requireContext())

            onBillingStartConnection(requireContext())
        }

        orderId?.isVisible = isInstalledGooglePlay && getOrderId(requireContext()) != null

        if(orderId!!.isVisible) orderId?.summary = getOrderId(requireContext())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            EXPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == RESULT_OK) exportSettings(requireContext(), data!!, prefPath, prefName)

            IMPORT_SETTINGS_REQUEST_CODE ->
                if(resultCode == RESULT_OK) importSettings(requireContext(), data!!.data!!, prefPath, prefName)
        }
    }
}