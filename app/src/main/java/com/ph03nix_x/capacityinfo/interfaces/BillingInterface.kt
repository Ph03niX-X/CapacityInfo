package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.utils.Utils
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface BillingInterface {

    fun onBillingClientBuilder(context: Context): BillingClient {

        return BillingClient.newBuilder(context).setListener(({ billingResult, purchasesList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchasesList.isNullOrEmpty()) {

                purchasesList.forEach {

                    if(it.sku == "donate") {

                        Toast.makeText(context, context.getString(R.string.thanks_for_the_donation), Toast.LENGTH_LONG).show()

                        CoroutineScope(Dispatchers.Default).launch {

                            onConsumePurchase(it.purchaseToken, it.developerPayload)
                        }
                    }
                }
            }

        })).enablePendingPurchases().build()
    }

    fun onBillingStartConnection(context: Context) {

        billingClient?.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) { }

            override fun onBillingServiceDisconnected() { }
        })
    }

    fun isPurchased(context: Context): Boolean {

        var isPurchased = false

        billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {

                response, purchaseHistoryList ->

            if (response.responseCode == BillingClient.BillingResponseCode.OK) {

                if(purchaseHistoryList.isNotEmpty())
                purchaseHistoryList.forEach {

                    if(it.sku == "donate") isPurchased = true
                }
            }
        }

        return isPurchased
    }

    fun onPurchase(context: Context, sku: String) {

        val skuDetailsMap = HashMap<String, SkuDetails>()

        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder().apply {

            setSkusList(getSkuList(sku))

            setType(BillingClient.SkuType.INAPP)

        }.build()

        billingClient?.querySkuDetailsAsync(skuDetailsParamsBuilder, ({ billingResult, skuDetailsList ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {

                skuDetailsList.forEach {

                    skuDetailsMap.put(it.sku, it)
                }

                billingClient?.launchBillingFlow((context as SettingsActivity),
                    BillingFlowParams.newBuilder().apply {

                        setSkuDetails(skuDetailsMap[sku])

                    }.build())
            }
        }))
    }

    private suspend fun onConsumePurchase(token: String, developerPayload: String?) {

        val consumeParams = ConsumeParams.newBuilder().apply {

            setPurchaseToken(token)
            setDeveloperPayload(developerPayload)

        }.build()

        withContext(Dispatchers.IO) {

            billingClient?.consumePurchase(consumeParams)
        }
    }

    fun getSkuList(sku: String) = arrayListOf(sku)
}