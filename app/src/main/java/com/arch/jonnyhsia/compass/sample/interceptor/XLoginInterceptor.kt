package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.ProcessableIntent
import com.arch.jonnyhsia.compass.interceptor.RouteInterceptor
import com.arch.jonnyhsia.compass.sample.App

object XLoginInterceptor : RouteInterceptor {
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