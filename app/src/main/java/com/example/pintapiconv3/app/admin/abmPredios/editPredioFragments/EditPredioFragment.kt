package com.example.pintapiconv3.app.admin.abmPredios.editPredioFragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.app.admin.abmPredios.MarkOnMapDialog
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.utils.Const.FieldStatus.CLOSED
import com.example.pintapiconv3.utils.Const.FieldStatus.ELIMINATED
import com.example.pintapiconv3.utils.Const.FieldStatus.OPEN
import com.example.pintapiconv3.utils.Const.FieldStatus.OUT_OF_SERVICE
import com.example.pintapiconv3.viewmodel.PredioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPredioFragment : Fragment() {

    private lateinit var predioId: EditText
    private lateinit var nombrePredio: EditText
    private lateinit var telefonoPredio: EditText
    private lateinit var callePredio: EditText
    private lateinit var numeroPredio: EditText
    private lateinit var barrioPredio: Spinner
    private lateinit var estadoPredio: Spinner
    private lateinit var ubicacionPredio: TextView
    private lateinit var btnMarkOnMap: Button

    private lateinit var viewModel: PredioViewModel
    private lateinit var mapLauncher: ActivityResultLauncher<Intent>

    private val sqlServerHelper = SQLServerHelper()

    companion object {
        fun newInstance() : EditPredioFragment {
            return EditPredioFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_predio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        viewModel = ViewModelProvider(requireActivity())[PredioViewModel::class.java]

        viewModel.predio.observe(viewLifecycleOwner, Observer { predio ->
            predio?.let {
                setDataToViews(it)
            }
        })

        viewModel.direccion.observe(viewLifecycleOwner, Observer { direccion ->
            direccion?.let {
                setDireccionToViews(it)
            }
        })

        setupListeners()

        mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val locationUrl = result.data?.getStringExtra("LOCATION_URL")
                if(!locationUrl.isNullOrEmpty()) {
                    ubicacionPredio.text = locationUrl
                    guardarUbicacion(locationUrl)
                }
            }
        }

        btnMarkOnMap.setOnClickListener {
            val intent = Intent(context, MarkOnMapDialog::class.java)
            mapLauncher.launch(intent)
        }

    }

    private fun initViews() {
        predioId = requireView().findViewById(R.id.et_predio_id)
        nombrePredio = requireView().findViewById(R.id.et_nombre_predio)
        telefonoPredio = requireView().findViewById(R.id.et_telefono_predio)
        callePredio = requireView().findViewById(R.id.et_calle_predio)
        numeroPredio = requireView().findViewById(R.id.et_numero_predio)
        barrioPredio = requireView().findViewById(R.id.sp_barrio_predio)
        estadoPredio = requireView().findViewById(R.id.sp_estado_predio)
        ubicacionPredio = requireView().findViewById(R.id.tv_ubicacion_predio)
        btnMarkOnMap = requireView().findViewById(R.id.btn_mark_on_map)
    }

    private fun setDataToViews(predio: Predio) {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isAdded) return@launch

            predioId.setText(predio.id.toString())


            if (nombrePredio.text.toString() != predio.nombre) {
                nombrePredio.setText(predio.nombre)
            }
            if (telefonoPredio.text.toString() != predio.telefono) {
                telefonoPredio.setText(predio.telefono)
            }
            if (ubicacionPredio.text.toString() != predio.url_google_maps) {
                ubicacionPredio.text = predio.url_google_maps
            }

            // Cargar estados en el Spinner de estado del predio
            val estados = withContext(Dispatchers.IO) { sqlServerHelper.getEstadosPredio() }

            if (!isAdded) return@launch
            val safeContext = requireActivity()

            val estadoAdapter = ArrayAdapter(safeContext, android.R.layout.simple_spinner_item, estados.map { it.second })
            estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            estadoPredio.adapter = estadoAdapter

            estadoPredio.onItemSelectedListener = null
            estadoPredio.setSelection(
                when(predio.idEstado) {
                    OPEN -> 0
                    CLOSED -> 1
                    OUT_OF_SERVICE -> 2
                    ELIMINATED -> 3
                    else -> 0
                }
            )
            estadoPredio.post { setupListeners() }
        }
    }

    private fun setDireccionToViews(direccion: Direccion) {
        CoroutineScope(Dispatchers.Main).launch {
            if(!isAdded) return@launch

            if (callePredio.text.toString() != direccion.calle) {
                callePredio.setText(direccion.calle)
            }
            if (numeroPredio.text.toString() != direccion.numero.toString()) {
                numeroPredio.setText(direccion.numero.toString())
            }

            val barrios = withContext(Dispatchers.IO) { sqlServerHelper.getBarrios() }

            if (!isAdded) return@launch
            val safeContext = requireActivity()

            val barrioAdapter = ArrayAdapter(safeContext, android.R.layout.simple_spinner_item, barrios.map { it.second })
            barrioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            barrioPredio.adapter = barrioAdapter

            // Evita disparar el listener del Spinner
            barrioPredio.onItemSelectedListener = null
            barrioPredio.setSelection(direccion.idBarrio - 1)
            barrioPredio.post { setupListeners() } // Reactivar el listener
        }
    }

    private fun setupListeners() {

        nombrePredio.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d("EditPredioFragment", "Guardando nombre del predio (al perder el foco)")
                guardarDatosPredio()
            }
        }

        telefonoPredio.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d("EditPredioFragment", "Guardando telefono del predio (al perder el foco)")
                guardarDatosPredio()
            }
        }

        callePredio.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d("EditPredioFragment", "Guardando calle del predio (al perder el foco)")
                guardarDatosDireccion()
            }
        }

        numeroPredio.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                Log.d("EditPredioFragment", "Guardando numero del predio (al perder el foco)")
                guardarDatosDireccion()
            }
        }

        estadoPredio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                guardarDatosPredio()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        barrioPredio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                guardarDatosDireccion()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun guardarDatosPredio() {
        viewModel.predio.value?.let { predio ->
            predio.nombre = nombrePredio.text.toString()
            predio.telefono = telefonoPredio.text.toString()
            predio.idEstado = when (estadoPredio.selectedItemPosition) {
                0 -> OPEN
                1 -> CLOSED
                2 -> OUT_OF_SERVICE
                else -> ELIMINATED
            }

            viewModel.updatePredio(predio)
        }
    }

    private fun guardarDatosDireccion() {
        viewModel.direccion.value?.let { direccion ->
            direccion.calle = callePredio.text.toString()
            direccion.numero = numeroPredio.text.toString().toIntOrNull() ?: 0
            direccion.idBarrio = barrioPredio.selectedItemPosition + 1

            viewModel.updateDireccion(direccion)
        }
    }

    private fun guardarUbicacion(locationUrl: String) {
        viewModel.predio.value?.let { predio ->
            predio.url_google_maps = locationUrl
            viewModel.updatePredio(predio)
            Log.d("EditPredioFragment", "Guardando ubicacion del predio")
        }
    }

}