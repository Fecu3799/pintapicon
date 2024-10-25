package com.example.pintapiconv3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Predio
import com.example.pintapiconv3.repository.PredioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PredioAdminAdapter (
    private val context: Context,
    private val predios: List<Predio>,
    private val onEditClick: (Predio) -> Unit,
    private val onDetailsClick: (Predio) -> Unit,
) : BaseAdapter() {

    override fun getCount(): Int = predios.size
    override fun getItem(position: Int): Any = predios[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.abm_predio_list_item, parent, false)

        val predio = predios[position]

        val tvNombrePredio: TextView = view.findViewById(R.id.tv_nombre_predio)
        val tvEstadoPredio: TextView = view.findViewById(R.id.tv_estado_predio)
        val tvCantidadCanchas: TextView = view.findViewById(R.id.tv_cantidad_canchas)
        val btnEditarPredio: ImageView = view.findViewById(R.id.btn_editar_predio)
        val btnVerDetallesPredio: ImageView = view.findViewById(R.id.btn_ver_detalles_predio)

        tvNombrePredio.text = predio.nombre
        tvEstadoPredio.text = when(predio.idEstado) {
            PredioRepository.OPEN -> "Estado: Abierto"
            PredioRepository.CLOSED -> "Estado: Cerrado"
            PredioRepository.OUT_OF_SERVICE -> "Estado: Fuera de Servicio"
            else -> "Estado: Eliminado"
        }

        tvCantidadCanchas.text = "Cantidad de canchas: ${predio.canchas.size}"

        btnEditarPredio.setOnClickListener {
            onEditClick(predio)
        }

        btnVerDetallesPredio.setOnClickListener {
            onDetailsClick(predio)
        }

        return view
    }
}