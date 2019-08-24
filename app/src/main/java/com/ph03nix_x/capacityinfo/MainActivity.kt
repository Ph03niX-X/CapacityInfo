package com.ph03nix_x.capacityinfo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.DecimalFormat

const val chargeFull = "/sys/class/power_supply/battery/charge_full"
const val chargeFullDesign = "/sys/class/power_supply/battery/charge_full_design"
const val chargeCounter = "/sys/class/power_supply/battery/charge_counter"
const val batteryLevelPath = "/sys/class/power_supply/battery/capacity"
class MainActivity : AppCompatActivity() {

    private lateinit var capacityDesign: TextView
    private lateinit var residualCapacity: TextView
    private lateinit var currentCapacity: TextView
    private lateinit var batteryWear: TextView
    private lateinit var pref: SharedPreferences
    private lateinit var relativeMain: RelativeLayout
    var asyncTask: AsyncTask<Void, Void, Unit>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)

        if(pref.getBoolean("dark_mode", false)) setTheme(R.style.DarkTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        relativeMain = findViewById(R.id.relative_main)
        capacityDesign = findViewById(R.id.capacity_design)
        currentCapacity = findViewById(R.id.current_capacity)
        residualCapacity = findViewById(R.id.residual_capacity)
        batteryWear = findViewById(R.id.battery_wear)

        if(pref.getBoolean("dark_mode", false)) relativeMain.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))
    }

    override fun onResume() {

        super.onResume()

        if(File(chargeFull).exists() && getDesignCapacity() > 0) {

            asyncTask = DoAsync {

                while(true) {

                    if (File(chargeFull).exists() && getDesignCapacity() > 0) {

                        runOnUiThread {

                            capacityDesign.text = getString(R.string.capacity_design, getDesignCapacity().toString())

                            residualCapacity.text = getString(R.string.residual_capacity, getDecimalFormat(getResidualCapacity()),
                                "${DecimalFormat("#.#").format(if(getResidualCapacity() >= 100000) ((getResidualCapacity() / 1000) / getDesignCapacity().toDouble()) * 100 
                                
                                else (getResidualCapacity() / getDesignCapacity().toDouble()) * 100)}%")

                            batteryWear.text = getString(R.string.battery_wear, getBatteryWear(getDesignCapacity().toDouble(),
                                if(getResidualCapacity() >= 100000) getResidualCapacity() / 1000 else getResidualCapacity()))
                        }
                    }

                    if (File(chargeCounter).exists() && File(batteryLevelPath).exists() && getCurrentCapacity() > 0) {

                        if (currentCapacity.visibility == View.GONE) currentCapacity.visibility = View.VISIBLE

                        runOnUiThread {

                            currentCapacity.text = getString(R.string.current_capacity, getDecimalFormat(getCurrentCapacity()), "${getBatteryLevel()}%")
                        }
                    }

                    else {

                        if (currentCapacity.visibility == View.VISIBLE) currentCapacity.visibility = View.GONE
                    }

                    Thread.sleep(5000)
                }

            }.execute()
        }

        else {

            capacityDesign.text = getString(R.string.capacity_design, "0")

            residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

            batteryWear.text = getString(R.string.battery_wear, "0%")

            if (currentCapacity.visibility == View.VISIBLE) currentCapacity.visibility = View.GONE
        }

    }

    override fun onStop() {

        super.onStop()

        asyncTask?.cancel(true)
    }

    override fun onBackPressed() {

        super.onBackPressed()

        asyncTask?.cancel(true)
    }

    override fun onDestroy() {

        asyncTask?.cancel(true)

        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.findItem(R.id.dark_mode)?.isChecked = pref.getBoolean("dark_mode", false)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.dark_mode -> {

                when (item.isChecked) {

                    true -> item.isChecked = false

                    false -> item.isChecked = true
                }

                pref.edit().putBoolean("dark_mode", item.isChecked).apply()

                recreate()
            }

            R.id.github -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ph03niX-X/CapacityInfo")))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getDesignCapacity(): Int {

        var capacity: Int

        val file = File(chargeFullDesign)

            if (file.exists()) {

                val input = FileInputStream(file)
                val isr = InputStreamReader(input)
                val buff = BufferedReader(isr)

                capacity = buff.readLine().toInt()

                if(capacity >= 100000) capacity /= 1000

                input.close()
            }

            else {

                val powerProfileClass = "com.android.internal.os.PowerProfile"

                val mPowerProfile = Class.forName(powerProfileClass).getConstructor(Context::class.java).newInstance(this)

                capacity = (Class.forName(powerProfileClass).getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double).toInt()

                if(capacity >= 100000) capacity /= 1000
            }

            return capacity
    }

    private fun getCurrentCapacity(): Double {

        var capacity = 0.0

        val file = File(chargeCounter)

            if (file.exists()) {

                val input = FileInputStream(file)
                val isr = InputStreamReader(input)
                val buff = BufferedReader(isr)

                capacity = buff.readLine().toDouble()

                input.close()
            }

            return capacity
    }

    private fun getBatteryLevel(): String {

        var batteryLevel = "0"

        val file = File(batteryLevelPath)

        if (file.exists()) {

            val input = FileInputStream(file)
            val isr = InputStreamReader(input)
            val buff = BufferedReader(isr)

            batteryLevel = buff.readLine()

            input.close()
        }

        return batteryLevel
    }

    private fun getResidualCapacity(): Double {

            var capacity = 0.0

            val file = File(chargeFull)

            if (file.exists()) {

                val input = FileInputStream(file)
                val isr = InputStreamReader(input)
                val buff = BufferedReader(isr)

                capacity = buff.readLine().toDouble()

                input.close()
            }

            return capacity
    }

    private fun getBatteryWear(capacityDesign: Double, capacity: Double) = "${DecimalFormat("#.#").format(100 - ((capacity / capacityDesign) * 100))}%"

    private fun getDecimalFormat(number: Double) = if(number >= 100000) DecimalFormat("#.#").format(number / 1000) else DecimalFormat("#.#").format(number)
}