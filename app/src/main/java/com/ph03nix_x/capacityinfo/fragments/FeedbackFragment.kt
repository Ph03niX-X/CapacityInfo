package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utils.Constants.TELEGRAM_LINK
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utils.Utils.isGooglePlay

class FeedbackFragment : PreferenceFragmentCompat() {

    private var telegram: Preference? = null
    private var email: Preference? = null
    private var rateTheApp: Preference? = null
    private var shareTheApp: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.feedback_settings)

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        telegram = findPreference("telegram")

        email = findPreference("email")

        rateTheApp = findPreference("rate_the_app")

        shareTheApp = findPreference("share_the_app")

        rateTheApp?.isVisible = isGooglePlay(requireContext()) || pref.getBoolean(
            IS_FORCIBLY_SHOW_RATE_THE_APP, false)

        telegram?.setOnPreferenceClickListener {

            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_LINK))) }

            catch(e: ActivityNotFoundException) {

                val clipboardManager = requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("telegram", TELEGRAM_LINK)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), getString(R.string.telegram_link_copied),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        email?.setOnPreferenceClickListener {

            try {

                val version = requireContext().packageManager?.getPackageInfo(
                    requireContext().packageName, 0)?.versionName
                val build = requireContext().packageManager?.getPackageInfo(requireContext()
                    .packageName, 0)?.versionCode?.toString()

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:${email
                    ?.summary}?subject=Capacity Info $version (Build $build). ${requireContext().getString(R.string.feedback)}")))
            }

            catch(e: ActivityNotFoundException) {

                val clipboardManager = requireContext().getSystemService(
                    Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("email", email?.summary)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), getString(R.string.email_copied),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        if(rateTheApp?.isVisible!!)

            rateTheApp?.setOnPreferenceClickListener {

                startActivity(Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=${requireContext().packageName}")))

                true
            }

        shareTheApp?.setOnPreferenceClickListener {

            val linkToGooglePlay = "https://play.google.com/store/apps/details?id=${requireContext()
                .packageName}"

            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {

                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, linkToGooglePlay)

            }, getString(R.string.share_the_app)))

            true
        }
    }
}