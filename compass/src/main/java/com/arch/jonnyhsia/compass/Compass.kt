package com.arch.jonnyhsia.compass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.collection.ArrayMap
import androidx.collection.LruCache
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.facade.*
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import java.lang.reflect.Constructor
import java.util.concurrent.atomic.AtomicBoolean

object Compass {

    private val initialized = AtomicBoolean(false)

    private lateinit var routePages: Map<String, CompassMeta>

    private var schemeInterceptor: SchemeInterceptor? = null
    private var pageHandler: UnregisterPageHandler? = null
    private var routeInterceptors = ArrayList<IRouteInterceptor>()

    private val cachedFragmentConstructor = LruCache<CompassPage, Constructor<*>>(20)

    private val cachedBlock = ArrayMap<CompassEcho, IRouteEcho>()

    /**
     * 初始化路由
     */
    @JvmStatic
    @Synchronized
    fun initialize(table: ICompassTable) {
        if (initialized.compareAndSet(false, true)) {
            routePages = HashMap(table.getPages())
        }
    }

    @JvmStatic
    fun navigate(url: String): RouteIntent {
        return if (url.contains("://")) {
            ProcessableIntent(url)
        } else {
            ProcessableIntent("://${url}")
        }
    }

    @JvmStatic
    fun navigate(uri: Uri): RouteIntent {
        return ProcessableIntent(uri)
    }

    @JvmStatic
    fun setSchemeInterceptor(interceptor: SchemeInterceptor) {
        schemeInterceptor = interceptor
    }

    @JvmStatic
    fun setUnregisterPageHandler(handler: UnregisterPageHandler) {
        pageHandler = handler
    }

    @JvmStatic
    fun addRouteInterceptor(interceptor: IRouteInterceptor) {
        routeInterceptors.add(interceptor)
    }

    /**
     * 验证是否有存在的 Page
     */
    @JvmStatic
    fun validatePagePath(key: String): Boolean {
        return routePages.containsKey(key)
    }

    internal fun internalNavigate(context: Any, routeIntent: ProcessableIntent): Any? {
        // 判断协议拦截 (拦截非原生页, 页面升级等)
        schemeInterceptor?.intercept(context.asContext(), routeIntent)
        if (routeIntent.isCleared) {
            return null
        }

        // 寻找 url 对应的页面
        var meta = routePages[routeIntent.path]

        // 若页面未找到, 页面降级
        val uriBeforePageHandler = routeIntent.uri
        if (meta == null && pageHandler != null) {
            pageHandler!!.intercept(context.asContext(), routeIntent)
            if (routeIntent.isCleared) {
                return null
            }

            // 如果 url 被 handle 了, 则需要更新 page 对象
            if (uriBeforePageHandler != routeIntent.uri) {
                meta = routePages[routeIntent.path]
            }
        }

        meta ?: return null

        if (meta is CompassPage) {
            // 若存在对应的页面, 则寻找 page 对应的拦截器
            val interceptorsOfPage = findInterceptorsOfPage(meta)
            // 遍历拦截器
            for (interceptor in interceptorsOfPage) {
                val uriBeforeIntercept = routeIntent.uri
                interceptor.intercept(context.asContext(), routeIntent)
                if (routeIntent.isCleared) {
                    return null
                }
                if (uriBeforeIntercept != routeIntent.uri) {
                    meta = routePages[routeIntent.path]
                }
            }
        }

        return performNavigate(context, meta!!, routeIntent)
    }

    private fun performNavigate(
        context: Any,
        meta: CompassMeta,
        routeIntent: ProcessableIntent
    ): Any? = when (meta.type) {
        TargetType.ACTIVITY -> {
            meta as CompassPage

            val activity = context.asActivity()
            val intent = Intent(activity, meta.target)
            if (routeIntent.innerBundle != null) {
                intent.putExtras(routeIntent.innerBundle!!)
            }

            if (meta.requestCode == 0) {
                ActivityCompat.startActivity(activity, intent, routeIntent.options)
            } else {
                val fragment = context.asFragment()
                if (fragment == null) {
                    ActivityCompat.startActivityForResult(
                        activity,
                        intent,
                        meta.requestCode,
                        routeIntent.options
                    )
                } else {
                    fragment.startActivityForResult(intent, meta.requestCode, routeIntent.options)
                }
            }
        }
        TargetType.FRAGMENT -> {
            meta as CompassPage

            var constructor = cachedFragmentConstructor[meta]
            if (constructor == null) {
                constructor = meta.target.getConstructor()
                cachedFragmentConstructor.put(meta, constructor)
            }
            val fragment = constructor!!.newInstance() as Fragment
            fragment.arguments = routeIntent.innerBundle
            fragment
        }
        TargetType.ECHO -> {
            meta as CompassEcho
            var block = cachedBlock[meta]
            if (block == null) {
                block = meta.target.getConstructor().newInstance() as IRouteEcho
                block.init(context.asContext())
            }
            block.run(context.asContext(), routeIntent.innerBundle)
            block
        }
        else -> null
    }

    private fun findInterceptorsOfPage(page: CompassPage): List<IRouteInterceptor> {
        if (page.interceptors.isEmpty() || routeInterceptors.isEmpty()) {
            return emptyList()
        }

        val definedInterceptorClzList = listOf(*page.interceptors)
        val interceptorInstanceList = ArrayList(routeInterceptors)

        val definedInterceptorInstanceList =
            ArrayList<IRouteInterceptor>(definedInterceptorClzList.size)

        definedInterceptorClzList.forEachIndexed { index, clz ->
            val i = interceptorInstanceList.firstOrNull { it::class.java == clz }
            if (i != null) {
                definedInterceptorInstanceList.add(i)
            }
        }

        return definedInterceptorInstanceList
    }

    private fun Any.asActivity(): Activity {
        return when (this) {
            is Activity -> this
            is Fragment -> requireActivity()
            else -> throw RuntimeException("$this can't cast to activity.")
        }
    }

    private fun Any.asFragment(): Fragment? {
        return this as? Fragment
    }

    private fun Any.asContext(): Context {
        return when (this) {
            is Activity -> this
            is Fragment -> requireContext()
            else -> throw RuntimeException("$this can't cast to context.")
        }
    }
}