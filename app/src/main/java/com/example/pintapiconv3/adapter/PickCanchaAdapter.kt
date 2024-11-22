package com.example.pintapiconv3.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Cancha

class PickCanchaAdapter(
    private val onCanchaClicked: (Cancha) -> Unit
) : RecyclerView.Adapter<PickCanchaAdapter.CanchaViewHolder>() {

    private val canchas = mutableListOf<Cancha>()
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    @SuppressLint("NotifyDataSetChanged")
    fun setCanchas(cancha: List<Cancha>) {
        canchas.clear()
        canchas.addAll(cancha)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancha_partido, parent, false)
        return CanchaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        val cancha = canchas[position]
        holder.bind(cancha)
    }

    override fun getItemCount(): Int = canchas.size

    inner class CanchaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombrePredio: TextView = itemView.findViewById(R.id.tv_nombre_predio)
        private val tvNombreCancha: TextView = itemView.findViewById(R.id.tv_nombre_cancha)
        private val tvPrecioHora: TextView = itemView.findViewById(R.id.tv_precio_hora_cancha)
        private val tvCanchaOcupada: TextView = itemView.findViewById(R.id.tv_cancha_ocupada)

        @SuppressLint("SetTextI18n", "DefaultLocale")
        fun bind(cancha: Cancha) {
            tvNombrePredio.text = cancha.nombrePredio
            tvNombreCancha.text = cancha.tipoCancha
            val precioPorParticipante = when(cancha.idTipoCancha) {
                1 -> cancha.precioHora / (10)
                2 -> cancha.precioHora / 14
                3 -> cancha.precioHora / 16
                else -> cancha.precioHora / 22
            }
            tvPrecioHora.text = "Precio/hora: $${cancha.precioHora} ($${String.format("%.1f", precioPorParticipante)} c/u)"


            if(adapterPosition == selectedPosition) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.lessgold))
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            if(cancha.disponibilidad) {
                itemView.isEnabled = true
                itemView.setOnClickListener {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = adapterPosition

                    notifyItemChanged(previousSelectedPosition)
                    notifyItemChanged(selectedPosition)

                    onCanchaClicked(cancha)
                }
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.lightgrey))
                itemView.isEnabled = false
                tvCanchaOcupada.visibility = View.VISIBLE
                itemView.setOnClickListener(null)
            }
        }
    }
}