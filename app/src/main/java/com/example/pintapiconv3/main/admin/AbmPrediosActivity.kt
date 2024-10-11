package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R

class AbmPrediosActivity : AppCompatActivity(), NewFieldDialog.FieldCreationListener {

    private lateinit var btn_atras: TextView
    private lateinit var btnAgregarPredio: TextView
    private lateinit var listViewPredios: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_abm_predios)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_atras = findViewById(R.id.btn_atras)
        btnAgregarPredio = findViewById(R.id.btnAgregarPredio)
        listViewPredios = findViewById(R.id.listViewPredios)

        btn_atras.setOnClickListener {
            finish()
        }

        btnAgregarPredio.setOnClickListener {
            val dialog = newFieldDialog()
            dialog.show(supportFragmentManager, "newFieldDialog")
        }
    }
}