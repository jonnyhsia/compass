package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.facade.annotation.Route

const val REQUEST_SUBSCRIBE = 11

@Route(
    name = "Subscribe",
    extras = RouteExtras.MEMBER_AND_LOGIN,
)
class SubscribeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
    }
}