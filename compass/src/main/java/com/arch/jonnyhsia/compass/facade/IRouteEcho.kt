package com.arch.jonnyhsia.compass.facade

import android.content.Context
import android.os.Bundle

interface IRouteEcho {

    fun init(context: Context) {
    }

    /**
     * 默认调用方法, 也可以自定义方法通过对 Compass 的返回值进行显式调用
     */
    fun run(context: Context, args: Bundle?)
}