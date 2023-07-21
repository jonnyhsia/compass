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
        if (intent.isIntentCanceled) {
            return
        }

        // 寻找 url 对应的页面
        val path = intent.path
        val meta = CompassRepo.routePages[path]
        if (meta == null) {
            // 若页面未找到, 页面降级
            if (CompassRepo.pageHandler != null) {
                // 显然 path 已经没有用处, 清除掉可以当做 pageHandler 是否处理的依据
                CompassRepo.pageHandler!!.onPageUnregister(intent)
                if (intent.isIntentCanceled) {
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

            val uri = intent.rawUri
            for (key in uri.queryParameterNames) {
                val value = uri.getQueryParameter(key)
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
    }
}