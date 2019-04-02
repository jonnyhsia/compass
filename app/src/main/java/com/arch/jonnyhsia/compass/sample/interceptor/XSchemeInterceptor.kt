package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.interceptor.SchemeInterceptor

object XSchemeInterceptor : SchemeInterceptor {
    override fun intercept(intent: RouteIntent) {
        val scheme = intent.uri.scheme
        if ("http" == scheme || "https" == scheme) {
            // 是内嵌页
            val nativePage = intent.uri.getQueryParameter("native_page")
            if (Compass.validatePageKey(nativePage)) {
                intent.redirect(nativePage!!)
            } else {
                intent.redirect("core://Web")
                    .addParameter("url", intent.uri.toString())
            }
        }
    }
}