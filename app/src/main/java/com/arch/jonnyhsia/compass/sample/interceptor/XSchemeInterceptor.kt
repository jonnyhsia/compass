package com.arch.jonnyhsia.compass.sample.interceptor

import android.net.Uri
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.SchemeInterceptor

object XSchemeInterceptor : SchemeInterceptor {

    override fun intercept(intent: ProcessableIntent) {
        val scheme = intent.uri.scheme
        if ("http" == scheme || "https" == scheme) {
            // 是内嵌页
            val nativePage: String? = intent.uri.getQueryParameter("native_page")
            if (nativePage != null) {
                val uri = Uri.parse(nativePage)
                val path = uri.host!!
                // 如果页面能升级, 则前往对应原生页
                if (Compass.validatePagePath(path)) {
                    intent.redirect(nativePage)
                    return
                }
            }

            intent.redirect("core://Web").addParameter("url", intent.uri)
        }
    }
}