package com.ph03nix_x.capacityinfo

import kotlinx.coroutines.Job

class Util {

    companion object {

        var jobUpdateNotification: Job? = null
        var isPowerConnected = false
        var tempCurrentCapacity: Double = 0.0
        var tempBatteryLevel = 0
        var capacityAdded = 0.0
        var percentAdded = 0
        var hoursDefault = 0
        var progressSeekBar = -1
    }
}