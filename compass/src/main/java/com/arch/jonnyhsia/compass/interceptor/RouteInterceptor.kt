package com.arch.jonnyhsia.compass.interceptor

import com.arch.jonnyhsia.compass.RouteIntent


/**
 * 协议拦截器 (最多只有一个)
 * 优先级: 高
 *
 * 主要用于内嵌页与内嵌页的升级(跳转内嵌页, 或对应原生页)
 */
interface SchemeInterceptor {
    fun intercept(intent: RouteIntent)
}

/**
 * 未注册的页面的跳转处理 (最多只有一个)
 * 优先级: 中
 *
 * 主要用于原生页的升级(老页面已经删除, 升级到新页面)
 * 与降级(无法兼容的原生页, 跳转到指定页)
 */
interface UnregisterPageHandler {
    fun handleUri(intent: RouteIntent)
}

/**
 * 原生页跳转拦截 (对应 @Route 注解中的 Interceptor)
 * 优先级: 低
 *
 * 主要用于用户登录等条件拦截, 跳转到其他页面
 */
interface RouteInterceptor {
    fun intercept(intent: RouteIntent)
}