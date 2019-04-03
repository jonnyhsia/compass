package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.ProcessableIntent
import com.arch.jonnyhsia.compass.interceptor.UnregisterPageHandler

object XUnregisterPageHandler : UnregisterPageHandler {

    override fun handleUri(intent: ProcessableIntent) {
        intent.redirect("sample://Main")
                .removeAllParameters()
    }
}