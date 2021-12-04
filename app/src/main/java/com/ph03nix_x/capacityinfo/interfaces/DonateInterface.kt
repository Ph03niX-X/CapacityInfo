package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.donationId
import com.ph03nix_x.capacityinfo.googlePlayLicenseKey

/**
 * Created by Ph03niX-X on 04.12.2021
 * Ph03niX-X@outlook.com
 */

@SuppressLint("StaticFieldLeak")
interface DonateInterface: BillingProcessor.IBillingHandler {

    companion object {
        var donateContext: Activity? = null
        var billingProcessor: BillingProcessor? = null
        var isDonation = false
        var isDonated = false
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        isDonation = false
        isDonated = billingProcessor?.isPurchased(donationId) == true
        if(isDonated) {
            if(donateContext != null) Toast.makeText(donateContext!!,
                R.string.thanks_for_the_donation, Toast.LENGTH_LONG).show()
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.donate)?.isVisible = false
        }
    }

    override fun onPurchaseHistoryRestored() {
        isDonation = false
        if(billingProcessor?.isPurchased(donationId) == true) {
            if(donateContext != null) Toast.makeText(donateContext!!,
                R.string.thanks_for_the_donation, Toast.LENGTH_LONG).show()
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.donate)?.isVisible = false
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        isDonation = false
        if(donateContext != null) Toast.makeText(donateContext!!,
            error?.message ?: donateContext!!.getString(R.string.unknown_error),
            Toast.LENGTH_LONG).show()
    }

    override fun onBillingInitialized() {
        if(donateContext != null && isDonation && BillingProcessor.isIabServiceAvailable(
                donateContext) && billingProcessor?.isInitialized == true) {
                    billingProcessor?.purchase(donateContext!!, donationId)
        }
    }

    fun openDonate() {
        isDonation = true
        if(donateContext != null && BillingProcessor.isIabServiceAvailable(donateContext!!))
            billingProcessor = BillingProcessor(donateContext, googlePlayLicenseKey, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
    }

    fun isDonated(): Boolean {
        if (donateContext != null && BillingProcessor.isIabServiceAvailable(donateContext))
            billingProcessor = BillingProcessor(donateContext, googlePlayLicenseKey, this)
        if (billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        return billingProcessor?.isPurchased(donationId) ?: false
    }
}