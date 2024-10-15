package com.example.pintapiconv3.main.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import com.example.pintapiconv3.utils.Utils.showToast

class NewPredioActivity : AppCompatActivity() {

    private val predioRepository = PredioRepository()

    private lateinit var layoutAltaPredio: LinearLayout
    private lateinit var layoutAgregarCancha: LinearLayout
    private lateinit var btnSiguiente: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnGuardar: Button
    private lateinit var btnAtras: Button

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

        btnSiguiente.setOnClickListener {
            nextLayout(1)
        }

        btnAtras.setOnClickListener {
            nextLayout(0)
        }

        btnGuardar.setOnClickListener {
            val error = validateFields()
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
        btnSiguiente = findViewById(R.id.btn_next)
        btnCancelar = findViewById(R.id.btn_cancel)
        btnGuardar = findViewById(R.id.btn_save)
        btnAtras = findViewById(R.id.btn_back)

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

    private fun nextLayout(next: Int) {

        if(next != 0) {
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
        } else {
            layoutAgregarCancha.animate()
                .translationX(layoutAgregarCancha.width.toFloat())
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    layoutAgregarCancha.visibility = View.GONE
                    layoutAltaPredio.translationX = 0f
                    layoutAltaPredio.alpha = 0f
                    layoutAltaPredio.visibility = View.VISIBLE
                    layoutAltaPredio.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
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

        val direccion = Direccion(
            calle = fieldStreet.text.toString(),
            numero = fieldNumber.text.toString().toInt(),
            idBarrio = fieldHood.selectedItemPosition + 1
        )

        val predio = Predio(
            id = fieldId.text.toString().toInt(),
            nombre = fieldName.text.toString(),
            telefono = fieldPhoneNumber.text.toString(),
            idDireccion = -1,
            idEstado = fieldState.selectedItemPosition + 1,
            disponibilidad = true,
            url_google_maps = googleMapsUrl.text.toString(),
            latitud = fieldLatitude.text.toString().toDouble(),
            longitud = fieldLongitude.text.toString().toDouble()
        )

        val canchas = setCanchas(predio.id)

        val horarios = setHorarios(predio.id)

        val success = predioRepository.savePredio(predio, direccion, canchas, horarios)

        if(success) {
            showToast("Predio guardado correctamente")
            finish()
        } else
            showToast("Error al guardar el predio")
    }

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
}