package com.arch.jonnyhsia.compass.facade.annotation

@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(AnnotationRetention.BINARY)
annotation class Route(
    val scheme: String = "",
    val name: String,
    val extras: Int = 0
)