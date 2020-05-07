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
import android.provider.Settings
import android.util.TypedValue
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_BATTERY_WEAR_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CAPACITY_ADDED_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CHARGING_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_LAST_CHARGE_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NUMBER_OF_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_NUMBER_OF_CYCLES_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SOURCE_OF_POWER
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_RESIDUAL_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import java.text.DecimalFormat

interface OverlayInterface : BatteryInfoInterface, ServiceInterface {

    companion object {

        private lateinit var view: View
        private lateinit var batteryLevelOverlay: AppCompatTextView
        private lateinit var numberOfChargesOverlay: AppCompatTextView
        private lateinit var numberOfCyclesOverlay: AppCompatTextView
        private lateinit var chargingTimeOverlay: AppCompatTextView
        private lateinit var currentCapacityOverlay: AppCompatTextView
        private lateinit var capacityAddedOverlay: AppCompatTextView
        private lateinit var batteryHealthOverlay: AppCompatTextView
        private lateinit var residualCapacityOverlay: AppCompatTextView
        private lateinit var statusOverlay: AppCompatTextView
        private lateinit var sourceOfPowerOverlay: AppCompatTextView
        private lateinit var chargeDischargeCurrentOverlay: AppCompatTextView
        private lateinit var maxChargeDischargeCurrentOverlay: AppCompatTextView
        private lateinit var averageChargeDischargeCurrentOverlay: AppCompatTextView
        private lateinit var minChargeDischargeCurrentOverlay: AppCompatTextView
        private lateinit var temperatureOverlay: AppCompatTextView
        private lateinit var voltageOverlay: AppCompatTextView
        private lateinit var lastChargeTimeOverlay: AppCompatTextView
        private lateinit var batteryWearOverlay: AppCompatTextView
        private lateinit var layoutParams: ViewGroup.LayoutParams
        private lateinit var pref: SharedPreferences
        lateinit var windowManager: WindowManager
        lateinit var linearLayout: LinearLayoutCompat

        fun isEnabledOverlay(context: Context, isEnabledOverlay: Boolean = false): Boolean {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            with(pref) {

                val overlayArray = arrayListOf(getBoolean(IS_BATTERY_LEVEL_OVERLAY,
                    false), getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, false),
                    getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, false),
                    getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, false),
                    getBoolean(IS_CHARGING_TIME_OVERLAY, false),
                    getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false),
                    getBoolean(IS_CAPACITY_ADDED_OVERLAY, false),
                    getBoolean(IS_BATTERY_HEALTH_OVERLAY, false),
                    getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY, false),
                    getBoolean(IS_STATUS_OVERLAY, false),
                    getBoolean(IS_SOURCE_OF_POWER, false),
                    getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false),
                    getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false),
                    getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, false),
                    getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false),
                    getBoolean(IS_TEMPERATURE_OVERLAY, false),
                    getBoolean(IS_VOLTAGE_OVERLAY, false),
                    getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, false),
                    getBoolean(IS_BATTERY_WEAR_OVERLAY, false))

                overlayArray.forEach {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if(Settings.canDrawOverlays(context)
                            && (getBoolean(IS_ENABLED_OVERLAY, false)
                                    || isEnabledOverlay) && it) return true
                    }

                    else {

                        if((getBoolean(IS_ENABLED_OVERLAY, false) || isEnabledOverlay)
                            && it) return true
                    }
                }
            }

            return false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun onCreateOverlay(context: Context) {

        if(isEnabledOverlay(context)) {

            pref = PreferenceManager.getDefaultSharedPreferences(context)

            windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager

            val parameters = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, if(Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)

            parameters.gravity = Gravity.TOP
            parameters.x = 0
            parameters.y = 0

            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            onCreateViews(context)

            windowManager.addView(linearLayout, parameters)

            linearLayout.setOnTouchListener(onLinearLayoutOnTouchListener(parameters))
        }

        else if(OverlayService.instance != null) onStopService(context, OverlayService::class.java)
    }

    private fun onCreateViews(context: Context) {

        view = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)

        linearLayout = view.findViewById(R.id.overlay_linear_layout)

        batteryLevelOverlay = view.findViewById(R.id.battery_level_overlay)
        numberOfChargesOverlay = view.findViewById(R.id.number_of_charges_overlay)
        numberOfCyclesOverlay = view.findViewById(R.id.number_of_cycles_overlay)
        chargingTimeOverlay = view.findViewById(R.id.charging_time_overlay)
        currentCapacityOverlay = view.findViewById(R.id.current_capacity_overlay)
        capacityAddedOverlay = view.findViewById(R.id.capacity_added_overlay)
        batteryHealthOverlay = view.findViewById(R.id.battery_health_overlay)
        residualCapacityOverlay = view.findViewById(R.id.residual_capacity_overlay)
        statusOverlay = view.findViewById(R.id.status_overlay)
        sourceOfPowerOverlay = view.findViewById(R.id.source_of_power_overlay)
        chargeDischargeCurrentOverlay = view.findViewById(R.id.charge_discharge_current_overlay)
        maxChargeDischargeCurrentOverlay = view.findViewById(R.id
            .max_charge_discharge_current_overlay)
        averageChargeDischargeCurrentOverlay = view.findViewById(R.id
            .average_charge_discharge_current_overlay)
        minChargeDischargeCurrentOverlay = view.findViewById(R.id
            .min_charge_discharge_current_overlay)
        temperatureOverlay = view.findViewById(R.id.temperature_overlay)
        voltageOverlay = view.findViewById(R.id.voltage_overlay)
        lastChargeTimeOverlay = view.findViewById(R.id.last_charge_time_overlay)
        batteryWearOverlay = view.findViewById(R.id.battery_wear_overlay)

        onUpdateOverlay(context)
    }

    fun onUpdateOverlay(context: Context) {

        batteryIntent = context.registerReceiver(null, IntentFilter(Intent
            .ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val extraPlugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED,
            -1) ?: -1

        val sourceOfPower = getSourceOfPower(context, extraPlugged)

        linearLayout.setBackgroundColor(onSetBackgroundLinearLayout())

        onUpdateBatteryLevelOverlay()
        onUpdateNumberOfChargesOverlay()
        onUpdateNumberOfCyclesOverlay()
        onUpdateChargingTimeOverlay()
        onUpdateCurrentCapacityOverlay()
        onUpdateCapacityAddedOverlay()
        onUpdateBatteryHealthOverlay()
        onUpdateResidualCapacityOverlay(status)
        onUpdateStatusOverlay(status)
        onUpdateSourceOfPowerOverlay(sourceOfPower)
        onUpdateChargeDischargeCurrentOverlay(status)
        onUpdateMaxChargeDischargeCurrentOverlay(status)
        onUpdateAverageChargeDischargeCurrentOverlay(status)
        onUpdateMinChargeDischargeCurrentOverlay(status)
        onUpdateTemperatureOverlay()
        onUpdateVoltageOverlay()
        onUpdateLastChargeTimeOverlay()
        onUpdateBatteryWearOverlay()
    }

    private fun onSetBackgroundLinearLayout() =
        Color.argb(if(pref.getInt(OVERLAY_OPACITY, 127) > 255
            || pref.getInt(OVERLAY_OPACITY, 127) < 0) 127
        else pref.getInt(OVERLAY_OPACITY, 127), 0, 0, 0)

    private fun onUpdateBatteryLevelOverlay() {

        if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, false)
            || batteryLevelOverlay.visibility == View.VISIBLE)
        batteryLevelOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.battery_level,
                "${getBatteryLevel(context)}%")

            visibility = if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateNumberOfChargesOverlay() {

        if(pref.getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, false)
            || numberOfChargesOverlay.visibility == View.VISIBLE)
        numberOfChargesOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.number_of_charges,
                pref.getLong(NUMBER_OF_CHARGES, 0))

            visibility = if(pref.getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateNumberOfCyclesOverlay() {

        if(pref.getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, false)
            || numberOfCyclesOverlay.visibility == View.VISIBLE)
        numberOfCyclesOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.number_of_cycles,
                DecimalFormat("#.##").format(pref.getFloat(NUMBER_OF_CYCLES, 0f)))

            visibility = if(pref.getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateChargingTimeOverlay() {

        if((pref.getBoolean(IS_CHARGING_TIME_OVERLAY, false)
                    && (CapacityInfoService.instance?.seconds ?: 0) > 0)
            || chargingTimeOverlay.visibility == View.VISIBLE)
        chargingTimeOverlay.apply {

            onSetTextSize(this)

            text = getChargingTime(this.context, (CapacityInfoService.instance?.seconds ?: 0))

            visibility = if(pref.getBoolean(IS_CHARGING_TIME_OVERLAY, false)
                && (CapacityInfoService.instance?.seconds ?: 0) > 0)
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateCurrentCapacityOverlay() {

        if((pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false)
            && pref.getBoolean(IS_SUPPORTED, true))
            || currentCapacityOverlay.visibility == View.VISIBLE)
            currentCapacityOverlay.apply {

                onSetTextSize(this)

                text = context.getString(R.string.current_capacity, DecimalFormat("#.#")
                    .format(getCurrentCapacity(context)))

                visibility = if(pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, false)
                    && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateCapacityAddedOverlay() {

        if((pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, false)
                    && pref.getBoolean(IS_SUPPORTED, true))
            || capacityAddedOverlay.visibility == View.VISIBLE)
        capacityAddedOverlay.apply {

            onSetTextSize(this)

            text = getCapacityAdded(this.context)

            visibility = if(pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, false)
                && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateBatteryHealthOverlay() {

        if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, false)
            || batteryHealthOverlay.visibility == View.VISIBLE)
        batteryHealthOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.battery_health, getBatteryHealth(context))

            visibility = if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateResidualCapacityOverlay(status: Int) {

        if((pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY, false)
                    && pref.getBoolean(IS_SUPPORTED, true))
            || residualCapacityOverlay.visibility == View.VISIBLE)
        residualCapacityOverlay.apply {

            onSetTextSize(this)

            text = getResidualCapacity(context,
                status == BatteryManager.BATTERY_STATUS_CHARGING)

            visibility = if(pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY, false)
                && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateStatusOverlay(status: Int) {

        if(pref.getBoolean(IS_STATUS_OVERLAY, false)
            || statusOverlay.visibility == View.VISIBLE)
        statusOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.status, getStatus(context, status))

            visibility = if(pref.getBoolean(IS_STATUS_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateSourceOfPowerOverlay(sourceOfPower: String) {

        if((pref.getBoolean(IS_SOURCE_OF_POWER, false)
            && sourceOfPower != "N/A") || sourceOfPowerOverlay.visibility == View.VISIBLE)
        sourceOfPowerOverlay.apply {

            onSetTextSize(this)

            text = sourceOfPower

            visibility = if(pref.getBoolean(IS_SOURCE_OF_POWER, false)
                && sourceOfPower != "N/A") View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)
            || chargeDischargeCurrentOverlay.visibility == View.VISIBLE)
        chargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING)
                R.string.charge_current else R.string.discharge_current,
                getChargeDischargeCurrent(context).toString())

            visibility = if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateMaxChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)
            || maxChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
        maxChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                .max_charge_current, BatteryInfoInterface.maxChargeCurrent) else context.getString(
                R.string.max_discharge_current, BatteryInfoInterface.maxDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateAverageChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)
            || averageChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
        averageChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                .average_charge_current, BatteryInfoInterface.averageChargeCurrent)
            else context.getString(R.string.average_discharge_current,
                BatteryInfoInterface.averageDischargeCurrent)

            visibility = if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY,
                    false)) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateMinChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false)
            || minChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
        minChargeDischargeCurrentOverlay.apply {

            onSetTextSize(this)

            text = if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                .min_charge_current, BatteryInfoInterface.minChargeCurrent) else context.getString(
                R.string.min_discharge_current, BatteryInfoInterface.minDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateTemperatureOverlay() {

        if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, false)
            || temperatureOverlay.visibility == View.VISIBLE)
        temperatureOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT, false))
                R.string.temperature_fahrenheit else R.string.temperature_celsius,
                getTemperature(context))

            visibility = if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateVoltageOverlay() {

        if(pref.getBoolean(IS_VOLTAGE_OVERLAY, false)
            || voltageOverlay.visibility == View.VISIBLE)
        voltageOverlay.apply {

            onSetTextSize(this)

            text = context.getString(if(pref.getBoolean(VOLTAGE_IN_MV, false))
                R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
                getVoltage(context)))

            visibility = if(pref.getBoolean(IS_VOLTAGE_OVERLAY, false)) View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateLastChargeTimeOverlay() {

        if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, false)
            || lastChargeTimeOverlay.visibility == View.VISIBLE)
        lastChargeTimeOverlay.apply {

            onSetTextSize(this)

            text = context.getString(R.string.last_charge_time, getLastChargeTime(context),
                "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%",
                "${pref.getInt(BATTERY_LEVEL_TO, 0)}%")

            visibility = if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, false))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateBatteryWearOverlay() {

        if((pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, false)
                    && pref.getBoolean(IS_SUPPORTED, true))
            || batteryWearOverlay.visibility == View.VISIBLE)
        batteryWearOverlay.apply {

            onSetTextSize(this)

            text = getBatteryWear(context)

            visibility = if(pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, false)
                && pref.getBoolean(IS_SUPPORTED, true)) View.VISIBLE else View.GONE
        }
    }

    private fun onSetTextSize(textView: AppCompatTextView) {

        when(pref.getString(OVERLAY_SIZE, "1")) {

            "0" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)

            "1" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)

            "2" -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
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
}