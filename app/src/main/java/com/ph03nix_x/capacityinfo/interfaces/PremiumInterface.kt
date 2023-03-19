package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.PurchaseInfo
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.GOOGLE_PLAY_LICENSE_KEY
import com.ph03nix_x.capacityinfo.PREMIUM_ID

/**
 * Created by Ph03niX-X on 04.12.2021
 * Ph03niX-X@outlook.com
 */

@SuppressLint("StaticFieldLeak")
interface PremiumInterface: BillingProcessor.IBillingHandler {

    companion object {

        var premiumContext: Context? = null
        var premiumActivity: Activity? = null
        var billingProcessor: BillingProcessor? = null

        var isPurchasePremium = false
        var isPremium = false
    }

    override fun onProductPurchased(productId: String, details: PurchaseInfo?) {
        isPurchasePremium = false
        isPremium = billingProcessor?.isPurchased(PREMIUM_ID) == true
        if(isPremium && premiumContext != null) {
            Toast.makeText(premiumContext, R.string.premium_features_unlocked,
                Toast.LENGTH_LONG).show()
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    override fun onPurchaseHistoryRestored() {
        isPurchasePremium = false
        isPremium = billingProcessor?.isPurchased(PREMIUM_ID) == true
        if(isPremium) {
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        isPurchasePremium = false
    }

    override fun onBillingInitialized() {
        if(premiumContext != null && premiumActivity != null && isPurchasePremium &&
            BillingProcessor.isIabServiceAvailable(premiumContext)
            && billingProcessor?.isInitialized == true){
            billingProcessor?.purchase(premiumActivity, PREMIUM_ID)
        }
        isPurchasePremium = false
    }

    fun getOrderId(): String? {
        if(premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext))
            billingProcessor = BillingProcessor(premiumContext, GOOGLE_PLAY_LICENSE_KEY, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        if(isPremium()) return billingProcessor?.getPurchaseInfo(PREMIUM_ID)?.purchaseData?.orderId
        return null
    }

    fun purchasePremium() {
        isPurchasePremium = true
        if(premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext!!))
            billingProcessor = BillingProcessor(premiumContext, GOOGLE_PLAY_LICENSE_KEY, this)
        if(billingProcessor?.isInitialized != true) billingProcessor?.initialize()
    }

    fun isPremium(): Boolean {
        if (premiumContext != null && BillingProcessor.isIabServiceAvailable(premiumContext))
            billingProcessor = BillingProcessor(premiumContext, GOOGLE_PLAY_LICENSE_KEY, this)
        if (billingProcessor?.isInitialized != true) billingProcessor?.initialize()
        return billingProcessor?.isPurchased(PREMIUM_ID) ?: false
    }
}