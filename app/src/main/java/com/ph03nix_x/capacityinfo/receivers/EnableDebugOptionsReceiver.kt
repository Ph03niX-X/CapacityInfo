package com.ph03nix_x.capacityinfo.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R

class EnableDebugOptionsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        when (intent.action) {

            "android.provider.Telephony.SECRET_CODE" -> {

                pref.edit().putBoolean("debug_options_is_enabled", true).apply()

                Toast.makeText(context, context.getString(R.string.debug_successfully_enabled),
                    Toast.LENGTH_LONG).show()
            }
        }
    }
}