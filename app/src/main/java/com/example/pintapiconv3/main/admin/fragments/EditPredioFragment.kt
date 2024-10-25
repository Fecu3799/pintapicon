package com.example.pintapiconv3.main.admin.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.main.admin.MarkOnMapDialog
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.BarrioRepository
import com.example.pintapiconv3.repository.PredioRepository
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

    private lateinit var mapLauncher: ActivityResultLauncher<Intent>

    private val sqlServerHelper = SQLServerHelper()
    private val barrioRepository = BarrioRepository()

    private lateinit var predio: Predio
    private lateinit var direccion: Direccion

    companion object {
        private const val ARG_PREDIO = "ARG_PREDIO"
        private const val ARG_DIRECCION = "ARG_DIRECCION"

        fun newInstance(predio: Predio, direccion: Direccion) : EditPredioFragment {
            val fragment = EditPredioFragment()
            val args = Bundle()
            args.putSerializable(ARG_PREDIO, predio)
            args.putSerializable(ARG_DIRECCION, direccion)
            fragment.arguments = args
            return fragment
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

        predio = arguments?.getSerializable(ARG_PREDIO) as Predio
        direccion = arguments?.getSerializable(ARG_DIRECCION) as Direccion

        setDataToViews(predio)
        setDireccionToViews(direccion)

        mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                val locationAddress = result.data?.getStringExtra("LOCATION_URL")
                ubicacionPredio.text = locationAddress ?: "Direccion no disponible"
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
            predioId.setText(predio.id.toString())
            nombrePredio.setText(predio.nombre)
            telefonoPredio.setText(predio.telefono)
            ubicacionPredio.text = predio.url_google_maps

            val estados = withContext(Dispatchers.IO) {
                sqlServerHelper.getEstadosPredio()
            }

            val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados.map { it.second })
            estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            estadoPredio.adapter = estadoAdapter
            estadoPredio.setSelection(
                when(predio.idEstado) {
                    PredioRepository.OPEN -> 0
                    PredioRepository.CLOSED -> 1
                    PredioRepository.OUT_OF_SERVICE -> 2
                    PredioRepository.ELIMINATED -> 3
                    else -> 0
                }
            )
        }
    }

    private fun setDireccionToViews(direccion: Direccion) {
        CoroutineScope(Dispatchers.Main).launch {
            callePredio.setText(direccion.calle)
            numeroPredio.setText(direccion.numero.toString())

            val barrios = withContext(Dispatchers.IO) {
                barrioRepository.getBarrios()
            }

            val barrioAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, barrios.map { it.second })
            barrioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            barrioPredio.adapter = barrioAdapter
            barrioPredio.setSelection(direccion.idBarrio - 1)
        }
    }

    fun getUpdatedPredio(): Predio {
        predio.let {
            it.nombre = nombrePredio.text.toString()
            it.telefono = telefonoPredio.text.toString()
            it.idEstado = when(estadoPredio.selectedItemPosition) {
                0 -> PredioRepository.OPEN
                1 -> PredioRepository.CLOSED
                2 -> PredioRepository.OUT_OF_SERVICE
                else -> PredioRepository.ELIMINATED
            }
            it.url_google_maps = ubicacionPredio.text.toString()
        }
        return predio
    }

    fun getUpdatedDireccion(): Direccion {
        direccion.let {
            it.calle = callePredio.text.toString()
            it.numero = numeroPredio.text.toString().toInt()
            it.idBarrio = barrioPredio.selectedItemPosition + 1
        }
        return direccion
    }

}