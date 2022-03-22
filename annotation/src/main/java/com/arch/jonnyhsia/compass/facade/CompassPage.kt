package com.arch.jonnyhsia.compass.facade

import com.arch.jonnyhsia.compass.facade.enums.TargetType


class CompassPage @JvmOverloads constructor(
    val name: String,
    val target: Class<*>,
    val requestCode: Int,
    val type: TargetType = TargetType.UNKNOWN,
    val group: String = "",

    @Deprecated(message = "拦截器将用独立的注解以独立的表维护")
    val interceptors: Array<Class<*>> = emptyArray()
)