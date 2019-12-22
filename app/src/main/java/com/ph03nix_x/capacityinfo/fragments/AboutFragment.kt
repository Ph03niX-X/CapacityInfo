package com.ph03nix_x.capacityinfo.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R

class AboutFragment : PreferenceFragmentCompat() {

    private val githubLink = "https://github.com/Ph03niX-X/CapacityInfo"
    private val designerLink = "https://t.me/F0x1d"
    private val romanianTranslationLink = "https://github.com/ygorigor"
    private val belorussianTranslationLink = "https://t.me/DrCyanogen"

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var designer: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belorussianTranslation: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.about)

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            if(pref.contains(Preferences.IsAutoDarkMode.prefKey)) pref.edit().remove(Preferences.IsAutoDarkMode.prefKey).apply()
        }

        else if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

        else if(pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        developer = findPreference("developer")

        version = findPreference("version")

        build = findPreference("build")

        buildDate = findPreference("build_date")

        github = findPreference("github")

        designer = findPreference("designer")

        romanianTranslation = findPreference("romanian_translation")

        belorussianTranslation = findPreference("belorussian_translation")

        version?.summary = context?.packageManager?.getPackageInfo(context!!.packageName, 0)?.versionName

        build?.summary = context?.packageManager?.getPackageInfo(context!!.packageName, 0)?.versionCode?.toString()

        buildDate?.summary = BuildConfig.BUILD_DATE

        developer?.setOnPreferenceClickListener {

            try { context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:${developer?.summary}"))) }

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
    }
}