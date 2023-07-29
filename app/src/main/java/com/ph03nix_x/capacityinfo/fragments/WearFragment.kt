package com.ph03nix_x.capacityinfo.fragments

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.databinding.WearFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.utilities.Constants.NOMINAL_BATTERY_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.Constants.NUMBER_OF_CYCLES_PATH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import kotlinx.coroutines.*
import java.io.*
import java.text.DecimalFormat

class WearFragment : Fragment(R.layout.wear_fragment), SettingsInterface, BatteryInfoInterface,
    PremiumInterface {
    
    private lateinit var binding: WearFragmentBinding
    
    private lateinit var pref: SharedPreferences
    
    private var isJob = false
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        
        binding = WearFragmentBinding.inflate(inflater, container, false)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        return binding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        updateTextAppearance()

        binding.designCapacity.setOnClickListener {

            onChangeDesignCapacity()

            (it as? AppCompatTextView)?.text = getDesignCapacity()
        }
    }

    override fun onResume() {

        super.onResume()

        binding.designCapacity.text = getDesignCapacity()

        binding.numberOfCyclesAndroid.visibility =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                || File(NUMBER_OF_CYCLES_PATH).exists()) View.VISIBLE else View.GONE

        binding.batteryHealth.apply {

            if(getBatteryHealth(requireContext()) != null) {
                visibility = View.VISIBLE
                text = getString(R.string.battery_health, getString(
                    getBatteryHealth(requireContext()) ?: R.string.battery_health_great))
            }
        }

        binding.batteryHealthAndroid.text = getString(R.string.battery_health_android,
            getBatteryAndroidHealth(requireContext()))

        binding.residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")

        binding.batteryWear.text = getString(R.string.battery_wear, "0%", "0")

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

        TextAppearanceHelper.setTextAppearance(requireContext(), binding.designCapacity,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.numberOfCharges,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.numberOfFullCharges,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.numberOfCycles,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.numberOfCyclesAndroid,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.currentCapacityWear,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.capacityAddedWear,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.batteryTechnology,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.batteryHealth,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.batteryHealthAndroid,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.residualCapacity,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
        TextAppearanceHelper.setTextAppearance(requireContext(), binding.batteryWear,
            pref.getString(TEXT_STYLE, "0"),
            pref.getString(PreferencesKeys.TEXT_FONT, "6"),
            pref.getString(TEXT_SIZE, "2"))
    }

    private fun getDesignCapacity(): String {

        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
            R.integer.min_design_capacity))

        val designCapacityWh = (designCapacity.toDouble() * NOMINAL_BATTERY_VOLTAGE) / 1000.0

        val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH, resources.getBoolean(
            R.bool.is_capacity_in_wh))

        return if(isCapacityInWh) getString(
            R.string.design_capacity_wh, DecimalFormat("#.#").format(designCapacityWh))
        else getString(R.string.design_capacity, "$designCapacity")
    }

    private suspend fun getNumberOfCyclesAndroid(): Int {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            return batteryIntent?.getStringExtra(BatteryManager.EXTRA_CYCLE_COUNT)?.toInt() ?: 0

        if(!File(NUMBER_OF_CYCLES_PATH).exists()) return 0

        var numberOfCycles = 0

        val cycleCount = File(NUMBER_OF_CYCLES_PATH).absolutePath

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

                        binding.designCapacity.text = getDesignCapacity()

                        binding.numberOfCharges.text = getString(R.string.number_of_charges,
                            pref.getLong(PreferencesKeys.NUMBER_OF_CHARGES, 0))

                        binding.numberOfFullCharges.text = getString(R.string.number_of_full_charges,
                            pref.getLong(NUMBER_OF_FULL_CHARGES, 0))

                        binding.numberOfCycles.text = getString(R.string.number_of_cycles,
                            DecimalFormat("#.##").format(pref.getFloat(
                                PreferencesKeys.NUMBER_OF_CYCLES, 0f)))

                        binding.numberOfCyclesAndroid.apply {

                            if(visibility == View.VISIBLE) text = getString(R.string
                                .number_of_cycles_android, getNumberOfCyclesAndroid())
                        }
                    }

                    withContext(Dispatchers.Main) {

                        binding.batteryHealthAndroid.apply {

                            if(getBatteryHealth(requireContext()) != null) {
                                visibility = View.VISIBLE
                                text = getString(R.string.battery_health,
                                    getString(getBatteryHealth(requireContext()) ?:
                                    R.string.battery_health_great))
                            }
                        }

                        binding.batteryHealthAndroid.text = getString(R.string.battery_health_android,
                            getBatteryAndroidHealth(requireContext()))
                    }

                    if(getCurrentCapacity(requireContext()) >= 0.0) {

                        if(pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                                R.integer.min_design_capacity)) >= resources.getInteger(
                                R.integer.min_design_capacity)
                            && pref.getInt(PreferencesKeys.RESIDUAL_CAPACITY, 0) > 0) {

                            withContext(Dispatchers.Main) {

                                binding.residualCapacity.text = getResidualCapacity(requireContext())

                                binding.batteryWear.text = getBatteryWear(requireContext())

                            }
                        }

                        if(getCurrentCapacity(requireContext()) > 0.0) {

                            if(binding.currentCapacityWear.visibility == View.GONE)
                                withContext(Dispatchers.Main) {
                                    binding.currentCapacityWear.visibility = View.VISIBLE }

                            withContext(Dispatchers.Main) {

                                val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
                                    resources.getBoolean(R.bool.is_capacity_in_wh))

                                binding.currentCapacityWear.text = getString(if(isCapacityInWh)
                                    R.string.current_capacity_wh else R.string.current_capacity,
                                    DecimalFormat("#.#").format(if(isCapacityInWh)
                                        getCapacityInWh(getCurrentCapacity(requireContext()))
                                    else getCurrentCapacity(requireContext())))

                                when {
                                    getSourceOfPower(requireContext(), sourceOfPower) != "N/A"
                                    -> {

                                        if(binding.capacityAddedWear.visibility == View.GONE)
                                            binding.capacityAddedWear.visibility = View.VISIBLE

                                        binding.capacityAddedWear.text =
                                            getCapacityAdded(requireContext())
                                    }
                                    getSourceOfPower(requireContext(), sourceOfPower) == "N/A"
                                    -> {

                                        if(binding.capacityAddedWear.visibility == View.GONE)
                                            binding.capacityAddedWear.visibility = View.VISIBLE

                                        binding.capacityAddedWear.text =
                                            getCapacityAdded(requireContext())
                                    }
                                    binding.capacityAddedWear.visibility == View.VISIBLE ->
                                        binding.capacityAddedWear.visibility = View.GONE
                                }
                            }
                        }

                        else {

                            if(binding.currentCapacityWear.visibility == View.VISIBLE)
                                withContext(Dispatchers.Main) {
                                    binding.currentCapacityWear.visibility = View.GONE }

                            if(binding.capacityAddedWear.visibility == View.GONE
                                && pref.getFloat(PreferencesKeys.CAPACITY_ADDED, 0f) > 0f)
                                withContext(Dispatchers.Main) {
                                    binding.capacityAddedWear.visibility = View.VISIBLE }

                            else withContext(Dispatchers.Main) {
                                binding.capacityAddedWear.visibility = View.GONE }
                        }
                    }

                    else {

                        if(binding.currentCapacityWear.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) {
                                binding.currentCapacityWear.visibility = View.GONE }

                        if(binding.capacityAddedWear.visibility == View.VISIBLE)
                            withContext(Dispatchers.Main) { binding.capacityAddedWear.visibility = View.GONE }

                        if(pref.contains(PreferencesKeys.CAPACITY_ADDED)) pref.edit().remove(
                            PreferencesKeys.CAPACITY_ADDED
                        ).apply()

                        if(pref.contains(PreferencesKeys.PERCENT_ADDED)) pref.edit().remove(
                            PreferencesKeys.PERCENT_ADDED
                        ).apply()
                    }

                    withContext(Dispatchers.Main) {

                        binding.batteryTechnology.text = getString(R.string.battery_technology,
                            batteryIntent?.getStringExtra(
                                BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))
                    }

                    when(status) {

                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if (getCurrentCapacity(requireContext()) > 0.0) 989L
                            else 996L)

                        else -> delay(1500L)
                    }
                }
            }
    }
}