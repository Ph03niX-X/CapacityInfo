package com.ph03nix_x.capacityinfo.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface

/**
 * Created by Ph03niX-X on 27.09.2021
 * Ph03niX-X@outlook.com
 */
class FakeBatteryWearFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    private var enableFakeBatteryWear: SwitchPreferenceCompat? = null
    private var fakeBatteryWearValue: SeekBarPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.fake_battery_wear_settings)

        enableFakeBatteryWear = findPreference("is_enable_fake_battery_wear")
        fakeBatteryWearValue = findPreference("fake_battery_wear_value")

        fakeBatteryWearValue?.isEnabled = enableFakeBatteryWear?.isChecked ?: resources
            .getBoolean(R.bool.is_enable_fake_battery_wear_value)

        enableFakeBatteryWear?.setOnPreferenceChangeListener { _, newValue ->
            fakeBatteryWearValue?.isEnabled = newValue as? Boolean == true
            BatteryInfoInterface.fakeResidualCapacity = 0.0
            BatteryInfoInterface.fakeCurrentCapacity = 0.0
            BatteryInfoInterface.tempBatteryLevel = 0
            true
        }

        fakeBatteryWearValue?.setOnPreferenceChangeListener { _, _ ->
            BatteryInfoInterface.fakeResidualCapacity = 0.0
            BatteryInfoInterface.fakeCurrentCapacity = 0.0
            BatteryInfoInterface.tempBatteryLevel = 0
            true
        }
    }

    override fun onResume() {
        super.onResume()
        fakeBatteryWearValue?.isEnabled = enableFakeBatteryWear?.isChecked ?: resources
            .getBoolean(R.bool.is_enable_fake_battery_wear_value)
    }
}