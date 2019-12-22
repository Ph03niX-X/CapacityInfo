package com.ph03nix_x.capacityinfo.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.view.CenteredToolbar

class DebugActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    lateinit var toolbar: CenteredToolbar

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.debug_activity)

        toolbar = findViewById(R.id.debug_toolbar)

        toolbar.title = getString(R.string.debug)

        toolbar.setNavigationOnClickListener {

            onBackPressed()
        }

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.container, DebugFragment())
            commit()
        }
    }
}