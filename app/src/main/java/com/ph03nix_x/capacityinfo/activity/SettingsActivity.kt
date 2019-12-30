package com.ph03nix_x.capacityinfo.activity

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.view.CenteredToolbar

class SettingsActivity : AppCompatActivity(), ServiceInterface {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        LocaleHelper.setLocale(this, pref.getString(Preferences.Language.prefKey, null) ?: defLang)

        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.settings_toolbar)

        toolbar.title = getString(R.string.settings)

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, SettingsFragment())
            commit()
        }

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)
            && CapacityInfoService.instance == null) startService(this)
    }

    override fun onResume() {

        super.onResume()

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)
            && CapacityInfoService.instance == null) startService(this)
    }

    override fun onBackPressed() {

        if(toolbar.title != getString(R.string.settings)) {

            toolbar.title = getString(R.string.settings)

            supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, SettingsFragment())
                commit()
            }
        }

        else super.onBackPressed()
    }
}