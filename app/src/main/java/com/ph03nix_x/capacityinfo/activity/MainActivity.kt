package com.ph03nix_x.capacityinfo.activity

import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.BatteryManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.Battery
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.async.DoAsync
import com.ph03nix_x.capacityinfo.services.*
import com.ph03nix_x.capacityinfo.view.CenteredToolbar

val sleepArray = arrayOf<Long>(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)
var tempCurrentCapacity: Double = 0.0
@SuppressWarnings("StaticFieldLeak")
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: CenteredToolbar

    private lateinit var capacityDesign: TextView
    private lateinit var batteryLevel: TextView
    private lateinit var chargingTime: TextView
    private lateinit var residualCapacity: TextView
    private lateinit var currentCapacity: TextView
    private lateinit var capacityAdded: TextView
    private lateinit var technology: TextView
    private lateinit var status: TextView
    private lateinit var plugged: TextView
    private lateinit var chargingCurrent: TextView
    private lateinit var temperatute: TextView
    private lateinit var voltage: TextView
    private lateinit var lastChargeTime: TextView
    private lateinit var batteryWear: TextView
    private lateinit var pref: SharedPreferences
    private lateinit var relativeMain: RelativeLayout
    private lateinit var batteryManager: BatteryManager
    private var batteryStatus: Intent? = null
    private var isDoAsync = false

    companion object {

        var instance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        if(pref.getBoolean(Preferences.DarkMode.prefKey, false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.app_name)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.menu.findItem(R.id.settings).setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            return@OnMenuItemClickListener true
        })
        toolbar.menu.findItem(R.id.instruction).setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            MaterialAlertDialogBuilder(this).apply {
                setTitle(getString(R.string.instruction))
                setMessage(getString(R.string.instruction_message))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }
            return@OnMenuItemClickListener true
        })
        toolbar.menu.findItem(R.id.github).setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ph03niX-X/CapacityInfo")))
            return@OnMenuItemClickListener true
        })

        relativeMain = findViewById(R.id.relative_main)
        capacityDesign = findViewById(R.id.capacity_design)
        batteryLevel = findViewById(R.id.battery_level)
        chargingTime = findViewById(R.id.charging_time)
        currentCapacity = findViewById(R.id.current_capacity)
        capacityAdded = findViewById(R.id.capacity_added)
        residualCapacity = findViewById(R.id.residual_capacity)
        technology = findViewById(R.id.battery_technology)
        status = findViewById(R.id.status)
        plugged = findViewById(R.id.plugged)
        chargingCurrent = findViewById(R.id.charging_current)
        temperatute = findViewById(R.id.temperature)
        voltage = findViewById(R.id.voltage)
        lastChargeTime = findViewById(R.id.last_charge_time)
        batteryWear = findViewById(R.id.battery_wear)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        if(pref.getBoolean(Preferences.DarkMode.prefKey, false)) relativeMain.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))

        if(pref.getBoolean(Preferences.IsShowInstruction.prefKey, true)) {

            MaterialAlertDialogBuilder(this).apply {
                setTitle(getString(R.string.instruction))
                setMessage(getString(R.string.instruction_message))
                setPositiveButton(android.R.string.ok) { _, _ -> pref.edit().putBoolean(Preferences.IsShowInstruction.prefKey, false).apply() }
                show()
            }
        }
    }

    override fun onResume() {

        super.onResume()

        if(pref.getBoolean(Preferences.EnableService.prefKey, true)
            && CapacityInfoService.instance == null) startService()

        val battery = Battery(this)

        var isShowDialog = true

        instance = this

        if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) <= 0 || pref.getInt(
                Preferences.DesignCapacity.prefKey, 0) >= 100000) {

            pref.edit().putInt(Preferences.DesignCapacity.prefKey, battery.getDesignCapacity()).apply()

            if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) < 0)
                pref.edit().putInt(Preferences.DesignCapacity.prefKey, (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1)).apply()
        }

        capacityDesign.text = getString(R.string.capacity_design, pref.getInt(Preferences.DesignCapacity.prefKey, 0).toString())

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%")

        isDoAsync = true

        DoAsync {

            while(isDoAsync) {

                batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                runOnUiThread {

                    batteryLevel.text = getString(R.string.battery_level, "${battery.getBatteryLevel()}%")
                }

                if(pref.getBoolean(Preferences.IsShowChargingTimeInApp.prefKey, true))
                when(plugged) {

                    BatteryManager.BATTERY_PLUGGED_AC, BatteryManager.BATTERY_PLUGGED_USB, BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                        runOnUiThread {

                            if(chargingTime.visibility == View.GONE) chargingTime.visibility = View.VISIBLE

                            chargingTime.text = battery.getChargingTime(CapacityInfoService.instance?.seconds?.toDouble() ?: 0.0)
                        }

                    else -> runOnUiThread { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }
                }

                else runOnUiThread { if(chargingTime.visibility == View.VISIBLE) chargingTime.visibility = View.GONE }

                if(pref.getBoolean(Preferences.IsShowLastChargeTimeInApp.prefKey, true)) {

                    runOnUiThread {

                        if(lastChargeTime.visibility == View.GONE) lastChargeTime.visibility = View.VISIBLE

                        if(pref.getInt(Preferences.LastChargeTime.prefKey, 0) > 0)
                            lastChargeTime.text = getString(R.string.last_charge_time, battery.getLastChargeTime(),
                                "${pref.getInt(Preferences.BatteryLevelWith.prefKey, 0)}%", "${pref.getInt(Preferences.BatteryLevelTo.prefKey, 0)}%")

                        else {

                            if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE
                        }

                    }
                }

                else {

                    runOnUiThread {

                        if(lastChargeTime.visibility == View.VISIBLE) lastChargeTime.visibility = View.GONE

                    }
                }

                runOnUiThread {

                    this.status.text = battery.getStatus(status)

                    if(battery.getPlugged(plugged) != "N/A") {

                        if(this.plugged.visibility == View.GONE) this.plugged.visibility = View.VISIBLE

                        this.plugged.text = battery.getPlugged(plugged)
                    }

                    else this.plugged.visibility = View.GONE
                }

                runOnUiThread {

                    batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                    technology.text = getString(R.string.battery_technology, batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown")

                    temperatute.text = if (!pref.getBoolean(Preferences.TemperatureInFahrenheit.prefKey, false)) getString(R.string.temperature_celsius,
                        battery.getTemperature())

                    else getString(R.string.temperature_fahrenheit, battery.getTemperature())

                    voltage.text = getString(if(pref.getBoolean(Preferences.VoltageInMv.prefKey, false)) R.string.voltage_mv else R.string.voltage,
                        battery.toDecimalFormat(battery.getVoltage()))
                }

                if (pref.getBoolean(Preferences.IsSupported.prefKey, true)) {

                        if (pref.getInt(Preferences.DesignCapacity.prefKey, 0) > 0 && pref.getInt(
                                Preferences.ChargeCounter.prefKey, 0) > 0) {

                            runOnUiThread {

                                residualCapacity.text = battery.getResidualCapacity()

                                batteryWear.text = battery.getBatteryWear()
                            }
                        }

                        if (battery.getCurrentCapacity() > 0) {

                            if (currentCapacity.visibility == View.GONE) runOnUiThread { currentCapacity.visibility = View.VISIBLE }

                            runOnUiThread {

                                currentCapacity.text = getString(R.string.current_capacity, battery.toDecimalFormat(battery.getCurrentCapacity()))

                                capacityAdded.text = battery.getCapacityAdded()
                            }
                        }

                        else {

                            if (currentCapacity.visibility == View.VISIBLE) runOnUiThread { currentCapacity.visibility = View.GONE }

                            if (capacityAdded.visibility == View.VISIBLE) runOnUiThread { capacityAdded.visibility = View.GONE }
                        }

                        val intentFilter = IntentFilter()

                        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

                        batteryStatus = registerReceiver(null, intentFilter)

                        if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                            if (chargingCurrent.visibility == View.GONE) runOnUiThread { chargingCurrent.visibility = View.VISIBLE }

                            runOnUiThread {

                                chargingCurrent.text = getString(R.string.charging_current, battery.getChargingCurrent().toString())
                            }

                        } else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                            if (chargingCurrent.visibility == View.GONE) runOnUiThread { chargingCurrent.visibility = View.VISIBLE }

                            runOnUiThread {

                                chargingCurrent.text = getString(R.string.discharge_current, battery.getChargingCurrent().toString())
                            }
                        } else {

                            if (chargingCurrent.visibility == View.VISIBLE) runOnUiThread {  chargingCurrent.visibility = View.GONE }
                        }

                }

                else {

                    if (capacityAdded.visibility == View.VISIBLE) runOnUiThread { capacityAdded.visibility = View.GONE }

                    if(pref.contains(Preferences.CapacityAdded.prefKey)) pref.edit().remove(Preferences.CapacityAdded.prefKey).apply()

                    if(isShowDialog) {

                        isShowDialog = false

                        val dialog = MaterialAlertDialogBuilder(this).apply {
                            setTitle(getString(R.string.information))
                            setMessage(getString(R.string.not_supported))
                            setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                        }

                        runOnUiThread {

                            dialog.show()
                        }
                    }
                }

                if(battery.getPlugged(batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1) == "N/A") Thread.sleep(5 * 914)

                else Thread.sleep(914)
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onStop() {

        super.onStop()

        isDoAsync = false
    }

    override fun onBackPressed() {

        super.onBackPressed()

        instance = null
    }

    private fun startService() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(Intent(this, CapacityInfoService::class.java))

        else startService(Intent(this, CapacityInfoService::class.java))
    }
}