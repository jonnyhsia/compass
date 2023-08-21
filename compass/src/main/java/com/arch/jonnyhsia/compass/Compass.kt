package com.arch.jonnyhsia.compass

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.core.CompassExecutor
import com.arch.jonnyhsia.compass.core.CompassInterceptHandler
import com.arch.jonnyhsia.compass.core.CompassLogistics
import com.arch.jonnyhsia.compass.core.CompassRepo
import com.arch.jonnyhsia.compass.core.InterceptCallback
import com.arch.jonnyhsia.compass.facade.ICompassTable
import com.arch.jonnyhsia.compass.facade.IRouteInterceptor
import com.arch.jonnyhsia.compass.facade.PathReplacement
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
    fun initialize(context: Context, vararg tables: ICompassTable) {
        if (initialized.compareAndSet(false, true)) {
            // TODO: jonny 23/8/21 后续改为自动注入路由表
            for (table in tables) {
                CompassRepo.installPages(table)
            }
            CompassLogistics.init(context, executor)
        }
    }

    @JvmStatic
    fun navigate(path: String): RouteIntent {
        val finalPath = CompassRepo.pathReplacement?.replaceString(path) ?: path
        val group = extractGroup(finalPath)
        val uri = finalPath.toUri()
        return ProcessableIntent(uri.path!!, group, uri)
    }

    @JvmStatic
    fun navigate(uri: Uri): RouteIntent {
        val finalUri = CompassRepo.pathReplacement?.replaceUri(uri) ?: uri
        val path = finalUri.path!!
        val group = extractGroup(path)
        return ProcessableIntent(path, group, finalUri)
    }

    private fun extractGroup(path: String): String {
        val startIndex = 1
        val endIndex = path.indexOf('/', startIndex)
        if (endIndex == -1) {
            return ""
        }
        return path.substring(startIndex, endIndex)
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
    fun setPathReplacement(replacement: PathReplacement) {
        CompassRepo.pathReplacement = replacement
    }

    @JvmStatic
    fun addRouteInterceptor(interceptor: IRouteInterceptor) {
        CompassRepo.routeInterceptors.add(interceptor)
    }

    internal fun internalNavigate(context: Any, routeIntent: ProcessableIntent): Any? {
        CompassLogistics.complete(context.asContext(), routeIntent)
        if (!routeIntent.greenChannel) {
            CompassInterceptHandler.performIntercept(routeIntent, object : InterceptCallback {
                override fun onContinue(intent: RouteIntent) {
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