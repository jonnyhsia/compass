package com.arch.jonnyhsia.compass.sample.interceptor

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.arch.jonnyhsia.compass.facade.IRouteEcho
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.sample.App

@Route(name = "/expired")
class ExpiredCallback : IRouteEcho {
    override fun run(context: Context, args: Bundle?) {
        App.INSTANCE.isLogin = false
        Toast.makeText(context, "登录失效", Toast.LENGTH_SHORT).show()
    }
}