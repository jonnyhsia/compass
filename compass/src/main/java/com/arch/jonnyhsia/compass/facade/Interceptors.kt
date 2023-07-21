package com.arch.jonnyhsia.compass.facade

import android.net.Uri


/**
 * 协议拦截器 (最多只有一个)
 * 优先级: 高
 *
 * 主要用于内嵌页与内嵌页的升级(跳转内嵌页, 或对应原生页)
 */
interface SchemeRecognizer {
    fun onRecognizeScheme(intent: RouteIntent)
}

/**
 * 未注册的页面的跳转处理 (最多只有一个)
 * 优先级: 中
 *
 * 主要用于原生页的升级(老页面已经删除, 升级到新页面)
 * 与降级(无法兼容的原生页, 跳转到指定页)
 */
interface UnregisterPageHandler {
    fun onPageUnregister(intent: RouteIntent)
}

interface PathReplacement {
    fun replaceString(path: String): String
    fun replaceUri(uri: Uri): Uri
}