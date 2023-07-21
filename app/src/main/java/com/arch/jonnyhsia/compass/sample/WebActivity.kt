package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.facade.annotation.Route

@Route(name = "/Web")
class WebActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = intent.getStringExtra("url")
        })
    }
}