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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.databinding.WearFragmentBinding
import com.ph03nix_x.capacityinfo.helpers.TextAppearanceHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService.Companion.NOMINAL_BATTERY_VOLTAGE
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.NUMBER_OF_CYCLES_PATH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_SIZE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_STYLE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.seconds
import androidx.core.content.edit

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

        if(!isInstalledFromGooglePlay())
            throw RuntimeException("Application not installed from Google Play")

        updateTextAppearance()

        binding.designCapacity.setOnClickListener {

            onChangeDesignCapacity()

            (it as? AppCompatTextView)?.text = getDesignCapacity()
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding) {
            designCapacity.text = getDesignCapacity()
            numberOfCyclesAndroid.isVisible =
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE || File(
                    NUMBER_OF_CYCLES_PATH).exists())
            batteryHealth.apply {
                if(getBatteryHealth(requireContext()) != null) {
                    isVisible = true
                    text = getString(R.string.battery_health, getString(
                        getBatteryHealth(requireContext()) ?: R.string.battery_health_great))
                }
            }
            residualCapacity.text = getString(R.string.residual_capacity, "0", "0%")
            batteryWear.text = getString(R.string.battery_wear, "0%", "0")
            batteryIntent = requireContext().registerReceiver(null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }
        
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
        with(binding) {
            val textViewArrayList = arrayListOf(designCapacity, numberOfCharges, numberOfFullCharges,
                numberOfCycles, numberOfCyclesAndroid, currentCapacityWear, capacityAddedWear,
                batteryTechnology, batteryHealth, residualCapacity, batteryWear)
            
            TextAppearanceHelper.setTextAppearance(requireContext(), textViewArrayList,
                pref.getString(TEXT_STYLE, "0"),
                pref.getString(PreferencesKeys.TEXT_FONT, "6"),
                pref.getString(TEXT_SIZE, "2"))
        }
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
            return batteryIntent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0) ?: 0

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
                    catch (_: FileNotFoundException) { null }
                }

                kotlin.runCatching { numberOfCycles = br?.readLine()?.toInt() ?: 0 }

                kotlin.runCatching { br?.close() }


            } catch (_: IOException) { numberOfCycles = 0 }
        }

        return numberOfCycles
    }

    private fun wearInformationJob() {

        if(job == null)
            job = CoroutineScope(Dispatchers.Default).launch {
                while(isJob) {

                    val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN
                    val sourceOfPower = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED,
                        -1) ?: -1

                    withContext(Dispatchers.Main) {
                        with(binding) {
                            designCapacity.text = getDesignCapacity()

                            numberOfCharges.text = getString(R.string.number_of_charges,
                                pref.getLong(PreferencesKeys.NUMBER_OF_CHARGES, 0))

                            numberOfFullCharges.text = getString(R.string.number_of_full_charges,
                                pref.getLong(NUMBER_OF_FULL_CHARGES, 0))

                            numberOfCycles.text = getString(R.string.number_of_cycles,
                                DecimalFormat("#.##").format(pref.getFloat(
                                    PreferencesKeys.NUMBER_OF_CYCLES, 0f)))

                            numberOfCyclesAndroid.apply {
                                if(isVisible) text = getString(R.string
                                    .number_of_cycles_android, getNumberOfCyclesAndroid())
                            }
                        }
                    }

                    if(getCurrentCapacity(requireContext()) >= 0.0) {

                        if(pref.getInt(DESIGN_CAPACITY, resources.getInteger(
                                R.integer.min_design_capacity)) >= resources.getInteger(
                                R.integer.min_design_capacity)
                            && pref.getInt(PreferencesKeys.RESIDUAL_CAPACITY, 0) > 0) {

                            withContext(Dispatchers.Main) {
                                binding.apply {
                                    residualCapacity.text = getResidualCapacity(requireContext())
                                    batteryWear.text = getBatteryWear(requireContext())
                                }
                            }
                        }

                        if(getCurrentCapacity(requireContext()) > 0.0) {
                            
                            binding.apply {
                                if(!currentCapacityWear.isVisible)
                                    withContext(Dispatchers.Main) {
                                        currentCapacityWear.isVisible = true }

                                withContext(Dispatchers.Main) {

                                    val isCapacityInWh = pref.getBoolean(IS_CAPACITY_IN_WH,
                                        resources.getBoolean(R.bool.is_capacity_in_wh))

                                    currentCapacityWear.text = getString(if(isCapacityInWh)
                                        R.string.current_capacity_wh else R.string.current_capacity,
                                        DecimalFormat("#.#").format(if(isCapacityInWh)
                                            getCapacityInWh(getCurrentCapacity(requireContext()))
                                        else getCurrentCapacity(requireContext())))

                                    when {
                                        !getSourceOfPower(requireContext(), sourceOfPower)
                                            .contains("N/A") -> {
                                            if(!capacityAddedWear.isVisible)
                                                capacityAddedWear.isVisible = true
                                            capacityAddedWear.text =
                                                getCapacityAdded(requireContext())
                                        }
                                        getSourceOfPower(requireContext(), sourceOfPower)
                                            .contains("N/A") -> {
                                            if(capacityAddedWear.isVisible)
                                                capacityAddedWear.isVisible = false
                                        }
                                    }
                                }   
                            }
                        }

                        else {
                            binding.apply {
                                if(currentCapacityWear.isVisible)
                                    withContext(Dispatchers.Main) {
                                        currentCapacityWear.isVisible = false }

                                if(!capacityAddedWear.isVisible && pref.getFloat(
                                        PreferencesKeys.CAPACITY_ADDED, 0f) > 0f)
                                    withContext(Dispatchers.Main) {
                                        capacityAddedWear.isVisible = true }
                                else withContext(Dispatchers.Main) {
                                    capacityAddedWear.isVisible = false }
                            }
                        }
                    }
                    else {
                        binding.apply {
                            if(currentCapacityWear.isVisible)
                                withContext(Dispatchers.Main) {
                                    currentCapacityWear.isVisible = false }
                            if(capacityAddedWear.isVisible)
                                withContext(Dispatchers.Main) {
                                    capacityAddedWear.isVisible = false }
                            if(pref.contains(PreferencesKeys.CAPACITY_ADDED))
                                pref.edit { remove(PreferencesKeys.CAPACITY_ADDED) }
                            if(pref.contains(PreferencesKeys.PERCENT_ADDED))
                                pref.edit { remove(PreferencesKeys.PERCENT_ADDED) }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        binding.batteryTechnology.text = getString(R.string.battery_technology,
                            batteryIntent?.getStringExtra(
                                BatteryManager.EXTRA_TECHNOLOGY) ?: getString(R.string.unknown))
                    }

                    when(status) {
                        BatteryManager.BATTERY_STATUS_CHARGING ->
                            delay(if (getCurrentCapacity(requireContext()) > 0.0) 0.989.seconds
                            else 0.996.seconds)

                        else -> delay(1.5.seconds)
                    }
                }
            }
    }

    @Suppress("DEPRECATION")
    private fun isInstalledFromGooglePlay() =
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
                .getInstallSourceInfo(requireContext().packageName).installingPackageName
        else Constants.GOOGLE_PLAY_PACKAGE_NAME == requireContext().packageManager
            .getInstallerPackageName(requireContext().packageName)
}