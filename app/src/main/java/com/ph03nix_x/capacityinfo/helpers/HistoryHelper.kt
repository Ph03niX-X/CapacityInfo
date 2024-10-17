package com.ph03nix_x.capacityinfo.helpers

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.History
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.Constants

object HistoryHelper {

    fun addHistory(context: Context, date: String, residualCapacity: Int) {

        val historyDB = HistoryDB(context)
        val history = History(date = date, residualCapacity = residualCapacity)
        removeFirstRowIfHistoryMax(context)
        historyDB.insertData(history)
    }

    private fun removeFirstRowIfHistoryMax(context: Context) {
        if(isHistoryMax(context)) HistoryDB(context).removeFirstRow()
    }

    fun remove(context: Context, residualCapacity: Int) = HistoryDB(context).remove(residualCapacity)

    fun clearHistory(context: Context) = HistoryDB(context).clear()

    fun getHistoryCount(context: Context) = HistoryDB(context).getCount()

    fun isHistoryMax(context: Context) = getHistoryCount(context).toInt() == Constants.HISTORY_COUNT_MAX

    fun isHistoryEmpty(context: Context) = getHistoryCount(context) < 1

    fun isHistoryNotEmpty(context: Context) = !isHistoryEmpty(context)

    fun clearHistory(context: Context, clearHistoryToolbarMenu: MenuItem) {
        if(HistoryAdapter.instance?.getHistoryList().isNullOrEmpty())
            Toast.makeText(context, context.getString(R.string.error_clearing_history),
                Toast.LENGTH_LONG).show()
        else if(isHistoryNotEmpty(context))
            MaterialAlertDialogBuilder(context).apply {
                setMessage(context.getString(R.string.clear_the_history_dialog_message))
                setPositiveButton(context.getString(android.R.string.ok)) { _, _ ->
                    try {
                        clearHistory(context)
                        val isHistoryNotEmpty = isHistoryNotEmpty(context)
                        clearHistoryToolbarMenu.isVisible = isHistoryNotEmpty
                        if(!isHistoryNotEmpty) {
                            HistoryFragment.instance?.binding?.apply {
                                historyRecyclerView.visibility = View.GONE
                                refreshHistory.visibility = View.GONE
                                refreshEmptyHistory.visibility = View.VISIBLE
                                emptyHistoryLayout.visibility = View.VISIBLE
                                emptyHistoryText.text =
                                    context.resources.getText(R.string.empty_history_text)
                            }
                            Toast.makeText(context, context.getString(
                                R.string.history_cleared_successfully), Toast.LENGTH_LONG).show()
                        }
                        else {
                            HistoryFragment.instance?.binding?.apply {
                                refreshEmptyHistory.visibility = View.GONE
                                refreshHistory.visibility = View.VISIBLE
                                emptyHistoryLayout.visibility = View.GONE
                                historyRecyclerView.visibility = View.VISIBLE
                            }
                            HistoryAdapter.instance?.update(context)
                            Toast.makeText(context, context.getString(R.string
                                .error_clearing_history), Toast.LENGTH_LONG).show()
                        }
                    }
                    catch (e: Exception) {
                        Toast.makeText(context, "${context.getString(R.string
                            .error_clearing_history)}\n${e.message ?: e.toString()}",
                            Toast.LENGTH_LONG).show()
                    }
                    finally {
                        MainActivity.instance?.toolbar?.title = context.getString(
                            if(PremiumInterface.isPremium && isHistoryNotEmpty(context))
                                R.string.history_title else R.string.history,
                            getHistoryCount(context), Constants.HISTORY_COUNT_MAX)
                    }
                }
                setNegativeButton(context.getString(android.R.string.cancel)) { d, _ -> d.dismiss() }
                show()
            }

        else Toast.makeText(context, context.getString(R.string.error_clearing_history),
            Toast.LENGTH_LONG).show()
    }
}