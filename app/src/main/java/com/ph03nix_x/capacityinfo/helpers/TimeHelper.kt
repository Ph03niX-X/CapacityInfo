package com.ph03nix_x.capacityinfo.helpers

object TimeHelper {

    fun getTime(seconds: Long) =
        "${getHours(seconds)}:${getMinutes(seconds)}:${getSeconds(seconds)}"

    private fun getHours(seconds: Long): String {

        return when(val hours = seconds / 3600) {

            in 0..9 -> "0$hours"
            else -> "$hours"
        }
    }

    private fun getMinutes(seconds: Long): String {

        return when(val minutes = (seconds % 3600) / 60) {

            in 0..9 -> "0$minutes"
            else -> "$minutes"
        }
    }

    private fun getSeconds(seconds: Long): String {

        return when(val resultSeconds = seconds % 60) {

            in 0..9 -> "0$resultSeconds"
            else -> "$resultSeconds"
        }
    }
}