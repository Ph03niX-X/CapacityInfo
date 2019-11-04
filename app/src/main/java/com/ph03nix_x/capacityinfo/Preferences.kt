package com.ph03nix_x.capacityinfo

enum class Preferences(val prefKey: String) {

    IsDarkMode("is_dark_mode"), ChargeCounter("charge_counter"), IsShowInstruction("is_show_instruction"),

    TemperatureInFahrenheit("temperature_in_fahrenheit"), DesignCapacity("design_capacity"), IsSupported("is_supported"),

    LastChargeTime("last_charge_time"), IsShowLastChargeTimeInApp("is_show_last_charge_time_in_app"),

    IsShowLastChargeTimeInNotification("is_show_last_charge_time_in_notification"), BatteryLevelWith("battery_level_with"),

    BatteryLevelTo("battery_level_to"), IsEnableService("is_enable_service"), NotificationRefreshRate("notification_refresh_rate"),

    IsShowServiceStop("is_show_stop_service"), VoltageInMv("voltage_in_mv"), IsShowInformationWhileCharging("is_show_information_while_charging"),

    IsShowInformationDuringDischarge("is_show_information_during_discharge"), IsServiceHours("is_service_time"), CapacityAdded("capacity_added"),

    IsShowChargingTimeInApp("is_show_charging_time_in_app"), IsAutoDarkMode("is_auto_dark_mode"), PercentAdded("percent_added"),

    IsShowCapacityAddedInNotification("is_show_capacity_added_notification"), IsShowCapacityAddedLastChargeInNotification("is_show_capacity_added_last_charge_in_notification"),

    IsShowCapacityAddedInApp("is_show_capacity_added_in_app"), IsShowCapacityAddedLastChargeInApp("is_show_capacity_added_last_charge_in_app"),
}