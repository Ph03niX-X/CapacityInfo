package com.ph03nix_x.capacityinfo.fragments

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.*
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.databases.HistoryDB
import com.ph03nix_x.capacityinfo.helpers.DateHelper
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import kotlinx.coroutines.*

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface {

    private lateinit var pref: SharedPreferences
    
    private var forciblyShowRateTheApp: SwitchPreferenceCompat? = null
    private var addSetting: Preference? = null
    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null
    private var addHistory: Preference? = null
    private var addTenHistory: Preference? = null
    private var addFiftyHistory: Preference? = null
    private var clearHistory: Preference? = null
    private var exportHistory: Preference? = null
    private var startCapacityInfoService: Preference? = null
    private var stopCapacityInfoService: Preference? = null
    private var restartCapacityInfoService: Preference? = null
    private var stopOverlayService: Preference? = null
    private var restartOverlayService: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.debug_settings)

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        addSetting = findPreference("add_setting")

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        addHistory = findPreference("add_history")

        addTenHistory = findPreference("add_ten_history")

        addFiftyHistory = findPreference("add_fifty_history")

        clearHistory = findPreference("clear_history")

        exportHistory = findPreference("export_history")

        startCapacityInfoService = findPreference("start_capacity_info_service")

        stopCapacityInfoService = findPreference("stop_capacity_info_service")

        restartCapacityInfoService = findPreference("restart_capacity_info_service")

        stopOverlayService = findPreference("stop_overlay_service")

        restartOverlayService = findPreference("restart_overlay_service")

        forciblyShowRateTheApp?.isVisible = !isGooglePlay(requireContext())

        addHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        addTenHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        addFiftyHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        clearHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0

        exportHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0

        addHistory?.setOnPreferenceClickListener {

            it.isEnabled = false
            addTenHistory?.isEnabled = false
            addFiftyHistory?.isEnabled = false
            clearHistory?.isEnabled = false
            exportHistory?.isEnabled = false

            val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                R.integer.min_design_capacity))
            val date =  DateHelper.getDate((1..31).random(), (1..12).random(),
                DateHelper.getCurrentYear())
            val residualCapacity = if(pref.getString(PreferencesKeys
                    .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") ((
                    designCapacity * 0.01).toInt() * 1000..(designCapacity + (
                    (designCapacity / 1000) * 5)) * 1000).random()
            else ((designCapacity * 0.01).toInt() * 100..(designCapacity + (
                    (designCapacity / 1000) * 5)) * 100).random()

            HistoryHelper.addHistory(requireContext(), date, residualCapacity)
            clearHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0
            exportHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0

            val historyDB = HistoryDB(requireContext()).readDB()

            if(historyDB.count() > 0 && historyDB[historyDB.size - 1].date == date)
                Toast.makeText(requireContext(), "$date: $residualCapacity",
                    Toast.LENGTH_LONG).show()
            else Toast.makeText(requireContext(), "0.0.0: 0", Toast.LENGTH_LONG).show()

            it.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
            addTenHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
            addFiftyHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

            true
        }

        addTenHistory?.setOnPreferenceClickListener {

            addHistory?.isEnabled = false
            it.isEnabled = false
            addFiftyHistory?.isEnabled = false
            clearHistory?.isEnabled = false
            exportHistory?.isEnabled = false

            CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

                for(i in 1..10) {

                    if(HistoryHelper.isHistoryMax(requireContext())) break

                    val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                        R.integer.min_design_capacity))
                    val date =  DateHelper.getDate((1..31).random(), (1..12).random(),
                        DateHelper.getCurrentYear())
                    val residualCapacity = if(pref.getString(PreferencesKeys
                            .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") ((
                            designCapacity * 0.01).toInt() * 1000..(designCapacity + (
                            (designCapacity / 1000) * 5)) * 1000).random()
                    else ((designCapacity * 0.01).toInt() * 100..(designCapacity + (
                            (designCapacity / 1000) * 5)) * 100).random()

                    HistoryHelper.addHistory(requireContext(), date, residualCapacity)

                    withContext(Dispatchers.Main) {

                        clearHistory?.isEnabled = HistoryHelper.getHistoryCount(
                            requireContext()) > 0
                        exportHistory?.isEnabled = HistoryHelper.getHistoryCount(
                            requireContext()) > 0
                    }

                    val historyDB = HistoryDB(requireContext()).readDB()

                    withContext(Dispatchers.Main) {
                        if(i == 10 && historyDB.count() > 0 && historyDB[historyDB.size - 1]
                                .date == date)
                            Toast.makeText(requireContext(), "$date: $residualCapacity",
                                Toast.LENGTH_LONG).show()
                        else if(historyDB.count() == 0 || (historyDB.count() > 0 &&
                                    historyDB[historyDB.size - 1].date != date)) {
                            Toast.makeText(requireContext(), "$i: 0.0.0: 0",
                                Toast.LENGTH_LONG).show()
                            delay(3500)
                        }
                    }
                }

                withContext(Dispatchers.Main) {

                    addHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                    it.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                    addFiftyHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                }
            }

            true
        }

        addFiftyHistory?.setOnPreferenceClickListener {

            addHistory?.isEnabled = false
            addTenHistory?.isEnabled = false
            it.isEnabled = false
            clearHistory?.isEnabled = false
            exportHistory?.isEnabled = false

            CoroutineScope(Dispatchers.Default).launch(Dispatchers.IO) {

                for(i in 1..50) {

                    if(HistoryHelper.isHistoryMax(requireContext())) break

                    val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                        R.integer.min_design_capacity))
                    val date =  DateHelper.getDate((1..31).random(), (1..12).random(),
                        DateHelper.getCurrentYear())
                    val residualCapacity = if(pref.getString(PreferencesKeys
                            .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh") ((
                            designCapacity * 0.01).toInt() * 1000..(designCapacity + (
                            (designCapacity / 1000) * 5)) * 1000).random()
                    else ((designCapacity * 0.01).toInt() * 100..(designCapacity + (
                            (designCapacity / 1000) * 5)) * 100).random()

                    HistoryHelper.addHistory(requireContext(), date, residualCapacity)

                    withContext(Dispatchers.Main) {

                        clearHistory?.isEnabled = HistoryHelper.getHistoryCount(
                            requireContext()) > 0
                        exportHistory?.isEnabled = HistoryHelper.getHistoryCount(
                            requireContext()) > 0
                    }

                    val historyDB = HistoryDB(requireContext()).readDB()

                    withContext(Dispatchers.Main) {
                        if(i == 10 && historyDB.count() > 0 && historyDB[historyDB.size - 1]
                                .date == date)
                            Toast.makeText(requireContext(), "$date: $residualCapacity",
                                Toast.LENGTH_LONG).show()
                        else if(historyDB.count() == 0 || (historyDB.count() > 0 &&
                                    historyDB[historyDB.size - 1].date != date)) {
                            Toast.makeText(requireContext(), "$i: 0.0.0: 0",
                                Toast.LENGTH_LONG).show()
                            delay(3500)
                        }
                    }
                }

                withContext(Dispatchers.Main) {

                    addHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                    addTenHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                    it.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
                }
            }

            true
        }


        clearHistory?.setOnPreferenceClickListener {

            HistoryHelper.clearHistory(requireContext())
            addHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
            addTenHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
            addFiftyHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())
            it.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0
            exportHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0
            true
        }

        exportHistory?.setOnPreferenceClickListener {
            try {

                startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
                    Constants.EXPORT_HISTORY_REQUEST_CODE)
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(),e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
            .isStartedCapacityInfoService()

        stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        stopOverlayService?.isEnabled = OverlayService.instance != null

        restartOverlayService?.isEnabled = OverlayService.instance != null
        addSetting?.setOnPreferenceClickListener {

            addSettingDialog(requireContext(), pref)

            true
        }

        changeSetting?.setOnPreferenceClickListener {

            changeSettingDialog(requireContext(), pref)

            true
        }

        resetSetting?.setOnPreferenceClickListener {

            resetSettingDialog(requireContext(), pref)

            true
        }

        resetSettings?.setOnPreferenceClickListener {

            resetSettingsDialog(requireContext(), pref)

            true
        }

        startCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            ServiceHelper.startService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(3700L)
                it.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
                    .isStartedCapacityInfoService()

                stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

                restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        stopCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            restartCapacityInfoService?.isEnabled = false

            ServiceHelper.stopService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(2500L)
                startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null
                        && !ServiceHelper.isStartedCapacityInfoService()

                it.isEnabled = CapacityInfoService.instance != null

                restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        restartCapacityInfoService?.setOnPreferenceClickListener {

            it.isEnabled = false

            stopCapacityInfoService?.isEnabled = false

            ServiceHelper.restartService(requireContext(), CapacityInfoService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(6200L)
                startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null
                        && !ServiceHelper.isStartedCapacityInfoService()

                stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

                it.isEnabled = CapacityInfoService.instance != null
            }

            true
        }

        stopOverlayService?.setOnPreferenceClickListener {

            it.isEnabled = false

            restartOverlayService?.isEnabled = false

            ServiceHelper.stopService(requireContext(), OverlayService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(1500L)
                it.isEnabled = OverlayService.instance != null

                restartOverlayService?.isEnabled = OverlayService.instance != null
            }

            true
        }

        restartOverlayService?.setOnPreferenceClickListener {

            it.isEnabled = false

            stopOverlayService?.isEnabled = false

            ServiceHelper.restartService(requireContext(), OverlayService::class.java)

            CoroutineScope(Dispatchers.Main).launch {

                delay(4800L)
                stopOverlayService?.isEnabled = OverlayService.instance != null

                it.isEnabled = OverlayService.instance != null
            }

            true
        }

    }

    override fun onResume() {

        super.onResume()

        addHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        addTenHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        addFiftyHistory?.isEnabled = !HistoryHelper.isHistoryMax(requireContext())

        clearHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0

        exportHistory?.isEnabled = HistoryHelper.getHistoryCount(requireContext()) > 0

        startCapacityInfoService?.isEnabled = CapacityInfoService.instance == null && !ServiceHelper
            .isStartedCapacityInfoService()

        stopCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        restartCapacityInfoService?.isEnabled = CapacityInfoService.instance != null

        stopOverlayService?.isEnabled = OverlayService.instance != null

        restartOverlayService?.isEnabled = OverlayService.instance != null

        if(!pref.getBoolean(PreferencesKeys.IS_ENABLED_DEBUG_OPTIONS, resources.getBoolean(R.bool
                .is_enabled_debug_options))) requireActivity().onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {

            Constants.EXPORT_HISTORY_REQUEST_CODE ->
                if(resultCode == Activity.RESULT_OK) onExportHistory(requireContext(), data)
        }
    }
}