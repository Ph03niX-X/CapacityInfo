package com.ph03nix_x.capacityinfo.interfaces

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.ph03nix_x.capacityinfo.activities.MainActivity

/**
 * Created by Ph03niX-X on 30.07.2023
 * Ph03niX-X@outlook.com
 */
interface CheckUpdateInterface {

    fun MainActivity.checkUpdateFromGooglePlay() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val isUpdateAvailable = appUpdateInfo.updateAvailability() ==
                    UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateDeveloperTriggered = appUpdateInfo.updateAvailability() ==
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
            val isUpdateAllowed = appUpdateInfo.isImmediateUpdateAllowed
                    || appUpdateInfo.isFlexibleUpdateAllowed

            val updateType = if(appUpdateInfo.isImmediateUpdateAllowed) AppUpdateType.IMMEDIATE
            else AppUpdateType.FLEXIBLE

            IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
                val request = IntentSenderRequest.Builder(intent).setFillInIntent(fillInIntent)
                    .setFlags(flagsValues, flagsMask).build()
                updateFlowResultLauncher.launch(request)
            }

            val appUpdateOptions =
                AppUpdateOptions.newBuilder(updateType).setAllowAssetPackDeletion(false).build()

            if(isUpdateDeveloperTriggered) startUpdate(appUpdateInfo, updateFlowResultLauncher,
                appUpdateOptions)
            else if(isUpdateAvailable && isUpdateAllowed)
                startUpdate(appUpdateInfo, updateFlowResultLauncher, appUpdateOptions)

            if((isUpdateAvailable && isUpdateAllowed) || isUpdateDeveloperTriggered)
                isCheckUpdateFromGooglePlay = false
        }
    }

    private fun MainActivity.startUpdate(appUpdateInfo: AppUpdateInfo, updateFlowResultLauncher:
    ActivityResultLauncher<IntentSenderRequest>, appUpdateOptions: AppUpdateOptions) =
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, updateFlowResultLauncher,
            appUpdateOptions)
}