package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Bundle
import androidx.preference.*
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface {

    private lateinit var pref: SharedPreferences
    
    private var forciblyShowRateTheApp: SwitchPreferenceCompat? = null
    private var startCapacityInfoService: Preference? = null
    private var stopCapacityInfoService: Preference? = null
    private var restartCapacityInfoService: Preference? = null
    private var stopOverlayService: Preference? = null
    private var restartOverlayService: Preference? = null
    private var addSetting: Preference? = null
    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.debug_settings)

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        startCapacityInfoService = findPreference("start_capacity_info_service")

        stopCapacityInfoService = findPreference("stop_capacity_info_service")

        restartCapacityInfoService = findPreference("restart_capacity_info_service")

        stopOverlayService = findPreference("stop_overlay_service")

        restartOverlayService = findPreference("restart_overlay_service")

        addSetting = findPreference("add_setting")

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        forciblyShowRateTheApp?.isVisible = !isGooglePlay(requireContext())

        startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
            .isStartedCapacityInfoService()

        stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        stopOverlayService?.isEnabled = OverlayService.instance != null

        restartOverlayService?.isEnabled = OverlayService.instance != null

        startCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            ServiceHelper.startService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(3700L)
                it.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
                    .isStartedCapacityInfoService()

                stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

                restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        stopCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            restartCapacityInfoService?.isEnabled = false

            ServiceHelper.stopService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(2500L)
                startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null
                        && !ServiceHelper.isStartedCapacityInfoService()

                it.isEnabled = CapacityInfoService.instance != null

                restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        restartCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            stopCapacityInfoService?.isEnabled = false

            ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(6200L)
                startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null
                        && !ServiceHelper.isStartedCapacityInfoService()

                stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

                it.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        stopOverlayService?.setOnPreferenceClickListener {

            it.isEnabled = false

            restartOverlayService?.isEnabled = false

            ServiceHelper.stopService(requireContext(), OverlayService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(1500L)
                it.isEnabled = OverlayService.instance != null

                restartOverlayService?.isEnabled = OverlayService.instance != null
            }

            true
        }

        restartOverlayService?.setOnPreferenceClickListener {

            it.isEnabled = false

            stopOverlayService?.isEnabled = false

            ServiceHelper.restartService(requireContext(), OverlayService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(4800L)
                stopOverlayService?.isEnabled = OverlayService.instance != null

                it.isEnabled = OverlayService.instance != null
            }

            true
        }

        addSetting?.setOnPreferenceClickListener {

            addSettingDialog(requireContext(), pref)

            true
        }

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
    }

    override fun onResume() {

        super.onResume()

        startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
            .isStartedCapacityInfoService()

        stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        stopOverlayService?.isEnabled = OverlayService.instance != null

        restartOverlayService?.isEnabled = OverlayService.instance != null
    }
}