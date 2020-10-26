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
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_LEVEL_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_WEAR_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_ADDED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CURRENT_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_LAST_CHARGE_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CYCLES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SOURCE_OF_POWER
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESIDUAL_CAPACITY_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_STATUS_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SUPPORTED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEMPERATURE_IN_FAHRENHEIT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.VOLTAGE_IN_MV
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.helpers.TimeHelper
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_CURRENT_LIMIT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_REMAINING_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ONLY_VALUES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_REMAINING_BATTERY_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SCREEN_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_COLOR
import java.text.DecimalFormat

interface OverlayInterface : BatteryInfoInterface {

    companion object {

        private lateinit var view: View
        private lateinit var batteryLevelOverlay: AppCompatTextView
        private lateinit var numberOfChargesOverlay: AppCompatTextView
        private lateinit var numberOfCyclesOverlay: AppCompatTextView
        private lateinit var chargingTimeOverlay: AppCompatTextView
        private lateinit var chargingTimeRemainingOverlay: AppCompatTextView
        private lateinit var remainingBatteryTimeOverlay: AppCompatTextView
        private lateinit var screenTimeOverlay: AppCompatTextView
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
        private lateinit var chargingCurrentLimitOverlay: AppCompatTextView
        private lateinit var temperatureOverlay: AppCompatTextView
        private lateinit var voltageOverlay: AppCompatTextView
        private lateinit var lastChargeTimeOverlay: AppCompatTextView
        private lateinit var batteryWearOverlay: AppCompatTextView
        private lateinit var layoutParams: ViewGroup.LayoutParams
        private lateinit var pref: SharedPreferences
        var linearLayout: LinearLayoutCompat? = null
        var windowManager: WindowManager? = null

        fun isEnabledOverlay(context: Context, isEnabledOverlay: Boolean = false): Boolean {

            val pref = PreferenceManager.getDefaultSharedPreferences(context)

            with(pref) {

                val overlayArray = arrayListOf(getBoolean(IS_BATTERY_LEVEL_OVERLAY,
                    context.resources.getBoolean(R.bool.is_battery_level_overlay)),
                    getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, context.resources.getBoolean(
                        R.bool.is_number_of_charges_overlay)), getBoolean(
                        IS_NUMBER_OF_CYCLES_OVERLAY, context.resources.getBoolean(
                            R.bool.is_number_of_cycles_overlay)), getBoolean(
                        IS_CHARGING_TIME_OVERLAY, context.resources.getBoolean(
                            R.bool.is_charging_time_overlay)), getBoolean(
                        IS_CHARGING_TIME_REMAINING_OVERLAY, context.resources.getBoolean(
                            R.bool.is_charging_time_remaining_overlay)), getBoolean(
                        IS_REMAINING_BATTERY_TIME_OVERLAY, context.resources.getBoolean(
                            R.bool.is_remaining_battery_time_overlay)), getBoolean(
                        IS_SCREEN_TIME_OVERLAY, context.resources.getBoolean(
                            R.bool.is_screen_time_overlay)), getBoolean(
                        IS_CURRENT_CAPACITY_OVERLAY, context.resources.getBoolean(
                            R.bool.is_current_capacity_overlay)), getBoolean(
                        IS_CAPACITY_ADDED_OVERLAY, context.resources.getBoolean(
                            R.bool.is_capacity_added_overlay)), getBoolean(
                        IS_BATTERY_HEALTH_OVERLAY, context.resources.getBoolean(
                            R.bool.is_battery_health_overlay)), getBoolean(
                        IS_RESIDUAL_CAPACITY_OVERLAY, context.resources.getBoolean(
                            R.bool.is_residual_capacity_overlay)), getBoolean(IS_STATUS_OVERLAY,
                        context.resources.getBoolean(R.bool.is_status_overlay)), getBoolean(
                        IS_SOURCE_OF_POWER, context.resources.getBoolean(
                            R.bool.is_source_of_power_overlay)), getBoolean(
                        IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_charge_discharge_current_overlay)), getBoolean(
                        IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_max_charge_discharge_current_overlay)), getBoolean(
                        IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_average_charge_discharge_current_overlay)), getBoolean(
                        IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_min_charge_discharge_current_overlay)),
                    getBoolean(IS_CHARGING_CURRENT_LIMIT_OVERLAY, context.resources.getBoolean(
                        R.bool.is_charging_current_limit_overlay)), getBoolean(
                        IS_TEMPERATURE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_temperature_overlay)), getBoolean(IS_VOLTAGE_OVERLAY,
                        context.resources.getBoolean(R.bool.is_voltage_overlay)), getBoolean(
                        IS_LAST_CHARGE_TIME_OVERLAY, context.resources.getBoolean(
                            R.bool.is_last_charge_time_overlay)), getBoolean(
                        IS_BATTERY_WEAR_OVERLAY, context.resources.getBoolean(
                            R.bool.is_battery_wear_overlay)))

                overlayArray.forEach {

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if(Settings.canDrawOverlays(context)
                            && (getBoolean(IS_ENABLED_OVERLAY, context.resources.getBoolean(
                                R.bool.is_enabled_overlay)) || isEnabledOverlay) && it) return true
                    }

                    else {

                        if((getBoolean(IS_ENABLED_OVERLAY, context.resources.getBoolean(
                                R.bool.is_enabled_overlay)) || isEnabledOverlay) && it) return true
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

            windowManager = context.getSystemService(WINDOW_SERVICE) as? WindowManager

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

            if(linearLayout != null && linearLayout?.windowToken == null) windowManager?.addView(
                linearLayout, parameters)

            else if(OverlayService.instance != null) {

                Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG).show()

                ServiceHelper.stopService(context, OverlayService::class.java)

                return
            }

            linearLayout?.setOnTouchListener(onLinearLayoutOnTouchListener(parameters))
        }

        else if(OverlayService.instance != null)
            ServiceHelper.stopService(context, OverlayService::class.java)
    }

    private fun onCreateViews(context: Context) {

        view = LayoutInflater.from(context).inflate(R.layout.overlay_layout, null)

        linearLayout = view.findViewById(R.id.overlay_linear_layout)

        batteryLevelOverlay = view.findViewById(R.id.battery_level_overlay)
        numberOfChargesOverlay = view.findViewById(R.id.number_of_charges_overlay)
        numberOfCyclesOverlay = view.findViewById(R.id.number_of_cycles_overlay)
        chargingTimeOverlay = view.findViewById(R.id.charging_time_overlay)
        chargingTimeRemainingOverlay = view.findViewById(R.id.charging_time_remaining_overlay)
        remainingBatteryTimeOverlay = view.findViewById(R.id.remaining_battery_time_overlay)
        screenTimeOverlay = view.findViewById(R.id.screen_time_overlay)
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
        chargingCurrentLimitOverlay = view.findViewById(R.id.charging_current_limit_overlay)
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

        val sourceOfPower = getOnSourceOfPower(context, extraPlugged, true)

        linearLayout?.setBackgroundColor(onSetBackgroundLinearLayout())

        onUpdateBatteryLevelOverlay()
        onUpdateNumberOfChargesOverlay()
        onUpdateNumberOfCyclesOverlay()
        onUpdateChargingTimeOverlay()
        onUpdateChargingTimeRemainingOverlay(status)
        onUpdateRemainingBatteryTimeOverlay(status)
        onUpdateScreenTimeOverlay()
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
        onUpdateChargingCurrentLimitOverlay()
        onUpdateTemperatureOverlay()
        onUpdateVoltageOverlay()
        onUpdateLastChargeTimeOverlay()
        onUpdateBatteryWearOverlay()
    }

    private fun onSetBackgroundLinearLayout() =
        Color.argb(if(pref.getInt(OVERLAY_OPACITY,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127 else 255) > 255
            || pref.getInt(OVERLAY_OPACITY,
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127 else 255) < 0) 127
        else pref.getInt(OVERLAY_OPACITY,
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 127
            else 255), 0, 0, 0)

    private fun onUpdateBatteryLevelOverlay() {

        if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, batteryLevelOverlay.resources.getBoolean(
                R.bool.is_battery_level_overlay))
            || batteryLevelOverlay.visibility == View.VISIBLE)
            batteryLevelOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.battery_level
            else R.string.battery_level_overlay_only_values,
                "${getOnBatteryLevel(context)}%")

            visibility = if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, context.resources
                    .getBoolean(R.bool.is_battery_level_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateNumberOfChargesOverlay() {

        if(pref.getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, numberOfChargesOverlay.context.resources
                .getBoolean(R.bool.is_number_of_charges_overlay)) ||
            numberOfChargesOverlay.visibility == View.VISIBLE)
            numberOfChargesOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.number_of_charges
            else R.string.number_of_charges_overlay_only_values, pref.getLong(NUMBER_OF_CHARGES,
                0))

            visibility = if(pref.getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_number_of_charges_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateNumberOfCyclesOverlay() {

        if(pref.getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, numberOfCyclesOverlay.context.resources
                .getBoolean(R.bool.is_number_of_cycles_overlay)) ||
            numberOfCyclesOverlay.visibility == View.VISIBLE)
            numberOfCyclesOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.number_of_cycles
            else R.string.number_of_cycles_overlay_only_values, DecimalFormat("#.##").format(
                pref.getFloat(NUMBER_OF_CYCLES, 0f)))

            visibility = if(pref.getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_number_of_cycles_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateChargingTimeOverlay() {

        if((pref.getBoolean(IS_CHARGING_TIME_OVERLAY, chargingTimeOverlay.context.resources
                .getBoolean(R.bool.is_charging_time_overlay))
                    && (CapacityInfoService.instance?.seconds ?: 0) > 1) ||
            chargingTimeOverlay.visibility == View.VISIBLE)
            chargingTimeOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = getOnChargingTime(context, (CapacityInfoService.instance?.seconds ?: 0), true)

            visibility = if(pref.getBoolean(IS_CHARGING_TIME_OVERLAY, context.resources
                    .getBoolean(R.bool.is_charging_time_overlay)) && (
                        CapacityInfoService.instance?.seconds ?: 0) > 1) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateChargingTimeRemainingOverlay(status: Int) {

        if((pref.getBoolean(IS_CHARGING_TIME_REMAINING_OVERLAY, chargingTimeRemainingOverlay
                .context.resources.getBoolean(R.bool.is_charging_time_remaining_overlay))
                    && status == BatteryManager.BATTERY_STATUS_CHARGING) ||
            chargingTimeRemainingOverlay.visibility == View.VISIBLE)
            chargingTimeRemainingOverlay.apply {

                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY,
                        context.resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.charging_time_remaining
                else R.string.charging_time_remaining_overlay_only_values,
                    getOnChargingTimeRemaining(context))

                visibility = if(pref.getBoolean(IS_CHARGING_TIME_REMAINING_OVERLAY, context
                        .resources.getBoolean(R.bool.is_charging_time_remaining_overlay)) &&
                    status == BatteryManager.BATTERY_STATUS_CHARGING) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateRemainingBatteryTimeOverlay(status: Int) {

        if((pref.getBoolean(IS_REMAINING_BATTERY_TIME_OVERLAY, remainingBatteryTimeOverlay.context
                .resources.getBoolean(R.bool.is_remaining_battery_time_overlay))
                    && status != BatteryManager.BATTERY_STATUS_CHARGING) ||
            remainingBatteryTimeOverlay.visibility == View.VISIBLE)
            remainingBatteryTimeOverlay.apply {

                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) R.string.remaining_battery_time
                else R.string.remaining_battery_time_overlay_only_values, getOnRemainingBatteryTime(
                    context))

                visibility = if(pref.getBoolean(IS_REMAINING_BATTERY_TIME_OVERLAY, context
                        .resources.getBoolean(R.bool.is_remaining_battery_time_overlay)) &&
                    status != BatteryManager.BATTERY_STATUS_CHARGING) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateScreenTimeOverlay() {

        if(pref.getBoolean(IS_SCREEN_TIME_OVERLAY, screenTimeOverlay.context.resources
                .getBoolean(R.bool.is_screen_time_overlay)) || screenTimeOverlay.visibility ==
            View.VISIBLE)

            screenTimeOverlay.apply {

                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY,
                        context.resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.screen_time else R.string.screen_time_overlay_only_values,
                    TimeHelper.getTime(CapacityInfoService.instance?.screenTime ?: 0L))

                visibility = if(pref.getBoolean(IS_SCREEN_TIME_OVERLAY, context.resources
                        .getBoolean(R.bool.is_screen_time_overlay))) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateCurrentCapacityOverlay() {

        if((pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, currentCapacityOverlay.context.resources
                .getBoolean(R.bool.is_current_capacity_overlay))
                    && pref.getBoolean(IS_SUPPORTED, currentCapacityOverlay.context.resources
                .getBoolean(R.bool.is_supported))) || currentCapacityOverlay.visibility ==
            View.VISIBLE)
            currentCapacityOverlay.apply {

                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context
                        .resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.current_capacity else R.string.current_capacity_overlay_only_values,
                    DecimalFormat("#.#").format(getOnCurrentCapacity(context)))

                visibility = if(pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY,
                        context.resources.getBoolean(R.bool.is_current_capacity_overlay))
                    && pref.getBoolean(IS_SUPPORTED, context.resources.getBoolean(
                        R.bool.is_supported))) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateCapacityAddedOverlay() {

        if((pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, capacityAddedOverlay.context.resources
                .getBoolean(R.bool.is_capacity_added_overlay))
                    && pref.getBoolean(IS_SUPPORTED, capacityAddedOverlay.context.resources
                .getBoolean(R.bool.is_supported))) || capacityAddedOverlay.visibility ==
            View.VISIBLE)
            capacityAddedOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = getOnCapacityAdded(context, true)

            visibility = if(pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, context.resources
                    .getBoolean(R.bool.is_capacity_added_overlay)) && pref.getBoolean(IS_SUPPORTED,
                    context.resources.getBoolean(R.bool.is_supported))) View.VISIBLE else
                View.GONE
        }
    }

    private fun onUpdateBatteryHealthOverlay() {

        if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, batteryHealthOverlay.context.resources
                .getBoolean(R.bool.is_battery_health_overlay)) ||
            batteryHealthOverlay.visibility == View.VISIBLE)
            batteryHealthOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.battery_health
            else R.string.battery_health_overlay_only_values, getOnBatteryHealth(context))

            visibility = if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, context.resources
                    .getBoolean(R.bool.is_battery_health_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateResidualCapacityOverlay(status: Int) {

        if((pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY, residualCapacityOverlay.context.resources
                .getBoolean(R.bool.is_residual_capacity_overlay)) && pref.getBoolean(
                IS_SUPPORTED, residualCapacityOverlay.context.resources.getBoolean(
                    R.bool.is_supported))) || residualCapacityOverlay.visibility == View.VISIBLE)
            residualCapacityOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = getOnResidualCapacity(context,true)

            visibility = if(pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY,
                    context.resources.getBoolean(R.bool.is_residual_capacity_overlay)) &&
                pref.getBoolean(IS_SUPPORTED, context.resources.getBoolean(
                    R.bool.is_supported))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateStatusOverlay(status: Int) {

        if(pref.getBoolean(IS_STATUS_OVERLAY, statusOverlay.context.resources.getBoolean(
                R.bool.is_status_overlay)) || statusOverlay.visibility == View.VISIBLE)
            statusOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.status
            else R.string.status_overlay_only_values, getOnStatus(context, status))

            visibility = if(pref.getBoolean(IS_STATUS_OVERLAY, context.resources.getBoolean(
                    R.bool.is_status_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateSourceOfPowerOverlay(sourceOfPower: String) {

        if((pref.getBoolean(IS_SOURCE_OF_POWER, sourceOfPowerOverlay.context.resources.getBoolean(
                R.bool.is_source_of_power_overlay)) && sourceOfPower != "N/A")
            || sourceOfPowerOverlay.visibility == View.VISIBLE)
            sourceOfPowerOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = sourceOfPower

            visibility = if(pref.getBoolean(IS_SOURCE_OF_POWER, context.resources.getBoolean(
                    R.bool.is_source_of_power_overlay)) && sourceOfPower != "N/A") View.VISIBLE
            else View.GONE
        }
    }

    private fun onUpdateChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, chargeDischargeCurrentOverlay
                .context.resources.getBoolean(R.bool.is_charge_discharge_current_overlay)) ||
            chargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            chargeDischargeCurrentOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay)))
                    context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING)
                        R.string.charge_current else R.string.discharge_current,
                        getOnChargeDischargeCurrent(context).toString())
                else context.getString(R.string.charge_discharge_overlay_only_values,
                    getOnChargeDischargeCurrent(context))

            visibility = if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, context
                    .resources.getBoolean(R.bool.is_charge_discharge_current_overlay)))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateMaxChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, maxChargeDischargeCurrentOverlay
                .context.resources.getBoolean(R.bool.is_max_charge_discharge_current_overlay))
            || maxChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            maxChargeDischargeCurrentOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                    BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                    .max_charge_current, BatteryInfoInterface.maxChargeCurrent) else context
                    .getString(R.string.max_discharge_current, BatteryInfoInterface
                        .maxDischargeCurrent) else if(status == BatteryManager
                        .BATTERY_STATUS_CHARGING) context.getString(
                    R.string.max_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .maxChargeCurrent) else context.getString(
                    R.string.max_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .maxDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, context
                    .resources.getBoolean(R.bool.is_max_charge_discharge_current_overlay)))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateAverageChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY,
                averageChargeDischargeCurrentOverlay.context.resources.getBoolean(
                    R.bool.is_residual_capacity_overlay)) ||
            averageChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            averageChargeDischargeCurrentOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                    BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                    .average_charge_current, BatteryInfoInterface.averageChargeCurrent) else
                    context.getString(R.string.average_discharge_current, BatteryInfoInterface
                        .averageDischargeCurrent) else if(status == BatteryManager
                        .BATTERY_STATUS_CHARGING) context.getString(
                    R.string.average_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .averageChargeCurrent) else context.getString(
                    R.string.average_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .averageDischargeCurrent)

            visibility = if(pref.getBoolean(IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, this
                    .resources.getBoolean(R.bool.is_average_charge_discharge_current_overlay)))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateMinChargeDischargeCurrentOverlay(status: Int) {

        if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, minChargeDischargeCurrentOverlay
                .context.resources.getBoolean(R.bool.is_min_charge_discharge_current_overlay)) ||
            minChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            minChargeDischargeCurrentOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                    BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                    .min_charge_current, BatteryInfoInterface.minChargeCurrent) else context
                    .getString(R.string.min_discharge_current, BatteryInfoInterface
                        .minDischargeCurrent) else if(status == BatteryManager
                        .BATTERY_STATUS_CHARGING) context.getString(
                    R.string.min_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .minChargeCurrent) else context.getString(
                    R.string.min_charge_discharge_overlay_only_values, BatteryInfoInterface
                        .minDischargeCurrent)

            visibility = if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, context
                    .resources.getBoolean(R.bool.is_min_charge_discharge_current_overlay)))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateChargingCurrentLimitOverlay() {

        if(pref.getBoolean(
                IS_CHARGING_CURRENT_LIMIT_OVERLAY, chargingCurrentLimitOverlay.context.resources
                    .getBoolean(R.bool.is_min_charge_discharge_current_overlay)) ||
            chargingCurrentLimitOverlay.visibility == View.VISIBLE)
            chargingCurrentLimitOverlay.apply {

                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) context.getString(
                    R.string.charging_current_limit, getOnChargingCurrentLimit(context))
                else context.getString(R.string.charging_current_limit_overlay_only_values,
                    getOnChargingCurrentLimit(context))

                visibility = if(pref.getBoolean(IS_CHARGING_CURRENT_LIMIT_OVERLAY, context
                        .resources.getBoolean(R.bool.is_charging_current_limit_overlay)))
                    View.VISIBLE else View.GONE
            }
    }
    private fun onUpdateTemperatureOverlay() {

        if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, temperatureOverlay.resources.getBoolean(
                R.bool.is_temperature_overlay)) || temperatureOverlay.visibility == View.VISIBLE)
            temperatureOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay)))
                context.getString(if(pref.getBoolean(TEMPERATURE_IN_FAHRENHEIT,
                    context.resources.getBoolean(R.bool.temperature_in_fahrenheit)))
                R.string.temperature_fahrenheit else R.string.temperature_celsius,
                getOnTemperature(context)) else context.getString(if(pref.getBoolean(
                    TEMPERATURE_IN_FAHRENHEIT, context.resources.getBoolean(
                        R.bool.temperature_in_fahrenheit)))
                R.string.temperature_fahrenheit_overlay_only_values else
                R.string.temperature_celsius_overlay_only_values, getOnTemperature(context))

            visibility = if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, this.resources.getBoolean(
                    R.bool.is_temperature_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateVoltageOverlay() {

        if(pref.getBoolean(IS_VOLTAGE_OVERLAY, voltageOverlay.resources.getBoolean(
                R.bool.is_voltage_overlay)) || voltageOverlay.visibility == View.VISIBLE)
            voltageOverlay.apply {

            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) context.getString(if(
                pref.getBoolean(VOLTAGE_IN_MV, context.resources.getBoolean(R.bool.voltage_in_mv)))
                R.string.voltage_mv else R.string.voltage, DecimalFormat("#.#").format(
                getOnVoltage(context))) else context.getString(if(pref.getBoolean(VOLTAGE_IN_MV,
                    context.resources.getBoolean(R.bool.voltage_in_mv)))
                R.string.voltage_mv_overlay_only_values else R.string.voltage_overlay_only_values,
                DecimalFormat("#.#").format(getOnVoltage(context)))

            visibility = if(pref.getBoolean(IS_VOLTAGE_OVERLAY, this.resources.getBoolean(
                    R.bool.is_voltage_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateLastChargeTimeOverlay() {

        if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, lastChargeTimeOverlay.context.resources
                .getBoolean(R.bool.is_last_charge_time_overlay))
            || lastChargeTimeOverlay.visibility == View.VISIBLE)
        lastChargeTimeOverlay.apply {
            
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

            setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.last_charge_time else
                R.string.last_charge_time_overlay_only_values, getOnLastChargeTime(context),
                "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%", "${pref.getInt(BATTERY_LEVEL_TO, 
                    0)}%")

            visibility = if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, context
                    .resources.getBoolean(R.bool.is_last_charge_time_overlay))) View.VISIBLE else
                View.GONE
        }
    }

    private fun onUpdateBatteryWearOverlay() {

        if((pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, batteryWearOverlay.resources.getBoolean(
                R.bool.is_battery_wear_overlay)) && pref.getBoolean(IS_SUPPORTED,
                batteryWearOverlay.context.resources.getBoolean(R.bool.is_supported))) ||
            batteryWearOverlay.visibility == View.VISIBLE)
            batteryWearOverlay.apply {
            
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))

                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))

            text = getOnBatteryWear(context, true)

            visibility = if(pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, this.resources.getBoolean(
                    R.bool.is_battery_wear_overlay)) && pref.getBoolean(IS_SUPPORTED,
                    batteryWearOverlay.context.resources.getBoolean(R.bool.is_supported)))
                View.VISIBLE else View.GONE
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
                        windowManager?.updateViewLayout(linearLayout, updatedParameters)
                    }
                }

                return false
            }
        }
}