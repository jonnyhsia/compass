package com.arch.jonnyhsia.compass.facade

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.Compass

interface RouteIntent {
    fun addParameter(key: String, value: String?): RouteIntent
    fun addParameter(key: String, value: Int): RouteIntent
    fun addParameter(key: String, parcelable: Parcelable): RouteIntent
    fun addParameters(bundle: Bundle): RouteIntent
    fun removeAllParameters(): RouteIntent
    fun go(context: Context): Any?
    fun go(fragment: Fragment): Any?
}

class ProcessableIntent internal constructor(
    uri: Uri
) : RouteIntent {

    private lateinit var context: Any

    internal constructor(url: String) : this(Uri.parse(url))

    var uri: Uri = uri
        private set

    internal var innerBundle: Bundle? = null
        private set

    internal var options: Bundle? = null
        private set

    val path: String
        get() = uri.host!!

    val requester: String
        get() = context.toString()

    fun redirect(url: String): RouteIntent {
        this.uri = Uri.parse(url)
        return this
    }

    /**
     * 重定向
     */
    fun redirect(uri: Uri): RouteIntent {
        this.uri = uri
        return this
    }

    override fun addParameters(bundle: Bundle): RouteIntent {
        if (innerBundle == null) {
            innerBundle = Bundle(bundle)
        } else {
            innerBundle!!.putAll(bundle)
        }

        return this
    }

    override fun addParameter(key: String, value: String?): RouteIntent {
        bundle().putString(key, value)
        return this
    }

    override fun addParameter(key: String, value: Int): RouteIntent {
        bundle().putInt(key, value)
        return this
    }

    override fun removeAllParameters(): RouteIntent {
        if (innerBundle != null) {
            innerBundle!!.clear()
        }
        return this
    }

    override fun addParameter(key: String, parcelable: Parcelable): RouteIntent {
        bundle().putParcelable(key, parcelable)
        return this
    }

    override fun go(context: Context): Any? {
        return internalGo(context)
    }

    override fun go(fragment: Fragment): Any? {
        return internalGo(fragment)
    }

    private fun internalGo(any: Any): Any? {
        this.context = any
        return Compass.internalNavigate(context, this)
    }

    private fun bundle(): Bundle {
        if (innerBundle == null) {
            innerBundle = Bundle()
        }
        return innerBundle!!
    }
}