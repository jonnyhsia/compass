package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.interceptor.RouteInterceptor
import com.arch.jonnyhsia.compass.sample.App

object XLoginInterceptor : RouteInterceptor {
    override fun intercept(intent: RouteIntent) {
        if (!App.INSTANCE.isLogin) {
            val pendingUri = intent.uri
            intent.redirect("core://Login")
                    .removeAllParameters()
                    .addParameter("entry", intent.requester)
                    .addParameter("pending", pendingUri)
        }
    }
}