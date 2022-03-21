package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.Route
import com.arch.jonnyhsia.compass.api.TargetType
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import kotlin.reflect.KClass

@KotlinPoetKspPreview
@KspExperimental
class KspRouteSymbol(symbol: KSClassDeclaration) {

    val route: Route

    val targetKsType = symbol.asType()
    val target = targetKsType.toClassName()

    init {
        val routeAnnotation = symbol.findAnnotationWithType<Route>()!!

        val name = routeAnnotation.name
        val scheme: String = routeAnnotation.scheme
        val interceptors: Array<KClass<*>>? = null
        val requestCode: Int = routeAnnotation.requestCode
        route = Route(scheme, name, interceptors ?: emptyArray(), requestCode)
    }

    override fun toString(): String {
        return "name: ${route.name}"
    }
}