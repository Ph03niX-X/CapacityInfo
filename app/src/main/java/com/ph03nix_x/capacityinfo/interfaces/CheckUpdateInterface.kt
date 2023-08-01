package com.ph03nix_x.capacityinfo.interfaces

import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.ph03nix_x.capacityinfo.R
import com.ph03nix_x.capacityinfo.activities.MainActivity
import com.ph03nix_x.capacityinfo.fragments.AboutFragment

/**
 * Created by Ph03niX-X on 30.07.2023
 * Ph03niX-X@outlook.com
 */
interface CheckUpdateInterface {

    fun MainActivity.checkUpdateFromGooglePlay() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = isUpdateAvailable(appUpdateInfo)
            val updateType = if(appUpdateInfo.isImmediateUpdateAllowed) AppUpdateType.IMMEDIATE
            else AppUpdateType.FLEXIBLE

            if(isUpdateDeveloperTriggered(appUpdateInfo)) {
                intentResultStarter()
                val appUpdateOptions =
                    AppUpdateOptions.newBuilder(updateType).setAllowAssetPackDeletion(false).build()
                startUpdate(appUpdateManager, appUpdateInfo, updateFlowResultLauncher,
                    appUpdateOptions)
            }
            else if(isUpdateAvailable) {
                intentResultStarter()
                val appUpdateOptions =
                    AppUpdateOptions.newBuilder(updateType).setAllowAssetPackDeletion(false).build()
                isCheckUpdateFromGooglePlay = false
                updateAvailableDialog(appUpdateManager, appUpdateInfo, appUpdateOptions)
            }
        }
    }
    
    private fun MainActivity.updateAvailableDialog(appUpdateManager: AppUpdateManager,
                                               appUpdateInfo: AppUpdateInfo,
                                               appUpdateOptions: AppUpdateOptions) {
        MaterialAlertDialogBuilder(this).apply { 
            setIcon(R.drawable.ic_check_update_24dp)
            setTitle(R.string.check_update)
            setMessage(R.string.update_available)
            setPositiveButton(R.string.update) {_, _ ->
                startUpdate(appUpdateManager, appUpdateInfo, updateFlowResultLauncher,
                    appUpdateOptions)
            }
            setNegativeButton(R.string.later_update) { _, _ ->
                isCheckUpdateFromGooglePlay = true
            }
            setCancelable(false)
            show()
        }
    }

    private fun MainActivity.intentResultStarter() =
        IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues,
                                       _, _ ->
            val request = IntentSenderRequest.Builder(intent).setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask).build()
            updateFlowResultLauncher.launch(request)
        }

    fun AboutFragment.checkUpdateFromGooglePlay() {
        val appUpdateManager = AppUpdateManagerFactory.create(requireContext())
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = isUpdateAvailable(appUpdateInfo)
            val updateType = if(appUpdateInfo.isImmediateUpdateAllowed) AppUpdateType.IMMEDIATE
            else AppUpdateType.FLEXIBLE
            if(isUpdateAvailable) {
                val updateFlowResultLauncher = MainActivity.instance?.updateFlowResultLauncher
                IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues,
                                               _, _ ->
                    val request = IntentSenderRequest.Builder(intent).setFillInIntent(fillInIntent)
                        .setFlags(flagsValues, flagsMask).build()
                    updateFlowResultLauncher?.launch(request)
                }
                val appUpdateOptions =
                    AppUpdateOptions.newBuilder(updateType).setAllowAssetPackDeletion(false).build()
                startUpdate(appUpdateManager, appUpdateInfo, updateFlowResultLauncher!!,
                    appUpdateOptions)
            }
            else {
                MainActivity.instance?.isCheckUpdateFromGooglePlay = true
                Toast.makeText(requireContext(), R.string.update_not_found, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isUpdateAvailable(appUpdateInfo: AppUpdateInfo): Boolean {
        val isUpdateAvailable = appUpdateInfo.updateAvailability() ==
                UpdateAvailability.UPDATE_AVAILABLE
        val isUpdateAllowed = appUpdateInfo.isImmediateUpdateAllowed
                || appUpdateInfo.isFlexibleUpdateAllowed
        return isUpdateAvailable && isUpdateAllowed
    }
    
    private fun isUpdateDeveloperTriggered(appUpdateInfo: AppUpdateInfo) =
        appUpdateInfo.updateAvailability() ==
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

    private fun startUpdate(appUpdateManager: AppUpdateManager, appUpdateInfo: AppUpdateInfo,
                            updateFlowResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
                            appUpdateOptions: AppUpdateOptions) = appUpdateManager
                                .startUpdateFlowForResult(appUpdateInfo, updateFlowResultLauncher,
                                    appUpdateOptions)
}