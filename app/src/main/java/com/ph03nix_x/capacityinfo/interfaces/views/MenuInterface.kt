package com.ph03nix_x.capacityinfo.interfaces.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeFragment
import com.ph03nix_x.capacityinfo.fragments.WearFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.ManufacturerInterface

/**
 * Created by Ph03niX-X on 21.06.2023
 * Ph03niX-X@outlook.com
 */

interface MenuInterface: ManufacturerInterface {

    fun MainActivity.inflateMenu() {
        if(fragment is HistoryFragment) {
            with(toolbar) {
                inflateMenu(R.menu.history_menu)
                menu.findItem(R.id.clear_history).apply {
                    isVisible = PremiumInterface.isPremium && HistoryHelper.isHistoryNotEmpty(
                        this@inflateMenu)
                    setOnMenuItemClickListener {
                        HistoryHelper.clearHistory(this@inflateMenu, this)
                        true
                    }
                }
                menu.findItem(R.id.history_premium).apply {
                    isVisible = !PremiumInterface.isPremium
                    setOnMenuItemClickListener {
                        showPremiumDialog()
                        true
                    }
                }   
            }
        }
        else {
            with(toolbar) {
                inflateMenu(R.menu.main_menu)
                menu.findItem(R.id.instruction)?.isVisible = getCurrentCapacity(
                    this@inflateMenu) > 0.0 && (fragment is ChargeDischargeFragment ||
                        fragment is LastChargeFragment || fragment is WearFragment)
                menu.findItem(R.id.instruction).setOnMenuItemClickListener {
                    showInstruction()
                    true
                }
                menu.findItem(R.id.faq).setOnMenuItemClickListener {
                    showFaq()
                    true
                }
                menu.findItem(R.id.tips).setOnMenuItemClickListener {
                    MaterialAlertDialogBuilder(this@inflateMenu).apply {
                        setIcon(R.drawable.ic_tips_for_extending_battery_life_24dp)
                        setTitle(getString(R.string.tips_dialog_title))
                        setMessage(getString(R.string.tip1) + getString(R.string.tip2)
                                + getString(R.string.tip3) + getString(R.string.tip4))
                        setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                        show()
                    }
                    true
                }
                menu.findItem(R.id.dont_kill_my_app).setOnMenuItemClickListener {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW,
                            getDontKillMyAppManufactures()))
                    }
                    catch(e: ActivityNotFoundException) {
                        Toast.makeText(this@inflateMenu, e.message ?: e.toString(),
                            Toast.LENGTH_LONG).show()
                    }
                    true
                }
                menu.findItem(R.id.support_ads).setOnMenuItemClickListener {
                    showAds(resources.getString(R.string.support_ad_unit_id))
                    true
                }
                menu.findItem(R.id.premium)?.isVisible = !PremiumInterface.isPremium
                if(!PremiumInterface.isPremium)
                    menu.findItem(R.id.premium).setOnMenuItemClickListener {
                        showPremiumDialog()
                        true
                    }   
            }
        }
    }

    fun MainActivity.clearMenu() = toolbar.menu.clear()

    fun MainActivity.showInstruction() {

        MaterialAlertDialogBuilder(this).apply {
            setIcon(R.drawable.ic_instruction_not_supported_24dp)
            setTitle(getString(R.string.instruction))
            setMessage(getString(R.string.instruction_message)
                    + getString(R.string.instruction_message_do_not_kill_the_service)
                    + getString(R.string.instruction_message_dont_kill_my_app))
            setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
            setCancelable(false)
            show()
        }
    }

    fun MainActivity.showFaq() {
        if(showFaqDialog == null) {
            showFaqDialog = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_faq_question_24dp)
                setTitle(getString(R.string.faq))
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                    setMessage(getString(R.string.faq_how_does_the_app_work)
                            + getString(R.string.faq_capacity_added)
                            + getString(R.string.faq_where_does_the_app_get_the_ccl)
                            + getString(R.string.faq_why_is_ccl_not_displayed)
                            + getString(R.string.faq_i_have_everything_in_zeros)
                            + getString(R.string.faq_units)
                            + getString(R.string.faq_current_capacity)
                            + getString(R.string.faq_residual_capacity_is_higher)
                            + getString(R.string.faq_battery_wear_changes_when_charger_is_disconnected)
                            + getString(R.string.faq_battery_wear_not_change)
                            + getString(R.string.faq_with_each_charge_battery_wear_changes))
                else setMessage(getString(R.string.faq_how_does_the_app_work)
                        + getString(R.string.faq_capacity_added)
                        + getString(R.string.faq_where_does_the_app_get_the_ccl)
                        + getString(R.string.faq_why_is_ccl_not_displayed)
                        + getString(R.string.faq_i_have_everything_in_zeros)
                        + getString(R.string.faq_units) + getString(R.string.faq_current_capacity)
                        + getString(R.string.faq_residual_capacity_is_higher)
                        + getString(R.string.faq_battery_wear_changes_when_charger_is_disconnected)
                        + getString(R.string.faq_battery_wear_not_change)
                        + getString(R.string.faq_with_each_charge_battery_wear_changes)
                        + getString(R.string.faq_where_does_the_app_get_the_number_of_cycles_android)
                        + getString(R.string.faq_not_displayed_number_of_cycles_android))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    showFaqDialog = null
                }
                setCancelable(false)
                show()
            }
        }
    }
}