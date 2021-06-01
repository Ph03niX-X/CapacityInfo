package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import com.ph03nix_x.capacityinfo.databases.History
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.utilities.Constants

object HistoryHelper {

    fun addHistory(context: Context, date: String, residualCapacity: Int) {

        val historyDB = HistoryDB(context)
        val historyList = historyDB.readDB()
        val history = History(date = date, residualCapacity = residualCapacity)
        if(historyList.count() == 0 || historyList[historyList.size - 1]
                .residualCapacity != residualCapacity) historyDB.insertData(history)
    }

    fun clearHistory(context: Context) = HistoryDB(context).clear()

    fun getHistoryCount(context: Context) = HistoryDB(context).readDB().count()

    fun autoClearHistory(context: Context) {

        if(isHistoryMax(context)) clearHistory(context)
    }

    fun isHistoryMax(context: Context) = getHistoryCount(context) >= Constants.HISTORY_COUNT_MAX

    fun isHistoryEmpty(context: Context) = getHistoryCount(context) < 1

    fun isHistoryNotEmpty(context: Context) = !isHistoryEmpty(context)
}