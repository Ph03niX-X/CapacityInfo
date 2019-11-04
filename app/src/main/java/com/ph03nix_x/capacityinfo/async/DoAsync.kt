package com.ph03nix_x.capacityinfo.async

import android.os.AsyncTask

class DoAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Unit>() {

    override fun doInBackground(vararg p0: Void?) = handler()
}