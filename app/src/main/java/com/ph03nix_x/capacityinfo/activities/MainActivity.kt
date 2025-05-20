package com.ph03nix_x.capacityinfo.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ph03nix_x.capacityinfo.MainApp
import com.ph03nix_x.capacityinfo.MainApp.Companion.batteryIntent
import com.ph03nix_x.capacityinfo.MainApp.Companion.isGooglePlay
import com.ph03nix_x.capacityinfo.MainApp.Companion.isInstalledGooglePlay
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.TOKEN_COUNT
import com.ph03nix_x.capacityinfo.TOKEN_PREF
import com.ph03nix_x.capacityinfo.fragments.AboutFragment
import com.ph03nix_x.capacityinfo.fragments.BackupSettingsFragment
import com.ph03nix_x.capacityinfo.fragments.BatteryStatusInformationFragment
import com.ph03nix_x.capacityinfo.fragments.ChargeDischargeFragment
import com.ph03nix_x.capacityinfo.fragments.DebugFragment
import com.ph03nix_x.capacityinfo.fragments.FeedbackFragment
import com.ph03nix_x.capacityinfo.fragments.HistoryFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeFragment
import com.ph03nix_x.capacityinfo.fragments.LastChargeNoPremiumFragment
import com.ph03nix_x.capacityinfo.fragments.OverlayFragment
import com.ph03nix_x.capacityinfo.fragments.SettingsFragment
import com.ph03nix_x.capacityinfo.fragments.WearFragment
import com.ph03nix_x.capacityinfo.helpers.HistoryHelper
import com.ph03nix_x.capacityinfo.helpers.ServiceHelper
import com.ph03nix_x.capacityinfo.helpers.ThemeHelper
import com.ph03nix_x.capacityinfo.interfaces.BatteryInfoInterface
import com.ph03nix_x.capacityinfo.interfaces.BatteryOptimizationsInterface
import com.ph03nix_x.capacityinfo.interfaces.CheckUpdateInterface
import com.ph03nix_x.capacityinfo.interfaces.ManufacturerInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.isPremium
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumActivity
import com.ph03nix_x.capacityinfo.interfaces.PremiumInterface.Companion.premiumContext
import com.ph03nix_x.capacityinfo.interfaces.SettingsInterface
import com.ph03nix_x.capacityinfo.interfaces.views.MenuInterface
import com.ph03nix_x.capacityinfo.interfaces.views.NavigationInterface
import com.ph03nix_x.capacityinfo.services.CapacityInfoService
import com.ph03nix_x.capacityinfo.services.OverlayService
import com.ph03nix_x.capacityinfo.utilities.Constants
import com.ph03nix_x.capacityinfo.utilities.Constants.IMPORT_RESTORE_SETTINGS_EXTRA
import com.ph03nix_x.capacityinfo.utilities.Constants.POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_AUTO_START_OPEN_APP
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLED_OVERLAY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.IS_ENABLE_CHECK_UPDATE
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.LAST_CHARGE_TIME
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_FULL_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.TAB_ON_APPLICATION_LAUNCH
import com.ph03nix_x.capacityinfo.views.CenteredToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import androidx.core.net.toUri
import androidx.core.content.edit
import com.ph03nix_x.capacityinfo.interfaces.AdsInterface

class MainActivity : AppCompatActivity(), BatteryInfoInterface, SettingsInterface, PremiumInterface,
    MenuInterface, ManufacturerInterface, NavigationInterface, CheckUpdateInterface,
    BatteryOptimizationsInterface, AdsInterface {

    lateinit var pref: SharedPreferences
    private var isDoubleBackToExitPressedOnce = false
    private var isRestoreImportSettings = false

    private var prefArrays: HashMap<*, *>? = null
    private var showNotInstalledFromGPDialog: MaterialAlertDialogBuilder? = null
    private var showRequestNotificationPermissionDialog: MaterialAlertDialogBuilder? = null

    val updateFlowResultLauncher = registerForActivityResult(ActivityResultContracts
        .StartIntentSenderForResult()) { _ -> }

    var isGooglePlay = false
    var isShowRequestIgnoringBatteryOptimizationsDialog = true
    var isShowXiaomiBackgroundActivityControlDialog = false

    var showFaqDialog: MaterialAlertDialogBuilder? = null
    var showXiaomiAutostartDialog: MaterialAlertDialogBuilder? = null
    var showHuaweiInformation: MaterialAlertDialogBuilder? = null
    var showRequestIgnoringBatteryOptimizationsDialog: MaterialAlertDialogBuilder? = null
    var showFailedRequestIgnoringBatteryOptimizationsDialog: MaterialAlertDialogBuilder? = null
    lateinit var toolbar: CenteredToolbar
    lateinit var navigation: BottomNavigationView

    var fragment: Fragment? = null

    companion object {

        var instance: MainActivity? = null
        var tempFragment: Fragment? = null
        var isLoadChargeDischarge = false
        var isLoadLastCharge = false
        var isLoadWear = false
        var isLoadHistory = false
        var isLoadSettings = false
        var isLoadDebug = false
        var isRecreate = false
        var isOnBackPressed = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        isGooglePlay = isGooglePlay(this)
        pref = PreferenceManager.getDefaultSharedPreferences(this)

        super.onCreate(savedInstanceState)

        ThemeHelper.setTheme(this)

        setContentView(R.layout.activity_main)

        if (premiumContext == null) premiumContext = this
        premiumActivity = this

        MainApp.currentTheme = ThemeHelper.currentTheme(resources.configuration)

        fragment = tempFragment

        batteryIntent = registerReceiver(
            null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val status = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        toolbar = findViewById(R.id.toolbar)

        navigation = findViewById(R.id.navigation)

        prefArrays = MainApp.getSerializable(
            this, IMPORT_RESTORE_SETTINGS_EXTRA,
            HashMap::class.java
        )

        if(isGooglePlay && fragment == null)
            fragment = when {
                isLoadChargeDischarge || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0")
                        != "1" && pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") != "2" &&
                        pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") != "3"
                        && prefArrays == null && !isLoadLastCharge && !isLoadWear && !isLoadHistory
                        && !isLoadSettings && !isLoadDebug) -> ChargeDischargeFragment()
                isLoadLastCharge || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "1"
                        && prefArrays == null && !isLoadChargeDischarge && !isLoadWear &&
                        !isLoadHistory && !isLoadSettings && !isLoadDebug) ->
                            if(!isPremium) LastChargeNoPremiumFragment() else LastChargeFragment()
                isLoadWear || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "2" &&
                        prefArrays == null && !isLoadChargeDischarge && !isLoadLastCharge &&
                        !isLoadHistory && !isLoadSettings && !isLoadDebug) -> WearFragment()
                isLoadHistory || (pref.getString(TAB_ON_APPLICATION_LAUNCH, "0") == "3")
                        && prefArrays == null && !isLoadChargeDischarge && !isLoadLastCharge &&
                        !isLoadHistory && !isLoadSettings && !isLoadDebug -> HistoryFragment()
                !isLoadChargeDischarge && !isLoadLastCharge && !isLoadWear && !isLoadHistory &&
                        !isLoadSettings && !isLoadDebug && prefArrays != null ->
                            BackupSettingsFragment()
                isLoadDebug && !isLoadChargeDischarge && !isLoadLastCharge && !isLoadWear &&
                        !isLoadHistory && isLoadSettings && prefArrays == null -> DebugFragment()
                else -> SettingsFragment()
            }

        toolbar.title = when (fragment) {
            is ChargeDischargeFragment -> getString(
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                else R.string.discharge)
            is LastChargeFragment -> getString(R.string.last_charge)
            is WearFragment -> getString(R.string.wear)
            is HistoryFragment -> getString(if(isPremium && HistoryHelper.isHistoryNotEmpty(this))
                R.string.history_title else R.string.history, HistoryHelper.getHistoryCount(this),
                Constants.HISTORY_COUNT_MAX)
            is SettingsFragment -> getString(R.string.settings)
            is DebugFragment -> getString(R.string.debug)
            else -> getString(R.string.app_name)
        }

        toolbar.navigationIcon = null

        if (fragment !is SettingsFragment) inflateMenu()

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        bottomNavigation(status)

        if(isGooglePlay && (!isRecreate || fragment !is SettingsFragment)) {
            loadFragment(
                fragment ?: ChargeDischargeFragment(), fragment is
                        BatteryStatusInformationFragment || fragment is BackupSettingsFragment
                        || fragment is OverlayFragment || fragment is DebugFragment ||
                        fragment is AboutFragment || fragment is FeedbackFragment)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        })
    }

    override fun onResume() {

        super.onResume()

        if(!isGooglePlay) {
            ServiceHelper.cancelAllJobs(this@MainActivity)
            pref.edit { clear() }
            if(showNotInstalledFromGPDialog == null)
                showNotInstalledFromGPDialog = MaterialAlertDialogBuilder(this).apply {
                    setIcon(R.drawable.ic_instruction_not_supported_24dp)
                    setTitle(getString(R.string.error))
                    setMessage(R.string.not_installed_from_gp)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW,
                                Constants.GOOGLE_PLAY_APP_LINK.toUri()))
                        }
                        catch(_: ActivityNotFoundException) {}
                        finally {
                            showNotInstalledFromGPDialog = null
                            finishAffinity()
                        }
                    }
                    setCancelable(false)
                    show()
                }
            return
        }

        tempFragment = null

        if (isRecreate) isRecreate = false

        if (instance == null) instance = this

        batteryIntent = registerReceiver(
            null, IntentFilter(
                Intent.ACTION_BATTERY_CHANGED
            )
        )

        val status = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        if (fragment !is ChargeDischargeFragment) {

            navigation.menu.findItem(R.id.charge_discharge_navigation).title = getString(
                if (
                    status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge else
                    R.string.discharge
            )

            navigation.menu.findItem(R.id.charge_discharge_navigation).icon = ContextCompat
                .getDrawable(
                    this, getChargeDischargeNavigationIcon(
                        status ==
                                BatteryManager.BATTERY_STATUS_CHARGING
                    )
                )
        }

        toolbar.title = when(fragment) {

            is ChargeDischargeFragment -> getString(
                if (status == BatteryManager.BATTERY_STATUS_CHARGING) R.string.charge
                else R.string.discharge)
            is WearFragment -> getString(R.string.wear)
            is LastChargeNoPremiumFragment, is LastChargeFragment -> getString(R.string.last_charge)
            is HistoryFragment -> getString(if(isPremium && HistoryHelper.isHistoryNotEmpty(this))
                R.string.history_title else R.string.history, HistoryHelper.getHistoryCount(this),
                Constants.HISTORY_COUNT_MAX)
            is SettingsFragment -> getString(R.string.settings)
            is BatteryStatusInformationFragment -> getString(R.string.battery_status_information)
            is OverlayFragment -> getString(R.string.overlay)
            is AboutFragment -> getString(R.string.about)
            is FeedbackFragment -> getString(R.string.feedback)
            is DebugFragment -> getString(R.string.debug)
            is BackupSettingsFragment -> getString(R.string.backup)
            else -> getString(R.string.app_name)
        }

        if (!pref.contains(DESIGN_CAPACITY) ||
            pref.getInt(DESIGN_CAPACITY, resources.getInteger(R.integer.min_design_capacity)) <
            resources.getInteger(R.integer.min_design_capacity) || pref.getInt(
                DESIGN_CAPACITY,
                resources.getInteger(R.integer.min_design_capacity)
            ) > resources.getInteger(
                R.integer.max_design_capacity
            )
        ) {

            pref.edit().apply {

                putInt(DESIGN_CAPACITY, getDesignCapacity(this@MainActivity))

                apply()
            }
        }

        if (fragment is ChargeDischargeFragment || fragment is WearFragment)
            toolbar.menu.findItem(R.id.instruction)?.isVisible = getCurrentCapacity(
                this
            ) > 0.0

        val prefArrays = MainApp.getSerializable(
            this, IMPORT_RESTORE_SETTINGS_EXTRA,
            HashMap::class.java
        )

        if (prefArrays != null) importSettings(prefArrays)

        if(isGooglePlay)
            ServiceHelper.startService(this, CapacityInfoService::class.java)

        if (pref.getBoolean(
                IS_AUTO_START_OPEN_APP, resources.getBoolean(
                    R.bool
                        .is_auto_start_open_app
                )
            ) && CapacityInfoService.instance == null &&
            !ServiceHelper.isStartedCapacityInfoService()
        )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat
                    .checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_DENIED)
                requestNotificationPermission()
            else checkBatteryOptimizations()

        if(pref.getBoolean(IS_ENABLED_OVERLAY, resources.getBoolean(R.bool.is_enabled_overlay))
            && OverlayService.instance == null && !ServiceHelper.isStartedOverlayService() &&
            isGooglePlay)
            ServiceHelper.startService(this, OverlayService::class.java)

        val numberOfCharges = pref.getLong(NUMBER_OF_CHARGES, 0L)
        val numberOfFullCharges = pref.getLong(NUMBER_OF_FULL_CHARGES, 0L)
        if(pref.getBoolean(IS_ENABLE_CHECK_UPDATE, resources.getBoolean(
                R.bool.is_enable_check_update)) && isInstalledGooglePlay &&
            isGooglePlay) checkUpdateFromGooglePlay()
        isShowRequestIgnoringBatteryOptimizationsDialog = true
        if(numberOfFullCharges > 0L && numberOfCharges % 2 == 0L && isInstalledGooglePlay &&
            !isGooglePlay &&
            (!isPremium || pref.getString(TOKEN_PREF, null)?.length != TOKEN_COUNT)
            && MainApp.isRequestPurchasePremium) requestPurchasePremium()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        super.onConfigurationChanged(newConfig)

        val newTheme = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES or
                newConfig.uiMode and Configuration.UI_MODE_NIGHT_NO

        if (newTheme != MainApp.currentTheme) {

            tempFragment = fragment

            isRecreate = true

            recreate()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE ->
                if ((grantResults.isNotEmpty() && grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED) || (grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_DENIED)
                ) checkManufacturer()

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onStop() {
        showRequestIgnoringBatteryOptimizationsDialog = null
        super.onStop()
    }

    override fun onDestroy() {
        instance = null
        fragment = null
        premiumActivity = null
        showNotInstalledFromGPDialog = null
        showFaqDialog = null
        if (!isRecreate) {
            tempFragment = null
            isLoadChargeDischarge = false
            isLoadLastCharge = false
            isLoadWear = false
            isLoadHistory = false
            isLoadSettings = false
            isLoadDebug = false
        }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (showRequestNotificationPermissionDialog == null)
            showRequestNotificationPermissionDialog =
                MaterialAlertDialogBuilder(this).apply {
                    setIcon(R.drawable.ic_instruction_not_supported_24dp)
                    setTitle(R.string.information)
                    setMessage(R.string.request_notification_message)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            POST_NOTIFICATIONS_PERMISSION_REQUEST_CODE)
                        showRequestNotificationPermissionDialog = null
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(2.5.seconds)
                            checkBatteryOptimizations()
                        }
                    }
                    setCancelable(false)
                    show()
                }
    }

    private fun checkBatteryOptimizations() {
        try {
            if(showRequestNotificationPermissionDialog == null) checkManufacturer()
            if(!isIgnoringBatteryOptimizations() && !isShowXiaomiBackgroundActivityControlDialog
                && isShowRequestIgnoringBatteryOptimizationsDialog &&
                showRequestIgnoringBatteryOptimizationsDialog == null &&
                showXiaomiAutostartDialog == null && showHuaweiInformation == null)
                showRequestIgnoringBatteryOptimizationsDialog()
        }
        catch(_: WindowManager.BadTokenException) {}
    }

    private fun requestPurchasePremium() {
        Snackbar.make(toolbar, getString(R.string.support_and_unlock_premium_features),
            5.seconds.inWholeMilliseconds.toInt()).apply {
            setAction(getString(R.string.get_premium)) {
                showPremiumDialog()
            }
            show()
        }
    }

    private fun importSettings(prefArrays: HashMap<*, *>?) {

        val prefsTempList = arrayListOf(
            BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH,
            DESIGN_CAPACITY, CAPACITY_ADDED, LAST_CHARGE_TIME, PERCENT_ADDED, RESIDUAL_CAPACITY
        )

        if (prefArrays != null)
            prefsTempList.forEach {

                with(prefArrays) {

                    when {

                        !containsKey(it) -> pref.edit().remove(it).apply()

                        else -> {

                            forEach {

                                when (it.key as String) {

                                    NUMBER_OF_CHARGES -> pref.edit().putLong(
                                        it.key as String,
                                        it.value as Long
                                    ).apply()

                                    BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, LAST_CHARGE_TIME,
                                    DESIGN_CAPACITY, RESIDUAL_CAPACITY, PERCENT_ADDED ->
                                        pref.edit().putInt(it.key as String, it.value as Int)
                                            .apply()

                                    CAPACITY_ADDED, NUMBER_OF_CYCLES ->
                                        pref.edit { putFloat(it.key as String, it.value as Float)
                                            .apply() }
                                }
                            }
                        }
                    }
                }
            }

        toolbar.menu.clear()

        isRestoreImportSettings = true

        this.prefArrays = null

        intent.removeExtra(IMPORT_RESTORE_SETTINGS_EXTRA)
    }

    fun backPressed() {
        if (isOnBackPressed) {
            if (toolbar.title != getString(R.string.settings) && !isRestoreImportSettings &&
                ((fragment != null && fragment !is SettingsFragment && fragment !is
                        ChargeDischargeFragment && fragment !is LastChargeNoPremiumFragment &&
                        fragment !is LastChargeFragment && fragment !is WearFragment &&
                        fragment !is HistoryFragment && fragment !is DebugFragment &&
                        fragment !is BackupSettingsFragment) || ((fragment is BackupSettingsFragment
                        || fragment is DebugFragment)
                        && supportFragmentManager.backStackEntryCount > 0))) {

                fragment = SettingsFragment()

                toolbar.title = getString(
                    if (fragment !is DebugFragment) R.string.settings
                    else R.string.debug
                )

                if (fragment is SettingsFragment) toolbar.navigationIcon = null

                supportFragmentManager.popBackStack()
            } else if (toolbar.title != getString(R.string.settings) && (fragment is BackupSettingsFragment &&
                        supportFragmentManager.backStackEntryCount == 0)
                || isRestoreImportSettings) {

                fragment = SettingsFragment()

                toolbar.title = getString(R.string.settings)

                toolbar.navigationIcon = null

                isRestoreImportSettings = false

                loadFragment(fragment ?: SettingsFragment())
            } else {

                if (isDoubleBackToExitPressedOnce) finish()
                else {

                    isDoubleBackToExitPressedOnce = true

                    Toast.makeText(
                        this@MainActivity, R.string.press_the_back_button_again,
                        Toast.LENGTH_LONG
                    ).show()

                    CoroutineScope(Dispatchers.Main).launch {

                        delay(3.seconds)
                        isDoubleBackToExitPressedOnce = false
                    }
                }
            }
        }
    }

}