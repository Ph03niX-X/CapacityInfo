package com.ph03nix_x.capacityinfo.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.fragments.AboutFragment
import com.ph03nix_x.capacityinfo.fragments.BackupSettingsFragment
import com.ph03nix_x.capacityinfo.fragments.BatteryStatusInformationFragment
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.fragments.FeedbackFragment
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.fragments.OverlayFragment
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.fragments.WearFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.billingClient
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.isPremium
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumActivity
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumContext
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.services.AutoBackupSettingsJobService
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.DONT_KILL_MY_APP_LINK
import com.ph03nix_x.capacityinfo.utilities.Constants.IMPORT_RESTORE_SETTINGS_EXTRA
import com.ph03nix_x.capacityinfo.utilities.Constants.IS_RESTORE_SETTINGS_EXTRA
import com.ph03nix_x.capacityinfo.utilities.Constants.POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_OPEN_APP
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_BATTERY_WEAR
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_BACKUP_INFORMATION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_SHOW_INSTRUCTION
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.views.CenteredToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xyz.kumaraswamy.autostart.Autostart
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.Locale


class MainActivity : AppCompatActivity(), BatteryInfoInterface, SettingsInterface, PremiumInterface {

    private lateinit var pref: SharedPreferences
    private var isDoubleBackToExitPressedOnce = false
    private var isRestoreImportSettings = false
    private var isRestoreSettingsFromBackup = false

    private var prefArrays: HashMap<*, *>? = null
    private var batteryWearDialog: MaterialAlertDialogBuilder? = null
    private var showFaqDialog: MaterialAlertDialogBuilder? = null
    private var showXiaomiAutostartDialog: MaterialAlertDialogBuilder? = null
    private var showHuaweiInformation: MaterialAlertDialogBuilder? = null
    lateinit var toolbar: CenteredToolbar
    lateinit var navigation: BottomNavigationView

    var fragment: Fragment? = null

    companion object {

        var instance: MainActivity? = null
        var tempFragment: Fragment? = null
        var isLoadChargeDischarge = false
        var isLoadWear = false
        var isLoadHistory = false
        var isLoadSettings = false
        var isLoadDebug = false
        var isRecreate = false
        var isOnBackPressed = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        ThemeHelper.setTheme(this)

        setContentView(R.layout.activity_main)

        premiumContext = this
        premiumActivity = this

        MainApp.currentTheme = ThemeHelper.currentTheme(resources.configuration)

        MainApp.isInstalledGooglePlay = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && MainApp.isGooglePlay(this)

        fragment = tempFragment

        batteryIntent = registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        toolbar = findViewById(R.id.toolbar)

        navigation = findViewById(R.id.navigation)

        prefArrays = MainApp.getSerializable(this, IMPORT_RESTORE_SETTINGS_EXTRA,
            HashMap::class.java)

        isRestoreSettingsFromBackup = intent.getBooleanExtra(IS_RESTORE_SETTINGS_EXTRA,
            false)

        if(fragment == null)
            fragment = when {

                isLoadChargeDischarge || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0")
                        != "1" && pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") != "2"
                        && prefArrays == null && !isLoadWear && !isLoadHistory && !isLoadSettings
                        && !isRestoreSettingsFromBackup && !isLoadDebug) ->
                    ChargeDischargeFragment()

                isLoadWear || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "1" &&
                        prefArrays == null && !isLoadChargeDischarge && !isLoadHistory &&
                        !isLoadSettings && !isRestoreSettingsFromBackup && !isLoadDebug) ->
                    WearFragment()

                isLoadHistory || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "2"
                        && HistoryHelper.isHistoryNotEmpty(this))
                        && prefArrays == null && !isLoadChargeDischarge && !isLoadHistory &&
                        !isLoadSettings && !isLoadDebug -> HistoryFragment()

                isRestoreSettingsFromBackup && !isLoadChargeDischarge && !isLoadWear &&
                        !isLoadHistory && !isLoadSettings && !isLoadDebug && prefArrays != null ->
                    BackupSettingsFragment()

                (isLoadDebug && !isLoadChargeDischarge && !isLoadWear && !isLoadHistory
                        && !isRestoreSettingsFromBackup && !isLoadSettings
                        && prefArrays == null) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                        && prefArrays != null && !isRestoreSettingsFromBackup) || (Build.VERSION
                    .SDK_INT >= Build.VERSION_CODES.R && prefArrays != null && !MainApp
                    .isInstalledGooglePlay && !isRestoreSettingsFromBackup) -> DebugFragment()

                else -> SettingsFragment()
            }

        toolbar.title = when(fragment) {

            is ChargeDischargeFragment -> getString(
                if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                else R.string.discharge)

            is WearFragment -> getString(R.string.wear)
            is HistoryFragment -> getString(R.string.history)
            is SettingsFragment -> getString(R.string.settings)
            is DebugFragment -> getString(R.string.debug)
            else -> getString(R.string.app_name)
        }

        toolbar.navigationIcon = null

        if(fragment !is SettingsFragment) inflateMenu()

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        navigation.menu.findItem(R.id.charge_discharge_navigation).title = getString(
            if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charging
            else R.string.discharge)

        navigation.menu.findItem(R.id.charge_discharge_navigation).icon = ContextCompat.getDrawable(
            this, getChargeDischargeNavigationIcon(status ==
                    BatteryManager.BATTERY_STATUS_CHARGING))

        navigation.setOnItemSelectedListener {

            when(it.itemId) {

                R.id.charge_discharge_navigation -> {

                    if(fragment !is ChargeDischargeFragment) {

                        fragment = ChargeDischargeFragment()

                        toolbar.navigationIcon = null

                        isLoadChargeDischarge = true

                        isLoadWear = false

                        isLoadHistory = false

                        isLoadSettings = false

                        isLoadDebug = false

                        clearMenu()

                        inflateMenu()

                        loadFragment(fragment ?: ChargeDischargeFragment())
                    }
                }

                R.id.wear_navigation -> {

                    if(fragment !is WearFragment) {

                        fragment = WearFragment()

                        toolbar.title = getString(R.string.wear)

                        toolbar.navigationIcon = null

                        isLoadChargeDischarge = false

                        isLoadWear = true

                        isLoadHistory = false

                        isLoadSettings = false

                        isLoadDebug = false

                        clearMenu()

                        inflateMenu()

                        loadFragment(fragment ?: WearFragment())
                    }
                }

                R.id.history_navigation -> {

                    if(fragment !is HistoryFragment) {

                        fragment = HistoryFragment()

                        toolbar.title = getString(R.string.history)

                        toolbar.navigationIcon = null

                        isLoadChargeDischarge = false

                        isLoadWear = false

                        isLoadHistory = true

                        isLoadSettings = false

                        isLoadDebug = false

                        clearMenu()

                        inflateMenu()

                        loadFragment(fragment ?: HistoryFragment())
                    }
                }

                R.id.settings_navigation -> {

                    when(fragment) {

                        null, is ChargeDischargeFragment, is WearFragment, is HistoryFragment -> {

                            fragment = SettingsFragment()

                            toolbar.title = getString(R.string.settings)

                            toolbar.navigationIcon = null

                            isLoadChargeDischarge = false

                            isLoadWear = false

                            isLoadSettings = true

                            isLoadDebug = false

                            clearMenu()

                            loadFragment(fragment ?: SettingsFragment())
                        }
                    }
                }
            }

            true
        }

        if(!isRecreate || fragment !is SettingsFragment)
            loadFragment(fragment ?: ChargeDischargeFragment(), fragment is
                    BatteryStatusInformationFragment || fragment is BackupSettingsFragment
                    || fragment is OverlayFragment || fragment is DebugFragment ||
                    fragment is AboutFragment || fragment is FeedbackFragment)

        onBackPressedDispatcher.addCallback(this, object:OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
    }

    override fun onResume() {

        super.onResume()
        
        tempFragment = null

        if(isRecreate) isRecreate = false

        if(instance == null) instance = this

        batteryIntent = registerReceiver(null, IntentFilter(
            Intent.ACTION_BATTERY_CHANGED))

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        if(fragment !is ChargeDischargeFragment) {

            navigation.menu.findItem(R.id.charge_discharge_navigation).title = getString(if(
                status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                R.string.discharge)

            navigation.menu.findItem(R.id.charge_discharge_navigation).icon = ContextCompat
                .getDrawable(this, getChargeDischargeNavigationIcon(status ==
                        BatteryManager.BATTERY_STATUS_CHARGING))
        }

        if(fragment is HistoryFragment && HistoryHelper.isHistoryEmpty(this)) {
            fragment = ChargeDischargeFragment()
            isLoadHistory = false
            isLoadChargeDischarge = true
            clearMenu()
            inflateMenu()
            loadFragment(fragment ?: ChargeDischargeFragment())
        }
        if(HistoryHelper.isHistoryEmpty(this) &&
            pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "2")
            pref.edit().remove(TAB_ON_APPLICATION_LAUNCH).apply()


        toolbar.title = when(fragment) {

            is ChargeDischargeFragment -> getString(
                if(status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                else R.string.discharge)

            is WearFragment -> getString(R.string.wear)
            is HistoryFragment -> getString(R.string.history)
            is SettingsFragment -> getString(R.string.settings)
            is BatteryStatusInformationFragment -> getString(R.string.battery_status_information)
            is OverlayFragment -> getString(R.string.overlay)
            is AboutFragment -> getString(R.string.about)
            is FeedbackFragment -> getString(R.string.feedback)
            is DebugFragment -> getString(R.string.debug)
            is BackupSettingsFragment -> getString(R.string.backup)
            else -> getString(R.string.app_name)
        }

        if(!pref.contains(DESIGN_CAPACITY) ||
            pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)) <
            resources.getInteger(R.integer.min_design_capacity) || pref.getInt(DESIGN_CAPACITY,
                resources.getInteger(R.integer.min_design_capacity)) > resources.getInteger(
                R.integer.max_design_capacity)) {

            pref.edit().apply {

                putInt(DESIGN_CAPACITY, getOnDesignCapacity(this@MainActivity))

                apply()
            }
        }


        if(!isHuawei() && isXiaomi() && Autostart(this).autoStartState ==
            Autostart.State.DISABLED) showXiaomiAutoStartDialog()

        else if(isHuawei()) showHuaweiInfo()

        if(!isHuawei() && pref.getBoolean(IS_SHOW_INSTRUCTION, resources.getBoolean(
                R.bool.is_show_instruction))) showInstruction()

        if(!pref.getBoolean(IS_BATTERY_WEAR, resources.getBoolean(R.bool.is_battery_wear)
                    && batteryWearDialog == null)) showBatteryWearDialog()

        if(fragment is ChargeDischargeFragment || fragment is WearFragment)
            toolbar.menu.findItem(R.id.instruction).isVisible = getOnCurrentCapacity(
                this) > 0.0

        if(pref.getBoolean(PreferencesKeys.IS_AUTO_BACKUP_SETTINGS, resources.getBoolean(
                R.bool.is_auto_backup_settings)) && ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ServiceHelper.jobSchedule(this, AutoBackupSettingsJobService::class.java,
                Constants.AUTO_BACKUP_SETTINGS_JOB_ID, (pref.getString(
                    PreferencesKeys.FREQUENCY_OF_AUTO_BACKUP_SETTINGS, "1")
                    ?.toLong() ?: 1L) * 60L * 60L * 1000L)

        else ServiceHelper.cancelJob(this, Constants.AUTO_BACKUP_SETTINGS_JOB_ID)

        val prefArrays = MainApp.getSerializable(this, IMPORT_RESTORE_SETTINGS_EXTRA,
            HashMap::class.java)

        if(prefArrays != null) importSettings(prefArrays)

        ServiceHelper.startService(this, CapacityInfoService::class.java)

        if(pref.getBoolean(IS_AUTO_START_OPEN_APP, resources.getBoolean(R.bool
                .is_auto_start_open_app)) && CapacityInfoService.instance == null &&
            !ServiceHelper.isStartedCapacityInfoService())
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat
                    .checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE)

        if(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(R.bool.is_enabled_overlay))
            && OverlayService.instance == null && !ServiceHelper.isStartedOverlayService())
            ServiceHelper.startService(this, OverlayService::class.java)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val newTheme = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_NO

        if(newTheme != MainApp.currentTheme) {

            tempFragment = fragment

            isRecreate = true

            recreate()
        }
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
//                                            grantResults: IntArray) {
//
//        when (requestCode) {
//            POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE ->
//                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    ServiceHelper.startService(this, CapacityInfoService::class.java)
//            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        }
//    }

    override fun onDestroy() {

        instance = null

        fragment = null

        premiumContext = null
        premiumActivity = null
        showFaqDialog = null

        if(!isRecreate) {

            tempFragment = null

            isLoadChargeDischarge = false
            isLoadWear = false
            isLoadHistory = false
            isLoadSettings = false
            isLoadDebug = false
        }

        super.onDestroy()
    }

    private fun inflateMenu() {

        if(fragment is HistoryFragment) {

            toolbar.inflateMenu(R.menu.history_menu)

            toolbar.menu.findItem(R.id.clear_history).apply {

                isVisible = isPremium && HistoryHelper.isHistoryNotEmpty(this@MainActivity)

                setOnMenuItemClickListener {

                    HistoryHelper.clearHistory(this@MainActivity, this)

                    true
                }
            }

            toolbar.menu.findItem(R.id.history_premium).apply {
                isVisible = !isPremium

                setOnMenuItemClickListener {
                    showPremiumDialog()

                    true
                }
            }
        }

        else {

            toolbar.inflateMenu(R.menu.main_menu)

            toolbar.menu.findItem(R.id.instruction).isVisible = getOnCurrentCapacity(
                this) > 0.0 && (fragment is ChargeDischargeFragment || fragment is WearFragment)

            toolbar.menu.findItem(R.id.instruction).setOnMenuItemClickListener {

                showInstruction()

                true
            }

            toolbar.menu.findItem(R.id.faq).setOnMenuItemClickListener {

                showFaq()

                true
            }

            toolbar.menu.findItem(R.id.tips).setOnMenuItemClickListener {

                MaterialAlertDialogBuilder(this).apply {

                    setIcon(R.drawable.ic_tips_for_extending_battery_life_24dp)
                    setTitle(getString(R.string.tips_dialog_title))
                    setMessage(getString(R.string.tip1) + getString(R.string.tip2)
                            + getString(R.string.tip3) + getString(R.string.tip4))
                    setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }
                    show()
                }

                true
            }

            toolbar.menu.findItem(R.id.dont_kill_my_app).setOnMenuItemClickListener {

                try {

                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DONT_KILL_MY_APP_LINK)))
                }
                catch(e: ActivityNotFoundException) {

                    Toast.makeText(this, e.message ?: e.toString(), Toast.LENGTH_LONG)
                        .show()
                }

                true
            }

            toolbar.menu.findItem(R.id.premium).isVisible = !isPremium

            if(!isPremium)
                toolbar.menu.findItem(R.id.premium).setOnMenuItemClickListener {
                    showPremiumDialog()
                    true
                }
        }
    }

    fun clearMenu() = toolbar.menu.clear()

    private fun isXiaomi() =
        Build.MANUFACTURER.uppercase(Locale.getDefault()) == "XIAOMI" && isMIUI()

    private fun isMIUI(): Boolean {

        val propName = "ro.miui.ui.version.name"
        val line: String
        var input: BufferedReader? = null
        return try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
            line.isNotEmpty()
        } catch (ex: IOException) {
            false
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun isHuawei() =
        Build.MANUFACTURER.uppercase(Locale.getDefault()) == "HUAWEI" ||
                Build.MANUFACTURER.uppercase(Locale.getDefault()) == "HONOR"

    private fun showInstruction() {

        MaterialAlertDialogBuilder(this).apply {

            if(pref.getBoolean(IS_SHOW_INSTRUCTION, resources.getBoolean(
                    R.bool.is_show_instruction)))
                pref.edit().putBoolean(IS_SHOW_INSTRUCTION, false).apply()

            setIcon(R.drawable.ic_instruction_not_supported_24dp)
            setTitle(getString(R.string.instruction))
            setMessage(getString(R.string.instruction_message)
                    + getString(R.string.instruction_message_do_not_kill_the_service)
                    + getString(R.string.instruction_message_dont_kill_my_app))

            setPositiveButton(android.R.string.ok) { d, _ -> d.dismiss() }

            setCancelable(false)

            show()        }
    }

    private fun showFaq() {
        if(showFaqDialog == null) {
            showFaqDialog = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_faq_question_24dp)
                setTitle(getString(R.string.faq))
                setMessage(getString(R.string.faq_how_does_the_app_work)
                        + getString(R.string.faq_capacity_added)
                        + getString(R.string.faq_where_does_the_app_get_the_ccl)
                        + getString(R.string.faq_why_is_ccl_not_displayed)
                        + getString(R.string.faq_i_have_everything_in_zeros)
                        + getString(R.string.faq_units) + getString(R.string.faq_current_capacity)
                        + getString(R.string.faq_residual_capacity_is_higher)
                        + getString(R.string.faq_battery_wear_changes_when_charger_is_disconnected)
                        + getString(R.string.faq_battery_wear_not_change)
                        + getString(R.string.faq_with_each_charge_battery_wear_changes)
                        + getString(R.string.faq_where_does_the_app_get_the_number_of_cycles_android)
                        + getString(R.string.faq_not_displayed_number_of_cycles_android))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    showFaqDialog = null
                }
                setCancelable(false)
                show()
            }
        }
    }

    private fun showXiaomiAutoStartDialog() {
        if(showXiaomiAutostartDialog == null && isXiaomi() &&
            Autostart(this).autoStartState == Autostart.State.DISABLED) {
            showXiaomiAutostartDialog = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.auto_start_xiaomi_dialog))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        startActivity(Intent("miui.intent.action.OP_AUTO_START")
                            .addCategory(Intent.CATEGORY_DEFAULT))

                        showXiaomiBackgroundActivityControlDialog()
                    }
                    catch (e: ActivityNotFoundException) {
                        startActivity(Intent().setComponent(ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity")))

                        showXiaomiBackgroundActivityControlDialog()
                    }
                    finally {
                        showXiaomiAutostartDialog = null
                    }
                }

                setCancelable(false)
                show()
            }
        }
    }

    private fun showXiaomiBackgroundActivityControlDialog() {
        if(isXiaomi()) {
            MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.background_activity_control_xiaomi_dialog))
                setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        startActivity(Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST")
                            .addCategory(Intent.CATEGORY_DEFAULT))
                    }
                    catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                show()
            }
        }
    }

    private fun showHuaweiInfo() {
        if(isHuawei() && showHuaweiInformation == null)
            showHuaweiInformation = MaterialAlertDialogBuilder(this).apply {
                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                setTitle(getString(R.string.information))
                setMessage(getString(R.string.huawei_honor_information))
                setPositiveButton(android.R.string.ok) { d, _ ->
                    showHuaweiInformation = null
                    d.dismiss()
                }
                show()
            }
    }

    fun showPremiumDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setIcon(R.drawable.ic_premium_24)
            setTitle(getString(R.string.premium))
            setMessage(getString(R.string.premium_dialog))
            setPositiveButton(R.string.purchase_premium) { d, _ ->
                if(billingClient?.isReady == true) purchasePremium()
                else initiateBilling(true)

                d.dismiss()
            }
            setNegativeButton(android.R.string.cancel) { d, _ ->
                d.dismiss()
            }
            setCancelable(false)
            show()
        }
    }

    private fun showBatteryWearDialog() {

        val designCapacity = pref.getInt(DESIGN_CAPACITY, resources.getInteger(
            R.integer.min_design_capacity)).toDouble()

        val residualCapacity = if(pref.getString(PreferencesKeys
                .UNIT_OF_MEASUREMENT_OF_CURRENT_CAPACITY, "μAh") == "μAh")
            pref.getInt(RESIDUAL_CAPACITY, 0).toDouble() / 1000.0 else pref.getInt(
            RESIDUAL_CAPACITY, 0).toDouble() / 100.0

        val batteryWear = if(BatteryInfoInterface.residualCapacity > 0.0 &&
            BatteryInfoInterface.residualCapacity < designCapacity)
            (100.0 - (BatteryInfoInterface.residualCapacity / designCapacity) * 100.0)
        else if(residualCapacity > 0.0 && residualCapacity < designCapacity)
                (100.0 - (residualCapacity / designCapacity) * 100.0) else 0.0

        when (batteryWear) {
            in 25.0..100.0 -> {
                with(pref) {
                        batteryWearDialog =
                            MaterialAlertDialogBuilder(this@MainActivity).apply {
                                setIcon(R.drawable.ic_instruction_not_supported_24dp)
                                setTitle(getString(R.string.information))
                                setMessage(getString(when (batteryWear) {

                                    in 25.0..39.9 -> R.string.battery_wear_dialog

                                    in 40.0..59.9 -> R.string.high_battery_wear_dialog

                                    in 60.0..74.9 -> R.string.very_high_battery_wear_dialog

                                    else -> R.string.critical_battery_wear_dialog },
                                    "${DecimalFormat("#.#").format(batteryWear)}%"))

                                setPositiveButton(android.R.string.ok) { d, _ ->
                                    edit().putBoolean(IS_BATTERY_WEAR, true).apply()
                                    batteryWearDialog = null
                                    d.dismiss()
                                }
                                setCancelable(false)
                                show()
                            }
                }
            }
            else -> pref.edit().remove(IS_BATTERY_WEAR).apply()
        }
    }

    fun loadFragment(fragment: Fragment, isAddToBackStack: Boolean = false) {

        supportFragmentManager.beginTransaction().apply {

            replace(R.id.fragment_container, fragment)
            if(isAddToBackStack) addToBackStack(null)

            if(!isRecreate || fragment is ChargeDischargeFragment || fragment is WearFragment)
                commit()
        }

        when {

            fragment !is BatteryStatusInformationFragment && fragment !is OverlayFragment
                    && fragment !is AboutFragment && fragment !is DebugFragment
                    && fragment !is FeedbackFragment && fragment !is BackupSettingsFragment -> {

                navigation.selectedItemId = when(fragment) {

                    is ChargeDischargeFragment -> R.id.charge_discharge_navigation
                    is WearFragment -> R.id.wear_navigation
                    is HistoryFragment -> R.id.history_navigation
                    is SettingsFragment -> R.id.settings_navigation
                    else -> R.id.charge_discharge_navigation
                }
            }

            else -> {

                navigation.selectedItemId = R.id.settings_navigation

                clearMenu()

                toolbar.navigationIcon = ContextCompat.getDrawable(this,
                    R.drawable.ic_arrow_back_24dp)
            }
        }
    }

    fun getChargeDischargeNavigationIcon(isCharge: Boolean): Int {

        val batteryLevel = getOnBatteryLevel(this) ?: 0

        if(isCharge)
            return when(batteryLevel) {

            in 0..29 -> R.drawable.ic_charge_navigation_20_24dp
            in 30..49 -> R.drawable.ic_charge_navigation_30_24dp
            in 50..59 -> R.drawable.ic_charge_navigation_50_24dp
            in 60..79 -> R.drawable.ic_charge_navigation_60_24dp
            in 80..89 -> R.drawable.ic_charge_navigation_80_24dp
            in 90..95 -> R.drawable.ic_charge_navigation_90_24dp
            else -> R.drawable.ic_charge_navigation_full_24dp
        }

        else return when(batteryLevel) {

            in 0..9 -> R.drawable.ic_discharge_navigation_9_24dp
            in 10..29 -> R.drawable.ic_discharge_navigation_20_24dp
            in 30..49 -> R.drawable.ic_discharge_navigation_30_24dp
            in 50..59 -> R.drawable.ic_discharge_navigation_50_24dp
            in 60..79 -> R.drawable.ic_discharge_navigation_60_24dp
            in 80..89 -> R.drawable.ic_discharge_navigation_80_24dp
            in 90..95 -> R.drawable.ic_discharge_navigation_90_24dp
            else -> R.drawable.ic_discharge_navigation_full_24dp
        }
    }

    private fun importSettings(prefArrays: HashMap<*, *>?) {

        val prefsTempList = arrayListOf(BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH,
            DESIGN_CAPACITY, CAPACITY_ADDED, LAST_CHARGE_TIME, PERCENT_ADDED, RESIDUAL_CAPACITY,
            IS_SHOW_INSTRUCTION, IS_SHOW_BACKUP_INFORMATION, IS_BATTERY_WEAR)

        if(prefArrays != null)
            prefsTempList.forEach {

            with(prefArrays) {

                when {

                    !containsKey(it) -> pref.edit().remove(it).apply()

                    else -> {

                        forEach {

                            when(it.key as String) {

                                NUMBER_OF_CHARGES -> pref.edit().putLong(it.key as String,
                                    it.value as Long).apply()

                                BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, LAST_CHARGE_TIME,
                                DESIGN_CAPACITY, RESIDUAL_CAPACITY, PERCENT_ADDED ->
                                    pref.edit().putInt(it.key as String, it.value as Int).apply()

                                CAPACITY_ADDED, NUMBER_OF_CYCLES ->
                                    pref.edit().putFloat(it.key as String,
                                        it.value as Float).apply()

                                IS_SHOW_INSTRUCTION, IS_SHOW_BACKUP_INFORMATION, IS_BATTERY_WEAR ->
                                    pref.edit().putBoolean(it.key as String,
                                        it.value as Boolean).apply()
                            }
                        }
                    }
                }
            }
        }

        toolbar.menu.clear()

        isRestoreImportSettings = true

        Toast.makeText(this, if(intent.getBooleanExtra(IS_RESTORE_SETTINGS_EXTRA,
                false)) R.string.settings_successfully_restored_from_backup else
            R.string.settings_imported_successfully, Toast.LENGTH_LONG).show()

        this.prefArrays = null

        intent.removeExtra(IMPORT_RESTORE_SETTINGS_EXTRA)

        intent.removeExtra(IS_RESTORE_SETTINGS_EXTRA)
    }

    fun backPressed() {
        if(isOnBackPressed) {
            if(toolbar.title != getString(R.string.settings) && !isRestoreImportSettings && ((fragment != null
                        && fragment !is SettingsFragment && fragment !is ChargeDischargeFragment
                        && fragment !is WearFragment && fragment !is HistoryFragment &&
                        fragment !is DebugFragment && fragment !is BackupSettingsFragment) || ((
                        fragment is BackupSettingsFragment || fragment is DebugFragment) &&
                        supportFragmentManager.backStackEntryCount > 0))) {

                fragment = SettingsFragment()

                toolbar.title = getString(if(fragment !is DebugFragment) R.string.settings
                else R.string.debug)

                if(fragment is SettingsFragment) toolbar.navigationIcon = null

                supportFragmentManager.popBackStack()
            }

            else if(toolbar.title != getString(R.string.settings) &&
                (fragment is BackupSettingsFragment && supportFragmentManager.backStackEntryCount == 0)
                || isRestoreImportSettings) {

                fragment = SettingsFragment()

                toolbar.title = getString(R.string.settings)

                toolbar.navigationIcon = null

                isRestoreImportSettings = false

                loadFragment(fragment ?: SettingsFragment())
            }

            else {

                if(isDoubleBackToExitPressedOnce) finish()

                else {

                    isDoubleBackToExitPressedOnce = true

                    Toast.makeText(this@MainActivity, R.string.press_the_back_button_again,
                        Toast.LENGTH_LONG).show()

                    CoroutineScope(Dispatchers.Main).launch {

                        delay(3000L)
                        isDoubleBackToExitPressedOnce = false
                    }
                }
            }
        }
    }
}