package com.ph03nix_x.capacityinfo.fragments

import android.content.*
import android.os.Bundle
import android.widget.Toast
import androidx.preference.*
import com.ph03nix_x.capacityinfo.interfaces.DebugOptionsInterface
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.interfaces.ServiceInterface
import com.ph03nix_x.capacityinfo.interfaces.BillingInterface
import com.ph03nix_x.capacityinfo.interfaces.JobServiceInterface
import com.ph03nix_x.capacityinfo.services.BillingJobService
import com.ph03nix_x.capacityinfo.utils.Constants.BILLING_JOB_SERVICE_ID
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_FORCIBLY_SHOW_RATE_THE_APP
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.IS_HIDE_DONATE
import com.ph03nix_x.capacityinfo.utils.PreferencesKeys.PERIODIC_BILLING_JOB_SERVICE
import com.ph03nix_x.capacityinfo.utils.Utils.isGooglePlay
import com.ph03nix_x.capacityinfo.utils.Utils.isInstalledGooglePlay

class DebugFragment : PreferenceFragmentCompat(), DebugOptionsInterface, ServiceInterface,
    BillingInterface, JobServiceInterface {

    private lateinit var pref: SharedPreferences
    
    private var forciblyShowRateTheApp: SwitchPreferenceCompat? = null
    private var hideDonate: SwitchPreferenceCompat? = null
    private var doNotScheduleBJS: SwitchPreferenceCompat? = null
    private var scheduleBJS: Preference? = null
    private var cancelBJS: Preference? = null
    private var cancelAllBJS: Preference? = null
    private var periodicBJS: ListPreference? = null
    private var addSetting: Preference? = null
    private var changeSetting: Preference? = null
    private var resetSetting: Preference? = null
    private var resetSettings: Preference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        addPreferencesFromResource(R.xml.debug_settings)

        forciblyShowRateTheApp = findPreference(IS_FORCIBLY_SHOW_RATE_THE_APP)

        hideDonate = findPreference(IS_HIDE_DONATE)

        doNotScheduleBJS = findPreference(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE)

        scheduleBJS = findPreference("schedule_billing_job_service")

        cancelBJS = findPreference("cancel_billing_job_service")

        cancelAllBJS = findPreference("cancel_all_billing_job_services")

        periodicBJS = findPreference(PERIODIC_BILLING_JOB_SERVICE)

        addSetting = findPreference("add_setting")

        changeSetting = findPreference("change_setting")

        resetSetting = findPreference("reset_setting")

        resetSettings = findPreference("reset_settings")

        forciblyShowRateTheApp?.isVisible = !isGooglePlay(requireContext())

        hideDonate?.isVisible = isInstalledGooglePlay

        doNotScheduleBJS?.isVisible = isInstalledGooglePlay

        cancelBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        scheduleBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        cancelAllBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        if(pref.getString(PERIODIC_BILLING_JOB_SERVICE, "12") !in
            resources.getStringArray(R.array.periodic_billing_job_service_values))
            pref.edit().putString(PERIODIC_BILLING_JOB_SERVICE, "12").apply()


        periodicBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
            summary = entry
        }

        doNotScheduleBJS?.setOnPreferenceChangeListener { _, newValue ->

            if((newValue as? Boolean) == true)
                onCancelJobService(requireContext(), BILLING_JOB_SERVICE_ID)

            else if(newValue == false) onScheduleJobService(requireContext(),
                BillingJobService::class.java, BILLING_JOB_SERVICE_ID,
                periodicHours = (pref.getString(
                    PERIODIC_BILLING_JOB_SERVICE, "12")?.toLong() ?: 12))

            scheduleBJS?.isEnabled = newValue as? Boolean == false

            cancelBJS?.isEnabled = newValue as? Boolean == false

            cancelAllBJS?.isEnabled = newValue as? Boolean == false

            periodicBJS?.isEnabled = newValue as? Boolean == false

            true
        }

        scheduleBJS?.setOnPreferenceClickListener {

            onScheduleJobService(requireContext(), BillingJobService::class.java,
                BILLING_JOB_SERVICE_ID, periodicHours = (pref.getString(
                    PERIODIC_BILLING_JOB_SERVICE, "12")?.toLong() ?: 12))

            Toast.makeText(requireContext(), getString(
                R.string.billing_job_service_is_scheduled,
                (periodicBJS?.entry ?: getString(R.string.unknown))), Toast.LENGTH_LONG).show()

            true
        }

        cancelBJS?.setOnPreferenceClickListener {

            onCancelJobService(requireContext(), BILLING_JOB_SERVICE_ID)

            Toast.makeText(requireContext(), getString(
                R.string.billing_job_service_is_cancelled_successfully), Toast.LENGTH_LONG).show()

            true
        }

        cancelAllBJS?.setOnPreferenceClickListener {

            onCancelAllJobServices(requireContext())

            Toast.makeText(requireContext(), getString(
                R.string.job_services_is_cancelled_successfully), Toast.LENGTH_LONG).show()

            true
        }

        periodicBJS?.setOnPreferenceChangeListener { preference, newValue ->

            preference.summary = resources.getStringArray(
                R.array.periodic_billing_job_service_list)[
                    ((newValue as? String)?.toInt() ?: 11) - 1]

            onReScheduleJobService(requireContext(), BillingJobService::class.java,
                BILLING_JOB_SERVICE_ID, periodicHours = ((newValue as? String)?.toLong() ?: 12))

            true
        }

        addSetting?.setOnPreferenceClickListener {

            addSettingDialog(requireContext(), pref)

            true
        }

        changeSetting?.setOnPreferenceClickListener {

            changeSettingDialog(requireContext(), pref)

            true
        }

        resetSetting?.setOnPreferenceClickListener {

            resetSettingDialog(requireContext(), pref)

            true
        }

        resetSettings?.setOnPreferenceClickListener {

            resetSettingsDialog(requireContext(), pref)

            true
        }
    }

    override fun onResume() {

        super.onResume()

        if(pref.getString(PERIODIC_BILLING_JOB_SERVICE, "12") !in
            resources.getStringArray(R.array.periodic_billing_job_service_values))
            pref.edit().putString(PERIODIC_BILLING_JOB_SERVICE, "12").apply()

        scheduleBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        cancelBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        cancelAllBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
        }

        periodicBJS?.apply {

            isVisible = isInstalledGooglePlay
            isEnabled = !pref.getBoolean(IS_DO_NOT_SCHEDULE_BILLING_JOB_SERVICE, false)
            summary = entry
        }
    }
}