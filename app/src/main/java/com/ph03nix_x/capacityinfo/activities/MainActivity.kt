package com.ph03nix_x.capacityinfo.activities

import android.content.*
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.*
import com.ph03nix_x.capacityinfo.MainApp.Companion.defLang
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import com.ph03nix_x.capacityinfo.services.*
import com.ph03nix_x.capacityinfo.view.CenteredToolbar
import java.text.DecimalFormat
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_CHARGING_TIME_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_LAST_CHARGE_TIME_IN_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
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

        LocaleHelper.setLocale(this, pref.getString(LANGUAGE, null) ?: defLang)

        setContentView(R.layout.activity_main)

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
                        + getString(R.string.faq_i_have_everything_in_zeros)
                        + getString(R.string.faq_units) + getString(R.string.faq_current_capacity)
                        + getString(R.string.faq_add_device_support))
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

        if(CapacityInfoService.instance == null) startService(this)

        if(instance == null) instance = this

        if(pref.getInt(DESIGN_CAPACITY, 0) <= 0 || pref.getInt(DESIGN_CAPACITY, 0) >= 100000) {

            pref.edit().apply {

                putInt(DESIGN_CAPACITY, getDesignCapacity(this@MainActivity))

                if(pref.getInt(DESIGN_CAPACITY, 0) < 0)
                    putInt(DESIGN_CAPACITY, (pref.getInt(DESIGN_CAPACITY, 0) / -1))

                apply()
            }
        }

        capacityDesign.text = getString(R.string.capacity_design, pref.getInt(DESIGN_CAPACITY, 0).toString())

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%", "0")

        isJob = true

        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        if(getCurrentCapacity(this) == 0.0 && pref.getBoolean(IS_SHOW_NOT_SUPPORTED_DIALOG, true)) {

            pref.edit().putBoolean(IS_SHOW_NOT_SUPPORTED_DIALOG, false).apply()

            pref.edit().putBoolean(IS_SUPPORTED, true).apply()

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_info_outline_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.not_supported))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }
        }

        if(getCurrentCapacity(this) == 0.0) toolbar.menu.findItem(R.id.instruction).isVisible = false

        else if(pref.getBoolean(IS_SHOW_INSTRUCTION, true)) showInstruction()

        startJob()

        val extras = intent.extras
        if(extras != null && extras.getBoolean("is_import_settings", false)) {

            launchActivity(this, SettingsActivity::class.java, arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK))

            Toast.makeText(this, getString(R.string.settings_imported_successfully), Toast.LENGTH_LONG).show()

            intent.removeExtra("is_import_settings")
        }
    }

    override fun onStop() {

        super.onStop()

        isJob = false
        job?.cancel()
        job = null
    }

    override fun onDestroy() {

        isJob = false
        job?.cancel()
        job = null
        instance = null

        super.onDestroy()
    }

    private fun showInstruction() {

        MaterialAlertDialogBuilder(this).apply {

            setIcon(R.drawable.ic_info_outline_24dp)
            setTitle(getString(R.string.instruction))
            setMessage(getString(R.string.instruction_message))
            setPositiveButton(android.R.string.ok) { _, _ -> pref.edit().putBoolean(IS_SHOW_INSTRUCTION, false).apply() }
            show()
        }
    }

    private fun startJob() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    withContext(Dispatchers.Main) {

                        batteryLevel.text = getString(R.string.battery_level, "${getBatteryLevel(this@MainActivity)}%")
                        numberOfCharges.text = getString(R.string.number_of_charges, pref.getLong(NUMBER_OF_CHARGES, 0))
                    }

                    if(pref.getBoolean(IS_SHOW_CHARGING_TIME_IN_APP, true))
                        when(plugged) {

                            BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                                withContext(Dispatchers.Main) {

                                    if(chargingTime.visibility == View.GONE) chargingTime.visibility = View.VISIBLE

                                    if(chargingTime.visibility == View.VISIBLE)
                                        chargingTime.text = getChargingTime(this@MainActivity, CapacityInfoService.instance?.seconds ?: 0)
                                }

                            else -> withContext(Dispatchers.Main) { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }
                        }

                    else withContext(Dispatchers.Main) { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }

                    if(pref.getBoolean(IS_SHOW_LAST_CHARGE_TIME_IN_APP, true)) {

                        withContext(Dispatchers.Main) {

                            if(lastChargeTime.visibility == View.GONE) lastChargeTime.visibility = View.VISIBLE

                            if(pref.getInt(LAST_CHARGE_TIME, 0) > 0)
                                lastChargeTime.text = getString(R.string.last_charge_time, getLastChargeTime(this@MainActivity),
                                    "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%", "${pref.getInt(BATTERY_LEVEL_TO, 0)}%")

                            else {

                                if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE
                            }
                        }
                    }

                    else {

                        withContext(Dispatchers.Main) {

                            if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE

                        }
                    }

                    withContext(Dispatchers.Main) {

                        this@MainActivity.status.text = getStatus(this@MainActivity, status)

                        if(getPlugged(this@MainActivity, plugged) != "N/A") {

                            if(this@MainActivity.plugged.visibility == View.GONE) this@MainActivity.plugged.visibility = View.VISIBLE

                            this@MainActivity.plugged.text = getPlugged(this@MainActivity, plugged)
                        }

                        else this@MainActivity.plugged.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {

                        technology.text = getString(R.string.battery_technology, batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))

                        temperatute.text = if (!pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) getString(R.string.temperature_celsius,
                            getTemperature(this@MainActivity))

                        else getString(R.string.temperature_fahrenheit, getTemperature(this@MainActivity))

                        voltage.text = getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
                            DecimalFormat("#.#").format(getVoltage(this@MainActivity)))
                    }

                    if (pref.getBoolean(IS_SUPPORTED, true)) {

                        if (pref.getInt(DESIGN_CAPACITY, 0) > 0 && pref.getInt(RESIDUAL_CAPACITY, 0) > 0) {

                            withContext(Dispatchers.Main) {

                                residualCapacity.text =  getResidualCapacity(this@MainActivity,
                                    batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING)

                                batteryWear.text = getBatteryWear(this@MainActivity)

                            }
                        }

                        if (getCurrentCapacity(this@MainActivity) > 0) {

                            if (currentCapacity.visibility == View.GONE) withContext(Dispatchers.Main) { currentCapacity.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(getCurrentCapacity(this@MainActivity)))

                                if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_IN_APP, true) && getPlugged(this@MainActivity, plugged) != "N/A") {

                                    if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                    capacityAdded.text = getCapacityAdded(this@MainActivity)
                                }

                                else if(pref.getBoolean(IS_SHOW_CAPACITY_ADDED_LAST_CHARGE_IN_APP, true) && getPlugged(this@MainActivity, plugged) == "N/A") {

                                    if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                    capacityAdded.text = getCapacityAdded(this@MainActivity)
                                }

                                else if(capacityAdded.visibility == View.VISIBLE) capacityAdded.visibility = View.GONE
                            }
                        }

                        else {

                            if (currentCapacity.visibility == View.VISIBLE) withContext(Dispatchers.Main) { currentCapacity.visibility = View.GONE }

                            if (capacityAdded.visibility == View.GONE && pref.getFloat(CAPACITY_ADDED, 0f) > 0f)
                                withContext(Dispatchers.Main) { capacityAdded.visibility = View.VISIBLE }

                            else withContext(Dispatchers.Main) { capacityAdded.visibility = View.GONE }
                        }
                    }

                    else {

                        if (capacityAdded.visibility == View.VISIBLE) withContext(Dispatchers.Main) { capacityAdded.visibility = View.GONE }

                        withContext(Dispatchers.Main) {

                            residualCapacity.text = getString(R.string.residual_capacity_not_supported)
                            batteryWear.text = getString(R.string.battery_wear_not_supported)
                        }

                        if(pref.contains(CAPACITY_ADDED)) pref.edit().remove(CAPACITY_ADDED).apply()

                        if(pref.contains(PERCENT_ADDED)) pref.edit().remove(PERCENT_ADDED).apply()
                    }

                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                        if (chargeCurrent.visibility == View.GONE) withContext(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                        if (numberOfCharges.visibility == View.VISIBLE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.GONE }

                        withContext(Dispatchers.Main) {

                            chargeCurrent.text = getString(R.string.charge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                        }

                    } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                        if (chargeCurrent.visibility == View.GONE) withContext(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                        if (numberOfCharges.visibility == View.GONE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }

                        withContext(Dispatchers.Main) {

                            chargeCurrent.text = getString(R.string.discharge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                        }
                    } else {

                        if (chargeCurrent.visibility == View.VISIBLE) withContext(Dispatchers.Main) {  chargeCurrent.visibility = View.GONE }

                        if (numberOfCharges.visibility == View.GONE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }
                    }

                    delay(if(getCurrentCapacity(this@MainActivity) > 0) 958 else 965)
                }
            }
    }
}