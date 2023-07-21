package com.arch.jonnyhsia.compass.core

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.facade.IRouteEcho
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import java.util.concurrent.ThreadPoolExecutor

@SuppressLint("StaticFieldLeak")
object CompassLogistics {

    private lateinit var context: Context
    internal lateinit var executor: ThreadPoolExecutor

    fun init(context: Context, executor: ThreadPoolExecutor) {
        this.context = context.applicationContext
        this.executor = executor
    }

    fun complete(context: Context, intent: ProcessableIntent) {
        // 判断协议拦截 (拦截非原生页, 页面升级等)
        CompassRepo.schemeInterceptor?.onRecognizeScheme(intent)
        if (intent.isPathCleared) {
            return
        }

        // 寻找 url 对应的页面
        val meta = CompassRepo.routePages[intent.path]
        if (meta == null) {
            val path = intent.uri.toString()
            // 若页面未找到, 页面降级
            if (CompassRepo.pageHandler != null) {
                // 显然 path 已经没有用处, 清除掉可以当做 pageHandler 是否处理的依据
                intent.clearPath()
                CompassRepo.pageHandler!!.onPageUnregister(intent)
                if (!intent.isPathCleared) {
                    complete(context, intent)
                    return
                }
            }
            Toast.makeText(context, "There is no route matched! $path", Toast.LENGTH_SHORT).show()
            // throw RuntimeException("There is no route matched! ${intent.uri}")
            return
        } else {
            intent.target = meta.target
            intent.type = meta.type
            intent.extras = meta.extras

            for (key in intent.uri.queryParameterNames) {
                val value = intent.uri.getQueryParameter(key)
                intent.addParameter(key, value)
            }

            when (meta.type) {
                TargetType.ECHO -> {
                    // echo 类型的 target 必然是 IRouteEcho 类型
                    val echoClz = meta.target as Class<IRouteEcho>
                    var echo = CompassRepo.cachedEcho[echoClz]
                    if (echo == null) {
                        try {
                            echo = echoClz.getConstructor().newInstance()
                            CompassRepo.cachedEcho.put(echoClz, echo)
                        } catch (e: Exception) {
                            Log.e(Compass.TAG, "Cannot create echo instance!", e)
                            throw RuntimeException("Cannot create echo instance!")
                        }
                    }
                    intent.echo = echo
                    intent.greenChannel()
                }

                TargetType.FRAGMENT -> {
                    val fragmentClz = meta.target
                    val fragment: Fragment
                    try {
                        var constructor = CompassRepo.cachedFragmentConstructor[fragmentClz]
                        if (constructor == null) {
                            constructor = fragmentClz.getConstructor()
                            CompassRepo.cachedFragmentConstructor.put(fragmentClz, constructor)
                        }
                        fragment = constructor!!.newInstance() as Fragment
                    } catch (e: Exception) {
                        Log.e(Compass.TAG, "Cannot create fragment instance!", e)
                        throw RuntimeException("Cannot create fragment instance!")
                    }
                    intent.fragment = fragment
                    intent.greenChannel()
                }

                TargetType.ACTIVITY, TargetType.UNKNOWN -> {

                }
            }
        }

//        if (meta is CompassPage) {
//            // 若存在对应的页面, 则寻找 page 对应的拦截器
//            val interceptorsOfPage = findInterceptorsOfPage(meta)
//            // 遍历拦截器
//            for (interceptor in interceptorsOfPage) {
//                val uriBeforeIntercept = routeIntent.uri
//                interceptor.intercept(context, routeIntent)
//                if (routeIntent.isCleared) {
//                    return null
//                }
//                if (uriBeforeIntercept != routeIntent.uri) {
//                    meta = CompassRepo.routePages[routeIntent.path]
//                }
//            }
//        }
    }

//    private fun findInterceptorsOfPage(page: CompassPage): List<IRouteInterceptor> {
//        if (page.interceptors.isEmpty() || routeInterceptors.isEmpty()) {
//            return emptyList()
//        }
//
//        val definedInterceptorClzList = listOf(*page.interceptors)
//        val interceptorInstanceList = ArrayList(routeInterceptors)
//
//        val definedInterceptorInstanceList =
//            ArrayList<IRouteInterceptor>(definedInterceptorClzList.size)
//
//        definedInterceptorClzList.forEachIndexed { index, clz ->
//            val i = interceptorInstanceList.firstOrNull { it::class.java == clz }
//            if (i != null) {
//                definedInterceptorInstanceList.add(i)
//            }
//        }
//
//        return definedInterceptorInstanceList
//    }
}