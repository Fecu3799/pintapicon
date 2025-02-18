package com.example.pintapiconv3.app.admin.abmPredios

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Horario
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.utils.Utils.showToast
import java.text.SimpleDateFormat
import java.util.Locale

class ViewPredioActivity : AppCompatActivity() {

    private lateinit var tvNombrePredio: TextView
    private lateinit var tvTelefonoPredio: TextView
    private lateinit var tvDireccionPredio: TextView
    private lateinit var tvVerMapa: TextView
    private lateinit var lvCanchas: ListView
    private lateinit var lvHorarios: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_view_predio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvNombrePredio = findViewById(R.id.tv_nombre_predio)
        tvTelefonoPredio = findViewById(R.id.tv_numero_telefono_predio)
        tvDireccionPredio = findViewById(R.id.tv_direccion_predio)
        tvVerMapa = findViewById(R.id.tv_ver_mapa)
        lvCanchas = findViewById(R.id.lv_canchas)
        lvHorarios = findViewById(R.id.lv_horarios)

        val predio = intent.getSerializableExtra("EXTRA_PREDIO") as? Predio
        val direccion = intent.getSerializableExtra("EXTRA_DIRECCION") as? Direccion
        val canchas = intent.getSerializableExtra("EXTRA_CANCHAS") as? List<Cancha>
        val horarios = intent.getSerializableExtra("EXTRA_HORARIOS") as? List<Horario>

        predio?.let {
            tvNombrePredio.text = it.nombre
            tvTelefonoPredio.text = it.telefono
        }

        direccion?.let {
            tvDireccionPredio.text = "${it.calle} ${it.numero}"
        }

        tvVerMapa.setOnClickListener {
            predio?.let {
                if(!it.url_google_maps.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.url_google_maps))
                    startActivity(intent)
                } else {
                    showToast("No se encontro la ubicacion del predio")
                }
            }
        }

        canchas?.let {
            val canchaAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, it.map { cancha ->
                "${cancha.nroCancha}: ${cancha.tipoCancha} - $${cancha.precioHora}"
            })
            lvCanchas.adapter = canchaAdapter
        }

        horarios?.let {
            val horarioAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, it.map { horario ->
                if(horario.horaApertura == horario.horaCierre) {
                    "${horario.dia}: CERRADO"
                } else {
                    "${horario.dia}: ${formatHora(horario.horaApertura)}-${formatHora(horario.horaCierre)}"
                }
            })
            lvHorarios.adapter = horarioAdapter
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    private fun formatHora(hora: String): String {
        return try {
            val formatoEntrada = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val formatoSalida = SimpleDateFormat("HH:mm", Locale.getDefault())

            val fecha = formatoEntrada.parse(hora)
            formatoSalida.format(fecha ?: "**:**")
        } catch (e: Exception) {
            "**:**"
        }
    }
}