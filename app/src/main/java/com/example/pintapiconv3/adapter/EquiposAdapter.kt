package com.example.pintapiconv3.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Equipo

class EquiposAdapter(
    private val onEquipoClicked: (equipoId: Int) -> Unit
) : RecyclerView.Adapter<EquiposAdapter.EquiposViewHolder>() {

    private val equipos = mutableListOf<Equipo>()

    @SuppressLint("NotifyDataSetChanged")
    fun setEquipos(nuevosEquipos: List<Equipo>) {
        equipos.clear()
        equipos.addAll(nuevosEquipos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquiposViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_equipo, parent, false)
        return EquiposViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquiposViewHolder, position: Int) {
        val equipo = equipos[position]
        holder.bind(equipo)
    }

    override fun getItemCount(): Int = equipos.size

    inner class EquiposViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreEquipo = itemView.findViewById<TextView>(R.id.tv_nombre_equipo)
        private val tvNombreCapitan = itemView.findViewById<TextView>(R.id.tv_nombre_capitan)

        @SuppressLint("SetTextI18n")
        fun bind(equipo: Equipo) {
            tvNombreEquipo.text = equipo.nombre
            tvNombreCapitan.text = "Capitan: ${equipo.capitan}"

            itemView.setOnClickListener {
                onEquipoClicked(equipo.id)
            }
        }

    }
}