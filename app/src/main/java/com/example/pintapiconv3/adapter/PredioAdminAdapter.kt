package com.example.pintapiconv3.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
    context: Context,
    private val predios: List<Predio>,
    private val onEditClick: (Predio) -> Unit,
    private val onDetailsClick: (Predio) -> Unit
) : ArrayAdapter<Predio>(context, 0, predios) {

    private val predioRepository = PredioRepository()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.abm_predio_list_item, parent, false)

        val predio = predios[position]

        val tvNombrePredio: TextView = view.findViewById(R.id.tv_nombre_predio)
        val tvEstadoPredio: TextView = view.findViewById(R.id.tv_estado_predio)
        val tvCantidadCanchas: TextView = view.findViewById(R.id.tv_cantidad_canchas)
        val tvTiposCanchas: TextView = view.findViewById(R.id.tv_tipos_canchas)
        val btnEditarPredio: ImageView = view.findViewById(R.id.btn_editar_predio)
        val btnVerDetallesPredio: ImageView = view.findViewById(R.id.btn_ver_detalles_predio)

        tvNombrePredio.text = predio.nombre
        tvEstadoPredio.text = if(predio.idEstado == PredioRepository.OPEN) "Estado: Abierto"
                                else if (predio.idEstado == PredioRepository.CLOSED) "Estado: Cerrado"
                                    else "Estado: Fuera de Servicio"

        CoroutineScope(Dispatchers.Main).launch {
            val canchas = withContext(Dispatchers.IO) {
                predioRepository.getCanchasByPredio(predio.id)
            }
            tvCantidadCanchas.text = "Cantidad de canchas: ${canchas.size}"

            val tiposCanchas = withContext(Dispatchers.IO) {
                canchas.map { it.tipoCancha }.distinct()
            }
            tvTiposCanchas.text = "Tipos de canchas: ${tiposCanchas.joinToString(", ")}"
        }

        btnEditarPredio.setOnClickListener {
            onEditClick(predio)
        }

        btnVerDetallesPredio.setOnClickListener {
            onDetailsClick(predio)
        }

        return view
    }
}