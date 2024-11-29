package com.example.pintapiconv3.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Miembro
import com.example.pintapiconv3.models.Participante

class ParticipanteAdapter(
    private val onDeleteClick: (participante: Participante) -> Unit
) : RecyclerView.Adapter<ParticipanteAdapter.ParticipanteViewHolder>() {

    private val participantes = mutableListOf<Participante>()
    private var organizadorId: Int? = null
    private var montoPorPersona: Double = 0.0
    private var isCurrentUserOrganizer: Boolean = false

    @SuppressLint("NotifyDataSetChanged")
    fun setParticipantes(participantesList: List<Participante>, organizadorId: Int?, isCurrentUserOrganizer: Boolean) {
        this.organizadorId = organizadorId
        this.isCurrentUserOrganizer = isCurrentUserOrganizer
        participantes.clear()
        participantes.addAll(participantesList)
        notifyDataSetChanged()
    }

    fun setMontoPorPersona(monto: Double) {
        this.montoPorPersona = monto
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ParticipanteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participante, parent, false)
        return ParticipanteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipanteViewHolder, position: Int) {
        val participante = participantes[position]
        holder.bind(participante)
    }

    override fun getItemCount() : Int = participantes.size

    inner class ParticipanteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tv_nombre)
        private val tvPosicion = itemView.findViewById<TextView>(R.id.tv_posicion)
        private val tvMontoPagado = itemView.findViewById<TextView>(R.id.tv_monto_pagado)
        private val btnDelete = itemView.findViewById<ImageButton>(R.id.btn_eliminar_participante)


        @SuppressLint("SetTextI18n")
        fun bind(participante: Participante) {
            tvNombre.text = participante.nombre
            tvPosicion.text = participante.posicion

            if (isCurrentUserOrganizer && participante.idParticipante != organizadorId) {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener {
                    onDeleteClick(participante)
                }
            } else {
                btnDelete.visibility = View.GONE
            }

            val montoPagado = participante.montoPagado ?: 0.0

            tvMontoPagado.text = "Pagado: $${String.format("%.1f", montoPagado)}"
        }
    }
}