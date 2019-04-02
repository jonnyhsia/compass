package com.arch.jonnyhsia.compass

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable

class RouteIntent(uri: Uri) {

    var uri: Uri = uri
        private set

    constructor(url: String) : this(Uri.parse(url))

    internal var innerBundle: Bundle? = null
        private set
    internal var options: Bundle? = null
        private set

    val pageKey: String
        get() = "${uri.scheme}://${uri.host}"

    lateinit var requester: String

    private fun bundle(): Bundle {
        if (innerBundle == null) {
            innerBundle = Bundle()
        }
        return innerBundle!!
    }

    fun addParameter(bundle: Bundle): RouteIntent {
        if (innerBundle == null) {
            innerBundle = bundle
        } else {
            innerBundle!!.putAll(bundle)
        }

        return this
    }

    fun addParameter(key: String, value: String?): RouteIntent {
        bundle().putString(key, value)
        return this
    }

    fun addParameter(key: String, value: Int): RouteIntent {
        bundle().putInt(key, value)
        return this
    }

    fun removeAllParameters(): RouteIntent {
        if (innerBundle != null) {
            innerBundle!!.clear()
        }
        return this
    }

    fun redirect(url: String): RouteIntent {
        this.uri = Uri.parse(url)
        return this
    }

    fun addParameter(key: String, parcelable: Parcelable): RouteIntent {
        bundle().putParcelable(key, parcelable)
        return this
    }
}