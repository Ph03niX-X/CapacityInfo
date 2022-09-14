package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.donationId
import com.ph03nix_x.capacityinfo.googlePlayLicenseKey
import com.ph03nix_x.capacityinfo.premiumId

/**
 * Created by Ph03niX-X on 04.12.2021
 * Ph03niX-X@outlook.com
 */

@SuppressLint("StaticFieldLeak")
interface DonateInterface: BillingProcessor.IBillingHandler {

    companion object {

        @Deprecated("Remove at the end of December")
        var donateContext: Context? = null
        @Deprecated("Remove at the end of December")
        var donateActivity: Activity? = null
        var premiumContext: Context? = null
        var premiumActivity: Activity? = null
        var billingProcessor: BillingProcessor? = null

        @Deprecated("Remove at the end of December")
        var isDonation = false
        @Deprecated("Remove at the end of December")
        var isDonated = false
        var isPurchasePremium = false
        var isPremium = false
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        isDonation = false
        isPurchasePremium = false
        isDonated = billingProcessor?.isPurchased(donationId) == true
        isPremium = billingProcessor?.isPurchased(premiumId) == true
        if(isDonated || isPremium) {
            if(donateContext != null || premiumContext != null)
                Toast.makeText(donateContext ?: premiumContext!!,
                    R.string.premium_features_unlocked, Toast.LENGTH_LONG).show()
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    override fun onPurchaseHistoryRestored() {
        isDonation = false
        isPurchasePremium = false
        isDonated = billingProcessor?.isPurchased(donationId) == true
        isPremium = billingProcessor?.isPurchased(premiumId) == true
        if(isDonated || isPremium) {
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        isDonation = false
        isPurchasePremium = false
    }

    override fun onBillingInitialized() {
        if(donateContext != null && donateActivity != null && isDonation &&
            BillingProcessor.isIabServiceAvailable(
                donateContext) && billingProcessor?.isInitialized == true) {
                    billingProcessor?.purchase(donateActivity!!, donationId)
        }
        else if(premiumContext != null && premiumActivity != null && isPurchasePremium &&
            BillingProcessor.isIabServiceAvailable(premiumContext)
            && billingProcessor?.isInitialized == true){
            billingProcessor?.purchase(premiumActivity, premiumId)
        }
        isDonation = false
        isPurchasePremium = false
    }

    @Deprecated("Remove at the end of December")
    fun openDonate() {
        isDonation = true
        if(donateContext != null && BillingProcessor.isIabServiceAvailable(donateContext!!))
            billingProcessor = BillingProcessor(donateContext, googlePlayLicenseKey, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
    }

    @Deprecated("Remove at the end of December")
    fun isDonated(): Boolean {
        if (donateContext != null && BillingProcessor.isIabServiceAvailable(donateContext))
            billingProcessor = BillingProcessor(donateContext, googlePlayLicenseKey, this)
        if (billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        return billingProcessor?.isPurchased(donationId) ?: false
    }

    fun getOrderId(): String? {
        if(premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext))
            billingProcessor = BillingProcessor(premiumContext, googlePlayLicenseKey, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        if(isDonated()) return billingProcessor?.getPurchaseInfo(donationId)?.purchaseData?.orderId
        else if(isPremium()) return billingProcessor?.getPurchaseInfo(premiumId)?.purchaseData?.orderId
        return null
    }

    fun purchasePremium() {
        isPurchasePremium = true
        if(premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext!!))
            billingProcessor = BillingProcessor(premiumContext, googlePlayLicenseKey, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
    }

    fun isPremium(): Boolean {
        if (premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext))
            billingProcessor = BillingProcessor(premiumContext, googlePlayLicenseKey, this)
        if (billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        return billingProcessor?.isPurchased(premiumId) ?: false
    }
}