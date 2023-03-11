package com.ph03nix_x.capacityinfo.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
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
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.databinding.HistoryFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper

class HistoryFragment : Fragment(R.layout.history_fragment) {

    private lateinit var binding: HistoryFragmentBinding

    private lateinit var pref: SharedPreferences
    private lateinit var historyAdapter: HistoryAdapter
    lateinit var recView: RecyclerView
    lateinit var refreshEmptyHistory: SwipeRefreshLayout
    lateinit var refreshHistory: SwipeRefreshLayout
    lateinit var emptyHistoryLayout: RelativeLayout

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: HistoryFragment? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = HistoryFragmentBinding.inflate(inflater, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        return binding.root.rootView
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
            binding.refreshEmptyHistory.visibility = View.GONE
            recView.visibility = View.VISIBLE
            historyAdapter = HistoryAdapter(historyDB.readDB())
            recView.adapter = historyAdapter

        }
        else {
            recView.visibility = View.GONE
            binding.refreshEmptyHistory.visibility = View.VISIBLE
            emptyHistoryLayout.visibility = View.VISIBLE
        }

        refreshEmptyHistory()

        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
            historyAdapter.update(requireContext())
            binding.refreshEmptyHistory.visibility = View.GONE
            emptyHistoryLayout.visibility = View.GONE
            recView.visibility = View.VISIBLE
        }
        else {
            recView.visibility = View.GONE
            binding.refreshEmptyHistory.visibility = View.VISIBLE
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
                    binding.refreshEmptyHistory.visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.GONE
                    recView.visibility = View.VISIBLE
                }
                else {
                    recView.visibility = View.GONE
                    binding.refreshEmptyHistory.visibility = View.GONE
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
                    binding.refreshEmptyHistory.visibility = View.GONE
                    visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.GONE
                    recView.visibility = View.VISIBLE
                }
                else {
                    recView.visibility = View.GONE
                    visibility = View.GONE
                    binding.refreshEmptyHistory.visibility = View.VISIBLE
                    emptyHistoryLayout.visibility = View.VISIBLE
                }
                isRefreshing = false
            }
        }
    }
}