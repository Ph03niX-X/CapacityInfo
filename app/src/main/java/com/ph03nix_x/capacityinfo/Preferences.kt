package com.ph03nix_x.capacityinfo

enum class Preferences(val prefKey: String) {

    DarkMode("dark_mode"), ChargeCounter("charge_counter"), IsShowInstruction("is_show_instruction"),

    TemperatureInFahrenheit("temperature_in_fahrenheit"), DesignCapacity("design_capacity"), IsSupported("is_supported"),

    LastChargeTime("last_charge_time"), IsShowLastChargeTimeInApp("is_show_last_charge_time_in_app"),

    IsShowLastChargeTimeInNotification("is_show_last_charge_time_in_notification"), BatteryLevelWith("battery_level_with"),

    BatteryLevelTo("battery_level_to"), EnableService("enable_service"), NotificationRefreshRate("notification_refresh_rate"),

    IsShowServiceStop("is_show_stop_service"), VoltageInMv("voltage_in_mv"), IsShowInformationWhileCharging("is_show_information_while_charging"),

    IsShowInformationDuringDischarge("is_show_information_during_discharge")
}