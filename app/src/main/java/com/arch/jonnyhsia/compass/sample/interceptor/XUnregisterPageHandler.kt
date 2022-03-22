package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.UnregisterPageHandler

object XUnregisterPageHandler : UnregisterPageHandler {

    override fun handleUri(intent: ProcessableIntent) {
        intent.redirect("sample://Main")
                .removeAllParameters()
    }
}