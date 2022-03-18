package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.Route
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import kotlin.reflect.KClass

@KspExperimental
class KspRouteSymbol(val symbol: KSClassDeclaration) {

    val route: Route

    // com.arch.jonnyhsia.compass.sample.DetailActivity
    val target = symbol.qualifiedName?.asString()!!

    init {
        val routeAnnotation = symbol.annotations.first { it.shortName.asString() == "Route" }

        var name = ""
        var scheme: String = ""
        var interceptors: Array<KClass<*>>? = null
        var requestCode: Int = 0
        routeAnnotation.arguments.forEach {
            when (it.name?.asString()) {
                "scheme" -> scheme = it.value as String? ?: ""
                "name" -> name = it.value as String
//                "interceptors" -> interceptors = it.value as Array<KClass<*>>?
                "requestCode" -> requestCode = it.value as Int
                else -> {}
            }
        }
        route = Route(scheme, name, interceptors ?: emptyArray(), requestCode)
    }

    override fun toString(): String {
        return "name: ${route.name}"
    }
}