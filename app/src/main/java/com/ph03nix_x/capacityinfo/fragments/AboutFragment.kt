package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.utilities.Constants.GITHUB_LINK
import com.ph03nix_x.capacityinfo.MainApp.Companion.isInstalledGooglePlay
import com.ph03nix_x.capacityinfo.helpers.LocaleHelper
import com.ph03nix_x.capacityinfo.interfaces.DonateInterface
import com.ph03nix_x.capacityinfo.utilities.Constants.UKRAINIAN_TRANSLATION_LINK
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys

class AboutFragment : PreferenceFragmentCompat(), DonateInterface {

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var belarusianTranslation: Preference? = null
    private var ukrainianTranslation: Preference? = null
    private var betaTester: Preference? = null
    private var orderId: Preference? = null

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

        belarusianTranslation = findPreference("belarusian_translation")

        ukrainianTranslation = findPreference("ukrainian_translation")

        betaTester = findPreference("become_a_beta_tester")

        orderId = findPreference("order_id")

        betaTester?.isVisible = isInstalledGooglePlay

        version?.summary = requireContext().packageManager?.getPackageInfo(
            requireContext().packageName, 0)?.versionName

        build?.summary = requireContext().packageManager?.getPackageInfo(requireContext().packageName,
            0)?.let { PackageInfoCompat.getLongVersionCode(it).toString() }

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

        orderId?.apply {

            isVisible = getOrderId() != null
            summary = getOrderId()

            setOnPreferenceClickListener {
                val clipboardManager = requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("order_id", orderId?.summary)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(requireContext(), R.string.order_id_copied, Toast.LENGTH_LONG).show()
                true
            }
        }

    }

    override fun onResume() {

        super.onResume()

        betaTester?.isVisible = isInstalledGooglePlay
    }
}