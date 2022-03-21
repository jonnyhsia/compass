package com.arch.jonnyhsia.compass

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.api.CompassPage
import com.arch.jonnyhsia.compass.api.ICompassTable
import com.arch.jonnyhsia.compass.api.PageKey
import com.arch.jonnyhsia.compass.api.TargetType
import com.arch.jonnyhsia.compass.interceptor.RouteInterceptor
import com.arch.jonnyhsia.compass.interceptor.SchemeInterceptor
import com.arch.jonnyhsia.compass.interceptor.UnregisterPageHandler
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

object Compass {

    private val initialized = AtomicBoolean(false)

    private lateinit var routePages: Map<PageKey, CompassPage>

    private var schemeInterceptor: SchemeInterceptor? = null
    private var pageHandler: UnregisterPageHandler? = null
    private var routeInterceptors = ArrayList<RouteInterceptor>()

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
    fun addRouteInterceptor(interceptor: RouteInterceptor) {
        routeInterceptors.add(interceptor)
    }

    /**
     * 验证是否有存在的 Page
     */
    @JvmStatic
    fun validatePageKey(key: PageKey): Boolean {
        return routePages.containsKey(key)
    }

    internal fun internalNavigate(context: Any, routeIntent: ProcessableIntent): Any? {
        // 判断协议拦截 (拦截非原生页, 页面升级等)
        schemeInterceptor?.intercept(routeIntent)

        // 寻找 url 对应的页面
        var page = routePages[routeIntent.pageKey]

        // 若页面未找到, 页面降级
        val uriBeforePageHandler = routeIntent.uri
        if (page == null && pageHandler != null) {
            pageHandler!!.handleUri(routeIntent)

            // 如果 url 被 handle 了, 则需要更新 page 对象
            if (uriBeforePageHandler != routeIntent.uri) {
                page = routePages[routeIntent.pageKey]
            }
        }

        page ?: return null

        // 若存在对应的页面, 则寻找 page 对应的拦截器
        val interceptorsOfPage = findInterceptorsOfPage(page)
        // 遍历拦截器
        for (interceptor in interceptorsOfPage) {
            val uriBeforeIntercept = routeIntent.uri
            interceptor.intercept(routeIntent)
            if (uriBeforeIntercept != routeIntent.uri) {
                page = routePages[routeIntent.pageKey]
            }
        }

        return performNavigate(context, page!!, routeIntent)
    }

    private fun performNavigate(
        context: Any,
        page: CompassPage,
        routeIntent: ProcessableIntent
    ): Any? = when (page.type) {
        TargetType.ACTIVITY -> {
            val activity = context.asActivity()
            val intent = Intent(activity, page.target)
            if (routeIntent.innerBundle != null) {
                intent.putExtras(routeIntent.innerBundle!!)
            }

            if (page.requestCode == 0) {
                ActivityCompat.startActivity(activity, intent, routeIntent.options)
            } else {
                val fragment = context.asFragment()
                if (fragment == null) {
                    ActivityCompat.startActivityForResult(
                        activity,
                        intent,
                        page.requestCode,
                        routeIntent.options
                    )
                } else {
                    fragment.startActivityForResult(intent, page.requestCode, routeIntent.options)
                }
            }
        }
        TargetType.FRAGMENT -> {
            val fragment = page.target.getConstructor().newInstance() as Fragment
            fragment.arguments = routeIntent.innerBundle
            fragment
        }
        else -> null
    }

    private fun findInterceptorsOfPage(page: CompassPage): List<RouteInterceptor> {
        if (page.interceptors.isEmpty() || routeInterceptors.isEmpty()) {
            return emptyList()
        }

        val definedInterceptorClzList = Arrays.asList(*page.interceptors)
        val interceptorInstanceList = ArrayList(routeInterceptors)

        val definedInterceptorInstanceList =
            ArrayList<RouteInterceptor>(definedInterceptorClzList.size)

        definedInterceptorClzList.forEachIndexed { index, clz ->
            val i = interceptorInstanceList.firstOrNull { it::class.java == clz }
            if (i != null) {
                definedInterceptorInstanceList.add(i)
            }
        }

        return definedInterceptorInstanceList
    }

    private fun Any.asActivity(): Activity {
        return when {
            this is Activity -> this
            this is Fragment -> requireActivity()
            else -> throw RuntimeException("$this can't cast to context.")
        }
    }

    private fun Any.asFragment(): Fragment? {
        return this as? Fragment
    }
}