package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
            val fragment = Compass.navigate(url = "frag").go(this)
                    as Fragment
            Log.d("MainActivity", "onCreate: $fragment")
//            Compass.navigate("sample://MembersOnly").go(this)
        }
    }
}