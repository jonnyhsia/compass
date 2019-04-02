package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.api.Route

@Route(name = "sample://Detail")
class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
    }
}
