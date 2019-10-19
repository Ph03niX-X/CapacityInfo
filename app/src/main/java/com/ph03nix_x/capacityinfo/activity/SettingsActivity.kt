package com.ph03nix_x.capacityinfo.activity

import android.app.UiModeManager
import android.content.*
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
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

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
//            && pref.getBoolean(Preferences.DarkMode.prefKey, false)) setTheme(R.style.DarkTheme)
//
//        else if(pref.getBoolean(Preferences.DarkMode.prefKey, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.settings_toolbar)
        toolbar.setTitle(R.string.settings)

//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
//        toolbar.navigationIcon = getDrawable(if (pref.getBoolean(Preferences.DarkMode.prefKey, false))
//            R.drawable.ic_arrow_back_white_24dp else R.drawable.ic_arrow_back_black_24dp)

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