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
import android.provider.Settings
import android.view.*
import android.widget.Toast
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
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_VOLTAGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_OPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_STYLE
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.databinding.OverlayLayoutBinding
import com.ph03nix_x.capacityinfo.helpers.TimeHelper
import com.ph03nix_x.capacityinfo.utilities.Constants.NUMBER_OF_CYCLES_PATH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AVERAGE_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_HEALTH_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_CURRENT_LIMIT_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_TIME_REMAINING_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FAST_CHARGE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MAXIMUM_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_MINIMUM_TEMPERATURE_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NUMBER_OF_FULL_CHARGES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ONLY_VALUES_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_REMAINING_BATTERY_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SCREEN_TIME_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_FONT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_LOCATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.OVERLAY_TEXT_COLOR
import java.io.*
import java.lang.Exception
import java.text.DecimalFormat

interface OverlayInterface : BatteryInfoInterface {

    companion object {
        private lateinit var binding: OverlayLayoutBinding

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
                        IS_NUMBER_OF_FULL_CHARGES_OVERLAY, context.resources.getBoolean(
                            R.bool.is_number_of_full_charges_overlay)), getBoolean(
                        IS_NUMBER_OF_CYCLES_OVERLAY, context.resources.getBoolean(
                            R.bool.is_number_of_cycles_overlay)), getBoolean(
                        IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY, context.resources.getBoolean(R.bool
                            .is_number_of_cycles_android_overlay)), getBoolean(
                        IS_CHARGING_TIME_OVERLAY, context.resources.getBoolean(R.bool
                            .is_charging_time_overlay)), getBoolean(
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
                        IS_FAST_CHARGE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_fast_charge_overlay)), getBoolean(
                        IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_max_charge_discharge_current_overlay)), getBoolean(
                        IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_average_charge_discharge_current_overlay)), getBoolean(
                        IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY, context.resources.getBoolean(
                            R.bool.is_min_charge_discharge_current_overlay)),
                    getBoolean(IS_CHARGING_CURRENT_LIMIT_OVERLAY, context.resources.getBoolean(
                        R.bool.is_charging_current_limit_overlay)), getBoolean(
                        IS_TEMPERATURE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_temperature_overlay)), getBoolean(
                        IS_MAXIMUM_TEMPERATURE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_maximum_temperature_overlay)), getBoolean(
                        IS_AVERAGE_TEMPERATURE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_average_temperature_overlay)), getBoolean(
                        IS_MINIMUM_TEMPERATURE_OVERLAY, context.resources.getBoolean(
                            R.bool.is_minimum_temperature_overlay)), getBoolean(IS_VOLTAGE_OVERLAY,
                        context.resources.getBoolean(R.bool.is_voltage_overlay)), getBoolean(
                        IS_LAST_CHARGE_TIME_OVERLAY, context.resources.getBoolean(
                            R.bool.is_last_charge_time_overlay)), getBoolean(
                        IS_BATTERY_WEAR_OVERLAY, context.resources.getBoolean(
                            R.bool.is_battery_wear_overlay)))
                overlayArray.forEach {
                    if(Settings.canDrawOverlays(context) && (getBoolean(IS_ENABLED_OVERLAY,
                            context.resources.getBoolean(R.bool.is_enabled_overlay))
                                || isEnabledOverlay) && it) return true
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
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams
                    .TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT)
            val overlayLocation = pref.getString(OVERLAY_LOCATION,
                "${context.resources.getInteger(R.integer.overlay_location_default)}")
            parameters.apply {
                gravity = when(overlayLocation?.toInt()) {
                    0 -> Gravity.TOP
                    1 -> Gravity.TOP or Gravity.START
                    2 -> Gravity.TOP or Gravity.END
                    3 -> Gravity.CENTER
                    4 -> Gravity.CENTER or Gravity.START
                    5 -> Gravity.CENTER or Gravity.END
                    6 -> Gravity.BOTTOM
                    7 -> Gravity.BOTTOM or Gravity.START
                    8 -> Gravity.BOTTOM or Gravity.END
                    else -> Gravity.TOP
                }
                x = 0
                y = 0
            }
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            onCreateViews(context)
            if(linearLayout != null && linearLayout?.windowToken == null)
                windowManager?.addView(linearLayout, parameters)
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
        binding = OverlayLayoutBinding.inflate(LayoutInflater.from(context), null,
            false)
        linearLayout = binding.overlayLinearLayout
        onUpdateOverlay(context)
    }

    fun onUpdateOverlay(context: Context) {
        try {
            batteryIntent = context.registerReceiver(null, IntentFilter(Intent
                .ACTION_BATTERY_CHANGED))
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
            val extraPlugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                -1) ?: -1
            val sourceOfPower = getSourceOfPower(context, extraPlugged, true,
                pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                    R.bool.is_only_values_overlay)))
            linearLayout?.setBackgroundColor(onSetBackgroundLinearLayout(context))

            onUpdateBatteryLevelOverlay()
            onUpdateNumberOfChargesOverlay()
            onUpdateNumberOfFullChargesOverlay()
            onUpdateNumberOfCyclesOverlay()
            onUpdateNumberOfCyclesAndroidOverlay()
            onUpdateChargingTimeOverlay()
            onUpdateChargingTimeRemainingOverlay(status)
            onUpdateRemainingBatteryTimeOverlay(status)
            onUpdateScreenTimeOverlay()
            onUpdateCurrentCapacityOverlay()
            onUpdateCapacityAddedOverlay()
            onUpdateBatteryHealthOverlay()
            onUpdateResidualCapacityOverlay()
            onUpdateStatusOverlay(status)
            onUpdateSourceOfPowerOverlay(sourceOfPower)
            onUpdateChargeDischargeCurrentOverlay(status)
            onUpdateFastChargeOverlay(status)
            onUpdateMaxChargeDischargeCurrentOverlay(status)
            onUpdateAverageChargeDischargeCurrentOverlay(status)
            onUpdateMinChargeDischargeCurrentOverlay(status)
            onUpdateChargingCurrentLimitOverlay()
            onUpdateTemperatureOverlay()
            onUpdateMaximumTemperatureOverlay()
            onUpdateAverageTemperatureOverlay()
            onUpdateMinimumTemperatureOverlay()
            onUpdateVoltageOverlay()
            onUpdateLastChargeTimeOverlay()
            onUpdateBatteryWearOverlay()
        }
        catch(e: Exception) { return }
    }

    private fun onSetBackgroundLinearLayout(context: Context) =
        Color.argb(if(pref.getInt(OVERLAY_OPACITY, context.resources.getInteger(
                R.integer.overlay_opacity_default)) > context.resources.getInteger(
                R.integer.overlay_opacity_max) || pref.getInt(OVERLAY_OPACITY,
                context.resources.getInteger(R.integer.overlay_opacity_default)) < 0)
                    context.resources.getInteger(R.integer.overlay_opacity_default)
        else pref.getInt(OVERLAY_OPACITY, context.resources.getInteger(
            R.integer.overlay_opacity_default)), 0, 0, 0)

    private fun onUpdateBatteryLevelOverlay() {
        if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, binding.batteryLevelOverlay.resources
                .getBoolean(R.bool.is_battery_level_overlay)) ||
            binding.batteryLevelOverlay.visibility == View.VISIBLE)
            binding.batteryLevelOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.battery_level
                else R.string.battery_level_overlay_only_values,
                "${getBatteryLevel(context)}%")
                visibility = if(pref.getBoolean(IS_BATTERY_LEVEL_OVERLAY, context.resources
                    .getBoolean(R.bool.is_battery_level_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateNumberOfChargesOverlay() {
        if(pref.getBoolean(IS_NUMBER_OF_CHARGES_OVERLAY, binding.numberOfChargesOverlay.context
                .resources.getBoolean(R.bool.is_number_of_charges_overlay)) ||
            binding.numberOfChargesOverlay.visibility == View.VISIBLE)
            binding.numberOfChargesOverlay.apply {
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

    private fun onUpdateNumberOfFullChargesOverlay() {
        if(pref.getBoolean(IS_NUMBER_OF_FULL_CHARGES_OVERLAY, binding.numberOfFullChargesOverlay
                .context.resources.getBoolean(R.bool.is_number_of_charges_overlay))
            || binding.numberOfFullChargesOverlay.visibility == View.VISIBLE)
            binding.numberOfFullChargesOverlay.apply {
                    TextAppearanceHelper.setTextAppearance(context, this,
                        pref.getString(OVERLAY_TEXT_STYLE, "0"),
                        pref.getString(OVERLAY_FONT, "6"),
                        pref.getString(OVERLAY_SIZE, "2"))
                    setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                    text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY,
                            context.resources.getBoolean(R.bool.is_only_values_overlay)))
                                R.string.number_of_full_charges else R.string
                        .number_of_full_charges_overlay_only_values, pref.getLong(
                        NUMBER_OF_FULL_CHARGES, 0))
                    visibility = if(pref.getBoolean(IS_NUMBER_OF_FULL_CHARGES_OVERLAY,
                            context.resources.getBoolean(R.bool.is_number_of_full_charges_overlay)))
                                View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateNumberOfCyclesOverlay() {
        if(pref.getBoolean(IS_NUMBER_OF_CYCLES_OVERLAY, binding.numberOfCyclesOverlay.context
                .resources.getBoolean(R.bool.is_number_of_cycles_overlay)) ||
            binding.numberOfCyclesOverlay.visibility == View.VISIBLE)
            binding.numberOfCyclesOverlay.apply {
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

    private fun onUpdateNumberOfCyclesAndroidOverlay() {
        if((pref.getBoolean(IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY, binding
                .numberOfCyclesAndroidOverlay.context.resources.getBoolean(
                    R.bool.is_number_of_cycles_android_overlay)) && File(
                NUMBER_OF_CYCLES_PATH).exists()) ||
            binding.numberOfCyclesAndroidOverlay.visibility == View.VISIBLE)
            binding.numberOfCyclesAndroidOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) R.string
                    .number_of_cycles_android else R.string
                    .number_of_cycles_android_overlay_only_values, getNumberOfCyclesAndroid())
                visibility = if(pref.getBoolean(IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY,
                        context.resources.getBoolean(R.bool.is_number_of_cycles_android_overlay)))
                            View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateChargingTimeOverlay() {
        if((pref.getBoolean(IS_CHARGING_TIME_OVERLAY, binding.chargingTimeOverlay.context.resources
                .getBoolean(R.bool.is_charging_time_overlay))
                    && (CapacityInfoService.instance?.seconds ?: 0) > 1) ||
            binding.chargingTimeOverlay.visibility == View.VISIBLE)
            binding.chargingTimeOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = getChargingTime(context, (CapacityInfoService.instance?.seconds ?: 0),
                true, pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                    R.bool.is_only_values_overlay)))
                visibility = if(pref.getBoolean(IS_CHARGING_TIME_OVERLAY, context.resources
                    .getBoolean(R.bool.is_charging_time_overlay)) && (
                        CapacityInfoService.instance?.seconds ?: 0) > 1) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateChargingTimeRemainingOverlay(status: Int) {
        if((pref.getBoolean(IS_CHARGING_TIME_REMAINING_OVERLAY, binding.chargingTimeRemainingOverlay
                .context.resources.getBoolean(R.bool.is_charging_time_remaining_overlay))
                    && status == BatteryManager.BATTERY_STATUS_CHARGING) ||
            binding.chargingTimeRemainingOverlay.visibility == View.VISIBLE)
            binding.chargingTimeRemainingOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY,
                        context.resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.charging_time_remaining
                else R.string.charging_time_remaining_overlay_only_values,
                    getChargingTimeRemaining(context))
                visibility = if(pref.getBoolean(IS_CHARGING_TIME_REMAINING_OVERLAY, context
                        .resources.getBoolean(R.bool.is_charging_time_remaining_overlay)) &&
                    status == BatteryManager.BATTERY_STATUS_CHARGING) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateRemainingBatteryTimeOverlay(status: Int) {
        if((pref.getBoolean(IS_REMAINING_BATTERY_TIME_OVERLAY, binding.remainingBatteryTimeOverlay
                .context.resources.getBoolean(R.bool.is_remaining_battery_time_overlay))
                    && status != BatteryManager.BATTERY_STATUS_CHARGING) ||
            binding.remainingBatteryTimeOverlay.visibility == View.VISIBLE)
            binding.remainingBatteryTimeOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) R.string.remaining_battery_time
                else R.string.remaining_battery_time_overlay_only_values, getRemainingBatteryTime(
                    context))
                visibility = if(pref.getBoolean(IS_REMAINING_BATTERY_TIME_OVERLAY, context
                        .resources.getBoolean(R.bool.is_remaining_battery_time_overlay)) &&
                    status != BatteryManager.BATTERY_STATUS_CHARGING) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateScreenTimeOverlay() {
        if(pref.getBoolean(IS_SCREEN_TIME_OVERLAY, binding.screenTimeOverlay.context.resources
                .getBoolean(R.bool.is_screen_time_overlay)) ||
            binding.screenTimeOverlay.visibility == View.VISIBLE)
            binding.screenTimeOverlay.apply {
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
        if((pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY, binding.currentCapacityOverlay.context
                .resources.getBoolean(R.bool.is_current_capacity_overlay))) ||
            binding.currentCapacityOverlay.visibility == View.VISIBLE)
            binding.currentCapacityOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isCapacityInWh = pref.getBoolean(PreferencesKeys.IS_CAPACITY_IN_WH,
                    context.resources.getBoolean(R.bool.is_capacity_in_wh))
                text = if(isCapacityInWh) context.getString(if(!pref.getBoolean(
                        IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                            R.bool.is_only_values_overlay))) R.string.current_capacity_wh
                else R.string.current_capacity_wh_overlay_only_values,
                DecimalFormat("#.#").format(
                    getCapacityInWh(getCurrentCapacity(context))))
                else context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context
                        .resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.current_capacity else R.string.current_capacity_overlay_only_values,
                DecimalFormat("#.#").format(getCurrentCapacity(context)))
                visibility = if(pref.getBoolean(IS_CURRENT_CAPACITY_OVERLAY,
                        context.resources.getBoolean(R.bool.is_current_capacity_overlay)))
                    View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateCapacityAddedOverlay() {
        if((pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, binding.capacityAddedOverlay.context
                .resources.getBoolean(R.bool.is_capacity_added_overlay))) ||
            binding.capacityAddedOverlay.visibility == View.VISIBLE)
            binding.capacityAddedOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = getCapacityAdded(context, true, pref.getBoolean(
                IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(R.bool.is_only_values_overlay)))
                visibility = if(pref.getBoolean(IS_CAPACITY_ADDED_OVERLAY, context.resources
                    .getBoolean(R.bool.is_capacity_added_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateBatteryHealthOverlay() {
        if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, binding.batteryHealthOverlay.context.resources
                .getBoolean(R.bool.is_battery_health_overlay)) ||
            binding.batteryHealthOverlay.visibility == View.VISIBLE)
            binding.batteryHealthOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY,
                        context.resources.getBoolean(R.bool.is_only_values_overlay)))
                    R.string.battery_health else R.string.battery_health_overlay_only_values,
                    getBatteryHealth(context))
                visibility = if(pref.getBoolean(IS_BATTERY_HEALTH_OVERLAY, context.resources
                        .getBoolean(R.bool.is_battery_health_overlay)) &&
                    getBatteryHealth(context) != null) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateResidualCapacityOverlay() {
        if((pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY, binding.residualCapacityOverlay.context
                .resources.getBoolean(R.bool.is_residual_capacity_overlay))) ||
            binding.residualCapacityOverlay.visibility == View.VISIBLE)
            binding.residualCapacityOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = getResidualCapacity(context,true,
                pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(
                    R.bool.is_only_values_overlay)))
                visibility = if(pref.getBoolean(IS_RESIDUAL_CAPACITY_OVERLAY,
                    context.resources.getBoolean(R.bool.is_residual_capacity_overlay))) View.VISIBLE
                else View.GONE
        }
    }

    private fun onUpdateStatusOverlay(status: Int) {
        if(pref.getBoolean(IS_STATUS_OVERLAY, binding.statusOverlay.context.resources.getBoolean(
                R.bool.is_status_overlay)) || binding.statusOverlay.visibility == View.VISIBLE)
            binding.statusOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.status
                else R.string.status_overlay_only_values, getStatus(context, status))
                visibility = if(pref.getBoolean(IS_STATUS_OVERLAY, context.resources.getBoolean(
                    R.bool.is_status_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateSourceOfPowerOverlay(sourceOfPower: String) {
        if((pref.getBoolean(IS_SOURCE_OF_POWER, binding.sourceOfPowerOverlay.context.resources
                .getBoolean(R.bool.is_source_of_power_overlay)) && sourceOfPower != "N/A")
            || binding.sourceOfPowerOverlay.visibility == View.VISIBLE)
            binding.sourceOfPowerOverlay.apply {
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
        if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY,
                binding.chargeDischargeCurrentOverlay.context.resources.getBoolean(
                    R.bool.is_charge_discharge_current_overlay)) ||
            binding.chargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            binding.chargeDischargeCurrentOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isChargingDischargeCurrentInWatt = pref.getBoolean(
                    PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
                    context.resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
                if(isChargingDischargeCurrentInWatt)
                    text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                            .getBoolean(R.bool.is_only_values_overlay)))
                        context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING)
                            R.string.charge_current_watt else R.string.discharge_current_watt,
                            DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                                getChargeDischargeCurrent(context),
                                status == BatteryManager.BATTERY_STATUS_CHARGING)))
                else context.getString(R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            getChargeDischargeCurrent(context),
                            status == BatteryManager.BATTERY_STATUS_CHARGING)))
                else text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay)))
                    context.getString(if(status == BatteryManager.BATTERY_STATUS_CHARGING)
                        R.string.charge_current else R.string.discharge_current,
                        getChargeDischargeCurrent(context).toString())
                else context.getString(R.string.charge_discharge_overlay_only_values,
                    getChargeDischargeCurrent(context))
            visibility = if(pref.getBoolean(IS_CHARGE_DISCHARGE_CURRENT_OVERLAY, context
                    .resources.getBoolean(R.bool.is_charge_discharge_current_overlay)))
                View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateFastChargeOverlay(status: Int) {
        if(pref.getBoolean(IS_FAST_CHARGE_OVERLAY, binding.fastChargeOverlay.context.resources
                .getBoolean(R.bool.is_fast_charge_overlay)) ||
            binding.fastChargeOverlay.visibility == View.VISIBLE)
            binding.fastChargeOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = getFastChargeOverlay(context)
                visibility = if(pref.getBoolean(IS_FAST_CHARGE_OVERLAY, context.resources
                        .getBoolean(R.bool.is_fast_charge_overlay)) &&
                    status == BatteryManager.BATTERY_STATUS_CHARGING) View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateMaxChargeDischargeCurrentOverlay(status: Int) {
        if(pref.getBoolean(IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY,
                binding.maxChargeDischargeCurrentOverlay.context.resources.getBoolean(
                    R.bool.is_max_charge_discharge_current_overlay))
            || binding.maxChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            binding.maxChargeDischargeCurrentOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isChargingDischargeCurrentInWatt = pref.getBoolean(
                    PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
                    resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
                if(isChargingDischargeCurrentInWatt)
                    text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                    BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                    .max_charge_current_watt, DecimalFormat("#.##").format(
                        getChargeDischargeCurrentInWatt(BatteryInfoInterface.maxChargeCurrent,
                            true)))
                    else context.getString(R.string.max_discharge_current_watt,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.maxDischargeCurrent)))
                    else if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.maxChargeCurrent, true)))
                    else context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.maxDischargeCurrent)))
                else text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
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
                binding.averageChargeDischargeCurrentOverlay.context.resources.getBoolean(
                    R.bool.is_residual_capacity_overlay)) ||
            binding.averageChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            binding.averageChargeDischargeCurrentOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isChargingDischargeCurrentInWatt = pref.getBoolean(
                    PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
                    resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
                if(isChargingDischargeCurrentInWatt)
                    text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                            .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                        BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                        .average_charge_current_watt, DecimalFormat("#.##").format(
                        getChargeDischargeCurrentInWatt(BatteryInfoInterface.averageChargeCurrent,
                        true)))
                    else context.getString(R.string.average_discharge_current_watt,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.averageDischargeCurrent)))
                    else if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.averageChargeCurrent,
                            true))) else context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.averageDischargeCurrent)))
                else text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
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
        if(pref.getBoolean(IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY,
                binding.minChargeDischargeCurrentOverlay.context.resources.getBoolean(
                    R.bool.is_min_charge_discharge_current_overlay)) ||
            binding.minChargeDischargeCurrentOverlay.visibility == View.VISIBLE)
            binding.minChargeDischargeCurrentOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isChargingDischargeCurrentInWatt = pref.getBoolean(
                    PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
                    resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
                if(isChargingDischargeCurrentInWatt)
                    text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                            .getBoolean(R.bool.is_only_values_overlay))) if(status ==
                        BatteryManager.BATTERY_STATUS_CHARGING) context.getString(R.string
                        .min_charge_current_watt, DecimalFormat("#.##").format(
                        getChargeDischargeCurrentInWatt(BatteryInfoInterface.minChargeCurrent,
                        true)))
                    else context.getString(R.string.min_discharge_current_watt,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.minDischargeCurrent)))
                    else if(status == BatteryManager.BATTERY_STATUS_CHARGING) context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.minChargeCurrent, true)))
                    else context.getString(
                        R.string.charging_discharge_current_watt_overlay_only_values,
                        DecimalFormat("#.##").format(getChargeDischargeCurrentInWatt(
                            BatteryInfoInterface.minDischargeCurrent)))
                else text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
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
                IS_CHARGING_CURRENT_LIMIT_OVERLAY, binding.chargingCurrentLimitOverlay.context
                    .resources.getBoolean(R.bool.is_min_charge_discharge_current_overlay)) ||
            binding.chargingCurrentLimitOverlay.visibility == View.VISIBLE)
            binding.chargingCurrentLimitOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                val isChargingDischargeCurrentInWatt = pref.getBoolean(
                    PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
                    resources.getBoolean(R.bool.is_charging_discharge_current_in_watt))
                val chargingCurrentLimit = getChargingCurrentLimit(context)
                if(chargingCurrentLimit != null)
                    text = if(isChargingDischargeCurrentInWatt)
                        if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                                .getBoolean(R.bool.is_only_values_overlay))) context.getString(
                            R.string.charging_current_limit_watt, DecimalFormat("#.##")
                                .format(getChargeDischargeCurrentInWatt(
                                    getChargingCurrentLimit(context)!!.toInt(), true)))
                        else context.getString(
                            R.string.charging_discharge_current_watt_overlay_only_values,
                            getChargingCurrentLimit(context))
                    else if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                            .getBoolean(R.bool.is_only_values_overlay))) context.getString(
                        R.string.charging_current_limit, getChargingCurrentLimit(context))
                    else context.getString(R.string.charging_current_limit_overlay_only_values,
                        getChargingCurrentLimit(context))
                visibility = if(pref.getBoolean(IS_CHARGING_CURRENT_LIMIT_OVERLAY, context
                        .resources.getBoolean(R.bool.is_charging_current_limit_overlay)))
                    View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateTemperatureOverlay() {
        if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, binding.temperatureOverlay.resources.getBoolean(
                R.bool.is_temperature_overlay)) || binding.temperatureOverlay.visibility ==
            View.VISIBLE)
            binding.temperatureOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
            text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) context.getString(R.string
                .temperature, DecimalFormat().format(getTemperatureInCelsius(context)),
                DecimalFormat().format(getTemperatureInFahrenheit(context))) else context
                .getString(R.string.temperature_overlay_only_values, DecimalFormat().format(
                    getTemperatureInCelsius(context)), DecimalFormat().format(
                    getTemperatureInFahrenheit(context)))
            visibility = if(pref.getBoolean(IS_TEMPERATURE_OVERLAY, this.resources.getBoolean(
                    R.bool.is_temperature_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateMaximumTemperatureOverlay() {
        if(pref.getBoolean(IS_MAXIMUM_TEMPERATURE_OVERLAY, binding.maximumTemperatureOverlay
                .resources.getBoolean(R.bool.is_maximum_temperature_overlay)) ||
            binding.maximumTemperatureOverlay.visibility == View.VISIBLE)
            binding.maximumTemperatureOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) context.getString(R.string
                    .maximum_temperature,
                    DecimalFormat().format(BatteryInfoInterface.maximumTemperature),
                    DecimalFormat().format(getTemperatureInFahrenheit(
                        BatteryInfoInterface.maximumTemperature))) else context.getString(
                    R.string.temperature_overlay_only_values, DecimalFormat().format(
                        BatteryInfoInterface.maximumTemperature), DecimalFormat().format(
                        getTemperatureInFahrenheit(BatteryInfoInterface.maximumTemperature)))
                visibility = if(pref.getBoolean(IS_MAXIMUM_TEMPERATURE_OVERLAY,
                        this.resources.getBoolean(R.bool.is_maximum_temperature_overlay)))
                    View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateAverageTemperatureOverlay() {
        if(pref.getBoolean(IS_AVERAGE_TEMPERATURE_OVERLAY, binding.averageTemperatureOverlay
                .resources.getBoolean(R.bool.is_average_temperature_overlay)) ||
            binding.averageTemperatureOverlay.visibility == View.VISIBLE)
            binding.averageTemperatureOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) context.getString(R.string
                    .average_temperature,
                    DecimalFormat().format(BatteryInfoInterface.averageTemperature),
                    DecimalFormat().format(getTemperatureInFahrenheit(
                        BatteryInfoInterface.averageTemperature))) else context.getString(
                    R.string.temperature_overlay_only_values, DecimalFormat().format(
                        BatteryInfoInterface.averageTemperature), DecimalFormat().format(
                        getTemperatureInFahrenheit(BatteryInfoInterface.averageTemperature)))
                visibility = if(pref.getBoolean(IS_AVERAGE_TEMPERATURE_OVERLAY,
                        this.resources.getBoolean(R.bool.is_average_temperature_overlay)))
                    View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateMinimumTemperatureOverlay() {
        if(pref.getBoolean(IS_MINIMUM_TEMPERATURE_OVERLAY, binding.minimumTemperatureOverlay
                .resources.getBoolean(R.bool.is_minimum_temperature_overlay)) ||
            binding.minimumTemperatureOverlay.visibility == View.VISIBLE)
            binding.minimumTemperatureOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                        .getBoolean(R.bool.is_only_values_overlay))) context.getString(R.string
                    .minimum_temperature,
                    DecimalFormat().format(BatteryInfoInterface.minimumTemperature),
                    DecimalFormat().format(getTemperatureInFahrenheit(
                        BatteryInfoInterface.minimumTemperature))) else context.getString(
                    R.string.temperature_overlay_only_values, DecimalFormat().format(
                        BatteryInfoInterface.minimumTemperature), DecimalFormat().format(
                        getTemperatureInFahrenheit(BatteryInfoInterface.minimumTemperature)))
                visibility = if(pref.getBoolean(IS_MINIMUM_TEMPERATURE_OVERLAY,
                        this.resources.getBoolean(R.bool.is_minimum_temperature_overlay)))
                    View.VISIBLE else View.GONE
            }
    }

    private fun onUpdateVoltageOverlay() {
        if(pref.getBoolean(IS_VOLTAGE_OVERLAY, binding.voltageOverlay.resources.getBoolean(
                R.bool.is_voltage_overlay)) || binding.voltageOverlay.visibility == View.VISIBLE)
            binding.voltageOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
            text = if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) context.getString(R.string.voltage,
                DecimalFormat("#.#").format(getVoltage(context))) else context.getString(
                R.string.voltage_overlay_only_values, DecimalFormat("#.#").format(
                    getVoltage(context)))
            visibility = if(pref.getBoolean(IS_VOLTAGE_OVERLAY, this.resources.getBoolean(
                    R.bool.is_voltage_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun onUpdateLastChargeTimeOverlay() {
        if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, binding.lastChargeTimeOverlay.context
                .resources.getBoolean(R.bool.is_last_charge_time_overlay))
            || binding.lastChargeTimeOverlay.visibility == View.VISIBLE)
            binding.lastChargeTimeOverlay.apply {
                TextAppearanceHelper.setTextAppearance(context, this,
                    pref.getString(OVERLAY_TEXT_STYLE, "0"),
                    pref.getString(OVERLAY_FONT, "6"),
                    pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
                text = context.getString(if(!pref.getBoolean(IS_ONLY_VALUES_OVERLAY, context.resources
                    .getBoolean(R.bool.is_only_values_overlay))) R.string.last_charge_time else
                R.string.last_charge_time_overlay_only_values, getLastChargeTime(context),
                    "${pref.getInt(BATTERY_LEVEL_WITH, 0)}%", "${pref.getInt(BATTERY_LEVEL_TO, 
                    0)}%")
                visibility = if(pref.getBoolean(IS_LAST_CHARGE_TIME_OVERLAY, context
                    .resources.getBoolean(R.bool.is_last_charge_time_overlay))) View.VISIBLE else
                        View.GONE
        }
    }

    private fun onUpdateBatteryWearOverlay() {
        if((pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, binding.batteryWearOverlay.resources
                .getBoolean(R.bool.is_battery_wear_overlay))) || binding.batteryWearOverlay
                .visibility == View.VISIBLE)
            binding.batteryWearOverlay.apply {
            TextAppearanceHelper.setTextAppearance(context, this,
                pref.getString(OVERLAY_TEXT_STYLE, "0"),
                pref.getString(OVERLAY_FONT, "6"),
                pref.getString(OVERLAY_SIZE, "2"))
                setTextColor(pref.getInt(OVERLAY_TEXT_COLOR, Color.WHITE))
            text = getBatteryWear(context, true, pref.getBoolean(
                IS_ONLY_VALUES_OVERLAY, context.resources.getBoolean(R.bool.is_only_values_overlay)))
            visibility = if(pref.getBoolean(IS_BATTERY_WEAR_OVERLAY, this.resources.getBoolean(
                    R.bool.is_battery_wear_overlay))) View.VISIBLE else View.GONE
        }
    }

    private fun getNumberOfCyclesAndroid(): Int {
        if(!File(NUMBER_OF_CYCLES_PATH).exists()) return 0
        val cycleCount = File(NUMBER_OF_CYCLES_PATH).absolutePath
        var numberOfCycles: Int
        try {
            val br = try {
                BufferedReader(FileReader(cycleCount))
            }
            catch (e: FileNotFoundException) { null }
            numberOfCycles = br?.readLine()?.toInt() ?: 0
            br?.close()

        } catch (e: IOException) { numberOfCycles = 0 }
        return numberOfCycles
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