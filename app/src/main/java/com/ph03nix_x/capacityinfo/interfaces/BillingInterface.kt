package com.ph03nix_x.capacityinfo.interfaces

import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.android.billingclient.api.*
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.SettingsActivity
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DONATED
import com.ph03nix_x.capacityinfo.utils.Utils.orderId

interface BillingInterface {

    fun onBillingClientBuilder(context: Context): BillingClient {

        return BillingClient.newBuilder(context).setListener(({ billingResult, purchasesList ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchasesList != null) {

                Toast.makeText(context, context.getString(R.string.thanks_for_the_donation), Toast.LENGTH_LONG).show()

                val pref = PreferenceManager.getDefaultSharedPreferences(context)

                pref.edit().putBoolean(IS_DONATED, true).apply()
            }

        })).enablePendingPurchases().build()
    }

    fun onBillingStartConnection(context: Context, billingClient: BillingClient) {

        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    val purchasesResultList = billingClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList

                    val pref = PreferenceManager.getDefaultSharedPreferences(context)

                    for(list in purchasesResultList)
                        if(list.sku == "donate") {

                            if(!pref.getBoolean(IS_DONATED, false)) pref.edit().putBoolean(IS_DONATED, true).apply()

                            orderId = list.orderId
                        }
                        else if(pref.getBoolean(IS_DONATED, false)) pref.edit().remove(IS_DONATED).apply()
                }
            }

            override fun onBillingServiceDisconnected() { }
        })
    }

    fun onPurchase(context: Context, billingClient: BillingClient) {

        val skuDetailsMap = HashMap<String, SkuDetails>()

        val skuList = arrayListOf("donate")

        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder().apply {

            setSkusList(skuList)

            setType(BillingClient.SkuType.INAPP)

        }.build()

        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder, ({ billingResult, skuDetailsList ->

            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                for(skuDetails in skuDetailsList) {

                    skuDetailsMap.put(skuDetails.sku, skuDetails)
                }

                billingClient.launchBillingFlow((context as SettingsActivity),
                    BillingFlowParams.newBuilder().apply {

                        setSkuDetails(skuDetailsMap[skuList[0]])

                    }.build())
            }
        }))
    }
}