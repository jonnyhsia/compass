package com.arch.jonnyhsia.compass.core

import androidx.collection.LruCache
import com.arch.jonnyhsia.compass.facade.CompassMeta
import com.arch.jonnyhsia.compass.facade.ICompassTable
import com.arch.jonnyhsia.compass.facade.IRouteEcho
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.PathReplacement
import com.arch.jonnyhsia.compass.facade.SchemeRecognizer
import com.arch.jonnyhsia.compass.facade.UnregisterPageHandler
import java.lang.reflect.Constructor

object CompassRepo {
    internal val routePages: Map<String, CompassMeta> = HashMap()

    internal var schemeInterceptor: SchemeRecognizer? = null
    internal var pageHandler: UnregisterPageHandler? = null
    internal var pathReplacement: PathReplacement? = null
    internal var routeInterceptors = ArrayList<IRouteInterceptor>()

    internal val cachedFragmentConstructor = LruCache<Class<*>, Constructor<*>>(24)
    internal val cachedEcho = LruCache<Class<*>, IRouteEcho>(24)

    fun installPages(table: ICompassTable) {
        (routePages as MutableMap).putAll(table.getPages())
    }
}