package com.ph03nix_x.capacityinfo.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.adapters.HistoryAdapter
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys

class HistoryFragment : Fragment(R.layout.history_fragment) {

    private lateinit var pref: SharedPreferences
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var recView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(PreferencesKeys.LANGUAGE,
            null) ?: MainApp.defLang)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyDB = HistoryDB(requireContext())

        recView = view.findViewById(R.id.history_recycler_view)
        historyAdapter = HistoryAdapter(historyDB.readDB())
        historyAdapter.setHasStableIds(true)
        recView.adapter = historyAdapter
    }
}