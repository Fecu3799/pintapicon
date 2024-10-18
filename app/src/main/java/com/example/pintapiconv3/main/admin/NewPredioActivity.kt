package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewPredioActivity : AppCompatActivity() {

    private val predioRepository = PredioRepository()
    private val sqlServerHelper = SQLServerHelper()

    private var canchasList = mutableListOf<Cancha>()
    private var currentLayout = 0

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
            nextLayout()
        }

        btnSiguiente2.setOnClickListener {
            nextLayout()
        }

        btnAtras1.setOnClickListener {
            previousLayout()
        }

        btnAtras2.setOnClickListener {
            previousLayout()
        }

        btnGuardar.setOnClickListener {

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

        fieldId.setText(predioRepository.getNextPredioId().toString())
        fieldId.isEnabled = false
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
        else if(fieldHood.selectedItemPosition == -1)
            error = "Por favor seleccione un barrio"
        else if(fieldState.selectedItemPosition == -1)
            error = "Por favor seleccione un estado"
        else if(googleMapsUrl.text.isEmpty() || (fieldLatitude.text.isEmpty() && fieldLongitude.text.isEmpty()))
            error = "Por favor ingrese la ubicacion del predio mediante el mapa o manualmente con sus coordenadas"

        return error
    }

    private fun savePredio() {

        lifecycleScope.launch {

            val direccion = Direccion(
                calle = fieldStreet.text.toString(),
                numero = fieldNumber.text.toString().toInt(),
                idBarrio = fieldHood.selectedItemPosition + 1
            )
            val idDireccion = withContext(Dispatchers.IO) {
                sqlServerHelper.insertDireccion(direccion.calle, direccion.numero, direccion.idBarrio)
            }

            if (idDireccion != null) {

                val predio = Predio(
                    id = fieldId.text.toString().toInt(),
                    nombre = fieldName.text.toString(),
                    telefono = fieldPhoneNumber.text.toString(),
                    idDireccion = idDireccion!!,
                    idEstado = fieldState.selectedItemPosition + 1,
                    disponibilidad = true,
                    url_google_maps = if (googleMapsUrl.text.isEmpty()) null else googleMapsUrl.text.toString(),
                    latitud = if (fieldLatitude.text.isEmpty()) null else fieldLatitude.text.toString().toDouble(),
                    longitud = if (fieldLongitude.text.isEmpty()) null else fieldLongitude.text.toString().toDouble(),
                )

                val idPredio = withContext(Dispatchers.IO) {
                    predioRepository.insertPredio(predio)
                }

                if (idPredio > 0) {

                    val canchasGuardadas = withContext(Dispatchers.IO) {
                        canchasList.all { cancha ->
                            predioRepository.insertCancha(cancha)
                        }
                    }
                }
            }
        }







        val predioSuccess = predioRepository.insertPredio(predio)

        if(predioSuccess) {
            showToast("Predio guardado correctamente")
            finish()
        } else
            showToast("Error al guardar el predio")
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
                    val tipoCanchaPosition = spnerTipoCancha.selectedItemPosition
                    val precioHoraText = etPrecioHora.text.toString()

                    if (tipoCanchaPosition == -1)
                        showToast("Por favor ingrese un tipo de cancha")
                    else if (precioHoraText.isEmpty())
                        showToast("Por favor ingrese un precio por hora")
                    else {
                        val idTipoCancha = tiposCanchas[tipoCanchaPosition].first
                        val precioHora = precioHoraText.toDouble()

                        val cancha = Cancha (
                            idPredio = idPredio,
                            idTipoCancha = idTipoCancha,
                            precioHora = precioHora
                        )

                        canchasList.add(cancha)
                        showToast("Cancha agregada correctamente")
                        alertDialog.dismiss()
                    }
                }

                alertDialog.show()
            } else {
                showToast("No hay canchas disponibles para seleccionar")
            }
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

    private lateinit var spnerTipoCancha: Spinner
    private lateinit var etPrecioHora: EditText
    private lateinit var btnGuardarCancha: Button
}