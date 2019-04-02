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
            // @com.arch.jonnyhsia.compass.api.Route(interceptors=com.arch.jonnyhsia.compass.sample.MainActivity,android.widget.TextView, name=Main)
            // com.arch.jonnyhsia.compass.sample.MainActivity,android.widget.TextView
            val s = route.toString()
            val start = s.indexOf("interceptors=") + "interceptors=".length
            // 特别留意代码修改之后是否还是 name
            val end = s.indexOf(", name")
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