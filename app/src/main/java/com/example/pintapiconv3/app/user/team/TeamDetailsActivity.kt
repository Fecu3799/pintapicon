package com.example.pintapiconv3.app.user.team

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EquipoAdapter
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamDetailsActivity : AppCompatActivity() {

    private lateinit var tvNombreEquipo: TextView
    private lateinit var tvDescripcionEquipo: TextView
    private lateinit var rvEquipo: RecyclerView
    private lateinit var equipoAdapter: EquipoAdapter

    private var equipoId: Int = -1
    private val equipoRepository = EquipoRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvNombreEquipo = findViewById(R.id.tv_nombre_equipo)
        tvDescripcionEquipo = findViewById(R.id.tv_descripcion_equipo)
        rvEquipo = findViewById(R.id.rv_equipo)

        //userViewModel = SharedUserData.userViewModel!!

        equipoAdapter = EquipoAdapter()
        rvEquipo.layoutManager = LinearLayoutManager(this)
        rvEquipo.adapter = equipoAdapter

        equipoId = intent.getIntExtra("equipoId", -1)
        if(equipoId != -1) {
            cargarDetallesEquipo(equipoId)
            Log.d("TeamDetailsActivity/onCreate", "Equipo cargado correctamente")
        } else {
            Log.e("TeamDetailsActivity/onCreate", "No se pudo cargar el equipo")
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cargarDetallesEquipo(teamId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val equipo = withContext(Dispatchers.IO) {
                    equipoRepository.getTeamDetailsById(equipoId)
                }
                if(equipo != null) {
                    tvNombreEquipo.text = "Bienvenido a ${equipo.nombre}"
                    tvDescripcionEquipo.text = equipo.descripcion
                    equipoAdapter.setMiembros(equipo.miembros)
                    Log.d("TeamDetailsActivity/cargarDetallesEquipo", "Equipo cargado correctamente")
                } else {
                    showToast("No se pudo cargar el equipo")
                    Log.e("TeamDetailsActivity/cargarDetallesEquipo", "No se pudo cargar el equipo")
                }
            } catch (e: Exception) {
                showToast("Error al cargar el equipo.")
                Log.e("TeamDetailsActivity/cargarDetallesEquipo", "Error al cargar el equipo: ${e.message}")
            }
        }
    }
}