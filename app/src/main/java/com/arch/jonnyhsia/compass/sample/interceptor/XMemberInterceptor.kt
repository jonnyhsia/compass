package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.RouteIntent
import com.arch.jonnyhsia.compass.sample.RouteExtras
import kotlin.random.Random

object XMemberInterceptor : IRouteInterceptor {
    override fun intercept(intent: RouteIntent, callback: InterceptCallback) {
        val isMember = Random.nextBoolean()
        if (!isMember && RouteExtras.memberRequired(intent.extras)) {
            Compass.navigate("/SubscribeMember").go(intent.context)
            callback.onInterrupt(null)
            return
        }
        callback.onContinue(intent)
    }
}