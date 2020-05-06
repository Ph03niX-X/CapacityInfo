package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import kotlinx.coroutines.*

interface BillingInterface {

    companion object {

        var billingClient: BillingClient? = null
        var isDonated: Boolean = false
    }

    private fun onBillingClientBuilder(context: Context): BillingClient {

        return BillingClient.newBuilder(context).setListener(({ billingResult, purchasesList ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                !purchasesList.isNullOrEmpty()) {

                purchasesList.forEach {

                    if(it.sku == "donate") {

                        CoroutineScope(Dispatchers.Default).launch {

                            onConsumePurchase(it.purchaseToken, it.developerPayload)

                            isDonated = !isDonated

                            withContext(Dispatchers.Main) {

                                Toast.makeText(context, context.getString(
                                    R.string.thanks_for_the_donation), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

        })).enablePendingPurchases().build()
    }

    fun onBillingStartConnection(context: Context) {

        if(billingClient == null) billingClient = onBillingClientBuilder(context)

        billingClient?.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                    onQueryPurchaseHistory()
            }

            override fun onBillingServiceDisconnected() { }
        })
    }

    private fun onQueryPurchaseHistory() {

        billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP) {

                response, purchaseHistoryList ->

            if(response.responseCode == BillingClient.BillingResponseCode.OK) {

                purchaseHistoryList.forEach {

                    isDonated = it.sku == "donate"
                }
            }
        }
    }

    fun onPurchase(context: Context, sku: String) {

        val skuDetailsMap = HashMap<String, SkuDetails>()

        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder().apply {

            setSkusList(onGetSkuList(sku))

            setType(BillingClient.SkuType.INAPP)

        }.build()

        billingClient?.querySkuDetailsAsync(skuDetailsParamsBuilder, ({ billingResult,
                                                                        skuDetailsList ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                skuDetailsList.isNotEmpty()) {

                skuDetailsList.forEach {

                    skuDetailsMap[it.sku] = it
                }

                billingClient?.launchBillingFlow((context as? SettingsActivity),
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

    private fun onGetSkuList(sku: String) = arrayListOf(sku)
}