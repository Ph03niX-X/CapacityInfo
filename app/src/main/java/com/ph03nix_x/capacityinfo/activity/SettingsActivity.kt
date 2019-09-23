package com.ph03nix_x.capacityinfo.activity

import android.content.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.fragment.SettingsFragment
import com.ph03nix_x.capacityinfo.view.CenteredToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: CenteredToolbar

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        if (pref.getBoolean(Preferences.DarkMode.prefName, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)

        toolbar.navigationIcon = getDrawable(if (pref.getBoolean(Preferences.DarkMode.prefName, false))
            R.drawable.ic_arrow_back_white_24dp else R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        supportFragmentManager
            .beginTransaction().apply {
                replace(R.id.container, SettingsFragment())
                commit()
            }
    }


}