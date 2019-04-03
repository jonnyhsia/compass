package com.arch.jonnyhsia.compass

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import com.arch.jonnyhsia.compass.api.PageKey

interface RouteIntent {
    fun addParameter(key: String, value: String?): RouteIntent
    fun addParameter(key: String, value: Int): RouteIntent
    fun addParameter(key: String, parcelable: Parcelable): RouteIntent
    fun addParameters(bundle: Bundle): RouteIntent
    fun removeAllParameters(): RouteIntent
    fun go()
}

class ProcessableIntent internal constructor(
    private val context: Any,
    uri: Uri
) : RouteIntent {

    internal constructor(context: Any, url: String) : this(context, Uri.parse(url))

    var uri: Uri = uri
        private set

    internal var innerBundle: Bundle? = null
        private set

    internal var options: Bundle? = null
        private set

    val pageKey: PageKey
        get() = PageKey(uri.scheme, uri.host)

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

    override fun go() {
        Compass.internalNavigate(context, this)
    }

    private fun bundle(): Bundle {
        if (innerBundle == null) {
            innerBundle = Bundle()
        }
        return innerBundle!!
    }
}