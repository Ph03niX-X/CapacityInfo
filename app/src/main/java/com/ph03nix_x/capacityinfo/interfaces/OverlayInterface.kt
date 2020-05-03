package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import java.text.DecimalFormat

interface OverlayInterface : BatteryInfoInterface {

    companion object {

        private lateinit var batteryLevelOverlay: TextView
        private lateinit var currentCapacityOverlay: TextView
        private lateinit var batteryHealthOverlay: TextView
        private lateinit var statusOverlay: TextView
        private lateinit var chargeDischargeCurrentOverlay: TextView
        private lateinit var maxChargeDischargeCurrentOverlay: TextView
        private lateinit var averageChargeDischargeCurrentOverlay: TextView
        private lateinit var minChargeDischargeCurrentOverlay: TextView
        private lateinit var temperatureOverlay: TextView
        private lateinit var voltageOverlay: TextView
        private lateinit var layoutParams: ViewGroup.LayoutParams
        private lateinit var pref: SharedPreferences
        lateinit var windowManager: WindowManager
        lateinit var linearLayout: LinearLayout
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onCreateOverlay(context: Context) {

        pref = PreferenceManager.getDefaultSharedPreferences(context)

        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

        onCreateLinearLayout(context)

        val parameters = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)

        parameters.gravity = Gravity.TOP
        parameters.x = 0
        parameters.y = 0

        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        onCreateViews(context)

        onLinearLayoutAddView()

        windowManager.addView(linearLayout, parameters)

        linearLayout.setOnTouchListener(onLinearLayoutOnTouchListener(parameters))
    }

    private fun onCreateLinearLayout(context: Context) {

        linearLayout = LinearLayout(context)

        val layoutParameters = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        linearLayout.orientation = LinearLayout.VERTICAL

        linearLayout.setBackgroundColor(Color.argb(100,0,0,0))

        linearLayout.layoutParams = layoutParameters
    }

    private fun onCreateViews(context: Context) {

        onCreateBatteryLevelOverlay(context)

        if(pref.getBoolean(IS_SUPPORTED, true)) onCreateCurrentCapacityOverlay(context)

        onCreateBatteryHealthOverlay(context)
        onCreateStatusOverlay(context)
        onCreateChargeDischargeCurrentOverlay(context)
        onCreateMaxChargeDischargeCurrentOverlay(context)
        onCreateAverageChargeDischargeCurrentOverlay(context)
        onCreateMinChargeDischargeCurrentOverlay(context)
        onCreateTemperatureOverlay(context)
        onCreateVoltageOverlay(context)
    }

    private fun onCreateBatteryLevelOverlay(context: Context) {

        batteryLevelOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(R.string.battery_level, "${getBatteryLevel(context)}%")

            visibility = if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateCurrentCapacityOverlay(context: Context) {

        currentCapacityOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))

            visibility = if(pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false)
                && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateBatteryHealthOverlay(context: Context) {

        batteryHealthOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(R.string.battery_health, getBatteryHealth(context))

            visibility = if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateStatusOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        statusOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(R.string.status, getStatus(context, status))

            visibility = if(pref.getBoolean(IS_STATUS_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateChargeDischargeCurrentOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        chargeDischargeCurrentOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge_current
                else R.string.discharge_current, getChargeDischargeCurrent(context).toString())

            visibility = if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateMaxChargeDischargeCurrentOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        maxChargeDischargeCurrentOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString( R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)
            else context.getString(R.string.max_discharge_current, BatteryInfoInterface.maxDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateAverageChargeDischargeCurrentOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        averageChargeDischargeCurrentOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)
            else context.getString(R.string.average_discharge_current, BatteryInfoInterface.averageDischargeCurrent)

            visibility = if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateMinChargeDischargeCurrentOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        minChargeDischargeCurrentOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString( R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
            else context.getString(R.string.min_discharge_current, BatteryInfoInterface.minDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateTemperatureOverlay(context: Context) {

        temperatureOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
            else R.string.temperature_celsius, getTemperature(context))

            visibility = if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onCreateVoltageOverlay(context: Context) {

        voltageOverlay = TextView(context).apply {

            typeface = ResourcesCompat.getFont(context, R.font.medium)

            onSetTextSize(this)

            setTextColor(Color.WHITE)

            text = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv
            else R.string.voltage, DecimalFormat("#.#").format(getVoltage(context)))

            visibility = if(pref.getBoolean(IS_VOLTAGE_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    fun onUpdateOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        batteryLevelOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.battery_level, "${getBatteryLevel(context)}%")

            visibility = if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }

        if(pref.getBoolean(IS_SUPPORTED, true) || currentCapacityOverlay.visibility ==
            View.VISIBLE)
            currentCapacityOverlay.apply {

                onSetTextSize(this)

                text = context.getString(R.string.current_capacity, DecimalFormat("#.#").format(getCurrentCapacity(context)))

                visibility = if(pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false)
                    && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
            }

        batteryHealthOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.battery_health, getBatteryHealth(context))

            visibility = if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        statusOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.status, getStatus(context, status))

            visibility = if(pref.getBoolean(IS_STATUS_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        chargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge_current
            else R.string.discharge_current, getChargeDischargeCurrent(context).toString())

            visibility = if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        maxChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString( R.string.max_charge_current, BatteryInfoInterface.maxChargeCurrent)
            else context.getString(R.string.max_discharge_current, BatteryInfoInterface.maxDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        averageChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString( R.string.average_charge_current, BatteryInfoInterface.averageChargeCurrent)
            else context.getString(R.string.average_discharge_current, BatteryInfoInterface.averageDischargeCurrent)

            visibility = if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        minChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString( R.string.min_charge_current, BatteryInfoInterface.minChargeCurrent)
            else context.getString(R.string.min_discharge_current, BatteryInfoInterface.minDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        temperatureOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false)) R.string.temperature_fahrenheit
            else R.string.temperature_celsius, getTemperature(context))

            visibility = if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, false)) View.VISIBLE else View.GONE
        }

        voltageOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false)) R.string.voltage_mv
            else R.string.voltage, DecimalFormat("#.#").format(getVoltage(context)))

            visibility = if(pref.getBoolean(IS_VOLTAGE_OVERLAY, false)) View.VISIBLE else View.GONE
        }
    }

    private fun onSetTextSize(textView: TextView) {

        when(pref.getString(OVERLAY_SIZE, "1")) {

            "0" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)

            "1" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

            "2" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
    }

    private fun onLinearLayoutAddView() {

        linearLayout.apply {

            addView(batteryLevelOverlay)
            addView(currentCapacityOverlay)
            addView(batteryHealthOverlay)
            addView(statusOverlay)
            addView(chargeDischargeCurrentOverlay)
            addView(maxChargeDischargeCurrentOverlay)
            addView(averageChargeDischargeCurrentOverlay)
            addView(minChargeDischargeCurrentOverlay)
            addView(temperatureOverlay)
            addView(voltageOverlay)
        }
    }

    private fun onLinearLayoutOnTouchListener(parameters: WindowManager.LayoutParams) =

        object : View.OnTouchListener {

            var updatedParameters = parameters
            var x = 0.0
            var y = 0.0
            var pressedX = 0.0
            var pressedY = 0.0

            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View, event: MotionEvent): Boolean {

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = updatedParameters.x.toDouble()
                        y = updatedParameters.y.toDouble()
                        pressedX = event.rawX.toDouble()
                        pressedY = event.rawY.toDouble()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        updatedParameters.x = (x + (event.rawX - pressedX)).toInt()
                        updatedParameters.y = (y + (event.rawY - pressedY)).toInt()
                        windowManager.updateViewLayout(linearLayout, updatedParameters)
                    }
                }

                return false
            }
        }

    fun stopOverlayService(context: Context) {

        context.stopService(Intent(context, OverlayService::class.java))
    }
}