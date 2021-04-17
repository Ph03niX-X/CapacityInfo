package com.ph03nix_x.capacityinfo.helpers

import java.util.*

object DateHelper {

    fun getDate(day: Int, month: Int, year: Int): String {

        return if(day > 0 && month > 0 && year > 0)
            "${getDay(day)}.${getMonth(month)}.${getYear(year)}"
        else "${getDay(getCurrentDay())}.${getMonth(getCurrentMonth())}.${getYear(getCurrentYear())}"
    }

    fun getCurrentDay() = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getCurrentMonth() = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun getCurrentYear() = Calendar.getInstance().get(Calendar.YEAR)

    private fun getDay(day: Int): String {

        return when(day) {

            in 0..9 -> "0$day"
            else -> "$day"
        }
    }

    private fun getMonth(month: Int): String {

        return when(month) {

            in 0..9 -> "0$month"
            else -> "$month"
        }
    }

    private fun getYear(year: Int): String {

        return when(year) {

            in 0..9 -> "0$year"
            else -> "$year"
        }
    }
}