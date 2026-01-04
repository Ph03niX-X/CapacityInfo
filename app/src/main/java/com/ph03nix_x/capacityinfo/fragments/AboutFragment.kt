package com.ph03nix_x.capacityinfo.fragments

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.isInstalledGooglePlay
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.CheckUpdateInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.utilities.Constants.GITHUB_LINK
import androidx.core.net.toUri
import com.ph03nix_x.capacityinfo.utilities.Constants.PRIVACY_POLICY_LINK

class AboutFragment : PreferenceFragmentCompat(), PremiumInterface, CheckUpdateInterface {

    private var checkUpdate: Preference? = null
    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var betaTester: Preference? = null
    private var orderID: Preference? = null
    private var privacyPolicy: Preference? = null

    private var isResume = false

    lateinit var pref: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.about_settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        checkUpdate = findPreference("check_update")

        github = findPreference("github")

        betaTester = findPreference("become_a_beta_tester")

        orderID = findPreference("order_id")

        privacyPolicy = findPreference("privacy_policy")

        checkUpdate?.apply {
            isVisible = isInstalledGooglePlay && MainApp.isGooglePlay(requireContext())
            setOnPreferenceClickListener {
                checkUpdateFromGooglePlay()
                true
            }
        }
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
                    "market://search?q=pub:${developer?.summary}".toUri()))

                else startActivity(Intent(Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/dev?id=8987494467330776667".toUri()))
            }

            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        github?.setOnPreferenceClickListener {

            try {

                startActivity(Intent(Intent.ACTION_VIEW, GITHUB_LINK.toUri()))
            }
            catch(e: ActivityNotFoundException) {

                Toast.makeText(requireContext(), e.message ?: e.toString(),
                    Toast.LENGTH_LONG).show()
            }

            true
        }

        betaTester?.apply {
            isVisible = isInstalledGooglePlay
            setOnPreferenceClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        "https://play.google.com/apps/testing/${requireContext().packageName}".toUri()))
                }
                catch(e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), e.message ?: e.toString(),
                        Toast.LENGTH_LONG).show()
                }
                true
            }
        }

        orderID?.apply {
            summary = PremiumInterface.orderID
            isVisible = !summary.isNullOrEmpty()
            setOnPreferenceClickListener {
                    val clipboardManager = requireContext().getSystemService(
                        Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("Order ID", orderID?.summary)
                    clipboardManager.setPrimaryClip(clipData)
                    Toast.makeText(requireContext(), R.string.order_id_copied,
                        Toast.LENGTH_LONG).show()
                    true
                }
        }

        privacyPolicy?.apply {
            setOnPreferenceClickListener {
                try {

                    startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_LINK.toUri()))
                }
                catch(e: ActivityNotFoundException) {

                    Toast.makeText(requireContext(), e.message ?: e.toString(),
                        Toast.LENGTH_LONG).show()
                }
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(isResume) betaTester?.isVisible = isInstalledGooglePlay else isResume = true
    }
}