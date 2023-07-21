package com.arch.jonnyhsia.compass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.core.CompassExecutor
import com.arch.jonnyhsia.compass.core.CompassInterceptHandler
import com.arch.jonnyhsia.compass.core.CompassLogistics
import com.arch.jonnyhsia.compass.core.CompassRepo
import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.ICompassTable
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.RouteIntent
import com.arch.jonnyhsia.compass.facade.SchemeRecognizer
import com.arch.jonnyhsia.compass.facade.UnregisterPageHandler
import com.arch.jonnyhsia.compass.facade.enums.TargetType
import java.util.concurrent.atomic.AtomicBoolean

object Compass {

    internal const val TAG = "Compass"

    private val initialized = AtomicBoolean(false)

    private val executor = CompassExecutor.getInstance()

    /**
     * 初始化路由
     */
    @JvmStatic
    @Synchronized
    fun initialize(context: Context, table: ICompassTable) {
        if (initialized.compareAndSet(false, true)) {
            CompassRepo.installPages(table)
            CompassLogistics.init(context, executor)
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
    fun setSchemeInterceptor(interceptor: SchemeRecognizer) {
        CompassRepo.schemeInterceptor = interceptor
    }

    @JvmStatic
    fun setUnregisterPageHandler(handler: UnregisterPageHandler) {
        CompassRepo.pageHandler = handler
    }

    @JvmStatic
    fun addRouteInterceptor(interceptor: IRouteInterceptor) {
        CompassRepo.routeInterceptors.add(interceptor)
    }

    internal fun internalNavigate(context: Any, routeIntent: ProcessableIntent): Any? {
        CompassLogistics.complete(context.asContext(), routeIntent)
        if (!routeIntent.greenChannel) {
            CompassInterceptHandler.performIntercept(routeIntent, object : InterceptCallback {
                override fun onContinue(intent: ProcessableIntent) {
                    performNavigate(context, routeIntent)
                }

                override fun onInterrupt(exception: Exception?) {
                    // TODO: jonny 23/7/21
                    exception?.printStackTrace()
                }
            })
        } else {
            return performNavigate(context, routeIntent)
        }
        return null
    }

    private fun performNavigate(
        context: Any,
        routeIntent: ProcessableIntent
    ): Any? = when (routeIntent.type) {
        TargetType.ACTIVITY -> {
            val activity = context.getActivity()
            val intent = Intent(activity, routeIntent.target)
            routeIntent.arguments?.let { args ->
                intent.putExtras(args)
            }

            if (routeIntent.requestCode == 0) {
                ActivityCompat.startActivity(activity, intent, routeIntent.options)
            } else {
                val fragment = context.asFragment()
                if (fragment == null) {
                    ActivityCompat.startActivityForResult(
                        activity,
                        intent,
                        routeIntent.requestCode,
                        routeIntent.options
                    )
                } else {
                    fragment.startActivityForResult(
                        intent,
                        routeIntent.requestCode,
                        routeIntent.options
                    )
                }
            }
        }

        TargetType.FRAGMENT -> {
            routeIntent.fragment!!.also {
                it.arguments = routeIntent.arguments
            }
        }

        TargetType.ECHO -> {
            routeIntent.echo!!.also {
                it.run(context.asContext(), routeIntent.arguments)
            }
        }

        else -> null
    }


    private fun Any.getActivity(): Activity {
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