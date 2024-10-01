package com.example.pintapiconv3.main

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.utils.UserAdapter
import kotlin.math.log

class MainActivityAdmin : AppCompatActivity() {

    private lateinit var btn_logout: TextView
    private lateinit var btn_cuentasABM: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_cuentasABM = findViewById(R.id.btn_cuentasABM)
        btn_logout = findViewById(R.id.btn_logout)

        btn_cuentasABM.setOnClickListener {
            val intent = Intent(this, AbmUserActivity::class.java)
            startActivity(intent)
        }

        btn_logout.setOnClickListener {
            logoutUser()
        }

    }

    private fun logoutUser() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}