package com.ph03nix_x.capacityinfo

object TimeSpan {

    fun toHours(seconds: Double) = if(seconds >= 3600) ((seconds / 3600) % 60).toInt() else 0

    fun toMinutes(seconds: Double) = if(seconds >= 60) (((seconds / 60) - (toHours(seconds) * 60)) % 60).toInt() else 0

    fun  ToSeconds(seconds: Double) = ((seconds - (toMinutes(seconds) * 60) - (toHours(seconds) * 3600)) % 60).toInt()
}