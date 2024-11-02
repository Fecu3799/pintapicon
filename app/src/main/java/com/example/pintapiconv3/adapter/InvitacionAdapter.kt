package com.example.pintapiconv3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Invitacion

class InvitacionAdapter(
    private val onInvitationAction: (invitacion: Invitacion, aceptar: Boolean) -> Unit
): RecyclerView.Adapter<InvitacionAdapter.InvitacionViewHolder>() {

    private val invitaciones = mutableListOf<Invitacion>()

    fun setInvitaciones(newInvitaciones: List<Invitacion>) {
        val diffCallback = InvitacionDiffCallback(invitaciones, newInvitaciones)
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
        private val invitacionEquipo = itemView.findViewById<TextView>(R.id.tv_invitacion_equipo)
        private val invitacionCapitan = itemView.findViewById<TextView>(R.id.tv_invitacion_capitan)
        private val btnAceptar = itemView.findViewById<ImageButton>(R.id.btn_aceptar)
        private val btnRechazar = itemView.findViewById<ImageButton>(R.id.btn_rechazar)

        fun bind(invitacion: Invitacion) {
            invitacionEquipo.text = "Equipo: ${invitacion.equipo}"
            invitacionCapitan.text = "Capitan: ${invitacion.capitan}"

            btnAceptar.setOnClickListener {
                onInvitationAction(invitacion, true)
            }

            btnRechazar.setOnClickListener {
                onInvitationAction(invitacion, false)
            }
        }
    }
}

class InvitacionDiffCallback(
    private val oldList: List<Invitacion>,
    private val newList: List<Invitacion>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}