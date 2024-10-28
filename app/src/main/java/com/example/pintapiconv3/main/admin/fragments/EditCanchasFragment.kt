package com.example.pintapiconv3.main.admin.fragments

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.CanchaAdapter
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditCanchasFragment : Fragment() {

    private lateinit var btnAgregarCancha: TextView
    private lateinit var rvCanchas: RecyclerView
    private lateinit var canchaAdapter: CanchaAdapter

    private var canchasList = mutableListOf<Cancha>()

    private val predioRepository = PredioRepository()
    private val sqlServerHelper = SQLServerHelper()
    private var predioId: Int = 0

    companion object {
        private const val ARG_ID_PREDIO = "ARG_ID_PREDIO"

        fun newInstance(idPredio: Int) : EditCanchasFragment {
            val fragment = EditCanchasFragment()
            val args = Bundle()
            args.putSerializable(ARG_ID_PREDIO, idPredio)
            fragment.arguments = args
            return fragment
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

        predioId = arguments?.getInt(ARG_ID_PREDIO) ?: 0

        setupRecyclerView()

        cargarCanchas()

        btnAgregarCancha.setOnClickListener {
            showDialogAddCancha()
        }
    }

    private fun setupRecyclerView() {
        canchaAdapter = CanchaAdapter(canchasList)
        rvCanchas.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = canchaAdapter
        }
    }

    private fun cargarCanchas() {
        CoroutineScope(Dispatchers.Main).launch {
            val canchas = withContext(Dispatchers.IO) {
                predioRepository.getCanchasByPredio(predioId)
            }
            canchasList.clear()
            canchasList.addAll(canchas)
            canchaAdapter.notifyDataSetChanged()
        }
    }

    private fun showDialogAddCancha() {
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
                    val nuevaCancha = Cancha (
                        idPredio = predioId,
                        idTipoCancha = idTipoCancha,
                        tipoCancha = tipoCancha,
                        precioHora = precioHora,
                        disponibilidad = true
                    )
                    canchaAdapter.addCancha(nuevaCancha)
                } else {
                    Toast.makeText(context, "Ingrese un precio valido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun getNewCanchas(): List<Cancha> {
        return canchaAdapter.getNewCanchas()
    }

    fun getUpdatedCanchas(): List<Cancha> {
        return canchaAdapter.getUpdatedCanchas()
    }

    fun getDeletedCanchas(): List<Cancha> {
        return canchaAdapter.getDeletedCanchas()
    }

}