package com.ph03nix_x.capacityinfo.helpers

object ChargingTimeHelper {

    fun getHours(seconds: Long): String {

        return when(val hours = seconds / 3600) {

            in 0..9 -> "0$hours"
            else -> "$hours"
        }
    }

    fun getMinutes(seconds: Long): String {

        return when(val minutes = (seconds % 3600) / 60) {

            in 0..9 -> "0$minutes"
            else -> "$minutes"
        }
    }

    fun getSeconds(seconds: Long): String {

        return when(val resultSeconds = seconds % 60) {

            in 0..9 -> "0$resultSeconds"
            else -> "$resultSeconds"
        }
    }
}