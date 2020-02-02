package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.widget.Toast
import com.android.billingclient.api.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.utils.Utils
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.purchasesList

interface BillingInterface {

    fun onBillingClientBuilder(context: Context): BillingClient {

        return BillingClient.newBuilder(context).setListener(({ billingResult, purchasesList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchasesList.isNullOrEmpty()) {

                purchasesList.forEach {

                    if(it.sku == "donate") {

                        Toast.makeText(context, context.getString(R.string.thanks_for_the_donation), Toast.LENGTH_LONG).show()

                        Utils.purchasesList = purchasesList
                    }
                }

                billingClient!!.endConnection()
            }

        })).enablePendingPurchases().build()
    }

    fun onBillingStartConnection(context: Context) {

        billingClient!!.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    purchasesList = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
                }
            }

            override fun onBillingServiceDisconnected() { }
        })
    }

    fun isPurchased(context: Context): Boolean {

        if(purchasesList.isNotEmpty()) {

            purchasesList.forEach {

                if(it.sku == "donate") return true
            }

            billingClient!!.endConnection()
        }

        return false
    }

    fun getOrderId(context: Context): String? {

        if(purchasesList.isNotEmpty())
            purchasesList.forEach {

                if (it.sku == "donate") return it.orderId
            }

        billingClient!!.endConnection()

        return null
    }

    fun onPurchase(context: Context, billingClient: BillingClient, idPurchase: String) {

        val skuDetailsMap = HashMap<String, SkuDetails>()

        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder().apply {

            setSkusList(getSkuList(idPurchase))

            setType(BillingClient.SkuType.INAPP)

        }.build()

        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder, ({ billingResult, skuDetailsList ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList.isNotEmpty()) {

                skuDetailsList.forEach {

                    skuDetailsMap.put(it.sku, it)
                }

                billingClient.launchBillingFlow((context as SettingsActivity),
                    BillingFlowParams.newBuilder().apply {

                        setSkuDetails(skuDetailsMap[idPurchase])

                    }.build())
            }
        }))
    }

    fun getSkuList(idPurchase: String) = arrayListOf(idPurchase)
}