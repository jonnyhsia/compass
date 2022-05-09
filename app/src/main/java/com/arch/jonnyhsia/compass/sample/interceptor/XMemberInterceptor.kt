package com.arch.jonnyhsia.compass.sample.interceptor

import android.content.Context
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.annotation.RouteInterceptor
import kotlin.random.Random

object XMemberInterceptor : IRouteInterceptor {
    override fun intercept(context: Context, intent: ProcessableIntent) {
        val isMember = Random.nextBoolean()
        if (!isMember) {
            intent.redirect("core://SubscribeMember")
                .addParameter("entry", intent.requester)
        }
    }
}