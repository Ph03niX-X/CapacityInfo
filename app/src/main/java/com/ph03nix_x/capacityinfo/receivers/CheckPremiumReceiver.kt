package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.Constants.CHECK_PREMIUM_HOST


/**
 * Created by Ph03niX-X on 08.09.2024
 * Ph03niX-X@outlook.com
 */

class CheckPremiumReceiver : BroadcastReceiver(), PremiumInterface {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val scheme = intent.data?.scheme
        val host = intent.data?.host

        when {
            action == "android.provider.Telephony.SECRET_CODE" && scheme == "android_secret_code"
                    && host == CHECK_PREMIUM_HOST -> checkPremium(context)
        }
    }
}