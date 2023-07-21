package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.facade.annotation.RouteInterceptor
import com.arch.jonnyhsia.compass.navigate

@Route(name = "Main")
@RouteInterceptor(name = "LoginInterceptor")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnGoDetail).setOnClickListener {
            navigate("sample://Detail") {
                addParameter("id", 100)
            }
        }
        findViewById<View>(R.id.btnGoMembersOnly).setOnClickListener {
            Compass.navigate("://MembersOnly").go(this)
        }
        findViewById<View>(R.id.btnWeb).setOnClickListener {
            navigate("https://jonnyhsia.com")
        }
        findViewById<View>(R.id.btnNativeWeb).setOnClickListener {
            navigate("https://jonnyhsia.com?native_page=%3A%2F%2FDetail")
        }
        findViewById<View>(R.id.btnNotFound).setOnClickListener {
            navigate("://ABCDEFG")
        }
        findViewById<View>(R.id.btnExpired).setOnClickListener {
            navigate("://expired")
        }
    }
}