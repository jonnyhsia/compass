package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.facade.RouteIntent
import com.arch.jonnyhsia.compass.facade.SchemeRecognizer

object XSchemeInterceptor : SchemeRecognizer {

    override fun onRecognizeScheme(intent: RouteIntent) {
        val scheme = intent.scheme
        if ("http" == scheme || "https" == scheme) {
            // 是否有可升级的 native 页面
            val nativePage = intent.query("native_page")
            if (nativePage is String) {
                Compass.navigate(nativePage).go(intent.context)
            } else {
                val url = intent.path
                Compass.navigate("/Web").addParameter("url", url).go(intent.context)
            }
            intent.cancel()
        }
    }
}