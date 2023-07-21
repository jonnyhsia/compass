package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.sample.App
import com.arch.jonnyhsia.compass.sample.RouteExtras

object XLoginInterceptor : IRouteInterceptor {
    override fun intercept(intent: ProcessableIntent, callback: InterceptCallback) {
        if (!App.INSTANCE.isLogin && RouteExtras.loginRequired(intent.extras)) {
            val pendingUri = intent.uri
            intent.redirect("://Login")
                .removeAllParameters()
                .addParameter("entry", intent.callerName)
                .addParameter("pending", pendingUri)
                .go(intent.context)
            callback.onInterrupt(null)
        }
        callback.onContinue(intent)
    }
}