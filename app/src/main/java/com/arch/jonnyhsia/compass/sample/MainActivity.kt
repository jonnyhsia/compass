package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.api.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route(name = "sample://Main")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGoDetail.setOnClickListener {
            Compass.navigate(this,
                    RouteIntent("sample://Detail").addParameter("id", 100))
        }

        btnGoMembersOnly.setOnClickListener {
            Compass.navigate(this,
                    RouteIntent("sample://MembersOnly"))
        }
    }
}