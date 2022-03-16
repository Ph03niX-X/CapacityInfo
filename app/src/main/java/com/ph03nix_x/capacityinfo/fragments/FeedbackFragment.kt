package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utilities.Constants.DONATE_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.GOOGLE_PLAY_APP_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.TELEGRAM_DEVELOPER_LINK
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP

class FeedbackFragment : PreferenceFragmentCompat() {

    // Telegram
    private var telegramDeveloper: Preference? = null

    // Other
    private var email: Preference? = null
    private var rateTheApp: Preference? = null
    private var shareTheApp: Preference? = null
    private var donate: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

       val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.feedback_settings)

        // Telegram
        telegramDeveloper = findPreference("telegram_developer")

        telegramDeveloper?.setOnPreferenceClickListener {

            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_DEVELOPER_LINK))) }

            catch(e: ActivityNotFoundException) {

                val clipboardManager = requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("telegram_developer",
                    TELEGRAM_DEVELOPER_LINK)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), R.string.telegram_link_copied,
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        // Other
        email = findPreference("email")

        rateTheApp = findPreference("rate_the_app")

        shareTheApp = findPreference("share_the_app")

        donate = findPreference("donate")

        rateTheApp?.isVisible = isGooglePlay(requireContext()) || pref.getBoolean(
            IS_FORCIBLY_SHOW_RATE_THE_APP, resources.getBoolean(
                R.bool.is_forcibly_show_rate_the_app))

        email?.setOnPreferenceClickListener {

            try {

                val version = requireContext().packageManager?.getPackageInfo(
                    requireContext().packageName, 0)?.versionName
                val build = requireContext().packageManager?.getPackageInfo(
                    requireContext().packageName, 0)?.let { PackageInfoCompat
                    .getLongVersionCode(it).toString() }

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:${email
                    ?.summary}?subject=Capacity Info $version (Build $build). ${requireContext().getString(R.string.feedback)}")))
            }

            catch(e: ActivityNotFoundException) {

                val clipboardManager = requireContext().getSystemService(
                    Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("email", email?.summary)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), R.string.email_copied,
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        if(rateTheApp?.isVisible == true)
            rateTheApp?.setOnPreferenceClickListener {

                val manager = ReviewManagerFactory.create(requireContext())

                val request = manager.requestReviewFlow()

                request.addOnCompleteListener {

                    if(it.isSuccessful) {

                        val flow = manager.launchReviewFlow(requireActivity(), it.result)

                        if(!flow.isSuccessful) try {
                            requireContext().startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse(GOOGLE_PLAY_APP_LINK)))
                        }
                        catch(e: ActivityNotFoundException) {
                            Toast.makeText(requireContext(), requireContext().getString(
                                R.string.unknown_error), Toast.LENGTH_LONG).show()
                        }
                    }

                    else try {
                        requireContext().startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse(GOOGLE_PLAY_APP_LINK)))
                    }
                    catch(e: ActivityNotFoundException) {
                        Toast.makeText(requireContext(), requireContext().getString(
                            R.string.unknown_error), Toast.LENGTH_LONG).show()
                    }
                }

                true
            }

        shareTheApp?.setOnPreferenceClickListener {

            val linkToGooglePlay = GOOGLE_PLAY_APP_LINK

            startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {

                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, linkToGooglePlay)

            }, getString(R.string.share_the_app)))

            true
        }

        donate?.setOnPreferenceClickListener {

            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DONATE_LINK)))
            }
            catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }
    }
}