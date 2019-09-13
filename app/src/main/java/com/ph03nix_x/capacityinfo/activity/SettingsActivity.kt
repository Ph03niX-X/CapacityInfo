package com.ph03nix_x.capacityinfo.activity

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.services.CapacityInfoJob
import com.ph03nix_x.capacityinfo.services.CapacityInfoService

class SettingsActivity : AppCompatActivity() {

    private lateinit var pref: SharedPreferences
    private lateinit var enableService: SwitchCompat
    private lateinit var darkMode: SwitchCompat
    private lateinit var fahrenheit: SwitchCompat
    private lateinit var showLastChargeTime: SwitchCompat
    private lateinit var settingsLayout: LinearLayout
    private lateinit var changeDesignCapacity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(pref.getBoolean(Preferences.DarkMode.prefName, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        enableService = findViewById(R.id.enable_service)
        darkMode = findViewById(R.id.dark_mode)
        fahrenheit = findViewById(R.id.temperature_in_fahrenheit)
        showLastChargeTime = findViewById(R.id.show_last_charge_time)
        settingsLayout = findViewById(R.id.settings_layout)
        changeDesignCapacity = findViewById(R.id.change_design_capacity)

        if(pref.getBoolean(Preferences.DarkMode.prefName, false)) {

            settingsLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))

            enableService.background = getDrawable(R.drawable.selecteditem)
            darkMode.background = getDrawable(R.drawable.selecteditem)
            fahrenheit.background = getDrawable(R.drawable.selecteditem)
            showLastChargeTime.background = getDrawable(R.drawable.selecteditem)
            changeDesignCapacity.background = getDrawable(R.drawable.selecteditem)

            changeDesignCapacity.setTextColor(Color.WHITE)
        }

        enableService.isChecked = pref.getBoolean(Preferences.EnableService.prefName, true)
        darkMode.isChecked = pref.getBoolean(Preferences.DarkMode.prefName, false)
        fahrenheit.isChecked = pref.getBoolean(Preferences.Fahrenheit.prefName, false)
        showLastChargeTime.isChecked = pref.getBoolean(Preferences.ShowLastChargeTime.prefName, true)

        enableService.setOnCheckedChangeListener { _, b ->

            if(!b) stopService(Intent(this, CapacityInfoService::class.java))

            else startJob()

            pref.edit().putBoolean(Preferences.EnableService.prefName, b).apply()
        }

        darkMode.setOnCheckedChangeListener { _, b ->

            pref.edit().putBoolean(Preferences.DarkMode.prefName, b).apply()
            MainActivity.instance!!.recreate()
            recreate()
        }

        fahrenheit.setOnCheckedChangeListener { _, b ->  pref.edit().putBoolean(Preferences.Fahrenheit.prefName, b).apply() }

        showLastChargeTime.setOnCheckedChangeListener { _, b -> pref.edit().putBoolean(Preferences.ShowLastChargeTime.prefName, b).apply() }

        changeDesignCapacity.setOnClickListener { changeDesignCapacity() }
    }

    private fun changeDesignCapacity() {

        val dialog = AlertDialog.Builder(this)

        val view = LayoutInflater.from(this).inflate(R.layout.change_design_capacity, null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<EditText>(R.id.change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefName, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefName, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefName, 0) / -1).toString())

        dialog.setPositiveButton(getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefName, changeDesignCapacity.text.toString().toInt()).apply() }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }

    private fun startJob() {

        val componentName = ComponentName(this, CapacityInfoJob::class.java)

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val job = JobInfo.Builder(1, componentName).apply {

            setMinimumLatency(1000)
            setRequiresCharging(false)
            setPersisted(false)
        }

        jobScheduler.schedule(job.build())
    }
}