package com.arch.jonnyhsia.compass.sample

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.navigate

@Route(scheme = "*", name = "Login")
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LoginTest", "onCreate")
        setContentView(R.layout.activity_login)

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            App.INSTANCE.isLogin = true

            val pending = intent.getParcelableExtra<Uri>("pending")
            navigate(pending)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
