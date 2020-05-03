package com.ph03nix_x.capacityinfo.utils

object PreferencesKeys {

    const val IS_SUPPORTED = "is_supported"
    const val IS_SHOW_NOT_SUPPORTED_DIALOG = "is_show_not_supported_dialog"
    const val IS_SHOW_INSTRUCTION = "is_show_instruction"

    // Service & Notification
    const val IS_AUTO_START_SERVICE = "is_auto_start_service"
    const val IS_SHOW_STOP_SERVICE = "is_show_stop_service"
    const val IS_SERVICE_TIME = "is_service_time"
    const val IS_SHOW_CAPACITY_ADDED_IN_NOTIFICATION = "is_show_capacity_added_in_notification"
    const val IS_SHOW_LAST_CHARGE_TIME_IN_NOTIFICATION = "is_show_last_charge_time_in_notification"
    const val IS_STOP_THE_SERVICE_WHEN_THE_CD = "is_stop_the_service_when_the_cd"

    // Appearance
    const val IS_AUTO_DARK_MODE = "is_auto_dark_mode" // Android 10+
    const val IS_DARK_MODE = "is_dark_mode"
    const val LANGUAGE = "language"

    // Misc
    const val TEMPERATURE_IN_FAHRENHEIT = "temperature_in_fahrenheit"
    const val VOLTAGE_IN_MV = "voltage_in_mv"
    const val UNIT_OF_CHARGE_DISCHARGE_CURRENT = "unit_of_charge_discharge_current"
    const val UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY = "unit_of_measurement_of_current_capacity"
    const val VOLTAGE_UNIT = "voltage_unit"
    const val DESIGN_CAPACITY = "design_capacity"

    // Overlay
    const val IS_ENABLED_OVERLAY = "is_enabled_overlay"
    const val OVERLAY_SIZE = "overlay_size"
    const val IS_BATTERY_LEVEL_OVERLAY = "is_battery_level_overlay"
    const val IS_CURRENT_CAPACITY_OVERLAY = "is_current_capacity_overlay"
    const val IS_BATTERY_HEALTH_OVERLAY = "is_battery_health_overlay"
    const val IS_STATUS_OVERLAY = "is_status_overlay"
    const val IS_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_charge_discharge_current_overlay"
    const val IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_max_charge_discharge_current_overlay"
    const val IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_average_charge_discharge_current_overlay"
    const val IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_min_charge_discharge_current_overlay"
    const val IS_TEMPERATURE_OVERLAY = "is_temperature_overlay"
    const val IS_VOLTAGE_OVERLAY = "is_voltage_overlay"

    // Debug
    const val IS_FORCIBLY_SHOW_RATE_THE_APP = "is_forcibly_show_rate_the_app"

    // Battery Information
    const val NUMBER_OF_CHARGES = "number_of_charges"
    const val CAPACITY_ADDED = "capacity_added"
    const val PERCENT_ADDED = "percent_added"
    const val RESIDUAL_CAPACITY = "residual_capacity"
    const val LAST_CHARGE_TIME = "last_charge_time"
    const val BATTERY_LEVEL_WITH = "battery_level_with"
    const val BATTERY_LEVEL_TO = "battery_level_to"
    const val NUMBER_OF_CYCLES = "number_of_cycles"
}