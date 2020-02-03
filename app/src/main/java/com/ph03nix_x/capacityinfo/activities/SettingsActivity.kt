package com.ph03nix_x.capacityinfo.activities

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity(), ServiceInterface, BillingInterface {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        LocaleHelper.setLocale(this, pref.getString(LANGUAGE, null) ?: defLang)

        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.toolbar)

        toolbar.title = getString(R.string.settings)

        toolbar.navigationIcon = getDrawable(R.drawable.ic_arrow_back_24dp)

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

        if(isInstalledGooglePlay)
        CoroutineScope(Dispatchers.Default).launch {

            if(billingClient == null)
                billingClient = onBillingClientBuilder(this@SettingsActivity)

            onBillingStartConnection(this@SettingsActivity)
        }

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, SettingsFragment())
            commit()
        }
    }

    override fun onResume() {

        super.onResume()

        if(CapacityInfoService.instance == null) startService(this)
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

    override fun onStop() {

        super.onStop()

        billingClient?.endConnection()
        billingClient = null
    }

    override fun onDestroy() {
        super.onDestroy()

        billingClient?.endConnection()
        billingClient = null
    }
}