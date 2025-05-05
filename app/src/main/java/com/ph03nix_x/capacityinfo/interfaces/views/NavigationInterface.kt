package com.ph03nix_x.capacityinfo.interfaces.views

import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.AboutFragment
import com.ph03nix_x.capacityinfo.fragments.BackupSettingsFragment
import com.ph03nix_x.capacityinfo.fragments.BatteryStatusInformationFragment
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.fragments.FeedbackFragment
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeNoPremiumFragment
import com.ph03nix_x.capacityinfo.fragments.OverlayFragment
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.fragments.WearFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.interfaces.AdsInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.Constants

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

interface NavigationInterface : BatteryInfoInterface, AdsInterface {
    
    fun MainActivity.bottomNavigation(status: Int) {
        
        navigation.apply {
            menu.findItem(R.id.charge_discharge_navigation).title = getString(
                if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charging
                else R.string.discharge)
            menu.findItem(R.id.charge_discharge_navigation).icon = ContextCompat.getDrawable(
                this@bottomNavigation, getChargeDischargeNavigationIcon(status ==
                        BatteryManager.BATTERY_STATUS_CHARGING))
            if(isGooglePlay)
                setOnItemSelectedListener {
                    when(it.itemId) {
                        R.id.charge_discharge_navigation -> {
                            if(fragment !is ChargeDischargeFragment) {
                                showAds()
                                fragment = ChargeDischargeFragment()
                                toolbar.navigationIcon = null
                                MainActivity.apply {
                                    isLoadChargeDischarge = true
                                    isLoadLastCharge = false
                                    isLoadWear = false
                                    isLoadHistory = false
                                    isLoadSettings = false
                                    isLoadDebug = false
                                }
                                clearMenu()
                                inflateMenu()
                                loadFragment(fragment ?: ChargeDischargeFragment())
                            }
                        }
                        R.id.last_charge_navigation -> {
                            if(fragment !is LastChargeNoPremiumFragment &&
                                fragment !is LastChargeFragment) {
                                fragment = if(PremiumInterface.isPremium) LastChargeFragment()
                                else LastChargeNoPremiumFragment()
                                toolbar.apply {
                                    title = getString(R.string.last_charge)
                                    navigationIcon = null
                                }
                                MainActivity.apply {
                                    isLoadChargeDischarge = false
                                    isLoadLastCharge = true
                                    isLoadWear = false
                                    isLoadHistory = false
                                    isLoadSettings = false
                                    isLoadDebug = false
                                }
                                clearMenu()
                                inflateMenu()
                                loadFragment(fragment ?: if(PremiumInterface.isPremium)
                                    LastChargeFragment() else LastChargeNoPremiumFragment())
                            }
                        }
                        R.id.wear_navigation -> {
                            if(fragment !is WearFragment) {
                                showAds()
                                fragment = WearFragment()
                                toolbar.apply {
                                    title = getString(R.string.wear)
                                    navigationIcon = null
                                }
                                MainActivity.apply {
                                    isLoadChargeDischarge = false
                                    isLoadLastCharge = false
                                    isLoadWear = true
                                    isLoadHistory = false
                                    isLoadSettings = false
                                    isLoadDebug = false
                                }
                                clearMenu()
                                inflateMenu()
                                loadFragment(fragment ?: WearFragment())
                            }
                        }
                        R.id.history_navigation -> {
                            if(fragment !is HistoryFragment) {
                                fragment = HistoryFragment()
                                toolbar.apply {
                                    title = getString(if(PremiumInterface.isPremium &&
                                        HistoryHelper.isHistoryNotEmpty(this@bottomNavigation))
                                        R.string.history_title else R.string.history, HistoryHelper
                                            .getHistoryCount(this@bottomNavigation),
                                        Constants.HISTORY_COUNT_MAX)
                                    navigationIcon = null
                                }
                                MainActivity.apply {
                                    isLoadChargeDischarge = false
                                    isLoadLastCharge = false
                                    isLoadWear = false
                                    isLoadHistory = true
                                    isLoadSettings = false
                                    isLoadDebug = false
                                }
                                clearMenu()
                                inflateMenu()
                                loadFragment(fragment ?: HistoryFragment())
                            }
                        }
                        R.id.settings_navigation -> {
                            when(fragment) {
                                null, is ChargeDischargeFragment, is LastChargeNoPremiumFragment,
                                is LastChargeFragment, is WearFragment, is HistoryFragment -> {
                                    fragment = SettingsFragment()
                                    toolbar.apply {
                                        title = getString(R.string.settings)
                                        navigationIcon = null
                                    }
                                    MainActivity.apply {
                                        isLoadChargeDischarge = false
                                        isLoadLastCharge = false
                                        isLoadWear = false
                                        isLoadSettings = true
                                        isLoadDebug = false
                                    }
                                    clearMenu()
                                    loadFragment(fragment ?: SettingsFragment())
                                }
                            }
                        }
                    }
                    true
                }
        }
    }

    fun MainActivity.loadFragment(fragment: Fragment, isAddToBackStack: Boolean = false) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            if(isAddToBackStack) addToBackStack(null)
            if(!MainActivity.isRecreate || fragment is ChargeDischargeFragment || fragment is WearFragment)
                commit()
        }
        when {
            fragment !is BatteryStatusInformationFragment
                    && fragment !is OverlayFragment && fragment !is AboutFragment &&
                    fragment !is DebugFragment && fragment !is FeedbackFragment &&
                    fragment !is BackupSettingsFragment -> {
                navigation.selectedItemId = when(fragment) {
                    is ChargeDischargeFragment -> R.id.charge_discharge_navigation
                    is LastChargeNoPremiumFragment, is LastChargeFragment ->
                        R.id.last_charge_navigation
                    is WearFragment -> R.id.wear_navigation
                    is HistoryFragment -> R.id.history_navigation
                    is SettingsFragment -> R.id.settings_navigation
                    else -> R.id.charge_discharge_navigation
                }
            }
            else -> {
                navigation.selectedItemId = R.id.settings_navigation
                clearMenu()
                toolbar.navigationIcon = ContextCompat.getDrawable(this,
                    R.drawable.ic_arrow_back_24dp)
            }
        }
    }

    fun MainActivity.getChargeDischargeNavigationIcon(isCharge: Boolean): Int {
        val batteryLevel = getBatteryLevel(this) ?: 0
        if(isCharge)
            return when(batteryLevel) {
                in 0..29 -> R.drawable.ic_charge_navigation_20_24dp
                in 30..49 -> R.drawable.ic_charge_navigation_30_24dp
                in 50..59 -> R.drawable.ic_charge_navigation_50_24dp
                in 60..79 -> R.drawable.ic_charge_navigation_60_24dp
                in 80..89 -> R.drawable.ic_charge_navigation_80_24dp
                in 90..95 -> R.drawable.ic_charge_navigation_90_24dp
                else -> R.drawable.ic_charge_navigation_full_24dp
            }
        else return when(batteryLevel) {
            in 0..9 -> R.drawable.ic_discharge_navigation_9_24dp
            in 10..29 -> R.drawable.ic_discharge_navigation_20_24dp
            in 30..49 -> R.drawable.ic_discharge_navigation_30_24dp
            in 50..59 -> R.drawable.ic_discharge_navigation_50_24dp
            in 60..79 -> R.drawable.ic_discharge_navigation_60_24dp
            in 80..89 -> R.drawable.ic_discharge_navigation_80_24dp
            in 90..95 -> R.drawable.ic_discharge_navigation_90_24dp
            else -> R.drawable.ic_discharge_navigation_full_24dp
        }
    }
}