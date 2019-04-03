package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.api.Route
import com.arch.jonnyhsia.compass.navigate
import kotlinx.android.synthetic.main.activity_main.*

@Route(name = "Main")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGoDetail.setOnClickListener {
            // Compass.navigate(this, "sample://Detail")
            //     .addParameter("id", 100)
            //     .go()

            navigate("sample://Detail") {
                addParameter("id", 100)
            }
        }

        btnGoMembersOnly.setOnClickListener {
            Compass.navigate(this, "sample://MembersOnly").go()
        }
    }
}