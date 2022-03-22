package com.arch.jonnyhsia.compass.facade

interface ICompassTable {

    fun getPages(): Map<String, CompassMeta>
}