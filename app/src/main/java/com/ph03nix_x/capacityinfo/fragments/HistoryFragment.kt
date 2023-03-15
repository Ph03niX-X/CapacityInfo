package com.ph03nix_x.capacityinfo.fragments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.databinding.HistoryFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper

class HistoryFragment : Fragment(R.layout.history_fragment) {

    private lateinit var pref: SharedPreferences
    private lateinit var historyAdapter: HistoryAdapter

    var binding: HistoryFragmentBinding? = null

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: HistoryFragment? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = HistoryFragmentBinding.inflate(inflater, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        return binding?.root?.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        instance = this

        val historyDB = HistoryDB(requireContext())

        if(historyDB.getCount() > 0) {
            binding?.emptyHistoryLayout?.visibility = View.GONE
            binding?.refreshEmptyHistory?.visibility = View.GONE
            binding?.historyRecyclerView?.visibility = View.VISIBLE
            historyAdapter = HistoryAdapter(historyDB.readDB())
            binding?.historyRecyclerView?.adapter = historyAdapter

        }
        else {
            binding?.historyRecyclerView?.visibility = View.GONE
            binding?.refreshEmptyHistory?.visibility = View.VISIBLE
            binding?.emptyHistoryLayout?.visibility = View.VISIBLE
        }

        refreshEmptyHistory()

        refreshHistory()
    }

    override fun onResume() {
        super.onResume()
        if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
            historyAdapter.update(requireContext())
            binding?.refreshEmptyHistory?.visibility = View.GONE
            binding?.emptyHistoryLayout?.visibility = View.GONE
            binding?.historyRecyclerView?.visibility = View.VISIBLE
        }
        else {
            binding?.historyRecyclerView?.visibility = View.GONE
            binding?.refreshEmptyHistory?.visibility = View.VISIBLE
            binding?.emptyHistoryLayout?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        instance = null
        HistoryAdapter.instance = null
        super.onDestroy()
    }

    private fun refreshEmptyHistory() {
        binding?.refreshEmptyHistory?.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress))
            setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress_background))
            setOnRefreshListener {
                isRefreshing = true
                if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
                    historyAdapter.update(requireContext())
                    visibility = View.GONE
                    binding?.refreshEmptyHistory?.visibility = View.VISIBLE
                    binding?.emptyHistoryLayout?.visibility = View.GONE
                    binding?.historyRecyclerView?.visibility = View.VISIBLE
                }
                else {
                    binding?.historyRecyclerView?.visibility = View.GONE
                    binding?.refreshEmptyHistory?.visibility = View.GONE
                    visibility = View.VISIBLE
                    binding?.emptyHistoryLayout?.visibility = View.VISIBLE
                }
                isRefreshing = false
            }
        }
    }

    private fun refreshHistory() {
        binding?.refreshHistory?.apply {
            setColorSchemeColors(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress))
            setProgressBackgroundColorSchemeColor(ContextCompat.getColor(requireContext(),
                R.color.swipe_refresh_layout_progress_background))
            setOnRefreshListener {
                isRefreshing = true
                if(HistoryHelper.getHistoryCount(requireContext()) > 0) {
                    historyAdapter.update(requireContext())
                    binding?.refreshEmptyHistory?.visibility = View.GONE
                    visibility = View.VISIBLE
                    binding?.emptyHistoryLayout?.visibility = View.GONE
                    binding?.historyRecyclerView?.visibility = View.VISIBLE
                }
                else {
                    binding?.historyRecyclerView?.visibility = View.GONE
                    visibility = View.GONE
                    binding?.refreshEmptyHistory?.visibility = View.VISIBLE
                    binding?.emptyHistoryLayout?.visibility = View.VISIBLE
                }
                isRefreshing = false
            }
        }
    }
}