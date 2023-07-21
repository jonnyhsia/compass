package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

@KspExperimental
class KspRouteSymbol(val symbol: KSClassDeclaration) {

    val route: Route

    val targetKsType = symbol.asType()
    val target = targetKsType.toClassName()

    init {
        val routeAnnotation = symbol.findAnnotationWithType<Route>()!!

        val name = routeAnnotation.name
        val scheme: String = routeAnnotation.scheme
        val extras = routeAnnotation.extras
        route = Route(scheme, name, extras)
    }

    override fun toString(): String {
        return "name: ${route.name}"
    }
}