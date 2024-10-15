package com.example.pintapiconv3.main.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R

class AbmPrediosActivity : AppCompatActivity() {

    private lateinit var btn_atras: View
    private lateinit var btnAgregarPredio: Button
    private lateinit var listViewPredios: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_abm_predios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main_admin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_atras = findViewById(R.id.btnAtras)
        btnAgregarPredio = findViewById(R.id.btnAgregarPredio)
        listViewPredios = findViewById(R.id.listViewPredios)

        btn_atras.setOnClickListener {
            finish()
        }

        btnAgregarPredio.setOnClickListener {
            intent = Intent(this, NewPredioActivity::class.java)
            startActivity(intent)
        }
    }
}