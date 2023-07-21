package com.arch.jonnyhsia.compass.facade

interface NavigationCallback {
    fun onFound(intent: RouteIntent)
    fun onLost(intent: RouteIntent)
    fun onInterrupt(intent: RouteIntent)
    fun onArrival(intent: RouteIntent)
}