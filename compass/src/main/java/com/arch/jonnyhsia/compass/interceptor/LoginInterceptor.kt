//package com.arch.jonnyhsia.compass.interceptor
//
//import com.arch.jonnyhsia.compass.RouteResult
//
//object LoginInterceptor : RouteInterceptor {
//
//    override fun intercept(chain: RouteInterceptor.Chain): RouteResult {
//        val intent = chain.intent()
//
//        if (!isLogin()) {
//            intent.redirect(name = "Login", isFinal = true)
//                .removeAllParameters()
//                .addParameter("entry", intent.requester)
//        }
//
//        return chain.proceed(intent)
//    }
//
//    private fun isLogin(): Boolean {
//        return true
//    }
//}