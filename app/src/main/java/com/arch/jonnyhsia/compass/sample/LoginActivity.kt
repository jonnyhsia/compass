package com.arch.jonnyhsia.compass.sample

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.api.Route
import com.arch.jonnyhsia.compass.navigate
import kotlinx.android.synthetic.main.activity_login.*

const val REQUEST_LOGIN = 10

@Route(scheme = "*", name = "Login", requestCode = REQUEST_LOGIN)
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin.setOnClickListener {
            App.INSTANCE.isLogin = true

            val pending = intent.getParcelableExtra<Uri>("pending")
            navigate(pending)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
