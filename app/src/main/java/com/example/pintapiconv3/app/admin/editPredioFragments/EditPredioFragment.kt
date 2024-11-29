package com.example.pintapiconv3.app.admin.editPredioFragments

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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.app.admin.MarkOnMapDialog
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
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

    private lateinit var mapLauncher: ActivityResultLauncher<Intent>
    private lateinit var viewModel: PredioViewModel

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
                    OPEN -> 0
                    CLOSED -> 1
                    OUT_OF_SERVICE -> 2
                    ELIMINATED -> 3
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
                sqlServerHelper.getBarrios()
            }

            val barrioAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, barrios.map { it.second })
            barrioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            barrioPredio.adapter = barrioAdapter
            barrioPredio.setSelection(direccion.idBarrio - 1)
        }
    }
}