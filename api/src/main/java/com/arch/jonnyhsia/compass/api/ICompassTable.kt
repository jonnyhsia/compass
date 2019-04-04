package com.arch.jonnyhsia.compass.api

interface ICompassTable {

    fun getPages(): HashMap<PageKey, CompassPage>
}