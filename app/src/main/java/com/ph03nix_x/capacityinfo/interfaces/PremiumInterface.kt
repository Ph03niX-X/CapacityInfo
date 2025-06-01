package com.ph03nix_x.capacityinfo.interfaces

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabaseLockedException
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.queryPurchaseHistory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.PREMIUM_ID
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.TOKEN_COUNT
import com.ph03nix_x.capacityinfo.TOKEN_PREF
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.receivers.CheckPremiumReceiver
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.CheckPremiumJob
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BYPASS_DND
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CAPACITY_IN_WH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_CHARGING_DISCHARGE_CURRENT_IN_WATT
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_DISCHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_BATTERY_IS_FULLY_CHARGED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_FULL_CHARGE_REMINDER
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_NOTIFY_OVERHEAT_OVERCOOL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BATTERY_INFORMATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BATTERY_LEVEL_IN_STATUS_BAR
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_EXPANDED_NOTIFICATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TEXT_FONT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds
import androidx.core.content.edit
import androidx.core.net.toUri

/**
 * Created by Ph03niX-X on 04.12.2021
 * Ph03niX-X@outlook.com
 */

@SuppressLint("StaticFieldLeak")
interface PremiumInterface: PurchasesUpdatedListener, NavigationInterface {

    companion object {

        private var mProductDetailsList: List<ProductDetails>? = null

        var premiumContext: Context? = null
        var premiumActivity: Activity? = null
        var billingClient: BillingClient? = null

        var isPremium = false
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if(premiumContext == null) premiumContext =
            MainActivity.instance ?: CapacityInfoService.instance
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                CoroutineScope(Dispatchers.Default).launch {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
            CoroutineScope(Dispatchers.IO).launch {
                val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)
                if(purchases != null) pref.edit { putString(TOKEN_PREF, purchases[0].purchaseToken) }
                val tokenPref = pref.getString(TOKEN_PREF, null)
                isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                if(isPremium) premiumFeaturesUnlocked(premiumContext!!, false)
                withContext(Dispatchers.Main) {
                    ServiceHelper.checkPremiumJobSchedule(premiumContext!!)
                }
            }
        }
    }

    fun initiateBilling(isPurchasePremium: Boolean) {
        if(premiumContext == null)
            premiumContext = MainActivity.instance ?: CapacityInfoService.instance
        billingClient = BillingClient.newBuilder(premiumContext!!)
            .setListener(purchasesUpdatedListener()).enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()
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
        if(premiumContext == null)
            premiumContext = MainActivity.instance ?: CapacityInfoService.instance
        val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                withContext(Dispatchers.IO) {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {
                        if (it.responseCode == BillingResponseCode.OK) {
                            pref.edit { putString(TOKEN_PREF, purchase.purchaseToken) }
                            val tokenPref = pref.getString(TOKEN_PREF, null)
                            isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                            if(isPremium) {
                                MainApp.isRequestPurchasePremium = false
                                launch {
                                    premiumFeaturesUnlocked(premiumContext!!)
                                }
                            }
                            launch {
                                delay(2.5.seconds)
                                ServiceHelper.checkPremiumJobSchedule(premiumContext!!)
                            }
                        }
                    }
                }
            } else {
                pref.edit { putString(TOKEN_PREF, purchase.purchaseToken) }
                val tokenPref = pref.getString(TOKEN_PREF, null)
                isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                if(isPremium) {
                    MainApp.isRequestPurchasePremium = false
                    premiumFeaturesUnlocked(premiumContext!!)
                }
                ServiceHelper.checkPremiumJobSchedule(premiumContext!!)
            }

        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            pref.edit { putString(TOKEN_PREF, purchase.purchaseToken) }
            val tokenPref = pref.getString(TOKEN_PREF, null)
            isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
            if(isPremium) {
                MainApp.isRequestPurchasePremium = false
                premiumFeaturesUnlocked(premiumContext!!)
            }
            ServiceHelper.checkPremiumJobSchedule(premiumContext!!)
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

    private suspend fun premiumFeaturesUnlocked(context: Context, isShowToast: Boolean = true) {
        withContext(Dispatchers.Main) {
            if(isShowToast)
                Toast.makeText(context, R.string.premium_features_unlocked, Toast.LENGTH_LONG).show()
            val mainActivity = MainActivity.instance
            val historyFragment = HistoryFragment.instance
            val isHistoryNotEmpty = HistoryHelper.isHistoryNotEmpty(context)
            mainActivity?.apply {
                toolbar.menu?.apply {
                    findItem(R.id.premium)?.isVisible = false
                    findItem(R.id.history_premium)?.isVisible = false
                    findItem(R.id.clear_history)?.isVisible = isHistoryNotEmpty
                }
                historyFragment?.binding?.apply {
                    refreshEmptyHistory.isVisible = !isHistoryNotEmpty
                    emptyHistoryLayout.isVisible = !isHistoryNotEmpty
                    historyRecyclerView.isVisible = isHistoryNotEmpty
                    refreshHistory.isVisible = isHistoryNotEmpty
                    emptyHistoryText.text = if(!isHistoryNotEmpty)
                        context.resources?.getText(R.string.empty_history_text) else null
                }
            }
        }
    }

    fun MainActivity.showPremiumDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setIcon(R.drawable.ic_premium_24)
            setTitle(getString(R.string.get_premium))
            setMessage(getString(R.string.premium_dialog))
            setPositiveButton(R.string.purchase_premium) { d, _ ->
                if(MainApp.isInstalledGooglePlay
                    && MainApp.isGooglePlay(this@showPremiumDialog)) {
                    if(premiumContext == null) premiumContext = this@showPremiumDialog
                    if(billingClient?.isReady == true) purchasePremium()
                    else initiateBilling(true)
                }
                else if(!MainApp.isGooglePlay(this@showPremiumDialog))
                    showInstallAppFromGooglePlayDialog(this@showPremiumDialog)
                else showNotInstalledGooglePlayDialog(this@showPremiumDialog)

                d.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { d, _ ->
                d.dismiss()
            }
            setCancelable(false)
            show()
        }
    }

    private fun showInstallAppFromGooglePlayDialog(context: Context) {
        MaterialAlertDialogBuilder(context).apply {
            setIcon(R.drawable.ic_instruction_not_supported_24dp)
            setTitle(R.string.premium_purchase_error)
            setMessage(R.string.install_the_app_from_gp)
            setPositiveButton(android.R.string.ok) { _, _ ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Constants.GOOGLE_PLAY_APP_LINK.toUri()))
                }
                catch(_: ActivityNotFoundException) {}
            }
            setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
            show()
        }
    }

    private fun showNotInstalledGooglePlayDialog(context: Context) {
     MaterialAlertDialogBuilder(context).apply {
         setIcon(R.drawable.ic_instruction_not_supported_24dp)
         setTitle(R.string.error)
         setMessage(R.string.not_installed_google_play_dialog)
         setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
         show()
     }
    }

    fun purchasePremium() {
        if(!mProductDetailsList.isNullOrEmpty()) {
            MainApp.isRequestPurchasePremium = false
            val productDetailsParamsList = listOf(BillingFlowParams.ProductDetailsParams
                .newBuilder().setProductDetails(mProductDetailsList!![0]).build())
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            billingClient?.launchBillingFlow(premiumActivity!!, billingFlowParams)
        }
    }

    fun checkPremium() {
        if(premiumContext == null)
            premiumContext = MainActivity.instance ?: CapacityInfoService.instance
        CoroutineScope(Dispatchers.IO).launch {
            MainApp.isRequestPurchasePremium = false
            val pref = PreferenceManager.getDefaultSharedPreferences(premiumContext!!)
            var tokenPref = pref.getString(TOKEN_PREF, null)
            if(tokenPref != null && tokenPref.count() == TOKEN_COUNT) isPremium = true
            else if(tokenPref != null && tokenPref.count() != TOKEN_COUNT)
                pref.edit { remove(TOKEN_PREF) }
           else if(tokenPref == null || tokenPref.count() != TOKEN_COUNT) {
                if(billingClient?.isReady != true) initiateBilling(false)
                delay(2.5.seconds)
                if(billingClient?.isReady == true) {
                    val params = QueryPurchaseHistoryParams.newBuilder()
                       .setProductType(ProductType.INAPP)
                   val purchaseHistoryResult = billingClient?.queryPurchaseHistory(params.build())
                   val purchaseHistoryRecordList = purchaseHistoryResult?.purchaseHistoryRecordList
                   if(!purchaseHistoryRecordList.isNullOrEmpty()) {
                       pref.edit { putString(TOKEN_PREF, purchaseHistoryRecordList[0].purchaseToken) }
                       tokenPref = pref.getString(TOKEN_PREF, null)
                       isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                       withContext(Dispatchers.Main) {
                           MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible =
                               isPremium
                       }
                       if(!isPremium) {
                           removePremiumFeatures(premiumContext!!)
                           MainApp.isRequestPurchasePremium = true
                       }
                       delay(5.seconds)
                       billingClient?.endConnection()
                       billingClient = null
                   }
                    if(!isPremium) {
                        removePremiumFeatures(premiumContext!!)
                        MainApp.isRequestPurchasePremium = true
                        delay(5.seconds)
                        billingClient?.endConnection()
                        billingClient = null
                    }
                }
                else {
                    MainApp.isRequestPurchasePremium = false
                    delay(5.seconds)
                    billingClient?.endConnection()
                    billingClient = null
                }
           }
        }
    }

    fun CheckPremiumJob.checkPremiumJob() {
        CoroutineScope(Dispatchers.IO).launch {
            val pref = PreferenceManager.getDefaultSharedPreferences(this@checkPremiumJob)
            if(billingClient?.isReady != true) initiateBilling(false)
            delay(2.5.seconds)
            if(billingClient?.isReady == true) {
                val params = QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(ProductType.INAPP)
                val purchaseHistoryResult = billingClient?.queryPurchaseHistory(params.build())
                val purchaseHistoryRecordList = purchaseHistoryResult?.purchaseHistoryRecordList
                if(!purchaseHistoryRecordList.isNullOrEmpty()) {
                    pref.edit { putString(TOKEN_PREF, purchaseHistoryRecordList[0].purchaseToken) }
                    val tokenPref = pref.getString(TOKEN_PREF, null)
                    isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                    withContext(Dispatchers.Main) {
                        MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible =
                            isPremium
                    }
                    MainApp.isRequestPurchasePremium = !isPremium
                    delay(5.seconds)
                    billingClient?.endConnection()
                    billingClient = null
                }
                else {
                    if(pref.contains(TOKEN_PREF)) pref.edit { remove(TOKEN_PREF) }
                    val tokenPref = pref.getString(TOKEN_PREF, null)
                    isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                    withContext(Dispatchers.Main) {
                        MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible =
                            isPremium
                    }
                    MainApp.isRequestPurchasePremium = !isPremium
                    delay(5.seconds)
                    billingClient?.endConnection()
                    billingClient = null
                }
                if(!isPremium) removePremiumFeatures(this@checkPremiumJob)
            }
        }
    }

    fun CheckPremiumReceiver.checkPremium(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, context.getString(R.string.checking_premium),
                    Toast.LENGTH_LONG).show()
            }
            val pref = PreferenceManager.getDefaultSharedPreferences(context)
            if(billingClient?.isReady != true) initiateBilling(false)
            delay(2.5.seconds)
            if(billingClient?.isReady == true) {
                val params = QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(ProductType.INAPP)
                val purchaseHistoryResult = billingClient?.queryPurchaseHistory(params.build())
                val purchaseHistoryRecordList = purchaseHistoryResult?.purchaseHistoryRecordList
                if(!purchaseHistoryRecordList.isNullOrEmpty()) {
                    pref.edit { putString(TOKEN_PREF, purchaseHistoryRecordList[0].purchaseToken) }
                    val tokenPref = pref.getString(TOKEN_PREF, null)
                    isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                    withContext(Dispatchers.Main) {
                        MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible =
                            isPremium
                    }
                    MainApp.isRequestPurchasePremium = !isPremium
                    delay(5.seconds)
                    billingClient?.endConnection()
                    billingClient = null
                }
                else {
                    if(pref.contains(TOKEN_PREF)) pref.edit { remove(TOKEN_PREF) }
                    val tokenPref = pref.getString(TOKEN_PREF, null)
                    isPremium = tokenPref != null && tokenPref.count() == TOKEN_COUNT
                    withContext(Dispatchers.Main) {
                        MainActivity.instance?.toolbar?.menu?.findItem(R.id.premium)?.isVisible =
                            isPremium
                    }
                    MainApp.isRequestPurchasePremium = !isPremium
                    delay(5.seconds)
                    billingClient?.endConnection()
                    billingClient = null
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.successful_premium_check,
                        "$isPremium"), Toast.LENGTH_LONG).show()
                }
            }
            else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.premium_check_error),
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun removePremiumFeatures(context: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        arrayListOf(IS_SHOW_BATTERY_LEVEL_IN_STATUS_BAR, IS_SHOW_BATTERY_INFORMATION,
            IS_SHOW_EXPANDED_NOTIFICATION, IS_BYPASS_DND, IS_NOTIFY_OVERHEAT_OVERCOOL,
            IS_NOTIFY_BATTERY_IS_FULLY_CHARGED, IS_NOTIFY_FULL_CHARGE_REMINDER,
            IS_NOTIFY_BATTERY_IS_CHARGED, IS_NOTIFY_BATTERY_IS_DISCHARGED,
            TEXT_FONT, IS_CAPACITY_IN_WH, IS_CHARGING_DISCHARGE_CURRENT_IN_WATT,
            IS_RESET_SCREEN_TIME_AT_ANY_CHARGE_LEVEL, TAB_ON_APPLICATION_LAUNCH,
            IS_ENABLED_OVERLAY).forEach {
            with(pref) {
                edit().apply {
                    if(contains(it)) remove(it)
                    apply()
                }
            }
            }

        try {
            if(HistoryHelper.isHistoryNotEmpty(context)) HistoryHelper.clearHistory(context)
        }
        catch (_: SQLiteDatabaseLockedException) {}
        finally {
            withContext(Dispatchers.Main) {
                ServiceHelper.cancelJob(context,
                    Constants.IS_NOTIFY_FULL_CHARGE_REMINDER_JOB_ID)
                if(OverlayService.instance != null)
                    ServiceHelper.stopService(context, OverlayService::class.java)
            }
        }
    }
}