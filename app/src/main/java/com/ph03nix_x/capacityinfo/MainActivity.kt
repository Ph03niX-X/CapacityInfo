package com.ph03nix_x.capacityinfo

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import java.text.DecimalFormat

@SuppressWarnings("StaticFieldLeak", "PrivateApi")
class MainActivity : AppCompatActivity() {

    private lateinit var capacityDesign: TextView
    private lateinit var residualCapacity: TextView
    private lateinit var currentCapacity: TextView
    private lateinit var technology: TextView
    private lateinit var chargingCurrent: TextView
    private lateinit var temperatute: TextView
    private lateinit var voltage: TextView
    private lateinit var batteryWear: TextView
    private lateinit var pref: SharedPreferences
    private lateinit var relativeMain: RelativeLayout
    private lateinit var batteryManager: BatteryManager
    private lateinit var jobScheduler: JobScheduler
    private lateinit var job: JobInfo.Builder
    private var asyncTask: AsyncTask<Void, Void, Unit>? = null
    private var asyncTempVolt: AsyncTask<Void, Void, Unit>? = null
    private var batteryStatus: Intent? = null

    companion object {

        var instance: MainActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(pref.getBoolean("dark_mode", false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        relativeMain = findViewById(R.id.relative_main)
        capacityDesign = findViewById(R.id.capacity_design)
        currentCapacity = findViewById(R.id.current_capacity)
        residualCapacity = findViewById(R.id.residual_capacity)
        technology = findViewById(R.id.battery_technology)
        chargingCurrent = findViewById(R.id.charging_current)
        temperatute = findViewById(R.id.temperature)
        voltage = findViewById(R.id.voltage)
        batteryWear = findViewById(R.id.battery_wear)

        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        if(pref.getBoolean("dark_mode", false)) relativeMain.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))

        if(pref.getBoolean("is_show_instruction", true)) {

            AlertDialog.Builder(this).apply {

                setIcon(if(pref.getBoolean("dark_mode", false)) getDrawable(R.drawable.ic_info_white_24dp) else getDrawable(R.drawable.ic_info_black_24dp))
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.instruction_message))
                setPositiveButton(android.R.string.ok) { d, _ -> pref.edit().putBoolean("is_show_instruction", false).apply() }
                show()
            }
        }

            val componentName = ComponentName(this, CapacityInfoService::class.java)

            jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            job = JobInfo.Builder(1, componentName).apply {

                setMinimumLatency(30 * 1000)
                setRequiresCharging(true)
                setPersisted(false)
            }

            jobScheduler.schedule(job.build())
    }

    override fun onResume() {

        super.onResume()

        instance = this

        if(pref.getInt("design_capacity", 0) <= 0 || pref.getInt("design_capacity", 0) >= 100000) {

            pref.edit().putInt("design_capacity", getDesignCapacity()).apply()

            if(pref.getInt("design_capacity", 0) < 0) pref.edit().putInt("design_capacity", (pref.getInt("design_capacity", 0) / -1)).apply()
        }

        capacityDesign.text = getString(R.string.capacity_design, pref.getInt("design_capacity", 0).toString())

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%")

        if(pref.getBoolean("is_supported", true)) {

            asyncTask = DoAsync {

                while (true) {

                    if (pref.getInt("design_capacity", 0) > 0 && pref.getInt("charge_counter", 0) > 0) {

                        runOnUiThread {

                            residualCapacity.text = getString(R.string.residual_capacity, toDecimalFormat(getResidualCapacity()),
                                "${DecimalFormat("#.#").format(if (getResidualCapacity() >= 100000) ((getResidualCapacity() / 1000) / pref.getInt("design_capacity", 0).toDouble()) * 100 
                                
                                else (getResidualCapacity() / pref.getInt("design_capacity", 0).toDouble()) * 100)}%")

                            batteryWear.text = getString(R.string.battery_wear, getBatteryWear(pref.getInt("design_capacity", 0).toDouble(),
                                if (getResidualCapacity() >= 100000) getResidualCapacity() / 1000 else getResidualCapacity()))
                        }
                    }

                    if (getCurrentCapacity() > 0) {

                        if (currentCapacity.visibility == View.GONE) currentCapacity.visibility = View.VISIBLE

                        runOnUiThread {

                            currentCapacity.text = getString(R.string.current_capacity, toDecimalFormat(getCurrentCapacity()), "${getBatteryLevel()}%")
                        }

                    }

                    else {

                        if (currentCapacity.visibility == View.VISIBLE) currentCapacity.visibility = View.GONE
                    }

                    val intentFilter = IntentFilter()

                    intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)

                    batteryStatus = registerReceiver(null, intentFilter)

                    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                        if (chargingCurrent.visibility == View.GONE) chargingCurrent.visibility = View.VISIBLE

                        runOnUiThread {

                            chargingCurrent.text = getString(R.string.charging_current, getChargingCurrent().toString())
                        }

                    }

                    else if(status == BatteryManager.BATTERY_STATUS_DISCHARGING || status == BatteryManager.BATTERY_STATUS_FULL || status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                        if (chargingCurrent.visibility == View.GONE) chargingCurrent.visibility = View.VISIBLE

                        runOnUiThread {

                            chargingCurrent.text = getString(R.string.discharge_current, getChargingCurrent().toString())
                        }
                    }

                    else {

                        if (chargingCurrent.visibility == View.VISIBLE) chargingCurrent.visibility = View.GONE
                    }

                    Thread.sleep(5000)
                }

            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }

        else {

                AlertDialog.Builder(this).apply {

                    setIcon(if(pref.getBoolean("dark_mode", false)) getDrawable(R.drawable.ic_info_white_24dp) else getDrawable(R.drawable.ic_info_black_24dp))
                    setTitle(getString(R.string.information))
                    setMessage(getString(R.string.not_supported))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                    show()
                }
        }

        asyncTempVolt = DoAsync {

            while(true) {

                if(!pref.getBoolean("is_supported", false)) batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

                runOnUiThread {

                    technology.text = getString(R.string.battery_technology, batteryStatus!!.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY))

                    temperatute.text = if(!pref.getBoolean("fahrenheit", false)) getString(R.string.temperature_celsius, toDecimalFormat(getTemperature()))

                    else getString(R.string.temperature_fahrenheit, toDecimalFormat(toFahrenheit(getTemperature())))

                    voltage.text = getString(R.string.voltage, toDecimalFormat(getVoltage()))
                }

                Thread.sleep(5 * 1100)
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun onStop() {

        super.onStop()

        asyncTask?.cancel(true)

        asyncTempVolt?.cancel(true)
    }

    override fun onBackPressed() {

        super.onBackPressed()

        asyncTask?.cancel(true)

        asyncTempVolt?.cancel(true)

        instance = null
    }

    override fun onDestroy() {

        asyncTask?.cancel(true)

        asyncTempVolt?.cancel(true)

        instance = null

        jobScheduler.schedule(job.build())

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))

            R.id.instruction -> AlertDialog.Builder(this).apply {

                setIcon(if(pref.getBoolean("dark_mode", false)) getDrawable(R.drawable.ic_info_white_24dp) else getDrawable(R.drawable.ic_info_black_24dp))
                setTitle(getString(R.string.instruction))
                setMessage(getString(R.string.instruction_message))
                setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                show()
            }

            R.id.github -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ph03niX-X/CapacityInfo")))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getDesignCapacity(): Int {
        
        val powerProfileClass = "com.android.internal.os.PowerProfile"

        val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(this)

        var capacity = (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()

        if(capacity >= 100000) capacity /= 1000

        return capacity
    }

    private fun getCurrentCapacity(): Double {

        var currentCapacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toDouble()

        if (currentCapacity < 0) currentCapacity /= -1

        if (currentCapacity >= 100000) currentCapacity /= 1000

        return currentCapacity
    }

    private fun getBatteryLevel() = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    private fun getResidualCapacity() = pref.getInt("charge_counter", 0).toDouble()

    private fun getChargingCurrent(): Int {

        var chargingCurrent = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        if(chargingCurrent < 0) chargingCurrent /= -1

        if(chargingCurrent >= 10000) chargingCurrent /= 1000

        return chargingCurrent
    }

    private fun getTemperature(): Double {

        var temp = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0).toDouble()

        if(temp >= 100) temp /= 10

        return temp
    }

    private fun getVoltage(): Double {

        var voltage = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toDouble()

        if(voltage >= 1000 && voltage < 1000000) voltage /= 1000 else if(voltage >= 1000000) voltage /= 1000000

        return voltage
    }

    private fun toFahrenheit(celsius: Double) = (celsius * 1.8) + 32

    private fun getBatteryWear(capacityDesign: Double, capacity: Double) = "${DecimalFormat("#.#").format(100 - ((capacity / capacityDesign) * 100))}%"

    private fun toDecimalFormat(number: Double) = if(number >= 100000) DecimalFormat("#.#").format(number / 1000) else DecimalFormat("#.#").format(number)
}