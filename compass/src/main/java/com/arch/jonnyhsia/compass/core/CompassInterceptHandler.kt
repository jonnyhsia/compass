package com.arch.jonnyhsia.compass.core

import com.arch.jonnyhsia.compass.facade.ProcessableIntent
import com.arch.jonnyhsia.compass.facade.RouteIntent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object CompassInterceptHandler {

    private const val DEFAULT_INTERCEPT_TIMEOUT = 10L

    fun performIntercept(intent: ProcessableIntent, callback: InterceptCallback) {
        CompassLogistics.executor.execute {
            val counter = CountDownLatch(CompassRepo.routeInterceptors.size)
            try {
                doIntercept(0, intent, TraversalCallback(intent, counter))
                counter.await(DEFAULT_INTERCEPT_TIMEOUT, TimeUnit.SECONDS)
                if (counter.count > 0) {
                    callback.onInterrupt(RuntimeException("Interceptors are timed out"))
                } else if (intent.tag is Exception) {
                    callback.onInterrupt(intent.tag as Exception)
                } else {
                    callback.onContinue(intent)
                }
            } catch (e: Exception) {
                callback.onInterrupt(e)
            }
        }
    }

    private fun doIntercept(
        index: Int,
        intent: ProcessableIntent,
        callback: InterceptCallback
    ) {
        if (index >= CompassRepo.routeInterceptors.size) return

        val interceptor = CompassRepo.routeInterceptors[index]
        interceptor.intercept(intent, callback)
    }

    private class TraversalCallback(
        private val intent: ProcessableIntent,
        private val counter: CountDownLatch
    ) : InterceptCallback {
        private var index = 0

        override fun onContinue(intent: RouteIntent) {
            counter.countDown()
            doIntercept(++index, intent as ProcessableIntent, this)
        }

        override fun onInterrupt(exception: Exception?) {
            intent.cancel()
            intent.tag = exception ?: RuntimeException("Interrupted")
            while (counter.count > 0) {
                counter.countDown()
            }
        }
    }
}

interface InterceptCallback {
    fun onContinue(intent: RouteIntent)
    fun onInterrupt(exception: Exception?)
}