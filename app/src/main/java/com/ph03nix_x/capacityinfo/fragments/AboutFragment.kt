package com.ph03nix_x.capacityinfo.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utils.Constants.githubLink
import com.ph03nix_x.capacityinfo.utils.Constants.designerLink
import com.ph03nix_x.capacityinfo.utils.Constants.romanianTranslationLink
import com.ph03nix_x.capacityinfo.utils.Constants.belorussianTranslationLink
import com.ph03nix_x.capacityinfo.utils.Constants.helpWithTranslationLink

class AboutFragment : PreferenceFragmentCompat() {

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var designer: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belorussianTranslation: Preference? = null
    private var helpWithTranslation: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.about)

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        github = findPreference("github")

        designer = findPreference("designer")

        romanianTranslation = findPreference("romanian_translation")

        belorussianTranslation = findPreference("belorussian_translation")

        helpWithTranslation = findPreference("help_with_translation")

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

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubLink)))

            true
        }

        designer?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(designerLink)))

            true
        }

        romanianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(romanianTranslationLink)))

            true
        }

        belorussianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(belorussianTranslationLink)))

            true
        }

        helpWithTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(helpWithTranslationLink)))

            true
        }
    }
}