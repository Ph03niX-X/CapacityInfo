package com.ph03nix_x.capacityinfo

 object Seconds
{

    fun ToDays(seconds: Double): Int
    {
        var days = 0.0
        if(seconds >= 86400) {
            days = ((seconds / (Math.pow(60.0, 2.0))) / 24) % 24
        }
        return  days.toInt()
    }
    fun  ToHours(seconds: Double): Int
    {
        var days: Int = ToDays(seconds) * 24
        var hours = 0.0
        if(seconds >= 3600) {
            hours = ((seconds / 3600) - days) % 60
        }
        return  hours.toInt()
    }
    fun ToMinutes(seconds: Double): Int
    {
        var hours: Int = ToHours(seconds) * 60
        var minutes = 0.0
        if(seconds >= 60) {
            minutes = ((seconds / 60) - hours) % 60
        }
        return minutes.toInt()
    }
    fun  ToSeconds(seconds: Double): Int
    {
        var minutes: Int = ToMinutes(seconds) * 60
        var hours: Int = ToHours(seconds) * 3600
        var seconds_: Double = (seconds - minutes - hours) % 60
        return  seconds_.toInt()
    }
}