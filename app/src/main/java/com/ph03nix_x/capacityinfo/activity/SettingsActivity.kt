package com.ph03nix_x.capacityinfo.activity

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.fragment.SettingsFragment
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.view.CenteredToolbar

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: CenteredToolbar

    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getBoolean(Preferences.DarkMode.prefName, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, SettingsFragment())
            .commit()
    }


}