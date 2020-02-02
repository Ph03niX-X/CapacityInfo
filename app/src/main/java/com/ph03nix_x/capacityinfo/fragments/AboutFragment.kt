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
import com.ph03nix_x.capacityinfo.utils.Constants.BELORUSSIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utils.Constants.HELP_WITH_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utils.Utils.billingClient
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay

class AboutFragment : PreferenceFragmentCompat(), BillingInterface {

    lateinit var pref: SharedPreferences

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var designer: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belorussianTranslation: Preference? = null
    private var helpWithTranslation: Preference? = null
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

        belorussianTranslation = findPreference("belorussian_translation")

        helpWithTranslation = findPreference("help_with_translation")

        donate = findPreference("donate")

        try {

            donate?.isVisible = isInstalledGooglePlay && !isPurchased(requireContext())
        }

        catch(e: Exception) {

            Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show()
        }


        version?.summary = requireContext().packageManager?.getPackageInfo(requireContext().packageName, 0)?.versionName

        build?.summary = requireContext().packageManager?.getPackageInfo(requireContext().packageName, 0)?.versionCode?.toString()

        buildDate?.summary = BuildConfig.BUILD_DATE

        developer?.setOnPreferenceClickListener {

            try {

                requireContext().startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:${developer?.summary}")))
            }

            catch(e: ActivityNotFoundException) {}

            true
        }

        github?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)))

            true
        }

        designer?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DESIGNER_LINK)))

            true
        }

        romanianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ROMANIAN_TRANSLATION_LINK)))

            true
        }

        belorussianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BELORUSSIAN_TRANSLATION_LINK)))

            true
        }

        helpWithTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(HELP_WITH_TRANSLATION_LINK)))

            true
        }

        if(isInstalledGooglePlay && billingClient != null && billingClient!!.isReady && !isPurchased(requireContext()))
        donate?.setOnPreferenceClickListener {

            onPurchase(requireActivity(), billingClient!!, "donate")

            true
        }
    }

    override fun onResume() {

        super.onResume()

        isInstalledGooglePlay = isInstalledGooglePlay(requireContext())

        if(billingClient == null && isInstalledGooglePlay) {

            billingClient = onBillingClientBuilder(requireContext())

            onBillingStartConnection(requireContext())
        }

        donate?.isVisible = isInstalledGooglePlay && !isPurchased(requireContext())
    }
}