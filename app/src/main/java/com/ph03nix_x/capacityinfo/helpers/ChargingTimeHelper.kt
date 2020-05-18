package com.ph03nix_x.capacityinfo.helpers

import java.util.concurrent.TimeUnit

object ChargingTimeHelper {

    fun getHours(seconds: Long): String {

        return when(val hours = TimeUnit.SECONDS.toHours(seconds)) {

            in 0..9 -> "0$hours"
            else -> "$hours"
        }
    }

    fun getMinutes(seconds: Long): String {

        return when(val minutes = TimeUnit.MINUTES.toHours(TimeUnit.SECONDS.toMinutes(
            seconds))) {

            in 0..9 -> "0$minutes"
            else -> "$minutes"
        }
    }

    fun getSeconds(seconds: Long): String {

        val minutes = TimeUnit.MINUTES.toHours(TimeUnit.SECONDS.toMinutes(seconds))

        return when(val resultSeconds = ((seconds - (
                TimeUnit.HOURS.toMinutes(minutes))) % 60)) {

            in 0..9 -> "0$resultSeconds"
            else -> "$resultSeconds"
        }
    }
}