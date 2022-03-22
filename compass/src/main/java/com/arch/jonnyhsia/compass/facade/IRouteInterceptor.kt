package com.arch.jonnyhsia.compass.facade

/**
 * 原生页跳转拦截 (对应 @Route 注解中的 Interceptor)
 * 优先级: 低
 *
 * 主要用于用户登录等条件拦截, 跳转到其他页面
 */
interface IRouteInterceptor {
    fun intercept(intent: ProcessableIntent)
}