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
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay

class DebugActivity : AppCompatActivity(), BillingInterface {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    override fun onCreate(savedInstanceState: Bundle?) {

        isInstalledGooglePlay = isInstalledGooglePlay(this)

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

        if(isInstalledGooglePlay) {

            billingClient = onBillingClientBuilder(this)

            onBillingStartConnection(this)
        }

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, DebugFragment())
            commit()
        }
    }

    override fun onStop() {

        super.onStop()

        if(billingClient != null) billingClient!!.endConnection()

        billingClient = null
    }
}