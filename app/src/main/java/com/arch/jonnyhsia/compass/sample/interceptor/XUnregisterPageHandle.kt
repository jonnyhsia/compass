package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.interceptor.UnregisterPageHandler

object XUnregisterPageHandle: UnregisterPageHandler {
    override fun handleUri(intent: RouteIntent) {
        intent.redirect("sample://Main")
            .removeAllParameters()
    }
}