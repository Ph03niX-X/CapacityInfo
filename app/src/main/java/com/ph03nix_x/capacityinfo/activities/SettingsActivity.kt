package com.ph03nix_x.capacityinfo.activities

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.receivers.OpenDebugReceiver.Companion.isDebug
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class SettingsActivity : AppCompatActivity(), ServiceInterface {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    companion object {

        var instance: SettingsActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        LocaleHelper.setLocale(this, pref.getString(LANGUAGE, null) ?: defLang)

        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.toolbar)

        toolbar.title = getString(if(!isDebug) R.string.settings else R.string.debug)

        toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_24dp)

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

        if(!isDebug)
        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, SettingsFragment())
            commit()
        }

        else {

            supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, DebugFragment())
                commit()
            }

            isDebug = false
        }
    }

    override fun onResume() {

        super.onResume()

        instance = this

        if(CapacityInfoService.instance == null && !isStartedService) {

            isStartedService = true

            onStartService(this, CapacityInfoService::class.java)
        }

        if(OverlayService.instance == null && OverlayInterface.isEnabledOverlay(this))
            onStartService(this, OverlayService::class.java)
    }

    override fun onDestroy() {

        instance = null

        super.onDestroy()
    }

    override fun onBackPressed() {

        if(toolbar.title != getString(R.string.settings)) {

            toolbar.title = getString(R.string.settings)

            supportFragmentManager.beginTransaction().apply {

                replace(R.id.container, SettingsFragment())
                commit()
            }
        }

        else {

            instance = null

            super.onBackPressed()
        }
    }
}