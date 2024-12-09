package com.example.pintapiconv3.app.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.app.LoginActivity
import com.example.pintapiconv3.repository.UserRepository


class MainActivityAdmin : AppCompatActivity() {

    private lateinit var adminName: TextView
    private lateinit var btn_logout: TextView
    private lateinit var btn_cuentasABM: TextView
    private lateinit var btn_prediosABM: TextView
    private lateinit var btnReportes: TextView

    private var userName: String = ""

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

    }

    private fun initViews() {
        btn_cuentasABM = findViewById(R.id.btn_cuentasABM)
        btn_prediosABM = findViewById(R.id.btn_prediosABM)
        btn_logout = findViewById(R.id.btn_logout)
        adminName = findViewById(R.id.tv_nombre)
        btnReportes = findViewById(R.id.btn_reportes)

        adminName.text = loadUserName()

        btnReportes.setOnClickListener {
            val intent = Intent(this, ReportsActivity::class.java)
            startActivity(intent)
        }

        btn_cuentasABM.setOnClickListener {
            val intent = Intent(this, AbmUserActivity::class.java)
            startActivity(intent)
        }

        btn_prediosABM.setOnClickListener {
            val intent = Intent(this, AbmPrediosActivity::class.java)
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

    private fun loadUserName(): String {
        val loginData = getLoginData()
        if(loginData != null) {
            userName = userRepository.getUserName(loginData.first, loginData.second)
        }
        return userName
    }

    private fun getLoginData(): Pair<String, String>? {
        val sharedPref = getSharedPreferences("userData", Context.MODE_PRIVATE)
        val email = sharedPref.getString("userEmail", null)
        val password = sharedPref.getString("userPassword", null)

        return if (email != null && password != null) {
            email to password
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            null
        }
    }

}