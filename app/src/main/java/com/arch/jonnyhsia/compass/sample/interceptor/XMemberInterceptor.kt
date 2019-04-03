package com.arch.jonnyhsia.compass.sample.interceptor

import com.arch.jonnyhsia.compass.ProcessableIntent
import com.arch.jonnyhsia.compass.interceptor.RouteInterceptor
import kotlin.random.Random

object XMemberInterceptor : RouteInterceptor {
    override fun intercept(intent: ProcessableIntent) {
        val isMember = Random.nextBoolean()
        if (!isMember) {
            intent.redirect("core://SubscribeMember")
                .addParameter("entry", intent.requester)
        }
    }
}