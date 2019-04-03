package com.arch.jonnyhsia.compass

import android.app.Activity
import android.net.Uri
import androidx.fragment.app.Fragment

inline fun Activity.navigate(url: String, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(this, url)
        .apply(block)
        .go()
}

inline fun Fragment.navigate(url: String, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(this, url)
        .apply(block)
        .go()
}

inline fun Activity.navigate(uri: Uri, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(this, uri)
        .apply(block)
        .go()
}

inline fun Fragment.navigate(uri: Uri, block: RouteIntent.() -> Unit = {}) {
    Compass.navigate(this, uri)
        .apply(block)
        .go()
}