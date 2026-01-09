package com.ph03nix_x.capacityinfo.interfaces

import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.services.AdsJobService
import com.ph03nix_x.capacityinfo.utilities.Constants.ADS_JOB_ID
import com.ph03nix_x.capacityinfo.utilities.Constants.ADS_JOB_SERVICE_PERIODIC
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES

/**
 * Created by Ph03niX-X on 05.05.2025
 * Ph03niX-X@outlook.com
 */

interface AdsInterface {

    private fun MainActivity.loadAds(adUnit: String) {
        if(adUnit == resources.getString(R.string.ad_unit_id) && loadAdsCount >= 3) return
        else if(adUnit != resources.getString(R.string.ad_unit_id) && loadAdsCount >= 3) {
            Toast.makeText(this, getString(R.string.try_again_in_an_hour),
                Toast.LENGTH_LONG).show()
            return
        }
        InterstitialAd.load(this, adUnit,
            AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    if(loadAdsCount < 3) {
                        loadAdsCount++
                        if(loadAdsCount >= 3) {
                            ServiceHelper.apply {
                                stopService(this@loadAds,
                                    AdsJobService::class.java)
                                jobSchedule(this@loadAds,
                                    AdsJobService::class.java,
                                    ADS_JOB_ID, ADS_JOB_SERVICE_PERIODIC)
                            }
                        }
                        interstitialAd.show(this@loadAds)
                    }
                }
            })
    }

    fun MainActivity.showAds(adUnit: String) {
        if(adUnit == resources.getString(R.string.support_ad_unit_id) && loadAdsCount >= 3) {
            Toast.makeText(this, getString(R.string.try_again_in_an_hour),
                Toast.LENGTH_LONG).show()
            return
        } else if(adUnit == resources.getString(R.string.ad_unit_id) && PremiumInterface.isPremium
            || pref.getLong(NUMBER_OF_CHARGES, 0L) < 1L ||
            pref.getLong(NUMBER_OF_FULL_CHARGES, 0L) < 1L || loadAdsCount >= 3)
            return
        loadAds(adUnit)
    }
}