package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.facade.annotation.Route
import kotlinx.android.synthetic.main.activity_detail.*

@Route(name = "Detail")
class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        textView.text = getString(R.string.detail, intent.getIntExtra("id", 0))
    }
}
