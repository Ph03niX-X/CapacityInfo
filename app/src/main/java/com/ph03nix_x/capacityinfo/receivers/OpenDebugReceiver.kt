package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.utils.Utils.launchActivity
import com.ph03nix_x.capacityinfo.activities.SettingsActivity

class OpenDebugReceiver : BroadcastReceiver() {

    companion object {

        var isDebug = false
    }

    override fun onReceive(context: Context, intent: Intent) {

        if(!isDebug)
        when(intent.action) {

            "android.provider.Telephony.SECRET_CODE" -> {

                isDebug = !isDebug

                SettingsActivity.instance?.finish()

                launchActivity(context,
                    SettingsActivity::class.java, arrayListOf(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }
}