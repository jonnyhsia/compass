package com.arch.jonnyhsia.compass.sample

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.Compass
import com.arch.jonnyhsia.compass.RouteIntent
import com.arch.jonnyhsia.compass.api.Route
import kotlinx.android.synthetic.main.activity_login.*

const val REQUEST_LOGIN = 10

@Route(name = "core://Login", requestCode = REQUEST_LOGIN)
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            App.INSTANCE.isLogin = true

            val pending = intent.getParcelableExtra<Uri>("pending")
            Compass.navigate(this, RouteIntent(pending))
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
