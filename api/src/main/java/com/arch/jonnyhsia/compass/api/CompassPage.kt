package com.arch.jonnyhsia.compass.api

class CompassPage(
        val name: String,
        val target: Class<*>,
        val requestCode: Int,
        val interceptors: Array<Class<*>>
)