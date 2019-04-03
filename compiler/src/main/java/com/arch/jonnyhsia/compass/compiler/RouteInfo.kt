package com.arch.jonnyhsia.compass.compiler

import com.arch.jonnyhsia.compass.api.Route
import com.squareup.javapoet.ClassName
import javax.lang.model.element.TypeElement

class RouteInfo(
    private val element: TypeElement
) {

    private val route = element.getAnnotation(Route::class.java)

    val name: String
        get() = route.name

    val target: ClassName
        get() = ClassName.get(element)

    val routeString: String
        get() = route.toString()

    val interceptors: List<ClassName>
        get() {
            // com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor
            // @com.arch.jonnyhsia.compass.api.Route(requestCode=11, interceptors=com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor, scheme=, name=sample://Subscribe)
            val s = route.toString().substringAfter("interceptors=")
            // com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor, scheme=, name=sample://Subscribe)
            val start = 0
            // com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor
            val end = s.indexOf(", ")
            if (start == end) {
                return emptyList()
            }

            return s.substring(start, end).split(",")
                .map {
                    val dotBeforeClass = it.lastIndexOf(".")
                    ClassName.get(it.substring(0, dotBeforeClass), it.substring(dotBeforeClass + 1))
                }
        }

    val requestCode: Int
        get() = route.requestCode

}