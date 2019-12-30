package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.MainApp.Companion.getLanguagesList
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.SettingsActivity

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface {

    private lateinit var pref: SharedPreferences

    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null
    private var openSettings: Preference? = null
    private var selectLanguage: ListPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.debug)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            if(pref.contains(Preferences.IsAutoDarkMode.prefKey)) pref.edit().remove(Preferences.IsAutoDarkMode.prefKey).apply()
        }

        else if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        else if(pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        openSettings = findPreference("open_settings")

        selectLanguage = findPreference(Preferences.Language.prefKey)

        if(pref.getString(Preferences.Language.prefKey, null) !in getLanguagesList())
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

        openSettings?.setOnPreferenceClickListener {

            startActivity(Intent(context!!, SettingsActivity::class.java).apply {

                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })

            true
        }

        selectLanguage?.setOnPreferenceChangeListener { _, newValue ->

            changeLanguage(requireContext(), newValue as String)

            true

        }
    }
}