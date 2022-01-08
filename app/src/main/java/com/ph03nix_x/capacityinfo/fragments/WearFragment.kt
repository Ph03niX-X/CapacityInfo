package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.utilities.Constants.NUMBER_OF_CYCLES_PATH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLE_FAKE_BATTERY_WEAR
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import kotlinx.coroutines.*
import java.io.*
import java.text.DecimalFormat

class WearFragment : Fragment(R.layout.wear_fragment), SettingsInterface, BatteryInfoInterface {
    
    private lateinit var pref: SharedPreferences

    private lateinit var designCapacity: AppCompatTextView
    private lateinit var numberOfCharges: AppCompatTextView
    private lateinit var numberOfFullCharges: AppCompatTextView
    private lateinit var numberOfCycles: AppCompatTextView
    private lateinit var numberOfCyclesAndroid: AppCompatTextView
    private lateinit var currentCapacity: AppCompatTextView
    private lateinit var batteryHealth: AppCompatTextView
    private lateinit var residualCapacity: AppCompatTextView
    private lateinit var capacityAdded: AppCompatTextView
    private lateinit var technology: AppCompatTextView
    private lateinit var batteryWear: AppCompatTextView
    private var isJob = false
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(PreferencesKeys.LANGUAGE,
            null) ?: MainApp.defLang
        )

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        designCapacity = view.findViewById(R.id.design_capacity)
        numberOfCharges = view.findViewById(R.id.number_of_charges)
        numberOfFullCharges = view.findViewById(R.id.number_of_full_charges)
        numberOfCycles = view.findViewById(R.id.number_of_cycles)
        numberOfCyclesAndroid = view.findViewById(R.id.number_of_cycles_android)
        currentCapacity = view.findViewById(R.id.current_capacity_wear)
        capacityAdded = view.findViewById(R.id.capacity_added_wear)
        technology = view.findViewById(R.id.battery_technology)
        batteryHealth = view.findViewById(R.id.battery_health)
        residualCapacity = view.findViewById(R.id.residual_capacity)
        batteryWear = view.findViewById(R.id.battery_wear)

        updateTextAppearance()

        designCapacity.setOnClickListener {

            onChangeDesignCapacity(it.context)

            (it as? AppCompatTextView)?.text = it.context.getString(
                R.string.design_capacity,
                pref.getInt(
                    DESIGN_CAPACITY, resources.getInteger(
                        R.integer.min_design_capacity
                    )
                ).toString()
            )
        }
    }

    override fun onResume() {

        super.onResume()

        designCapacity.text = getString(
            R.string.design_capacity, pref.getInt(
                DESIGN_CAPACITY,
                resources.getInteger(R.integer.min_design_capacity)
            ).toString()
        )

        numberOfCyclesAndroid.visibility = if(File(NUMBER_OF_CYCLES_PATH).exists()) View.VISIBLE
        else View.GONE

        batteryHealth.text = getString(R.string.battery_health, getOnBatteryHealth(requireContext()))

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%", "0")

        batteryIntent = requireContext().registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        isJob = true

        wearInformationJob()
    }

    override fun onStop() {

        super.onStop()

        isJob = false
        job?.cancel()
        job = null
    }

    override fun onDestroy() {

        isJob = false
        job?.cancel()
        job = null

        super.onDestroy()
    }

    private fun updateTextAppearance() {

        TextAppearanceHelper.setTextAppearance(requireContext(), designCapacity,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfCharges,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfFullCharges,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfCycles,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfCyclesAndroid,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), currentCapacity,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), capacityAdded,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), technology,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), batteryHealth,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), residualCapacity,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), batteryWear,
            pref.getString(TEXT_STYLE, "0"), pref.getString(TEXT_SIZE, "2"))
    }

    private suspend fun getNumberOfCyclesAndroid(): Int {

        if(!File(NUMBER_OF_CYCLES_PATH).exists()) return 0

        val cycleCount = File(NUMBER_OF_CYCLES_PATH).absolutePath

        var numberOfCycles = 0

        withContext(Dispatchers.IO) {

            try {
                var br: BufferedReader? = null

                kotlin.runCatching {
                    br = try {

                        BufferedReader(FileReader(cycleCount))
                    }
                    catch (e: FileNotFoundException) { null }
                }

                kotlin.runCatching { numberOfCycles = br?.readLine()?.toInt() ?: 0 }

                kotlin.runCatching { br?.close() }


            } catch (e: IOException) { numberOfCycles = 0 }
        }

        return numberOfCycles
    }

    private fun wearInformationJob() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    val status = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN
                    ) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1
                    ) ?: -1

                    withContext(Dispatchers.Main) {

                        designCapacity.text = getString(
                            R.string.design_capacity, pref.getInt(
                                DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)
                            )
                                .toString()
                        )

                        numberOfCharges.text = getString(R.string.number_of_charges,
                            pref.getLong(PreferencesKeys.NUMBER_OF_CHARGES, 0))

                        numberOfFullCharges.text = getString(R.string.number_of_full_charges,
                            pref.getLong(NUMBER_OF_FULL_CHARGES, 0))

                        numberOfCycles.text = getString(
                            R.string.number_of_cycles,
                            DecimalFormat("#.##")
                                .format(pref.getFloat(PreferencesKeys.NUMBER_OF_CYCLES, 0f))
                        )

                        numberOfCyclesAndroid.apply {

                            if(visibility == View.VISIBLE) text = getString(R.string
                                .number_of_cycles_android, getNumberOfCyclesAndroid())
                        }
                    }

                    withContext(Dispatchers.Main) {

                        batteryHealth.text = getString(R.string.battery_health,
                            getOnBatteryHealth(requireContext()))
                    }

                    if(pref.getBoolean(PreferencesKeys.IS_SUPPORTED,
                            requireContext().resources.getBoolean(R.bool.is_supported))
                        || getOnCurrentCapacity(requireContext()) >= 0.0) {

                        if(pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                                R.integer.min_design_capacity)) >= resources.getInteger(
                                R.integer.min_design_capacity)
                            && pref.getInt(PreferencesKeys.RESIDUAL_CAPACITY, 0) > 0 ||
                            pref.getBoolean(IS_ENABLE_FAKE_BATTERY_WEAR, resources.getBoolean(
                                R.bool.is_enable_fake_battery_wear))) {

                            withContext(Dispatchers.Main) {

                                residualCapacity.text = getOnResidualCapacity(requireContext())

                                batteryWear.text = getOnBatteryWear(requireContext())

                            }
                        }

                        if(getOnCurrentCapacity(requireContext()) > 0.0) {

                            if(currentCapacity.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    currentCapacity.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(
                                        getOnCurrentCapacity(requireContext())))

                                when {
                                    getOnSourceOfPower(requireContext(), sourceOfPower) != "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = getOnCapacityAdded(requireContext())
                                    }
                                    getOnSourceOfPower(requireContext(), sourceOfPower) == "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = getOnCapacityAdded(requireContext())
                                    }
                                    capacityAdded.visibility == View.VISIBLE ->
                                        capacityAdded.visibility = View.GONE
                                }
                            }
                        }

                        else {

                            if(currentCapacity.visibility == View.VISIBLE)
                                withContext(Dispatchers.Main) {
                                    currentCapacity.visibility = View.GONE }

                            if(capacityAdded.visibility == View.GONE
                                && pref.getFloat(PreferencesKeys.CAPACITY_ADDED, 0f) > 0f)
                                withContext(Dispatchers.Main) {
                                    capacityAdded.visibility = View.VISIBLE }

                            else withContext(Dispatchers.Main) {
                                capacityAdded.visibility = View.GONE }
                        }
                    }

                    else {

                        if(currentCapacity.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) {
                                currentCapacity.visibility = View.GONE }

                        if(capacityAdded.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) { capacityAdded.visibility = View.GONE }

                        withContext(Dispatchers.Main) {

                            residualCapacity.text = getString(
                                R.string
                                    .residual_capacity_not_supported
                            )
                            batteryWear.text = getString(R.string.battery_wear_not_supported)
                        }

                        if(pref.contains(PreferencesKeys.CAPACITY_ADDED)) pref.edit().remove(
                            PreferencesKeys.CAPACITY_ADDED
                        ).apply()

                        if(pref.contains(PreferencesKeys.PERCENT_ADDED)) pref.edit().remove(
                            PreferencesKeys.PERCENT_ADDED
                        ).apply()
                    }

                    withContext(Dispatchers.Main) {

                        technology.text = getString(
                            R.string.battery_technology,
                            batteryIntent?.getStringExtra(
                                BatteryManager.EXTRA_TECHNOLOGY
                            ) ?: getString(R.string.unknown)
                        )
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if (getOnCurrentCapacity(requireContext()) > 0.0) 991L
                            else 998L)

                        else -> delay(1500L)
                    }
                }
            }
    }
}