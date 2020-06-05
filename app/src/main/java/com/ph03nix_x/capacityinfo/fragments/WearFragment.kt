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
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_FONT
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.TEXT_STYLE
import com.ph03nix_x.capacityinfo.utils.Utils.batteryIntent
import kotlinx.coroutines.*
import java.text.DecimalFormat

class WearFragment : Fragment(), SettingsInterface, BatteryInfoInterface {
    
    private lateinit var pref: SharedPreferences

    private lateinit var designCapacity: AppCompatTextView
    private lateinit var numberOfCharges: AppCompatTextView
    private lateinit var numberOfCycles: AppCompatTextView
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

        return inflater.inflate(R.layout.wear_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        designCapacity = view.findViewById(R.id.design_capacity)
        numberOfCharges = view.findViewById(R.id.number_of_charges)
        numberOfCycles = view.findViewById(R.id.number_of_cycles)
        currentCapacity = view.findViewById(R.id.current_capacity_wear)
        capacityAdded = view.findViewById(R.id.capacity_added_wear)
        technology = view.findViewById(R.id.battery_technology)
        batteryHealth = view.findViewById(R.id.battery_health)
        residualCapacity = view.findViewById(R.id.residual_capacity)
        batteryWear = view.findViewById(R.id.battery_wear)

        updateTextAppearance()

        designCapacity.setOnClickListener {

            onChangeDesignCapacity(it.context)

            (it as? AppCompatTextView)?.text = it.context.getString(R.string.design_capacity,
                pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                    R.integer.min_design_capacity)).toString())
        }
    }

    override fun onResume() {

        super.onResume()

        designCapacity.text = getString(R.string.design_capacity, pref.getInt(DESIGN_CAPACITY,
            resources.getInteger(R.integer.min_design_capacity)).toString())

        batteryHealth.text = getString(R.string.battery_health, onGetBatteryHealth(
            context ?: batteryHealth.context))

        residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        batteryWear.text = getString(R.string.battery_wear, "0%", "0")

        batteryIntent = requireContext().registerReceiver(null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED))

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
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfCharges,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), numberOfCycles,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), currentCapacity,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), capacityAdded,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), technology,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), batteryHealth,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), residualCapacity,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), batteryWear,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
    }

    private fun wearInformationJob() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    val status = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager
                        .BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = batteryIntent?.getIntExtra(
                        BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                    withContext(Dispatchers.Main) {

                        designCapacity.text = getString(R.string.design_capacity, pref.getInt(
                            DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity))
                            .toString())

                        numberOfCharges.text = getString(R.string.number_of_charges,
                            pref.getLong(PreferencesKeys.NUMBER_OF_CHARGES, 0))

                        numberOfCycles.text = getString(R.string.number_of_cycles,
                            DecimalFormat("#.##")
                                .format(pref.getFloat(PreferencesKeys.NUMBER_OF_CYCLES, 0f)))
                    }

                    withContext(Dispatchers.Main) {

                        batteryHealth.text = getString(R.string.battery_health,
                            onGetBatteryHealth(context ?: batteryHealth.context))
                    }

                    if(pref.getBoolean(PreferencesKeys.IS_SUPPORTED, true)) {

                        if(pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                                R.integer.min_design_capacity)) >= resources.getInteger(
                                R.integer.min_design_capacity)
                            && pref.getInt(PreferencesKeys.RESIDUAL_CAPACITY, 0) > 0) {

                            withContext(Dispatchers.Main) {

                                residualCapacity.text =  onGetResidualCapacity(
                                    context ?: residualCapacity.context,
                                    batteryIntent?.getIntExtra(
                                        BatteryManager.EXTRA_STATUS, BatteryManager
                                            .BATTERY_STATUS_UNKNOWN) == BatteryManager
                                        .BATTERY_STATUS_CHARGING)

                                batteryWear.text = onGetBatteryWear(
                                    context ?: batteryWear.context)

                            }
                        }

                        if(onGetCurrentCapacity(context ?: currentCapacity.context) > 0) {

                            if(currentCapacity.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    currentCapacity.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                currentCapacity.text = getString(R.string.current_capacity,
                                    DecimalFormat("#.#").format(onGetCurrentCapacity(
                                        context ?: currentCapacity.context)))

                                when {
                                    onGetSourceOfPower(context ?: capacityAdded.context,
                                        sourceOfPower) != "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = onGetCapacityAdded(
                                            context ?: capacityAdded.context)
                                    }
                                    onGetSourceOfPower(context ?: capacityAdded.context,
                                        sourceOfPower) == "N/A" -> {

                                        if(capacityAdded.visibility == View.GONE)
                                            capacityAdded.visibility = View.VISIBLE

                                        capacityAdded.text = onGetCapacityAdded(
                                            context ?: capacityAdded.context)
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

                            residualCapacity.text = getString(R.string
                                .residual_capacity_not_supported)
                            batteryWear.text = getString(R.string.battery_wear_not_supported)
                        }

                        if(pref.contains(PreferencesKeys.CAPACITY_ADDED)) pref.edit().remove(
                            PreferencesKeys.CAPACITY_ADDED).apply()

                        if(pref.contains(PreferencesKeys.PERCENT_ADDED)) pref.edit().remove(
                            PreferencesKeys.PERCENT_ADDED).apply()
                    }

                    withContext(Dispatchers.Main) {

                        technology.text = getString(R.string.battery_technology,
                            batteryIntent?.getStringExtra(
                                BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if(onGetCurrentCapacity(
                                    context ?: currentCapacity.context) > 0L) 986L
                            else 993L)

                        else -> delay(3000L)
                    }
                }
            }
    }
}