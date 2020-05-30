package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Bundle
import androidx.preference.*
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utils.Utils.isGooglePlay

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface, ServiceInterface {

    private lateinit var pref: SharedPreferences
    
    private var forciblyShowRateTheApp: SwitchPreferenceCompat? = null
    private var addSetting: Preference? = null
    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.debug_settings)

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        addSetting = findPreference("add_setting")

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        forciblyShowRateTheApp?.isVisible = !isGooglePlay(requireContext())

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
}