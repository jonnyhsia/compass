@file:JvmName(name = "CompassExt")

package com.arch.jonnyhsia.compass

import android.app.Activity
import android.net.Uri
import androidx.fragment.app.Fragment

inline fun Activity.navigate(url: String, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(url)
        .apply(block)
        .go(this)
}

inline fun Fragment.navigate(url: String, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(url)
        .apply(block)
        .go(this)
}

inline fun Activity.navigate(uri: Uri?, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(uri ?: return)
        .apply(block)
        .go(this)
}

inline fun Fragment.navigate(uri: Uri, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(uri)
        .apply(block)
        .go(this)
}