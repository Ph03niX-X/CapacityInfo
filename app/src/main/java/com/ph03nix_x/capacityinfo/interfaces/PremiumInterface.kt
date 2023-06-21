package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.queryPurchaseHistory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.PREMIUM_ID
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.TOKEN_COUNT
import com.ph03nix_x.capacityinfo.TOKEN_PREF
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.services.CheckPremiumJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Ph03niX-X on 04.12.2021
 * Ph03niX-X@outlook.com
 */

@SuppressLint("StaticFieldLeak")
interface PremiumInterface: PurchasesUpdatedListener {

    companion object {

        private var mProductDetailsList: List<ProductDetails>? = null

        var premiumContext: Context? = null
        var premiumActivity: Activity? = null
        var billingClient: BillingClient? = null

        var isPremium = false
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {

        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                CoroutineScope(Dispatchers.Default).launch {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
            val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)
            if(purchases != null) pref.edit().putString(TOKEN_PREF,
                purchases[0].purchaseToken).apply()
            val tokenPref = pref.getString(TOKEN_PREF, null)
            isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    fun initiateBilling(isPurchasePremium: Boolean = false) {

        billingClient = BillingClient.newBuilder(premiumContext!!)
            .setListener(purchasesUpdatedListener()).enablePendingPurchases().build()

        if (billingClient?.connectionState == BillingClient.ConnectionState.DISCONNECTED)
            startConnection(isPurchasePremium)
    }

    private fun startConnection(isPurchasePremium: Boolean) {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    querySkuDetails()
                    if(isPurchasePremium) purchasePremium()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun purchasesUpdatedListener() = PurchasesUpdatedListener { _, purchases ->
        if (purchases != null) {
            for (purchase in purchases) {
                CoroutineScope(Dispatchers.Default).launch {
                    handlePurchase(purchase)
                }
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {

        val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                        if (it.responseCode == BillingResponseCode.OK) {
                            pref.edit().putString(TOKEN_PREF, purchase.purchaseToken).apply()
                            val tokenPref = pref.getString(TOKEN_PREF, null)
                            isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
                            Toast.makeText(premiumContext, R.string.premium_features_unlocked,
                                Toast.LENGTH_LONG).show()
                            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)
                                ?.isVisible = false
                            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)
                                ?.isVisible = false
                            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)
                                ?.isVisible = true
                        }
                    }
                }
            } else {
                pref.edit().putString(TOKEN_PREF, purchase.purchaseToken).apply()
                val tokenPref = pref.getString(TOKEN_PREF, null)
                isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
                Toast.makeText(premiumContext, R.string.premium_features_unlocked,
                    Toast.LENGTH_LONG).show()
                MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
                MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible =
                    false
                MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
            }

        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            pref.edit().putString(TOKEN_PREF, purchase.purchaseToken).apply()
            val tokenPref = pref.getString(TOKEN_PREF, null)
            isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
            Toast.makeText(premiumContext, R.string.premium_features_unlocked, Toast.LENGTH_LONG)
                .show()
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.history_premium)?.isVisible = false
            MainActivity.instance?.toolbar?.menu?.findItem(R.id.clear_history)?.isVisible = true
        }
    }

    private fun querySkuDetails() {
        val productList = mutableListOf(QueryProductDetailsParams.Product.newBuilder().apply {
            setProductId(PREMIUM_ID)
            setProductType(ProductType.INAPP)
        }.build())

        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList).build()

        billingClient?.queryProductDetailsAsync(queryProductDetailsParams) { billingResult,
                                                                             productDetailsList ->

            if (billingResult.responseCode == BillingResponseCode.OK)
                mProductDetailsList = productDetailsList
        }
    }

    fun MainActivity.showPremiumDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setIcon(R.drawable.ic_premium_24)
            setTitle(getString(R.string.premium))
            setMessage(getString(R.string.premium_dialog))
            setPositiveButton(R.string.purchase_premium) { d, _ ->
                if(billingClient?.isReady == true) purchasePremium()
                else initiateBilling(true)

                d.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { d, _ ->
                d.dismiss()
            }
            setCancelable(false)
            show()
        }
    }

    fun purchasePremium() {

        if(!mProductDetailsList.isNullOrEmpty()) {
            val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams
                .newBuilder().setProductDetails(mProductDetailsList!![0]).build())

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient?.launchBillingFlow(premiumActivity!!, billingFlowParams)
        }
    }

    fun checkPremium() {

        CoroutineScope(Dispatchers.IO).launch {

            val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)

            var tokenPref = pref.getString(TOKEN_PREF, null)

            if(tokenPref != null && tokenPref.count() >= TOKEN_COUNT) isPremium = true

            else if(tokenPref != null && tokenPref.count() < TOKEN_COUNT)
                pref.edit().remove(TOKEN_PREF).apply()

           else if(tokenPref == null || tokenPref.count() < TOKEN_COUNT) {

                if(billingClient?.isReady != true) initiateBilling()

                delay(2500L)
                val params = QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(ProductType.INAPP)

                val purchaseHistoryResult = billingClient?.queryPurchaseHistory(params.build())

                val purchaseHistoryRecordList = purchaseHistoryResult?.purchaseHistoryRecordList

                if(!purchaseHistoryRecordList.isNullOrEmpty()) {

                    pref.edit().putString(TOKEN_PREF, purchaseHistoryRecordList[0].purchaseToken)
                        .apply()

                    tokenPref = pref.getString(TOKEN_PREF, null)

                    isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT

                    delay(5000L)
                    billingClient?.endConnection()
                }
            }
        }

    }

    fun CheckPremiumJob.checkPremiumJob() {

        CoroutineScope(Dispatchers.IO).launch {

            val pref = PreferenceManager.getDefaultSharedPreferences(this@checkPremiumJob)

            if(billingClient?.isReady != true) initiateBilling()

            delay(2500L)
            val params = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(ProductType.INAPP)

            val purchaseHistoryResult = billingClient?.queryPurchaseHistory(params.build())

            val purchaseHistoryRecordList = purchaseHistoryResult?.purchaseHistoryRecordList

            if(!purchaseHistoryRecordList.isNullOrEmpty()) {
                pref.edit().putString(TOKEN_PREF, purchaseHistoryRecordList[0].purchaseToken)
                    .apply()
                val tokenPref = pref.getString(TOKEN_PREF, null)
                isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
                delay(5000L)
                billingClient?.endConnection()
            }
            else {
                if(pref.contains(TOKEN_PREF)) pref.edit().remove(TOKEN_PREF).apply()
                val tokenPref = pref.getString(TOKEN_PREF, null)
                isPremium = tokenPref != null && tokenPref.count() >= TOKEN_COUNT
            }
        }
    }
}