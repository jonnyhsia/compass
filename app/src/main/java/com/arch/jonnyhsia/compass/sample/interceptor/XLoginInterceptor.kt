package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.annotation.RouteInterceptor
import com.arch.jonnyhsia.compass.sample.App

object XLoginInterceptor : IRouteInterceptor {
    override fun intercept(intent: ProcessableIntent) {
        if (!App.INSTANCE.isLogin) {
            val pendingUri = intent.uri
            intent.redirect("*://Login")
                .removeAllParameters()
                .addParameter("entry", intent.requester)
                .addParameter("pending", pendingUri)
        }
    }
}