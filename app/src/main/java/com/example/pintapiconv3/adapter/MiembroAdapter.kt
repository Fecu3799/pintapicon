package com.example.pintapiconv3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Miembro
import com.example.pintapiconv3.utils.DiffCallback

class MiembroAdapter(
    private val miembrosInvitados: Set<Int>,
    private val onInvitarClick: (miembro: Miembro) -> Unit // Lambda para manejar el click de invitar
) : RecyclerView.Adapter<MiembroAdapter.MiembroViewHolder>() {

    val miembros = mutableListOf<Miembro>()

    fun setMiembros(newMiembros: List<Miembro>) {
        val diffCallback = DiffCallback(
            miembros,
            newMiembros,
            areItemsSame = { oldItem, newItem -> oldItem.id == newItem.id },
            areContentsSame = { oldItem, newItem -> oldItem == newItem }
        )
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        miembros.clear()
        miembros.addAll(newMiembros)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiembroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_miembro_equipo, parent, false)
        return MiembroViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiembroViewHolder, position: Int) {
        val miembro = miembros[position]
        holder.bind(miembro)
    }

    override fun getItemCount(): Int = miembros.size

    inner class MiembroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tv_nombre_miembro)
        private val tvHabilidad: TextView = itemView.findViewById(R.id.tv_habilidad_miembro)
        private val tvPosicion: TextView = itemView.findViewById(R.id.tv_posicion_miembro)
        private val btnInvitar: TextView = itemView.findViewById(R.id.btn_invitar_miembro)

        fun bind(miembro: Miembro) {
            tvNombre.text = miembro.nombre
            tvHabilidad.text = miembro.habilidad
            tvPosicion.text = miembro.posicion

            val yaInvitado = miembrosInvitados.contains(miembro.id)
            actualizarBotonInvitacion(yaInvitado)

            if (!yaInvitado) {
                btnInvitar.setOnClickListener {
                    onInvitarClick(miembro)
                    (miembrosInvitados as MutableSet).add(miembro.id)
                    actualizarBotonInvitacion(true)
                }
            } else {
                btnInvitar.setOnClickListener(null)
            }
        }

        private fun actualizarBotonInvitacion(yaInvitado: Boolean) {
            if (yaInvitado) {
                btnInvitar.text = "Invitado"
                btnInvitar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_invited, 0, 0, 0)
                btnInvitar.isEnabled = false
            } else {
                btnInvitar.text = "Invitar"
                btnInvitar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_send_invitation, 0, 0, 0)
                btnInvitar.isEnabled = true
            }
        }
    }
}

