package com.example.pintapiconv3.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Invitacion
import com.example.pintapiconv3.utils.DiffCallback

class InvitacionAdapter(
    private val onInvitationAction: (invitacion: Invitacion, aceptar: Boolean) -> Unit
): RecyclerView.Adapter<InvitacionAdapter.InvitacionViewHolder>() {

    private val invitaciones = mutableListOf<Invitacion>()

    fun setInvitaciones(newInvitaciones: List<Invitacion>) {
        val diffCallback = DiffCallback(
            invitaciones,
            newInvitaciones,
            areItemsSame = { oldItem, newItem -> oldItem.id == newItem.id },
            areContentsSame = { oldItem, newItem -> oldItem == newItem }
        )
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        invitaciones.clear()
        invitaciones.addAll(newInvitaciones)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invitacion, parent, false)
        return InvitacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvitacionViewHolder, position: Int) {
        val invitacion = invitaciones[position]
        holder.bind(invitacion)
    }

    override fun getItemCount(): Int = invitaciones.size

    inner class InvitacionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tvInvitacionTitulo = itemView.findViewById<TextView>(R.id.tv_invitacion_titulo)
        private val tvIinvitacionOrganizador = itemView.findViewById<TextView>(R.id.tv_invitacion_organizador)
        private val btnAceptar = itemView.findViewById<ImageButton>(R.id.btn_aceptar)
        private val btnRechazar = itemView.findViewById<ImageButton>(R.id.btn_rechazar)

        @SuppressLint("SetTextI18n")
        fun bind(invitacion: Invitacion) {
            if(invitacion.equipo != null) {
                tvInvitacionTitulo.text = "Invitacion al equipo: ${invitacion.equipo}"
                tvIinvitacionOrganizador.text = "Capitán: ${invitacion.capitan}"
            } else if(invitacion.idPartido != null) {
                tvInvitacionTitulo.text = "Invitacion de partido"
                tvIinvitacionOrganizador.text = "Organizador: ${invitacion.organizador}"
            } else {
                tvInvitacionTitulo.text = "Invitacion desconocida"
                tvIinvitacionOrganizador.text = "Organizador desconocido"
            }

            btnAceptar.setOnClickListener {
                onInvitationAction(invitacion, true)
            }

            btnRechazar.setOnClickListener {
                onInvitationAction(invitacion, false)
            }
        }
    }
}

