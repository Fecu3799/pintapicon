package com.example.pintapiconv3.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Participante

class ParticipanteAdapter : RecyclerView.Adapter<ParticipanteAdapter.ParticipanteViewHolder>() {

    private val participantes = mutableListOf<Participante>()
    private var organizadorId: Int? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setParticipantes(participantesList: List<Participante>, organizadorId: Int? = null) {
        this.organizadorId = organizadorId
        participantes.clear()
        participantes.addAll(participantesList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ParticipanteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participante, parent, false)
        return ParticipanteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipanteViewHolder, position: Int) {
        val participante = participantes[position]
        holder.bind(participante, organizadorId)
    }

    override fun getItemCount() : Int = participantes.size

    inner class ParticipanteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tv_nombre)
        private val tvOrganizador = itemView.findViewById<TextView>(R.id.tv_organizador)
        private val tvPosicion = itemView.findViewById<TextView>(R.id.tv_posicion)
        private val tvMontoPagado = itemView.findViewById<TextView>(R.id.tv_monto_pagado)
        private val tvMontoRestante = itemView.findViewById<TextView>(R.id.tv_monto_restante)

        @SuppressLint("SetTextI18n")
        fun bind(participante: Participante, organizadorId: Int?) {
            tvNombre.text = participante.nombre
            tvPosicion.text = participante.posicion

            if(participante.id == organizadorId) {
                tvOrganizador.visibility = View.VISIBLE
                tvOrganizador.text = "Organizador"
            } else {
                tvOrganizador.visibility = View.GONE
            }

            participante.montoPagado?.let {
                tvMontoPagado.text = "Pagado: $$it"
            } ?: run {
                tvMontoPagado.visibility = View.GONE
            }
            participante.montoRestante?.let {
                tvMontoRestante.text = "Restante: $$it"
            } ?: run {
                tvMontoRestante.visibility = View.GONE
            }
        }
    }
}