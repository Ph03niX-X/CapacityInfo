package com.ph03nix_x.capacityinfo.helpers

object TimeSpan {

    fun toHours(seconds: Int) = if(seconds >= 3600) ((seconds / 3600) % 60) else 0

    fun toMinutes(seconds: Int) = if(seconds >= 60) (((seconds / 60) - (toHours(seconds) * 60)) % 60) else 0

    fun toSeconds(seconds: Int) = ((seconds - (toMinutes(seconds) * 60) - (toHours(seconds) * 3600)) % 60)
}