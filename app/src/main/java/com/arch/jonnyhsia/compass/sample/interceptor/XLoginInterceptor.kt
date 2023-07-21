package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.RouteIntent
import com.arch.jonnyhsia.compass.sample.App
import com.arch.jonnyhsia.compass.sample.RouteExtras

object XLoginInterceptor : IRouteInterceptor {
    override fun intercept(intent: RouteIntent, callback: InterceptCallback) {
        if (!App.INSTANCE.isLogin && RouteExtras.loginRequired(intent.extras)) {
            Compass.navigate("/Login").go(intent.context)
            callback.onInterrupt(null)
        }
        callback.onContinue(intent)
    }
}