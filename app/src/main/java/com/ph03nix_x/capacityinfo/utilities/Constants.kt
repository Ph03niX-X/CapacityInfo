package com.ph03nix_x.capacityinfo.utilities

import kotlin.time.Duration.Companion.hours

object Constants {

    const val IMPORT_RESTORE_SETTINGS_EXTRA = "import_settings"
    const val GOOGLE_PLAY_APP_LINK = "https://play.google.com/store/apps/details?id=com.ph03nix_x.capacityinfo"
    const val GITHUB_LINK = "https://github.com/Ph03niX-X/CapacityInfo"
    const val TELEGRAM_DEVELOPER_LINK = "https://t.me/Ph03niX_X"
    const val DONT_KILL_MY_APP_LINK = "https://dontkillmyapp.com"
    const val GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending"
    const val SERVICE_CHANNEL_ID = "service_channel"
    const val OVERHEAT_OVERCOOL_CHANNEL_ID = "overheat_overcool"
    const val FULLY_CHARGED_CHANNEL_ID = "fully_charged_channel"
    const val CHARGED_CHANNEL_ID = "charged_channel"
    const val DISCHARGED_CHANNEL_ID = "discharged_channel"
    const val CHARGING_CURRENT_ID = "charging_current"
    const val DISCHARGE_CURRENT_ID = "discharge_current"
    const val ENABLED_DEBUG_OPTIONS_HOST = "243243622533" // CIDBGENABLED
    const val DISABLED_DEBUG_OPTIONS_HOST = "2432434722533" // CIDBGDISABLED
    const val NUMBER_OF_CYCLES_PATH = "/sys/class/power_supply/battery/cycle_count"
    const val EXPORT_SETTINGS_REQUEST_CODE = 0
    const val IMPORT_SETTINGS_REQUEST_CODE = 1
    const val OPEN_APP_REQUEST_CODE = 0
    const val CLOSE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE = 0
    const val DISABLE_NOTIFICATION_BATTERY_STATUS_INFORMATION_REQUEST_CODE = 1
    const val POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE = 2
    const val IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID = 0
    const val CHECK_PREMIUM_JOB_ID = 1
    const val HISTORY_COUNT_MAX = 1500
    const val EXPORT_HISTORY_REQUEST_CODE = 2
    const val IMPORT_HISTORY_REQUEST_CODE = 3
    const val EXPORT_NOTIFICATION_SOUNDS_REQUEST_CODE = 0
    const val STOP_SERVICE_REQUEST_CODE = 1
    const val NOMINAL_BATTERY_VOLTAGE = 3.87
    const val CHARGING_VOLTAGE_WATT = 5.0

    val CHECK_PREMIUM_JOB_SERVICE_PERIODIC = 4.hours.inWholeMilliseconds
}