package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import com.ph03nix_x.capacityinfo.interfaces.CheckUpdateInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface

class AboutFragment : PreferenceFragmentCompat(), PremiumInterface, CheckUpdateInterface {

    private var checkUpdate: Preference? = null
    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var betaTester: Preference? = null

    lateinit var pref: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.about_settings)

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        checkUpdate = findPreference("check_update")

        github = findPreference("github")

        betaTester = findPreference("become_a_beta_tester")

        checkUpdate?.apply {
            isVisible = isInstalledGooglePlay && MainApp.isGooglePlay(requireContext())
            setOnPreferenceClickListener {
                checkUpdateFromGooglePlay()
                true
            }
        }

        betaTester?.isVisible = isInstalledGooglePlay

        version?.summary = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireContext().packageManager?.getPackageInfo(requireContext().packageName,
                PackageManager.PackageInfoFlags.of(0))?.versionName
        else{
            requireContext().packageManager?.getPackageInfo(requireContext().packageName,
                0)?.versionName
        }

        build?.summary = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            requireContext().packageManager?.getPackageInfo(requireContext().packageName,
            PackageManager.PackageInfoFlags.of(0))?.let {
                PackageInfoCompat.getLongVersionCode(it).toString()
            }
        else {
            requireContext().packageManager?.getPackageInfo(requireContext().packageName,
                0)?.let { PackageInfoCompat.getLongVersionCode(it).toString() }
        }

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