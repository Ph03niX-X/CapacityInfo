package com.ph03nix_x.capacityinfo.utilities

object PreferencesKeys {

    //Other
    const val IS_REQUEST_RATE_THE_APP = "is_request_rate_the_app"
    const val UPDATE_TEMP_SCREEN_TIME = "update_temp_screen_time"

    // Service & Notification
    const val IS_SHOW_STOP_SERVICE = "is_show_stop_service"
    const val IS_SERVICE_TIME = "is_service_time"
    const val IS_STOP_THE_SERVICE_WHEN_THE_CD = "is_stop_the_service_when_the_cd"
    const val IS_SHOW_BATTERY_INFORMATION = "is_show_battery_information"
    const val IS_SHOW_EXPANDED_NOTIFICATION = "is_show_expanded_notification"

    // Battery Status Information
    const val IS_BYPASS_DND = "is_bypass_dnd_mode"
    const val IS_NOTIFY_OVERHEAT_OVERCOOL = "is_notify_overheat_overcool"
    const val OVERHEAT_DEGREES = "overheat_degrees"
    const val OVERCOOL_DEGREES = "overcool_degrees"
    const val IS_NOTIFY_BATTERY_IS_FULLY_CHARGED = "is_notify_battery_is_fully_charged"
    const val IS_NOTIFY_FULL_CHARGE_REMINDER = "is_notify_full_charge_reminder"
    const val FULL_CHARGE_REMINDER_FREQUENCY = "full_charge_reminder_frequency"
    const val IS_NOTIFY_BATTERY_IS_CHARGED = "is_notify_battery_is_charged"
    const val BATTERY_LEVEL_NOTIFY_CHARGED = "battery_level_notify_charged"
    const val IS_NOTIFY_BATTERY_IS_DISCHARGED = "is_notify_battery_is_discharged"
    const val BATTERY_LEVEL_NOTIFY_DISCHARGED = "battery_level_notify_discharged"

    // Appearance
    const val IS_AUTO_DARK_MODE = "is_auto_dark_mode" // Android 10+
    const val IS_DARK_MODE = "is_dark_mode"
    const val TEXT_SIZE = "text_size"
    const val TEXT_FONT = "text_font"
    const val TEXT_STYLE = "text_style"

    // Misc
    const val IS_FAST_CHARGE_SETTING = "is_fast_charge_setting"
    const val IS_CAPACITY_IN_WH = "is_capacity_in_wh"
    const val IS_CHARGING_DISCHARGE_CURRENT_IN_WATT = "is_charging_discharge_current_in_watt"
    const val IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL = "is_reset_screen_time_at_any_charge_level"
    const val TAB_ON_APPLICATION_LAUNCH = "tab_on_application_launch"
    const val UNIT_OF_CHARGE_DISCHARGE_CURRENT = "unit_of_charge_discharge_current"
    const val UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY = "unit_of_measurement_of_current_capacity"
    const val VOLTAGE_UNIT = "voltage_unit"
    const val DESIGN_CAPACITY = "design_capacity"

    // Overlay Preferences
    const val IS_ENABLED_OVERLAY = "is_enabled_overlay"
    const val IS_ONLY_VALUES_OVERLAY = "is_only_values_overlay"
    const val OVERLAY_LOCATION = "overlay_location"

    // Overlay Appearance
    const val OVERLAY_SIZE = "overlay_size"
    const val OVERLAY_FONT = "overlay_font"
    const val OVERLAY_TEXT_STYLE = "overlay_text_style"
    const val OVERLAY_TEXT_COLOR = "overlay_text_color"
    const val OVERLAY_OPACITY = "overlay_opacity"

    // Show/Hide
    const val IS_BATTERY_LEVEL_OVERLAY = "is_battery_level_overlay"
    const val IS_NUMBER_OF_CHARGES_OVERLAY = "is_number_of_charges_overlay"
    const val IS_NUMBER_OF_FULL_CHARGES_OVERLAY = "is_number_of_full_charges_overlay"
    const val IS_NUMBER_OF_CYCLES_OVERLAY = "is_number_of_cycles_overlay"
    const val IS_NUMBER_OF_CYCLES_ANDROID_OVERLAY = "is_number_of_cycles_android_overlay"
    const val IS_CHARGING_TIME_OVERLAY = "is_charging_time_overlay"
    const val IS_CHARGING_TIME_REMAINING_OVERLAY = "is_charging_time_remaining_overlay"
    const val IS_REMAINING_BATTERY_TIME_OVERLAY = "is_remaining_battery_time_overlay"
    const val IS_SCREEN_TIME_OVERLAY = "is_screen_time_overlay"
    const val IS_CURRENT_CAPACITY_OVERLAY = "is_current_capacity_overlay"
    const val IS_CAPACITY_ADDED_OVERLAY = "is_capacity_added_overlay"
    const val IS_BATTERY_HEALTH_OVERLAY = "is_battery_health_overlay"
    const val IS_RESIDUAL_CAPACITY_OVERLAY = "is_residual_capacity_overlay"
    const val IS_STATUS_OVERLAY = "is_status_overlay"
    const val IS_SOURCE_OF_POWER = "is_source_of_power_overlay"
    const val IS_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_charge_discharge_current_overlay"
    const val IS_FAST_CHARGE_OVERLAY = "is_fast_charge_overlay"
    const val IS_MAX_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_max_charge_discharge_current_overlay"
    const val IS_AVERAGE_CHARGE_DISCHARGE_CURRENT_OVERLAY =
        "is_average_charge_discharge_current_overlay"
    const val IS_MIN_CHARGE_DISCHARGE_CURRENT_OVERLAY = "is_min_charge_discharge_current_overlay"
    const val IS_CHARGING_CURRENT_LIMIT_OVERLAY = "is_charging_current_limit_overlay"
    const val IS_TEMPERATURE_OVERLAY = "is_temperature_overlay"
    const val IS_MAXIMUM_TEMPERATURE_OVERLAY = "is_maximum_temperature_overlay"
    const val IS_AVERAGE_TEMPERATURE_OVERLAY = "is_average_temperature_overlay"
    const val IS_MINIMUM_TEMPERATURE_OVERLAY = "is_minimum_temperature_overlay"
    const val IS_VOLTAGE_OVERLAY = "is_voltage_overlay"
    const val IS_LAST_CHARGE_TIME_OVERLAY = "is_last_charge_time_overlay"
    const val IS_BATTERY_WEAR_OVERLAY = "is_battery_wear_overlay"

    // Debug
    const val IS_ENABLED_DEBUG_OPTIONS = "is_enabled_debug_options"
    const val IS_FORCIBLY_SHOW_RATE_THE_APP = "is_forcibly_show_rate_the_app"
    const val IS_AUTO_START_BOOT = "is_auto_start_boot"
    const val IS_AUTO_START_OPEN_APP = "is_auto_start_open_app"
    const val IS_AUTO_START_UPDATE_APP = "is_auto_start_update_app"

    // Battery Information
    const val NUMBER_OF_CHARGES = "number_of_charges"
    const val CAPACITY_ADDED = "capacity_added"
    const val PERCENT_ADDED = "percent_added"
    const val RESIDUAL_CAPACITY = "residual_capacity"
    const val LAST_CHARGE_TIME = "last_charge_time"
    const val BATTERY_LEVEL_WITH = "battery_level_with"
    const val BATTERY_LEVEL_TO = "battery_level_to"
    const val NUMBER_OF_CYCLES = "number_of_cycles"
    const val NUMBER_OF_FULL_CHARGES = "number_of_full_charges"
}