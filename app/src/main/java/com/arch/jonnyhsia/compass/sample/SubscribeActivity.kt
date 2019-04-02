package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.api.Route
import com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor

const val REQUEST_SUBSCRIBE = 11

@Route(name = "sample://Subscribe", interceptors = [XLoginInterceptor::class], requestCode = REQUEST_SUBSCRIBE)
class SubscribeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
    }
}