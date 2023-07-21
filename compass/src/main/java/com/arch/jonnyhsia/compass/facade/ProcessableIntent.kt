package com.arch.jonnyhsia.compass.facade

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.facade.enums.TargetType

interface RouteIntent {
    val extras: Int
    val context: Context
    val path: String
    val group: String

    fun addParameter(key: String, value: String?): RouteIntent
    fun addParameter(key: String, value: Int): RouteIntent
    fun addParameter(key: String, parcelable: Parcelable): RouteIntent
    fun addParameters(bundle: Bundle): RouteIntent
    fun options(bundle: Bundle): RouteIntent
    fun arguments(): Bundle
    fun removeAllParameters(): RouteIntent
    fun go(context: Context): Any?
    fun go(fragment: Fragment): Any?
}

class ProcessableIntent internal constructor(
    uri: Uri
) : RouteIntent {
    internal constructor(url: String) : this(Uri.parse(url))

    private lateinit var caller: Any

    override val context: Context
        get() = when (caller) {
            is Context -> caller as Context
            is Fragment -> (caller as Fragment).requireContext()
            else -> throw RuntimeException()
        }

    override var extras: Int = 0
        internal set

    private var _uri: Uri? = uri

    val uri: Uri
        get() = _uri!!

    val isPathCleared: Boolean
        get() = _uri == null

    override val group: String
        get() = uri.host!!

    override val path: String
        get() = uri.path!!

    internal var arguments: Bundle? = null

    internal var options: Bundle? = null
        private set

    internal var requestCode = 0
    internal var target: Class<*>? = null
    internal var type: Int = TargetType.UNKNOWN
    internal var echo: IRouteEcho? = null
    internal var fragment: Fragment? = null
    internal var tag: Any? = null
    internal var greenChannel = false
        private set

    fun redirect(uri: Uri): RouteIntent {
        return ProcessableIntent(uri)
    }

    fun redirect(url: String): RouteIntent {
        return ProcessableIntent(url)
    }

    fun clearPath() {
        this._uri = null
    }

    fun clearAll() {
        clearPath()
        arguments?.clear()
        arguments = null
        options = null
        target = null
        echo = null
        fragment = null
        tag = null
        type = TargetType.UNKNOWN
    }

    override fun addParameters(bundle: Bundle): RouteIntent {
        if (arguments == null) {
            arguments = Bundle(bundle)
        } else {
            arguments!!.putAll(bundle)
        }
        return this
    }

    override fun addParameter(key: String, value: String?): RouteIntent {
        arguments().putString(key, value)
        return this
    }

    override fun addParameter(key: String, value: Int): RouteIntent {
        arguments().putInt(key, value)
        return this
    }

    override fun addParameter(key: String, parcelable: Parcelable): RouteIntent {
        arguments().putParcelable(key, parcelable)
        return this
    }

    override fun removeAllParameters(): RouteIntent {
        if (arguments != null) {
            arguments?.clear()
        }
        return this
    }

    override fun arguments(): Bundle {
        if (arguments == null) {
            arguments = Bundle()
        }
        return arguments!!
    }

    override fun options(bundle: Bundle): RouteIntent {
        this.options = bundle
        return this
    }

    internal fun greenChannel() {
        greenChannel = true
    }

    override fun go(context: Context): Any? {
        return internalGo(context)
    }

    override fun go(fragment: Fragment): Any? {
        return internalGo(fragment)
    }

    private fun internalGo(any: Any): Any? {
        this.caller = any
        return Compass.internalNavigate(context, this)
    }
}