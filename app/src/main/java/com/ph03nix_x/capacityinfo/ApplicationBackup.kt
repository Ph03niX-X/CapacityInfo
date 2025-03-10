package com.ph03nix_x.capacityinfo

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.content.SharedPreferences
import android.os.ParcelFileDescriptor
import androidx.preference.PreferenceManager
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_TO
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.BATTERY_LEVEL_WITH
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.CAPACITY_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.DESIGN_CAPACITY
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CHARGES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.NUMBER_OF_CYCLES
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.PERCENT_ADDED
import com.ph03nix_x.capacityinfo.utilities.PreferencesKeys.RESIDUAL_CAPACITY
import androidx.core.content.edit

class ApplicationBackup : BackupAgent() {

    private var pref: SharedPreferences? = null
    private var prefArrays: MutableMap<String, *>? = null

    override fun onCreate() {
        super.onCreate()
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        prefArrays = pref.all
    }

    override fun onBackup(oldState: ParcelFileDescriptor?, data: BackupDataOutput?,
                          newState: ParcelFileDescriptor?) {}

    override fun onRestore(data: BackupDataInput?, appVersionCode: Int,
                           newState: ParcelFileDescriptor?) {}

    override fun onRestoreFinished() {
        super.onRestoreFinished()
        val prefsTempList = arrayListOf(BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH,
            DESIGN_CAPACITY, CAPACITY_ADDED, PERCENT_ADDED, RESIDUAL_CAPACITY)
        prefsTempList.forEach {
            with(prefArrays) {
                when {
                    this?.containsKey(it) == false -> pref?.edit { remove(it) }
                    else -> {
                        this?.forEach {
                            when(it.key) {
                                NUMBER_OF_CHARGES -> pref?.edit { putLong(it.key, it.value as Long) }
                                BATTERY_LEVEL_TO, BATTERY_LEVEL_WITH, DESIGN_CAPACITY,
                                RESIDUAL_CAPACITY, PERCENT_ADDED -> pref?.edit { putInt(it.key,
                                    it.value as Int) }
                                CAPACITY_ADDED, NUMBER_OF_CYCLES -> pref?.edit { putFloat(it.key,
                                    it.value as Float) }
                            }
                        }
                    }
                }
            }
        }
    }
}