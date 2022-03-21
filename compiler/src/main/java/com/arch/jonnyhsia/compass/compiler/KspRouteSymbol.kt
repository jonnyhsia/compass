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
        val routeAnnotation = symbol.annotations.first { it.shortName.asString() == "Route" }

        var name = ""
        var scheme: String = ""
        var interceptors: Array<KClass<*>>? = null
        var requestCode: Int = 0
        routeAnnotation.arguments.forEach {
            val argName = it.name?.asString()
            when (argName) {
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