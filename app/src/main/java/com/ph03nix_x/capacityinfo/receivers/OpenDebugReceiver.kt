package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ph03nix_x.capacityinfo.activity.DebugActivity

class OpenDebugReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action) {

            "android.provider.Telephony.SECRET_CODE" -> {

                context.startActivity(Intent(context, DebugActivity::class.java).apply {

                    flags = Intent.FLAG_ACTIVITY_NEW_TASK

                })
            }
        }
    }
}