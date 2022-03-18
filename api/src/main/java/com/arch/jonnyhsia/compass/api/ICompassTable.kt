package com.arch.jonnyhsia.compass.api

interface ICompassTable {

    fun getPages(): Map<PageKey, CompassPage>
}