package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.utils.Utils
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.purchasesList
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

                        Utils.purchasesList = purchasesList

                        CoroutineScope(Dispatchers.Default).launch {

                                onConsumePurchase(it.purchaseToken, it.developerPayload)
                            }
                    }
                }

                billingClient?.endConnection()
            }

        })).enablePendingPurchases().build()
    }

    fun onBillingStartConnection(context: Context) {

        billingClient?.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    purchasesList = billingClient?.queryPurchases(BillingClient.SkuType.INAPP)?.purchasesList
                }
            }

            override fun onBillingServiceDisconnected() { }
        })
    }

    fun isPurchased(context: Context): Boolean {

        if(!purchasesList.isNullOrEmpty()) {

            purchasesList?.forEach {

                if(it.sku == "donate") return true
            }

            billingClient?.endConnection()
        }

        return false
    }

    fun getOrderId(context: Context): String? {

        if(!purchasesList.isNullOrEmpty())
            purchasesList?.forEach {

                if (it.sku == "donate") return it.orderId
            }

        billingClient!!.endConnection()

        return null
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