package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.sample.RouteExtras
import kotlin.random.Random

object XMemberInterceptor : IRouteInterceptor {
    override fun intercept(intent: ProcessableIntent, callback: InterceptCallback) {
        val isMember = Random.nextBoolean()
        if (!isMember && RouteExtras.memberRequired(intent.extras)) {
            intent.redirect("://SubscribeMember")
                .removeAllParameters()
                .addParameter("entry", intent.callerName)
                .go(intent.context)
            callback.onInterrupt(null)
            return
        }
        callback.onContinue(intent)
    }
}