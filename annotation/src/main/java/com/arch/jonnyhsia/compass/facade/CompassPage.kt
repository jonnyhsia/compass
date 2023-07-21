package com.arch.jonnyhsia.compass.facade

import com.arch.jonnyhsia.compass.facade.enums.TargetType

abstract class CompassMeta {
    abstract val name: String
    abstract val target: Class<*>
    abstract val type: Int
    abstract val extras: Int
    abstract val group: String
}

class CompassEcho(
    override val name: String,
    override val target: Class<*>,
    override val type: Int = TargetType.UNKNOWN,
    override val extras: Int,
    override val group: String = ""
) : CompassMeta()


class CompassPage @JvmOverloads constructor(
    override val name: String,
    override val target: Class<*>,
    override val type: Int = TargetType.UNKNOWN,
    override val extras: Int,
    override val group: String = "",
) : CompassMeta()