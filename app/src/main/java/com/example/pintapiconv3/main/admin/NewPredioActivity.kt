package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.CanchaAdapter
import com.example.pintapiconv3.adapter.Horario
import com.example.pintapiconv3.adapter.HorarioAdapter
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.BarrioRepository
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException

class NewPredioActivity : AppCompatActivity() {

    private val predioRepository = PredioRepository()
    private val sqlServerHelper = SQLServerHelper()
    private val barrioRepository = BarrioRepository()

    private var canchasList = mutableListOf<Cancha>()
    private var currentLayout = 0

    private var horariosList = mutableListOf<Horario>()

    private var predio: Predio? = null
    private var cancha: Cancha? = null
    private var direccion: Direccion? = null
    private var horario: Horario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_predio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main_admin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        btnSiguiente1.setOnClickListener {
            val error = validateFields()
            if(error.isEmpty()) {
                savePredioData()
                tvNombrePredio.text = predio?.nombre
                nextLayout()
            } else
                showToast(error)
        }

        btnAgregarCancha.setOnClickListener {
            showDialogNewField(predio?.id!!)
        }

        btnSiguiente2.setOnClickListener {
            if(canchasList.isEmpty())
                showToast("Debe agregar al menos una cancha")
            else
                nextLayout()
        }

        btnAtras1.setOnClickListener {
            previousLayout()
        }

        btnAtras2.setOnClickListener {
            previousLayout()
        }

        btnGuardar.setOnClickListener {
            collectHorarios()
            val error = validateHorarios()
            if(error.isNotEmpty())
                showToast(error)
            else
                savePredio()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        layoutAltaPredio = findViewById(R.id.layout_alta_predio)
        layoutAgregarCancha = findViewById(R.id.layout_agregar_cancha)
        layoutAgregarHorarios = findViewById(R.id.layout_agregar_horarios)
        btnSiguiente1 = findViewById(R.id.btn_next1)
        btnSiguiente2 = findViewById(R.id.btn_next2)
        btnCancelar = findViewById(R.id.btn_cancel)
        btnGuardar = findViewById(R.id.btn_save)
        btnAtras1 = findViewById(R.id.btn_back1)
        btnAtras2 = findViewById(R.id.btn_back2)
        btnAgregarCancha = findViewById(R.id.btn_agregar_cancha)

        fieldId = findViewById(R.id.et_fieldId)
        fieldName = findViewById(R.id.et_fieldName)
        fieldPhoneNumber = findViewById(R.id.et_fieldPhoneNumber)
        fieldStreet = findViewById(R.id.et_fieldStreet)
        fieldNumber = findViewById(R.id.et_fieldNumber)
        fieldHood = findViewById(R.id.spner_fieldHood)
        fieldState = findViewById(R.id.spner_fieldState)
        markOnMap = findViewById(R.id.btn_markOnMap)
        googleMapsUrl = findViewById(R.id.et_googleMapsUrl)
        fieldLatitude = findViewById(R.id.et_fieldLatitude)
        fieldLongitude = findViewById(R.id.et_fieldLongitude)

        tvNombrePredio = findViewById(R.id.tv_nombre_predio)
        rvCanchas = findViewById(R.id.rv_canchas)
        canchaAdapter = CanchaAdapter(canchasList)
        rvCanchas.layoutManager = LinearLayoutManager(this)
        rvCanchas.adapter = canchaAdapter

        rvHorarios = findViewById(R.id.rv_horarios)
        horarioAdapter = HorarioAdapter()
        rvHorarios.layoutManager = LinearLayoutManager(this)
        rvHorarios.adapter = horarioAdapter

        fieldId.setText(predioRepository.getNextPredioId().toString())
        fieldId.isEnabled = false

        loadSpinners()
    }

    private fun loadSpinners() {
        val barrios = barrioRepository.getBarrios()
        val estados = sqlServerHelper.getEstadosPredio()

        setSpinners(fieldHood, barrios)
        setSpinners(fieldState, estados)
    }

    private fun setSpinners(spinner: Spinner, items: List<Pair<Int, String>>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(0)
    }

    private fun nextLayout() {

        when(currentLayout) {
            0 -> {
                layoutAltaPredio.animate()
                    .translationX(-layoutAltaPredio.width.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        layoutAltaPredio.visibility = View.GONE
                        layoutAgregarCancha.translationX = layoutAgregarCancha.width.toFloat()
                        layoutAgregarCancha.alpha = 0f
                        layoutAgregarCancha.visibility = View.VISIBLE
                        layoutAgregarCancha.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
                currentLayout = 1
            }

            1 -> {
                layoutAgregarCancha.animate()
                    .translationX(-layoutAgregarCancha.width.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        layoutAgregarCancha.visibility = View.GONE
                        layoutAgregarHorarios.translationX = layoutAgregarHorarios.width.toFloat()
                        layoutAgregarHorarios.alpha = 0f
                        layoutAgregarHorarios.visibility = View.VISIBLE
                        layoutAgregarHorarios.animate()
                            .translationX(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
                currentLayout = 2
            }
        }
    }

    private fun previousLayout() {
        when(currentLayout) {
            1 -> {
                layoutAgregarCancha.animate()
                    .translationX(layoutAgregarCancha.width.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        layoutAgregarCancha.visibility = View.GONE
                        layoutAltaPredio.translationX = 0f //layoutAltaPredio.width.toFloat()
                        layoutAltaPredio.alpha = 0f
                        layoutAltaPredio.visibility = View.VISIBLE
                        layoutAltaPredio.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
                currentLayout = 0
            }
            2 -> {
                layoutAgregarHorarios.animate()
                    .translationX(layoutAgregarHorarios.width.toFloat())
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        layoutAgregarHorarios.visibility = View.GONE
                        layoutAgregarCancha.translationX = 0f //layoutAltaPredio.width.toFloat()
                        layoutAgregarCancha.alpha = 0f
                        layoutAgregarCancha.visibility = View.VISIBLE
                        layoutAgregarCancha.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
                currentLayout = 1
            }
        }

    }

    private fun validateFields(): String {
        var error = ""
        if(fieldName.text.isEmpty())
            error = "Por favor ingrese el nombre del predio"
        else if(fieldPhoneNumber.text.isEmpty())
            error = "Por favor ingrese el telefono del predio"
        else if(fieldStreet.text.isEmpty())
            error = "Por favor ingrese la calle del predio"
        else if(fieldNumber.text.isEmpty())
            error = "Por favor ingrese el numero del predio"
        else if(googleMapsUrl.text.isEmpty() && (fieldLatitude.text.trim().isEmpty() || fieldLongitude.text.trim().isEmpty()))
            error = "Por favor ingrese la ubicacion del predio"

        return error
    }

    private fun validateHorarios(): String {
        var error = ""

        horariosList.forEach { horario ->
            if(horario.horaApertura == "Apertura" || horario.horaCierre == "Cierre" ||
                horario.horaApertura.isEmpty() || horario.horaCierre.isEmpty()) {
                error = "Debe agregar horarios para todos los dias de la semana"
                return error
            }
        }

        return error
    }

    private fun savePredioData() {
        predio = Predio(
            id = fieldId.text.toString().toInt(),
            nombre = fieldName.text.toString(),
            telefono = fieldPhoneNumber.text.toString(),
            idDireccion = 0,
            idEstado = if(fieldState.selectedItemPosition == 0) PredioRepository.OPEN else PredioRepository.CLOSED,
            disponibilidad = true,
            url_google_maps = if (googleMapsUrl.text.isEmpty()) null else googleMapsUrl.text.toString(),
            latitud = if (fieldLatitude.text.isEmpty()) null else fieldLatitude.text.toString().toDouble(),
            longitud = if (fieldLongitude.text.isEmpty()) null else fieldLongitude.text.toString().toDouble(),
        )

        direccion = Direccion(
            calle = fieldStreet.text.toString(),
            numero = fieldNumber.text.toString().toInt(),
            idBarrio = fieldHood.selectedItemPosition + 1
        )
    }

    private fun savePredio() {

        CoroutineScope(Dispatchers.IO).launch {

            var conn: Connection? = null

            try {

                conn = DBConnection.getConnection()
                conn?.autoCommit = false


                val idDireccion = sqlServerHelper.insertDireccionWithConnection(conn!!, direccion!!)
                    ?: throw SQLException("Error al insertar direccion")
                predio?.idDireccion = idDireccion

                val predioId = predioRepository.insertPredioWithConnection(conn, predio!!)
                if(predioId == 0) throw SQLException("Error al insertar predio")

                canchasList.forEach { cancha ->
                    val canchaInserted = predioRepository.insertCanchaWithConnection(conn, cancha)
                    if(!canchaInserted) throw SQLException("Error al insertar cancha")
                }

                horariosList.forEach { horario ->
                    val horarioInserted = predioRepository.insertHorarioPredioWithConnection(conn, predio!!.id, horario)
                    if(!horarioInserted) throw SQLException("Error al insertar horario")
                }

                conn.commit()
                withContext(Dispatchers.Main) {
                    showToast("Predio guardado correctamente")
                    finish()
                }
            } catch (e: SQLException) {
                conn?.rollback()
                withContext(Dispatchers.Main) {
                    showToast("Error al guardar el predio. ${e.message}")
                }
            } finally {
                conn?.autoCommit = true
                conn?.close()
            }
        }
    }

    private fun showDialogNewField(idPredio: Int) {

        CoroutineScope(Dispatchers.Main).launch {
            val tiposCanchas = withContext(Dispatchers.IO) {
                sqlServerHelper.getTipoCanchas()
            }

            if(tiposCanchas.isNotEmpty()) {

                val dialogView = LayoutInflater.from(this@NewPredioActivity).inflate(R.layout.dialog_new_field, null)

                spnerTipoCancha = dialogView.findViewById(R.id.spner_tipo_cancha)
                etPrecioHora = dialogView.findViewById(R.id.et_precio_hora)
                btnGuardarCancha = dialogView.findViewById(R.id.btn_guardar_cancha)

                val adapter = ArrayAdapter(this@NewPredioActivity, android.R.layout.simple_spinner_item, tiposCanchas.map {it.second})
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnerTipoCancha.adapter = adapter

                val dialogBuilder = AlertDialog.Builder(this@NewPredioActivity)
                    .setView(dialogView)
                    .setTitle("Agragar cancha")

                val alertDialog = dialogBuilder.create()

                btnGuardarCancha.setOnClickListener {
                    val idTipoCancha = spnerTipoCancha.selectedItemPosition + 1
                    val tipoCancha = spnerTipoCancha.selectedItem.toString()
                    val precioHora = etPrecioHora.text.toString().toDoubleOrNull()

                    if (precioHora != null && precioHora <= 99999.99) {
                        cancha = Cancha(
                            idPredio = idPredio,
                            idTipoCancha = idTipoCancha,
                            tipoCancha = tipoCancha,
                            precioHora = precioHora
                        )

                        canchaAdapter.addCancha(cancha!!)
                        showToast("Cancha agregada correctamente")
                        alertDialog.dismiss()
                    } else
                        showToast("Ingrese un precio válido. No puede exceder $99.999")
                }
                alertDialog.show()
            } else {
                showToast("No hay canchas disponibles para seleccionar")
            }
        }
    }

    private fun collectHorarios() {
        horariosList.clear()

        for(i in 0 until rvHorarios.childCount) {
            val view = rvHorarios.getChildAt(i)

            val tvHoraApertura = view.findViewById<TextView>(R.id.tv_hora_apertura)
            val tvHoraCierre = view.findViewById<TextView>(R.id.tv_hora_cierre)
            val spnerEstadoPredio = view.findViewById<Spinner>(R.id.spner_estado_predio)

            val horaApertura = tvHoraApertura.text.toString()
            val horaCierre = tvHoraCierre.text.toString()

            horario = Horario (
                dia = horarioAdapter.diasDeLaSemana[i],
                horaApertura = horaApertura,
                horaCierre = horaCierre
            )

            horariosList.add(horario!!)
        }
    }

    private lateinit var layoutAltaPredio: LinearLayout
    private lateinit var layoutAgregarCancha: LinearLayout
    private lateinit var layoutAgregarHorarios: LinearLayout
    private lateinit var btnSiguiente1: Button
    private lateinit var btnSiguiente2: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnAtras1: Button
    private lateinit var btnAtras2: Button
    private lateinit var btnAgregarCancha: TextView

    private lateinit var fieldId: EditText
    private lateinit var fieldName: EditText
    private lateinit var fieldPhoneNumber: EditText
    private lateinit var fieldStreet: EditText
    private lateinit var fieldNumber: EditText
    private lateinit var fieldHood: Spinner
    private lateinit var fieldState: Spinner
    private lateinit var markOnMap: Button
    private lateinit var googleMapsUrl: EditText
    private lateinit var fieldLatitude: EditText
    private lateinit var fieldLongitude: EditText

    private lateinit var tvNombrePredio: TextView
    private lateinit var rvCanchas: RecyclerView
    private lateinit var canchaAdapter: CanchaAdapter
    private lateinit var spnerTipoCancha: Spinner
    private lateinit var etPrecioHora: EditText
    private lateinit var btnGuardarCancha: Button

    private lateinit var horarioAdapter: HorarioAdapter
    private lateinit var rvHorarios: RecyclerView
}