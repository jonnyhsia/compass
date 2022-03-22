package com.arch.jonnyhsia.compass.facade.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class RouteInterceptor(
    val name: String,
    val priority: Int = 10
)