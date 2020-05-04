package com.ph03nix_x.capacityinfo.activities

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.interfaces.OverlayInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService

class DebugActivity : AppCompatActivity(), BillingInterface, ServiceInterface {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    companion object {

        var instance: DebugActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        LocaleHelper.setLocale(this, pref.getString(LANGUAGE, null) ?: defLang)

        setContentView(R.layout.debug_activity)

        toolbar = findViewById(R.id.toolbar)

        toolbar.title = getString(R.string.debug)

        toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_24dp)

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, DebugFragment())
            commit()
        }
    }

    override fun onResume() {

        super.onResume()

        SettingsActivity.instance?.finish()

        instance = this

        if(CapacityInfoService.instance == null && !isStartedService) {

            isStartedService = true

            onStartService(this, CapacityInfoService::class.java)
        }

        if(OverlayInterface.isEnabledOverlay(this) && OverlayService.instance == null)
            onStartService(this, OverlayService::class.java)
    }

    override fun onDestroy() {

        instance = null

        super.onDestroy()
    }

    override fun onBackPressed() {

        instance = null

        super.onBackPressed()
    }
}