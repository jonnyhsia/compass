package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.SchemeRecognizer

object XSchemeInterceptor : SchemeRecognizer {

    override fun onRecognizeScheme(intent: ProcessableIntent) {
        val scheme = intent.uri.scheme
        if ("http" == scheme || "https" == scheme) {
            // 是否有可升级的 native 页面
            val nativePage: String? = intent.uri.getQueryParameter("native_page")
            if (nativePage != null) {
                intent.redirect(nativePage)
            } else {
                val url = intent.uri.toString()
                intent.redirect("://Web").addParameter("url", url)
            }
        }
    }
}