package com.example.pintapiconv3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Miembro

class EquipoAdapter(
    private val onDeleteMember: (miembroId: Int) -> Unit
) : RecyclerView.Adapter<EquipoAdapter.EquipoViewHolder>(){

    private val miembros = mutableListOf<Miembro>()

    fun setMiembros(nuevosMiembros: List<Miembro>) {
        miembros.clear()
        miembros.addAll(nuevosMiembros)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_miembro, parent, false)
        return EquipoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val miembro = miembros[position]
        holder.bind(miembro)
    }

    override fun getItemCount(): Int = miembros.size

    inner class EquipoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tv_nombre_miembro)
        private val tvHabilidad = itemView.findViewById<TextView>(R.id.tv_habilidad_miembro)
        private val tvPosicion = itemView.findViewById<TextView>(R.id.tv_posicion_miembro)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btn_eliminar_miembro)

        fun bind(miembro: Miembro) {
            tvNombre.text = miembro.nombre
            tvHabilidad.text = "Habilidad: ${miembro.habilidad}"
            tvPosicion.text = "Posición: ${miembro.posicion}"

            if(miembro.isCaptain) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.lessgold))
                btnEliminar.visibility = View.GONE
            }

            btnEliminar.setOnClickListener {
                onDeleteMember(miembro.id)
            }
        }
    }

}