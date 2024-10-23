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
import com.example.pintapiconv3.adapter.PredioAdminAdapter
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbmPrediosActivity : AppCompatActivity() {

    private lateinit var btn_atras: View
    private lateinit var btnAgregarPredio: Button
    private lateinit var listViewPredios: ListView
    private val predioRepository = PredioRepository()
    private lateinit var predioAdminAdapter: PredioAdminAdapter
    private var prediosList = mutableListOf<Predio>()

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

        cargarPredios()

        predioAdminAdapter = PredioAdminAdapter(
            this, prediosList, onEditClick = { predio ->
                editarPredio(predio)
            }, onDetailsClick = { predio ->
                verDetallesPredio(predio)
            }
        )

        listViewPredios.adapter = predioAdminAdapter
    }

    private fun cargarPredios() {
        CoroutineScope(Dispatchers.Main).launch {
            val predios = withContext(Dispatchers.IO) {
                predioRepository.getAllPredios()
            }
            prediosList.clear()
            prediosList.addAll(predios)
            predioAdminAdapter.notifyDataSetChanged()
        }
    }

    private fun editarPredio(predio: Predio) {
    }

    private fun verDetallesPredio(predio: Predio) {
        //TODO: Ver detalles del predio
    }

    private fun actualizarLista() {
        cargarPredios()
    }
}