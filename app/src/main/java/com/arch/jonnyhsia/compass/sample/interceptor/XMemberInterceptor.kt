package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.interceptor.RouteInterceptor
import kotlin.random.Random

object XMemberInterceptor : RouteInterceptor {
    override fun intercept(intent: RouteIntent) {
        val isMember = Random.nextBoolean()
        if (!isMember) {
            intent.redirect("core://SubscribeMember")
                .addParameter("entry", intent.requester)
        }
    }
}