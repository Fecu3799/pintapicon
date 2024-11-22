package com.example.pintapiconv3.app.admin.editPredioFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EditCanchaAdapter
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.viewmodel.PredioViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditCanchasFragment : Fragment() {

    private lateinit var btnAgregarCancha: TextView
    private lateinit var rvCanchas: RecyclerView
    private lateinit var editCanchaAdapter: EditCanchaAdapter

    private val sqlServerHelper = SQLServerHelper()

    private lateinit var viewModel: PredioViewModel

    companion object {
        fun newInstance() : EditCanchasFragment {
            return EditCanchasFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_canchas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCanchas = view.findViewById(R.id.rv_canchas)
        btnAgregarCancha = view.findViewById(R.id.btn_agregar_cancha)

        viewModel = ViewModelProvider(requireActivity())[PredioViewModel::class.java]

        editCanchaAdapter = EditCanchaAdapter(
            mutableListOf(),
            onDeleteCancha = { cancha ->
                viewModel.deleteCancha(cancha)
            }, onUpdateCanchaPrice = { cancha, nuevoPrecio ->
                viewModel.updateCanchaPrice(cancha, nuevoPrecio)
            }
        )
        rvCanchas.adapter = editCanchaAdapter
        rvCanchas.layoutManager = LinearLayoutManager(requireContext())


        viewModel.canchas.observe(viewLifecycleOwner) { updatedCanchas ->
            editCanchaAdapter.updateCanchas(updatedCanchas.toMutableList())
        }

        btnAgregarCancha.setOnClickListener {
            showAddCanchaDialog()
        }
    }

    private fun showAddCanchaDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_new_field, null)
        val spnerTipoCancha: Spinner = dialogView.findViewById(R.id.spner_tipo_cancha)
        val etPrecioHora: EditText = dialogView.findViewById(R.id.et_precio_hora)

        CoroutineScope(Dispatchers.Main).launch {
            val tiposCanchas = withContext(Dispatchers.IO) {
                sqlServerHelper.getTipoCanchas()
            }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                tiposCanchas.map { it.second }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnerTipoCancha.adapter = adapter
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Agregar Cancha")
            .setPositiveButton("Agregar") { _, _ ->
                val tipoCancha = spnerTipoCancha.selectedItem.toString()
                val idTipoCancha = spnerTipoCancha.selectedItemPosition + 1
                val precioHora = etPrecioHora.text.toString().toDoubleOrNull()

                if(precioHora != null && precioHora <= 999999.99 && precioHora > 0) {
                    if(viewModel.predio.value?.id != null && viewModel.predio.value!!.id > 0) {
                        val nuevaCancha = Cancha (
                            id = 0,
                            idPredio = viewModel.predio.value!!.id,
                            idTipoCancha = idTipoCancha,
                            tipoCancha = tipoCancha,
                            nroCancha = "Cancha ${editCanchaAdapter.itemCount + 1}",
                            precioHora = precioHora,
                            disponibilidad = true,
                            isNew = true
                        )
                        viewModel.addCancha(nuevaCancha)
                    } else
                        Log.e("Error", "No se pudo obtener el id del predio")
                } else {
                    Toast.makeText(context, "Ingrese un precio valido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}