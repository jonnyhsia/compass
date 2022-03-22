package com.arch.jonnyhsia.compass.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arch.jonnyhsia.compass.facade.annotation.Route
import com.arch.jonnyhsia.compass.sample.interceptor.XLoginInterceptor

@Route(name = "MembersOnly", interceptors = [XLoginInterceptor::class])
class MembersOnlyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_only_for_members)
    }
}
