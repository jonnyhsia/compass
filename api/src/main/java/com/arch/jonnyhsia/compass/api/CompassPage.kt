package com.arch.jonnyhsia.compass.api


class CompassPage @JvmOverloads constructor(
    val name: String,
    val target: Class<*>,
    val requestCode: Int,
    val type: TargetType = TargetType.UNKNOWN,
    val group: String = "",
    val interceptors: Array<Class<*>> = emptyArray()
)