package com.ph03nix_x.capacityinfo.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.utils.Constants.GITHUB_LINK
import com.ph03nix_x.capacityinfo.utils.Constants.DESIGNER_LINK
import com.ph03nix_x.capacityinfo.utils.Constants.ROMANIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utils.Constants.BELARUSIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utils.Constants.HELP_WITH_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.isDonated
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay
import kotlinx.coroutines.*
import java.lang.IllegalStateException

class AboutFragment : PreferenceFragmentCompat(), BillingInterface {

    lateinit var pref: SharedPreferences

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var designer: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belarusianTranslation: Preference? = null
    private var helpWithTranslation: Preference? = null
    private var betaTester: Preference? = null
    private var donate: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.about)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        github = findPreference("github")

        designer = findPreference("designer")

        romanianTranslation = findPreference("romanian_translation")

        belarusianTranslation = findPreference("belarusian_translation")

        helpWithTranslation = findPreference("help_with_translation")

        betaTester = findPreference("become_a_beta_tester")

        betaTester?.isVisible = isInstalledGooglePlay(requireContext())

        donate = findPreference("donate")

        if(pref.getBoolean("is_hide_donate", false)) donate?.isVisible = false
        else donate?.isVisible = isInstalledGooglePlay && !isDonated

        version?.summary = requireContext().packageManager?.getPackageInfo(requireContext().packageName, 0)?.versionName

        build?.summary = requireContext().packageManager?.getPackageInfo(requireContext().packageName, 0)?.versionCode?.toString()

        buildDate?.summary = BuildConfig.BUILD_DATE

        developer?.setOnPreferenceClickListener {

            try {

                if(isInstalledGooglePlay)
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:${developer?.summary}")))

                else startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=${developer?.summary}")))
            }

            catch(e: ActivityNotFoundException) {}

            true
        }

        github?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        designer?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DESIGNER_LINK)))

            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        romanianTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ROMANIAN_TRANSLATION_LINK)))

            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        belarusianTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BELARUSIAN_TRANSLATION_LINK)))

            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        helpWithTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_WITH_TRANSLATION_LINK)))

            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        betaTester?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/${requireContext().packageName}")))

            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), getString(R.string.error_opening_link), Toast.LENGTH_LONG).show()
            }

            true
        }

        donate?.setOnPreferenceClickListener {

            if(isInstalledGooglePlay) {

                CoroutineScope(Dispatchers.Default).launch {

                    if(billingClient == null)
                        billingClient = onBillingClientBuilder(requireContext())
                    onBillingStartConnection()

                    delay(450)
                    try {

                        if(isDonated) {

                            donate?.isVisible = false

                            withContext(Dispatchers.Main) {

                                Toast.makeText(requireContext(), getString(R.string.thanks_for_the_donation), Toast.LENGTH_LONG).show()
                            }
                        }
                        else onPurchase(requireActivity(), "donate")
                    }
                    catch(e: IllegalStateException) {}
                }
            }

            true
        }
    }

    override fun onResume() {

        super.onResume()

        if(pref.getBoolean("is_hide_donate", false)) donate?.isVisible = false
        else donate?.isVisible = isInstalledGooglePlay && !isDonated
    }
}