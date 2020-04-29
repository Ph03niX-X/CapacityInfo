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
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SHOW_NOT_SUPPORTED_DIALOG
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LANGUAGE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utils.Utils.isStartedService
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), ServiceInterface, BatteryInfoInterface, SettingsInterface {

    private lateinit var toolbar: CenteredToolbar

    lateinit var designCapacity: TextView
    private lateinit var batteryLevel: TextView
    private lateinit var numberOfCharges: TextView
    private lateinit var numberOfCycles: TextView
    private lateinit var chargingTime: TextView
    private lateinit var batteryHealth: TextView
    private lateinit var residualCapacity: TextView
    private lateinit var currentCapacity: TextView
    private lateinit var capacityAdded: TextView
    private lateinit var technology: TextView
    private lateinit var status: TextView
    private lateinit var plugged: TextView
    private lateinit var chargeCurrent: TextView
    private lateinit var maxChargeDischargeCurrent: TextView
    private lateinit var averageChargeDischargeCurrent: TextView
    private lateinit var minChargeDischargeCurrent: TextView
    private lateinit var temperature: TextView
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

                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.instruction))
                setMessage(getString(R.string.instruction_message + R.string.instruction_message_do_not_kill_the_service
                        + R.string.instruction_message_huawei_honor))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }

            true
        }

        toolbar.menu.findItem(R.id.faq).setOnMenuItemClickListener {

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_faq_question_24dp)
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

        toolbar.menu.findItem(R.id.tips).setOnMenuItemClickListener {

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_tips_for_extending_battery_life_24dp)
                setTitle(getString(R.string.tips_dialog_title))
                setMessage(getString(R.string.tip1) + getString(R.string.tip2) + getString(R.string.tip3)
                        + getString(R.string.tip4) + getString(R.string.tip5) + getString(R.string.tip6)
                        + getString(R.string.tip7))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }

            true
        }

        designCapacity = findViewById(R.id.design_capacity)
        batteryLevel = findViewById(R.id.battery_level)
        numberOfCharges = findViewById(R.id.number_of_charges)
        numberOfCycles = findViewById(R.id.number_of_cycles)
        chargingTime = findViewById(R.id.charging_time)
        currentCapacity = findViewById(R.id.current_capacity)
        capacityAdded = findViewById(R.id.capacity_added)
        batteryHealth = findViewById(R.id.battery_health)
        residualCapacity = findViewById(R.id.residual_capacity)
        technology = findViewById(R.id.battery_technology)
        status = findViewById(R.id.status)
        plugged = findViewById(R.id.plugged)
        chargeCurrent = findViewById(R.id.charge_current)
        maxChargeDischargeCurrent = findViewById(R.id.max_charge_discharge_current)
        averageChargeDischargeCurrent = findViewById(R.id.average_charge_discharge_current)
        minChargeDischargeCurrent = findViewById(R.id.min_charge_discharge_current)
        temperature = findViewById(R.id.temperature)
        voltage = findViewById(R.id.voltage)
        lastChargeTime = findViewById(R.id.last_charge_time)
        batteryWear = findViewById(R.id.battery_wear)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        designCapacity.setOnClickListener {

            changeDesignCapacity(this)
        }
    }

    override fun onResume() {

        super.onResume()

        if(SettingsActivity.instance != null) {

            launchActivity(this, SettingsActivity::class.java)
            overridePendingTransition(0, 0)
        }
        else if(DebugActivity.instance != null) {

            launchActivity(this, DebugActivity::class.java)

            overridePendingTransition(0, 0)
        }

        if(CapacityInfoService.instance == null && !isStartedService) {

            isStartedService = true

            startService(this)
        }

        instance = this

        if(pref.getInt(DESIGN_CAPACITY, 0) <= 0 || pref.getInt(DESIGN_CAPACITY, 0) > 18500) {

            pref.edit().apply {

                putInt(DESIGN_CAPACITY, getDesignCapacity(this@MainActivity))

                if(pref.getInt(DESIGN_CAPACITY, 0) < 0)
                    putInt(DESIGN_CAPACITY, (pref.getInt(DESIGN_CAPACITY, 0) / -1))

                apply()
            }
        }

        designCapacity.text = getString(R.string.design_capacity, pref.getInt(DESIGN_CAPACITY, 0).toString())

        batteryHealth.text = getString(R.string.battery_health, getBatteryHealth(this))

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%", "0")

        isJob = true

        batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        if(getCurrentCapacity(this) == 0.0 && pref.getBoolean(IS_SHOW_NOT_SUPPORTED_DIALOG, true)) {

            pref.edit().putBoolean(IS_SHOW_NOT_SUPPORTED_DIALOG, false).apply()

            pref.edit().putBoolean(IS_SUPPORTED, true).apply()

            MaterialAlertDialogBuilder(this).apply {

                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.not_supported))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }
        }

        if(getCurrentCapacity(this) == 0.0) toolbar.menu.findItem(R.id.instruction).isVisible = false

        else if(pref.getBoolean(IS_SHOW_INSTRUCTION, true)) showInstruction()

        batteryInformation()

        val prefArrays = intent.getSerializableExtra("pref_arrays") as? HashMap<*, *>
        if(prefArrays != null) importSettings(prefArrays)
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

            setIcon(R.drawable.ic_instruction_not_supported_24dp)
            setTitle(getString(R.string.instruction))
            setMessage(getString(R.string.instruction_message))
            setPositiveButton(android.R.string.ok) { _, _ -> pref.edit().putBoolean(IS_SHOW_INSTRUCTION, false).apply() }
            show()
        }
    }

    private fun batteryInformation() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
                    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    withContext(Dispatchers.Main) {

                        designCapacity.text = getString(R.string.design_capacity, pref.getInt(DESIGN_CAPACITY, 0).toString())

                        batteryLevel.text = getString(R.string.battery_level, "${getBatteryLevel(this@MainActivity)}%")

                        numberOfCharges.text = getString(R.string.number_of_charges, pref.getLong(NUMBER_OF_CHARGES, 0))

                        numberOfCycles.text = getString(R.string.number_of_cycles, DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))

                        if(CapacityInfoService.instance?.seconds ?: 0 > 0) {

                            chargingTime.visibility = View.VISIBLE

                            chargingTime.text = getChargingTime(this@MainActivity, CapacityInfoService.instance?.seconds ?: 0)
                        }
                        else if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.VISIBLE

                        lastChargeTime.text = getString(R.string.last_charge_time, getLastChargeTime(this@MainActivity),
                            "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%", "${pref.getInt(BATTERY_LEVEL_TO, 0)}%")
                    }

                    withContext(Dispatchers.Main) {

                        this@MainActivity.status.text = getString(R.string.status, getStatus(this@MainActivity, status))

                        if(getPlugged(this@MainActivity, plugged) != "N/A") {

                            if(this@MainActivity.plugged.visibility == View.GONE) this@MainActivity.plugged.visibility = View.VISIBLE

                            this@MainActivity.plugged.text = getPlugged(this@MainActivity, plugged)
                        }

                        else this@MainActivity.plugged.visibility = View.GONE
                    }

                    withContext(Dispatchers.Main) {

                        technology.text = getString(R.string.battery_technology, batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))

                        temperature.text = if (!pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) getString(R.string.temperature_celsius,
                            getTemperature(this@MainActivity))

                        else getString(R.string.temperature_fahrenheit, getTemperature(this@MainActivity))

                        voltage.text = getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv else R.string.voltage,
                            DecimalFormat("#.#").format(getVoltage(this@MainActivity)))

                        batteryHealth.text = getString(R.string.battery_health, getBatteryHealth(this@MainActivity))
                    }

                    if (pref.getBoolean(IS_SUPPORTED, true)) {

                        if (pref.getInt(DESIGN_CAPACITY, 0) > 0 && pref.getInt(RESIDUAL_CAPACITY, 0) > 0) {

                            withContext(Dispatchers.Main) {

                                residualCapacity.text =  getResidualCapacity(this@MainActivity,
                                    batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                                        BatteryManager.BATTERY_STATUS_UNKNOWN) == BatteryManager.BATTERY_STATUS_CHARGING)

                                batteryWear.text = getBatteryWear(this@MainActivity)

                            }
                        }

                        if (getCurrentCapacity(this@MainActivity) > 0) {

                            if (currentCapacity.visibility == View.GONE) withContext(Dispatchers.Main) { currentCapacity.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(getCurrentCapacity(this@MainActivity)))

                                when {
                                    getPlugged(this@MainActivity, plugged) != "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = getCapacityAdded(this@MainActivity)
                                    }
                                    getPlugged(this@MainActivity, plugged) == "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE) capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = getCapacityAdded(this@MainActivity)
                                    }
                                    capacityAdded.visibility == View.VISIBLE -> capacityAdded.visibility = View.GONE
                                }
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

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING -> {

                            if(chargeCurrent.visibility == View.GONE) withContext(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                            if(numberOfCharges.visibility == View.VISIBLE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.GONE }

                            withContext(Dispatchers.Main) {

                                chargeCurrent.text = getString(R.string.charge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                            }
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING, BatteryManager.BATTERY_STATUS_FULL, BatteryManager.BATTERY_STATUS_NOT_CHARGING -> {

                            if(chargeCurrent.visibility == View.GONE) withContext(Dispatchers.Main) { chargeCurrent.visibility = View.VISIBLE }

                            if(numberOfCharges.visibility == View.GONE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                chargeCurrent.text = getString(R.string.discharge_current, getChargeDischargeCurrent(this@MainActivity).toString())
                            }
                        }

                        else -> {

                            if(chargeCurrent.visibility == View.VISIBLE) withContext(Dispatchers.Main) {  chargeCurrent.visibility = View.GONE }

                            if(numberOfCharges.visibility == View.GONE) withContext(Dispatchers.Main) { numberOfCharges.visibility = View.VISIBLE }
                        }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING, BatteryManager.BATTERY_STATUS_FULL, BatteryManager.BATTERY_STATUS_NOT_CHARGING ->

                            withContext(Dispatchers.Main) {

                                if(maxChargeDischargeCurrent.visibility ==  View.GONE) maxChargeDischargeCurrent.visibility = View.VISIBLE

                                if(averageChargeDischargeCurrent.visibility ==  View.GONE) averageChargeDischargeCurrent.visibility = View.VISIBLE

                                if(minChargeDischargeCurrent.visibility ==  View.GONE) minChargeDischargeCurrent.visibility = View.VISIBLE

                                maxChargeDischargeCurrent.text = getString(R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)

                                averageChargeDischargeCurrent.text = getString(R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)

                                minChargeDischargeCurrent.text = getString(R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
                        }

                        BatteryManager.BATTERY_STATUS_DISCHARGING -> withContext(Dispatchers.Main) {

                            if(maxChargeDischargeCurrent.visibility ==  View.GONE) maxChargeDischargeCurrent.visibility = View.VISIBLE

                            if(averageChargeDischargeCurrent.visibility ==  View.GONE) averageChargeDischargeCurrent.visibility = View.VISIBLE

                            if(minChargeDischargeCurrent.visibility ==  View.GONE) minChargeDischargeCurrent.visibility = View.VISIBLE

                            maxChargeDischargeCurrent.text = getString(R.string.max_discharge_current, BatteryInfoInterface.maxDischargeCurrent)

                            averageChargeDischargeCurrent.text = getString(R.string.average_discharge_current, BatteryInfoInterface.averageDischargeCurrent)

                            minChargeDischargeCurrent.text = getString(R.string.min_discharge_current, BatteryInfoInterface.minDischargeCurrent)
                        }

                        else -> {

                            withContext(Dispatchers.Main) {

                                if(maxChargeDischargeCurrent.visibility ==  View.VISIBLE) maxChargeDischargeCurrent.visibility = View.GONE

                                if(averageChargeDischargeCurrent.visibility ==  View.VISIBLE) averageChargeDischargeCurrent.visibility = View.GONE

                                if(minChargeDischargeCurrent.visibility ==  View.VISIBLE) minChargeDischargeCurrent.visibility = View.GONE

                            }
                        }
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING -> delay(if(getCurrentCapacity(this@MainActivity) > 0) 955 else 962)

                        else -> delay(3000)
                    }
                }
            }
    }

    private fun importSettings(prefArrays: HashMap<*, *>) {

        val prefsTempList = arrayListOf(BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, DESIGN_CAPACITY,
            CAPACITY_ADDED, LAST_CHARGE_TIME, PERCENT_ADDED, RESIDUAL_CAPACITY, IS_SUPPORTED,
            IS_SHOW_NOT_SUPPORTED_DIALOG, IS_SHOW_INSTRUCTION)

        prefsTempList.forEach {

            with(prefArrays) {

                when {

                    !containsKey(it) -> pref.edit().remove(it).apply()

                    else -> {

                        prefArrays.forEach {

                            when(it.key as String) {

                                NUMBER_OF_CHARGES -> pref.edit().putLong(it.key as String, it.value as Long).apply()

                                BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, LAST_CHARGE_TIME,
                                DESIGN_CAPACITY, RESIDUAL_CAPACITY, PERCENT_ADDED -> pref.edit().putInt(it.key as String, it.value as Int).apply()

                                CAPACITY_ADDED, NUMBER_OF_CYCLES -> pref.edit().putFloat(it.key as String, it.value as Float).apply()

                                IS_SUPPORTED, IS_SHOW_NOT_SUPPORTED_DIALOG, IS_SHOW_INSTRUCTION ->
                                    pref.edit().putBoolean(it.key as String, it.value as Boolean).apply()
                            }
                        }
                    }
                }
            }
        }

        launchActivity(this, SettingsActivity::class.java, arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK))

        Toast.makeText(this, getString(R.string.settings_imported_successfully), Toast.LENGTH_LONG).show()

        intent.removeExtra("pref_arrays")
    }
}