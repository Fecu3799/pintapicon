package com.example.pintapiconv3.app.admin.abmPredios

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.PredioAdminAdapter
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.DireccionRepository
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbmPrediosActivity : AppCompatActivity() {

    private lateinit var btn_atras: View
    private lateinit var btnAgregarPredio: Button
    private lateinit var listViewPredios: ListView

    private val predioRepository = PredioRepository()
    private val direccionRepository = DireccionRepository()

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
            val intent = Intent(this, NewPredioActivity::class.java)
            addPredioLauncher.launch(intent)
        }

        predioAdminAdapter = PredioAdminAdapter(
            this, prediosList, onEditClick = { predio ->
                editarPredio(predio)
            }, onDetailsClick = { predio ->
                verDetallesPredio(predio)
            }
        )

        listViewPredios.adapter = predioAdminAdapter

        cargarPredios()
    }

    private fun cargarPredios() {
        CoroutineScope(Dispatchers.Main).launch {
            val predios = withContext(Dispatchers.IO) {
                predioRepository.getAllPredios()
            }
            prediosList.clear()
            prediosList.addAll(predios)

            Log.d("AbmPrediosActivity", "Predios actualizados: $prediosList")

            runOnUiThread {
                predioAdminAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun editarPredio(predio: Predio) {
        CoroutineScope(Dispatchers.Main).launch {
            val direccion = withContext(Dispatchers.IO) {
                direccionRepository.getDireccionById(predio.idDireccion)
            }
            if(direccion != null) {
                val intent = Intent(this@AbmPrediosActivity, EditPredioActivity::class.java)
                intent.putExtra("EXTRA_PREDIO", predio)
                intent.putExtra("EXTRA_DIRECCION", direccion)
                editPredioLauncher.launch(intent)
            } else {
                showToast("Error al cargar la direccion del predio")
            }
        }
    }

    private fun verDetallesPredio(predio: Predio) {
        //TODO: Ver detalles del predi

        CoroutineScope(Dispatchers.Main).launch {
            val direccion = withContext(Dispatchers.IO) {
                direccionRepository.getDireccionById(predio.idDireccion)
            }
            val canchas = withContext(Dispatchers.IO) {
                predioRepository.getCanchasByPredio(predio.id)
            }
            val horarios = withContext(Dispatchers.IO) {
                predioRepository.getHorariosByPredio(predio.id)
            }

            if(direccion != null) {
                val intent = Intent(this@AbmPrediosActivity, ViewPredioActivity::class.java).apply {
                    putExtra("EXTRA_PREDIO", predio)
                    putExtra("EXTRA_DIRECCION", direccion)
                    putExtra("EXTRA_CANCHAS", ArrayList(canchas))
                    putExtra("EXTRA_HORARIOS", ArrayList(horarios))
                }
                viewPredioLauncher.launch(intent)
            } else {
                showToast("Error al cargar los datos del predio")
            }
        }
    }

    private val addPredioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK)
                cargarPredios()
        }

    private val editPredioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            Log.d("AbmPrediosActivity", "Se actualizo el predio")
            cargarPredios()
        } else {
            Log.e("AbmPrediosActivity", "No se actualizo el predio")
        }
    }

    private val viewPredioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            cargarPredios()
        }
    }
}