package com.arch.jonnyhsia.compass.facade

import com.arch.jonnyhsia.compass.facade.enums.TargetType

abstract class CompassMeta {
    abstract val name: String
    abstract val target: Class<*>
    abstract val type: TargetType
}

class CompassEcho(
    override val name: String,
    override val target: Class<*>,
    override val type: TargetType
) : CompassMeta()


class CompassPage @JvmOverloads constructor(
    override val name: String,
    override val target: Class<*>,
    override val type: TargetType = TargetType.UNKNOWN,
    val group: String = "",
    val requestCode: Int = 0,

    @Deprecated(message = "拦截器将用独立的注解以独立的表维护")
    val interceptors: Array<Class<*>> = emptyArray()
) : CompassMeta()