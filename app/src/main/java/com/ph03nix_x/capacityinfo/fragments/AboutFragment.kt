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
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utilities.Constants.GITHUB_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.ROMANIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.BELARUSIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.MainApp.Companion.isInstalledGooglePlay
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.utilities.Constants.SPANISH_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.UKRAINIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys

class AboutFragment : PreferenceFragmentCompat() {

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var spanishTranslation: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belarusianTranslation: Preference? = null
    private var ukrainianTranslation: Preference? = null
    private var betaTester: Preference? = null

    lateinit var pref: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        LocaleHelper.setLocale(requireContext(), pref.getString(
            PreferencesKeys.LANGUAGE, null) ?: MainApp.defLang)

        addPreferencesFromResource(R.xml.about_settings)

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        github = findPreference("github")

        spanishTranslation = findPreference("spanish_translation")

        romanianTranslation = findPreference("romanian_translation")

        belarusianTranslation = findPreference("belarusian_translation")

        ukrainianTranslation = findPreference("ukrainian_translation")

        betaTester = findPreference("become_a_beta_tester")

        betaTester?.isVisible = isInstalledGooglePlay

        version?.summary = requireContext().packageManager?.getPackageInfo(
            requireContext().packageName, 0)?.versionName

        build?.summary = requireContext().packageManager?.getPackageInfo(
            requireContext().packageName, 0)?.versionCode?.toString()

        buildDate?.summary = BuildConfig.BUILD_DATE

        developer?.setOnPreferenceClickListener {

            try {

                if(isInstalledGooglePlay)
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:${developer?.summary}")))

                else startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/developer?id=${developer
                        ?.summary}")))
            }

            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        github?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        spanishTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SPANISH_TRANSLATION_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        romanianTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ROMANIAN_TRANSLATION_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        belarusianTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BELARUSIAN_TRANSLATION_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        ukrainianTranslation?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UKRAINIAN_TRANSLATION_LINK)))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(), Toast.LENGTH_LONG)
                    .show()
            }

            true
        }

        betaTester?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/apps/testing/${requireContext()
                        .packageName}")))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }
    }

    override fun onResume() {

        super.onResume()

        betaTester?.isVisible = isInstalledGooglePlay
    }
}