package com.ph03nix_x.capacityinfo.fragment

import android.app.NotificationManager
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.BuildConfig
import com.ph03nix_x.capacityinfo.Preferences
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activity.MainActivity
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.isStopCheck
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.ph03nix_x.capacityinfo.Utils

const val githubLink = "https://github.com/Ph03niX-X/CapacityInfo"
const val designerLink = "https://t.me/F0x1d"
const val romanianTranslationLink = "https://github.com/ygorigor"
const val belorussianTranslationLink = "https://t.me/DrCyanogen"
const val telegramLink = "https://t.me/Ph03niX_X"
class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var pref: SharedPreferences

    // Service and Notification

    private var enableService: SwitchPreferenceCompat? = null
    private var showStopService: SwitchPreferenceCompat? = null
    private var showInformationWhileCharging: SwitchPreferenceCompat? = null
    private var serviceHours: SwitchPreferenceCompat? = null
    private var showCapacityAddedInNotification: SwitchPreferenceCompat? = null
    private var showInformationDuringDischarge: SwitchPreferenceCompat? = null
    private var showLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var showCapacityAddedLastChargeTimeInNotification: SwitchPreferenceCompat? = null
    private var openNotificationCategorySettings: Preference? = null
    private var notificationRefreshRate: Preference? = null

    // Appearance

    private var autoDarkMode: SwitchPreferenceCompat? = null
    private var darkMode: SwitchPreferenceCompat? = null

    // Other

    private var temperatureInFahrenheit: SwitchPreferenceCompat? = null
    private var voltageInMv: SwitchPreferenceCompat? = null
    private var changeDesignCapacity: Preference? = null

    // About

    private var developer: Preference? = null
    private var version: Preference? = null
    private var build: Preference? = null
    private var buildDate: Preference? = null
    private var github: Preference? = null
    private var designer: Preference? = null
    private var romanianTranslation: Preference? = null
    private var belorussianTranslation: Preference? = null

    // Feedback

    private var telegram: Preference? = null
    private var email: Preference? = null
    private var rateTheApp: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.settings)

        pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {

            AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        }

        else {

            if(!pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true)) {

                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Service and Notification

        enableService = findPreference(Preferences.IsEnableService.prefKey)!!

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        showStopService = findPreference(Preferences.IsShowServiceStop.prefKey)

        showInformationWhileCharging = findPreference(Preferences.IsShowInformationWhileCharging.prefKey)

        serviceHours = findPreference(Preferences.IsServiceHours.prefKey)

        showCapacityAddedInNotification = findPreference(Preferences.IsShowCapacityAddedInNotification.prefKey)

        showInformationDuringDischarge = findPreference(Preferences.IsShowInformationDuringDischarge.prefKey)

        showLastChargeTimeInNotification = findPreference(Preferences.IsShowLastChargeTimeInNotification.prefKey)

        showCapacityAddedLastChargeTimeInNotification = findPreference(Preferences.IsShowCapacityAddedLastChargeInNotification.prefKey)

        openNotificationCategorySettings = findPreference("open_notification_category_settings")

        notificationRefreshRate = findPreference(Preferences.NotificationRefreshRate.prefKey)

        showStopService?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showInformationWhileCharging?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        serviceHours?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationWhileCharging.prefKey, true)

        showInformationDuringDischarge?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        showLastChargeTimeInNotification?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        openNotificationCategorySettings?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)

        notificationRefreshRate?.isEnabled = pref.getBoolean(Preferences.IsEnableService.prefKey, true)
                && pref.getBoolean(Preferences.IsShowInformationDuringDischarge.prefKey, true)

        enableService?.setOnPreferenceChangeListener { _, newValue ->

            if(!(newValue as Boolean) && CapacityInfoService.instance != null) requireActivity().stopService(Intent(requireContext(), CapacityInfoService::class.java))

            else if(newValue && CapacityInfoService.instance == null) Utils.startService(context)

            showInformationWhileCharging?.isEnabled = newValue
            serviceHours?.isEnabled = newValue
            showInformationDuringDischarge?.isEnabled = newValue
            showLastChargeTimeInNotification?.isEnabled = newValue
            showCapacityAddedLastChargeTimeInNotification?.isEnabled = newValue
            showStopService?.isEnabled = newValue
            showCapacityAddedInNotification?.isEnabled = newValue
            openNotificationCategorySettings?.isEnabled = newValue
            notificationRefreshRate?.isEnabled = newValue

            return@setOnPreferenceChangeListener true
        }

        showStopService?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) {

                isStopCheck = true

                updateNotification()
            }

            return@setOnPreferenceChangeListener true
        }

        showInformationWhileCharging?.setOnPreferenceChangeListener { _ , newValue ->

            serviceHours?.isEnabled = newValue as Boolean

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        serviceHours?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        showCapacityAddedInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        showInformationDuringDischarge?.setOnPreferenceChangeListener { _ , newValue ->

            showLastChargeTimeInNotification?.isEnabled = newValue as Boolean
            notificationRefreshRate?.isEnabled = newValue

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        showLastChargeTimeInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        showCapacityAddedLastChargeTimeInNotification?.setOnPreferenceChangeListener { _, _ ->

            if(CapacityInfoService.instance != null) updateNotification()

            return@setOnPreferenceChangeListener true
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)

            openNotificationCategorySettings?.setOnPreferenceClickListener {

                openNotificationCategorySettings()

                return@setOnPreferenceClickListener true
            }

        else openNotificationCategorySettings?.isVisible = false

        notificationRefreshRate?.setOnPreferenceClickListener {
            notificationRefreshRateDialog()
            return@setOnPreferenceClickListener true
        }

        // Appearance

        autoDarkMode = findPreference(Preferences.IsAutoDarkMode.prefKey)

        darkMode = findPreference(Preferences.IsDarkMode.prefKey)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) darkMode?.isEnabled = !pref.getBoolean(Preferences.IsAutoDarkMode.prefKey, true)

        autoDarkMode?.setOnPreferenceChangeListener { _, newValue ->

            MainActivity.instance?.recreate()

            if(!(newValue as Boolean)) {

                darkMode?.isEnabled = true

                AppCompatDelegate.setDefaultNightMode(if(pref.getBoolean(Preferences.IsDarkMode.prefKey, false))
                    AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
            }

            else {

                darkMode?.isEnabled = false

                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            return@setOnPreferenceChangeListener true
        }

        darkMode?.setOnPreferenceChangeListener { _, newValue ->

            AppCompatDelegate.setDefaultNightMode(if(newValue as Boolean)
                AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)

            return@setOnPreferenceChangeListener true
        }

        // Other

        temperatureInFahrenheit = findPreference(Preferences.TemperatureInFahrenheit.prefKey)

        voltageInMv = findPreference(Preferences.VoltageInMv.prefKey)

        changeDesignCapacity = findPreference("change_design_capacity")

        temperatureInFahrenheit?.setOnPreferenceChangeListener { _, _ ->

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification()

            return@setOnPreferenceChangeListener true
        }

        voltageInMv?.setOnPreferenceChangeListener { _, _ ->

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification()

            return@setOnPreferenceChangeListener true
        }

        changeDesignCapacity?.setOnPreferenceClickListener {

            changeDesignCapacity()

            return@setOnPreferenceClickListener true
        }

        // About

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

            try {

                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:${developer?.summary}")))
            }

            catch(e: ActivityNotFoundException) {}

            return@setOnPreferenceClickListener true
        }

        github?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(githubLink)))

            return@setOnPreferenceClickListener true
        }

        designer?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(designerLink)))

            return@setOnPreferenceClickListener true
        }

        romanianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(romanianTranslationLink)))

            return@setOnPreferenceClickListener true
        }

        belorussianTranslation?.setOnPreferenceClickListener {

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(belorussianTranslationLink)))

            return@setOnPreferenceClickListener true
        }

        // Feedback

        telegram = findPreference("telegram")

        email = findPreference("email")

        rateTheApp = findPreference("rate_the_app")

        rateTheApp?.isVisible = isGooglePlay()

        telegram?.setOnPreferenceClickListener {

            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(telegramLink))) }

            catch (e: ActivityNotFoundException) {

                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("telegram", telegramLink)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context!!, getString(R.string.telegram_link_copied), Toast.LENGTH_LONG).show()
            }

            return@setOnPreferenceClickListener true
        }

        email?.setOnPreferenceClickListener {

            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("mailto:${email?.summary}?subject=Capacity Info ${version?.summary} (Build ${build?.summary}). Feedback"))) }

            catch (e: ActivityNotFoundException) {

                val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("email", email?.summary)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(context!!, getString(R.string.email_copied), Toast.LENGTH_LONG).show()
            }

            return@setOnPreferenceClickListener true
        }

        if(isGooglePlay())

            rateTheApp?.setOnPreferenceClickListener {

                context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context?.packageName}")))

                return@setOnPreferenceClickListener true
            }
    }

    private fun isGooglePlay() = "com.android.vending" == context?.packageManager?.getInstallerPackageName(context!!.packageName)

    private fun updateNotification() {

        Handler().postDelayed({

            CapacityInfoService.instance?.updateNotification()

        }, 50)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNotificationCategorySettings() {

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = notificationManager.getNotificationChannel("service_channel")

        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {

            putExtra(Settings.EXTRA_CHANNEL_ID, notificationChannel.id)

            putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)

        }

        startActivity(intent)
    }

    private fun notificationRefreshRateDialog() {

        val dialog = MaterialAlertDialogBuilder(requireActivity())

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.notification_refresh_rate, null)

        dialog.setView(view)

        val notificationRefreshRate = view.findViewById<TextView>(R.id.notification_refresh_rate_textView)

        val notificationRefreshRateSeekBar = view.findViewById<SeekBar>(R.id.notification_refresh_rate_seekBar)

        val notificationRefreshRatePref = pref.getLong(Preferences.NotificationRefreshRate.prefKey,40)

        setProgress(notificationRefreshRatePref, notificationRefreshRateSeekBar)

        getNotificationRefreshRateTime(notificationRefreshRatePref, notificationRefreshRate, notificationRefreshRateSeekBar)

        notificationRefreshRateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { progressChanged(progress, notificationRefreshRate) }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialogApply(dialog, notificationRefreshRateSeekBar.progress)
    }

    private fun setProgress(notificationRefreshRate: Long, notificationRefreshRateSeekBar: SeekBar) {

        when(notificationRefreshRate) {

            5.toLong() -> notificationRefreshRateSeekBar.progress = 0
            10.toLong() -> notificationRefreshRateSeekBar.progress = 9
            15.toLong() -> notificationRefreshRateSeekBar.progress = 17
            20.toLong() -> notificationRefreshRateSeekBar.progress = 25
            25.toLong() -> notificationRefreshRateSeekBar.progress = 33
            30.toLong() -> notificationRefreshRateSeekBar.progress = 41
            35.toLong() -> notificationRefreshRateSeekBar.progress = 49
            40.toLong() -> notificationRefreshRateSeekBar.progress = 57
            45.toLong() -> notificationRefreshRateSeekBar.progress = 65
            50.toLong() -> notificationRefreshRateSeekBar.progress = 73
            55.toLong() -> notificationRefreshRateSeekBar.progress = 81
            60.toLong() -> notificationRefreshRateSeekBar.progress = 100
        }
    }

    private fun getNotificationRefreshRateTime(notificationRefreshRatePref: Long, notificationRefreshRate: TextView, notificationRefreshRateSeekBar: SeekBar) {

        val sleepArray = arrayOf<Long>(5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60)

        if(notificationRefreshRatePref !in sleepArray) {

            notificationRefreshRateSeekBar.progress = 57

            pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()
        }

        notificationRefreshRate.text = getString(if(notificationRefreshRatePref != 60.toLong()) R.string.seconds
        else R.string.minute, if(notificationRefreshRatePref < 60) notificationRefreshRatePref.toString() else "1")
    }

    private fun progressChanged(progress: Int, notificationRefreshRate: TextView) {

        when(progress) {

            in 0..8 -> notificationRefreshRate.text = getString(R.string.seconds, "5")

            in 9..16 -> notificationRefreshRate.text = getString(R.string.seconds, "10")

            in 17..24 -> notificationRefreshRate.text = getString(R.string.seconds, "15")

            in 25..32 -> notificationRefreshRate.text = getString(R.string.seconds, "20")

            in 33..40 -> notificationRefreshRate.text = getString(R.string.seconds, "25")

            in 41..48 -> notificationRefreshRate.text = getString(R.string.seconds, "30")

            in 49..56 -> notificationRefreshRate.text = getString(R.string.seconds, "35")

            in 57..64 -> notificationRefreshRate.text = getString(R.string.seconds, "40")

            in 65..72 -> notificationRefreshRate.text = getString(R.string.seconds, "45")

            in 73..80 -> notificationRefreshRate.text = getString(R.string.seconds, "50")

            in 81..88 -> notificationRefreshRate.text = getString(R.string.seconds, "55")

            in 89..100 -> notificationRefreshRate.text = getString(R.string.minute, "1")
        }
    }

    private fun dialogApply(dialog: MaterialAlertDialogBuilder, progress: Int) {

        dialog.apply {

            setTitle(getString(R.string.notification_refresh_rate))

            setPositiveButton(getString(R.string.apply)) { _, _ ->

                when(progress) {

                    in 0..8 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 5).apply()

                    in 9..16 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 10).apply()

                    in 17..24 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 15).apply()

                    in 25..32 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 20).apply()

                    in 33..40 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 25).apply()

                    in 41..48 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 30).apply()

                    in 49..56 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 35).apply()

                    in 57..64 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 40).apply()

                    in 65..72 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 45).apply()

                    in 73..80 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 50).apply()

                    in 81..88 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 55).apply()

                    in 89..100 -> pref.edit().putLong(Preferences.NotificationRefreshRate.prefKey, 60).apply()
                }
            }

            setNegativeButton(getString(android.R.string.cancel)) { d, _ -> d.dismiss() }

            show()
        }
    }

    private fun changeDesignCapacity() {

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.change_design_capacity, null)

        dialog.setView(view)

        val changeDesignCapacity = view.findViewById<EditText>(R.id.change_design_capacity_edit)

        changeDesignCapacity.setText(if(pref.getInt(Preferences.DesignCapacity.prefKey, 0) >= 0) pref.getInt(
            Preferences.DesignCapacity.prefKey, 0).toString()

        else (pref.getInt(Preferences.DesignCapacity.prefKey, 0) / -1).toString())

        dialog.setPositiveButton(getString(R.string.change)) { _, _ ->

            if(changeDesignCapacity.text.isNotEmpty()) pref.edit().putInt(Preferences.DesignCapacity.prefKey, changeDesignCapacity.text.toString().toInt()).apply()

            CapacityInfoService.instance?.sleepTime = pref.getLong(Preferences.NotificationRefreshRate.prefKey, 40)

            if(pref.getBoolean(Preferences.IsEnableService.prefKey, true) && CapacityInfoService.instance != null)
                updateNotification()
        }

        dialog.setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }

        dialog.show()
    }
}