package com.ph03nix_x.capacityinfo

enum class Preferences(val prefName: String) {

    DarkMode("dark_mode"), ChargeCounter("charge_counter"), IsShowInstruction("is_show_instruction"),

    Fahrenheit("fahrenheit"), DesignCapacity("design_capacity"), IsSupported("is_supported"),

    LastChargeTime("last_charge_time"), ShowLastChargeTime("show_last_charge_time"), BatteryLevelWith("battery_level_with"),

    BatteryLevelTo("battery_level_to"), EnableService("enable_service"), AlwaysShowNotification("always_show_notification"),

    NotificationRefreshRate("notification_refresh_rate")
}