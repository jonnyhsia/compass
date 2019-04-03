package com.arch.jonnyhsia.compass.api

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class Route(
    val scheme: String = "",
    val name: String,
    val interceptors: Array<KClass<*>> = [],
    val requestCode: Int = -1
)