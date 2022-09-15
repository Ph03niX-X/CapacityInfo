package com.ph03nix_x.capacityinfo.fragments

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import kotlinx.android.synthetic.main.history_fragment.*

class HistoryFragment : Fragment(R.layout.history_fragment) {

    private lateinit var pref: SharedPreferences
    private lateinit var historyAdapter: HistoryAdapter
    lateinit var recView: RecyclerView
    lateinit var refreshEmptyHistory: SwipeRefreshLayout
    lateinit var refreshHistory: SwipeRefreshLayout
    lateinit var emptyHistoryLayout: RelativeLayout

    companion object {

        var instance: HistoryFragment? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            LocaleHelper.setLocale(requireContext(), pref.getString(PreferencesKeys.LANGUAGE,
                null) ?: MainApp.defLang)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instance = this

        recView = view.findViewById(R.id.history_recycler_view)
        refreshEmptyHistory = view.findViewById(R.id.refresh_empty_history)
        refreshHistory = view.findViewById(R.id.refresh_history)
        emptyHistoryLayout = view.findViewById(R.id.empty_history_layout)

        val historyDB = HistoryDB(requireContext())

        if(historyDB.getCount() > 0) {
            emptyHistoryLayout.visibility = View.GONE
            refresh_empty_history.visibility = View.GONE
            recView.visibility = View.VISIBLE
            historyAdapter = HistoryAdapter(historyDB.readDB())
            recView.adapter = historyAdapter

        }
        else {
            recView.visibility = View.GONE
            refresh_empty_history.visibility = View.VISIBLE
            emptyHistoryLayout.visibility = View.VISIBLE
        }

        refreshEmptyHistory()

        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
            historyAdapter.update(requireContext())
            refresh_empty_history.visibility = View.GONE
            emptyHistoryLayout.visibility = View.GONE
            recView.visibility = View.VISIBLE
        }
        else {
            recView.visibility = View.GONE
            refresh_empty_history.visibility = View.VISIBLE
            emptyHistoryLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        instance = null
        HistoryAdapter.instance = null
        super.onDestroy()
    }

    private fun refreshEmptyHistory() {
        refreshEmptyHistory.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress))
            setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress_background))
            setOnRefreshListener {
                isRefreshing = true
                if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
                    historyAdapter.update(requireContext())
                    visibility = View.GONE
                    refresh_history.visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.GONE
                    recView.visibility = View.VISIBLE
                }
                else {
                    recView.visibility = View.GONE
                    refresh_history.visibility = View.GONE
                    visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.VISIBLE
                }
                isRefreshing = false
            }
        }
    }

    private fun refreshHistory() {
        refreshHistory.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress))
            setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress_background))
            setOnRefreshListener {
                isRefreshing = true
                if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
                    historyAdapter.update(requireContext())
                    refresh_empty_history.visibility = View.GONE
                    visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.GONE
                    recView.visibility = View.VISIBLE
                }
                else {
                    recView.visibility = View.GONE
                    visibility = View.GONE
                    refresh_empty_history.visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.VISIBLE
                }
                isRefreshing = false
            }
        }
    }
}