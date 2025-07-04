package com.ph03nix_x.capacityinfo.interfaces

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES

/**
 * Created by Ph03niX-X on 05.05.2025
 * Ph03niX-X@outlook.com
 */

interface AdsInterface {

    private fun MainActivity.loadAds() {
        if(loadAdsCount >= 3) return
        InterstitialAd.load(this, resources.getString(R.string.ad_unit_id),
            AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if(loadAdsCount < 3) {
                        loadAdsCount++
                        interstitialAd.show(this@loadAds)
                    }
                }
            })
    }

    fun MainActivity.showAds() {
        if(PremiumInterface.isPremium || pref.getLong(NUMBER_OF_CHARGES, 0L) < 1L ||
            pref.getLong(NUMBER_OF_FULL_CHARGES, 0L) < 1L || loadAdsCount >= 3) return
        loadAds()
    }
}