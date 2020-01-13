package com.ph03nix_x.capacityinfo.activity

import android.content.*
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.Util.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.services.*
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import java.text.DecimalFormat
import com.ph03nix_x.capacityinfo.MainApp.Companion.setModeNight
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), ServiceInterface, BatteryInfoInterface {

    private lateinit var toolbar: CenteredToolbar

    private lateinit var capacityDesign: TextView
    private lateinit var batteryLevel: TextView
    private lateinit var numberOfCharges: TextView
    private lateinit var chargingTime: TextView
    private lateinit var residualCapacity: TextView
    private lateinit var currentCapacity: TextView
    private lateinit var capacityAdded: TextView
    private lateinit var technology: TextView
    private lateinit var status: TextView
    private lateinit var plugged: TextView
    private lateinit var chargeCurrent: TextView
    private lateinit var temperatute: TextView
    private lateinit var voltage: TextView
    private lateinit var lastChargeTime: TextView
    private lateinit var batteryWear: TextView
    private lateinit var pref: SharedPreferences
    private lateinit var batteryManager: BatteryManager
    private var isJob = false
    private var job: Job? = null

    companion object {

        var instance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        LocaleHelper.setLocale(this, pref.getString(Preferences.Language.prefKey, null) ?: defLang)

        setContentView(R.layout.activity_main)

        setModeNight(this)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.menu.findItem(R.id.settings).setOnMenuItemClickListener {

            startActivity(Intent(this, SettingsActivity::class.java))

            true
        }

        toolbar.menu.findItem(R.id.instruction).setOnMenuItemClickListener {

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_info_outline_24dp)
                setTitle(getString(R.string.instruction))
                setMessage(getString(R.string.instruction_message))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }

            true
        }

        toolbar.menu.findItem(R.id.faq).setOnMenuItemClickListener {

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_help_outline_dialog_24dp)
                setTitle(getString(R.string.faq))
                setMessage(getString(R.string.faq_how_does_the_app_work) + getString(R.string.faq_capacity_added)
                        + getString(R.string.faq_units) + getString(R.string.faq_add_device_support))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }

            true
        }

        capacityDesign = findViewById(R.id.capacity_design)
        batteryLevel = findViewById(R.id.battery_level)
        numberOfCharges = findViewById(R.id.number_of_charges)
        chargingTime = findViewById(R.id.charging_time)
        currentCapacity = findViewById(R.id.current_capacity)
        capacityAdded = findViewById(R.id.capacity_added)
        residualCapacity = findViewById(R.id.residual_capacity)
        technology = findViewById(R.id.battery_technology)
        status = findViewById(R.id.status)
        plugged = findViewById(R.id.plugged)
        chargeCurrent = findViewById(R.id.charge_current)
        temperatute = findViewById(R.id.temperature)
        voltage = findViewById(R.id.voltage)
        lastChargeTime = findViewById(R.id.last_charge_time)
        batteryWear = findViewById(R.id.battery_wear)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun onResume() {

        super.onResume()

        if(pref.getBoolean(Preferences.IsEnableService.prefKey, true)
            && CapacityInfoService.instance == null) startService(this)

        instance = this

        if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) <= 0 || pref.getInt(
                Preferences.DesignCapacity.prefKey, 0) >= 100000) {

            pref.edit().apply {

                putInt(Preferences.DesignCapacity.prefKey, getDesignCapacity(this@MainActivity))

                if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) < 0)
                    putInt(Preferences.DesignCapacity.prefKey, (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1))

                apply()
            }
        }

        capacityDesign.text = getString(R.string.capacity_design, pref.getInt(Preferences.DesignCapacity.prefKey, 0).toString())

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%", "0")

        isJob = true

        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        if(getCurrentCapacity(this) == 0.0 && pref.getBoolean(Preferences.IsShowNotSupportedDialog.prefKey, true)) {

            pref.edit().putBoolean(Preferences.IsShowNotSupportedDialog.prefKey, false).apply()

            pref.edit().putBoolean(Preferences.IsSupported.prefKey, true).apply()

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_info_outline_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.not_supported))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }
        }

        if(getCurrentCapacity(this) == 0.0) toolbar.menu.findItem(R.id.instruction).isVisible = false

        else if(pref.getBoolean(Preferences.IsShowInstruction.prefKey, true)) showInstruction()

        startJob()
    }

    override fun onStop() {

        super.onStop()

        isJob = false

        job = null
    }

    override fun onDestroy() {

        isJob = false

        job = null

        instance = null

        super.onDestroy()
    }

    private fun showInstruction() {

        MaterialAlertDialogBuilder(this).apply {

            setIcon(R.drawable.ic_info_outline_24dp)
            setTitle(getString(R.string.instruction))
            setMessage(getString(R.string.instruction_message))
            setPositiveButton(android.R.string.ok) { _, _ -> pref.edit().putBoolean(Preferences.IsShowInstruction.prefKey, false).apply() }
            show()
        }
    }

    private fun startJob() {

        if(job == null)
            job = GlobalScope.launch {

                while(isJob) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    launch(Dispatchers.Main) {

                        batteryLevel.text = getString(R.string.battery_level, "${getBatteryLevel(this@MainActivity)}%")
                        numberOfCharges.text = getString(R.string.number_of_charges, pref.getLong(Preferences.NumberOfCharges.prefKey, 0))
                    }

                    if(pref.getBoolean(Preferences.IsShowChargingTimeInApp.prefKey, true))
                        when(plugged) {

                            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                                launch(Dispatchers.Main) {

                                    if(chargingTime.visibility == View.GONE && pref.getBoolean(Preferences.IsEnableService.prefKey, true))
                                        chargingTime.visibility = View.VISIBLE

                                    else if(chargingTime.visibility == View.VISIBLE && !pref.getBoolean(Preferences.IsEnableService.prefKey, true))
                                        chargingTime.visibility = View.GONE

                                    if(chargingTime.visibility == View.VISIBLE)
                                        chargingTime.text = getChargingTime(this@MainActivity, CapacityInfoService.instance?.seconds ?: 0)
                                }

                            else -> launch(Dispatchers.Main) { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }
                        }

                    else launch(Dispatchers.Main) { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }

                    if(pref.getBoolean(Preferences.IsShowLastChargeTimeInApp.prefKey, true)) {

                        launch(Dispatchers.Main) {

                            if(lastChargeTime.visibility == View.GONE) lastChargeTime.visibility = View.VISIBLE

                            if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0)
                                lastChargeTime.text = getString(R.string.last_charge_time, getLastChargeTime(this@MainActivity),
                                    "${pref.getInt(Preferences.BatteryLevelWith.prefKey, 0)}%", "${pref.getInt(Preferences.BatteryLevelTo.prefKey, 0)}%")

                            else {

                                if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE
                            }

                        }
                    }

                    else {

                        launch(Dispatchers.Main) {

                            if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE

                        }
                    }

                    launch(Dispatchers.Main) {

                        this@MainActivity.status.text = getStatus(this@MainActivity, status)

                        if(getPlugged(this@MainActivity, plugged) != "N/A") {

                            if(this@MainActivity.plugged.visibility == View.GONE) this@MainActivity.plugged.visibility = View.VISIBLE

                            this@MainActivity.plugged.text = getPlugged(this@MainActivity, plugged)
                        }

                        else this@MainActivity.plugged.visibility = View.GONE
                    }

                    launch(Dispatchers.Main) {

                        technology.text = getString(R.string.battery_technology, batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))

                        temperatute.text = if (!pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) getString(R.string.temperature_celsius,
                            getTemperature(this@MainActivity))

                        else getString(R.string.temperature_fahrenheit, getTemperature(this@MainActivity))

                        voltage.text = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                            DecimalFormat("#.#").format(getVoltage(this@MainActivity)))
                    }

                    if (pref.getBoolean(Preferences.IsSupported.prefKey, true)) {

                        if (pref.getInt(Preferences.DesignCapacity.prefKey, 0) > 0 && pref.getInt(
                                Preferences.ResidualCapacity.prefKey, 0) > 0) {

                            launch(Dispatchers.Main) {

                                residualCapacity.text =  getResidualCapacity(this@MainActivity,
                                    batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING)

                                batteryWear.text = getBatteryWear(this@MainActivity)
                            }
                        }

                        if (getCurrentCapacity(this@MainActivity) > 0) {

                            if (currentCapacity.visibility == View.GONE) launch(Dispatchers.Main) { currentCapacity.visibility = View.VISIBLE }

                            launch(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(getCurrentCapacity(this@MainActivity)))

                                if(pref.getBoolean(Preferences.IsShowCapacityAddedInApp.prefKey, true) && getPlugged(this@MainActivity, plugged) != "N/A") {

                                    if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                    capacityAdded.text = getCapacityAdded(this@MainActivity)
                                }

                                else if(pref.getBoolean(Preferences.IsShowCapacityAddedLastChargeInApp.prefKey, true) && getPlugged(this@MainActivity, plugged) == "N/A") {

                                    if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                    capacityAdded.text = getCapacityAdded(this@MainActivity)
                                }

                                else if(capacityAdded.visibility == View.VISIBLE) capacityAdded.visibility = View.GONE
                            }
                        }

                        else {

                            if (currentCapacity.visibility == View.VISIBLE) launch(Dispatchers.Main) { currentCapacity.visibility = View.GONE }

                            if (capacityAdded.visibility == View.VISIBLE) launch(Dispatchers.Main) { capacityAdded.visibility = View.GONE }

                            if(pref.getBoolean(Preferences.IsSupported.prefKey, true)) pref.edit().putBoolean(Preferences.IsSupported.prefKey, false).apply()
                        }

                    }

                    else {

                        if (capacityAdded.visibility == View.VISIBLE) launch(Dispatchers.Main) { capacityAdded.visibility = View.GONE }

                        launch(Dispatchers.Main) {

                            residualCapacity.text = getString(R.string.residual_capacity_not_supported)
                            batteryWear.text = getString(R.string.battery_wear_not_supported)
                        }

                        if(pref.contains(Preferences.CapacityAdded.prefKey)) pref.edit().remove(Preferences.CapacityAdded.prefKey).apply()

                        if(pref.contains(Preferences.PercentAdded.prefKey)) pref.edit().remove(Preferences.PercentAdded.prefKey).apply()
                    }

                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                        if (chargeCurrent.visibility == View.GONE) launch(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                        if (numberOfCharges.visibility == View.VISIBLE) launch(Dispatchers.Main) { numberOfCharges.visibility = View.GONE }

                        launch(Dispatchers.Main) {

                            chargeCurrent.text = getString(R.string.charge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                        }

                    } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                        if (chargeCurrent.visibility == View.GONE) launch(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                        if (numberOfCharges.visibility == View.GONE) launch(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }

                        launch(Dispatchers.Main) {

                            chargeCurrent.text = getString(R.string.discharge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                        }
                    } else {

                        if (chargeCurrent.visibility == View.VISIBLE) launch(Dispatchers.Main) {  chargeCurrent.visibility = View.GONE }

                        if (numberOfCharges.visibility == View.GONE) launch(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }
                    }

                    delay(if(getCurrentCapacity(this@MainActivity) > 0) 958 else 965)
                }
            }
    }
}