package com.arch.jonnyhsia.compass.sample

import android.app.Application
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.CompassTable
import com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor
import com.arch.jonnyhsia.compass.sample.interceptor.XMemberInterceptor
import com.arch.jonnyhsia.compass.sample.interceptor.XSchemeInterceptor
import com.arch.jonnyhsia.compass.sample.interceptor.XUnregisterPageHandle
import kotlin.properties.Delegates

class App : Application() {

    var isLogin = false

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        setupRouter()
    }

    private fun setupRouter() {
        Compass.run {
            initialize(CompassTable.getPages())
            // 设置协议拦截器
            setSchemeInterceptor(XSchemeInterceptor)
            // 404 处理
            setUnregisterPageHandler(XUnregisterPageHandle)
            // 登录拦截器
            addRouteInterceptor(XLoginInterceptor)
            // 订阅会员拦截器
            addRouteInterceptor(XMemberInterceptor)
        }
    }

    companion object {
        @JvmStatic
        var INSTANCE: App by Delegates.notNull()
            private set
    }
}