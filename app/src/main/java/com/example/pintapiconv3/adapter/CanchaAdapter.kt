package com.example.pintapiconv3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Cancha
import java.text.DecimalFormat

class CanchaAdapter(
    private val canchaList: MutableList<Cancha>
) : RecyclerView.Adapter<CanchaAdapter.CanchaViewHolder>() {

    class CanchaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipoCancha: TextView = itemView.findViewById(R.id.tv_tipo_cancha)
        val precioHora: TextView = itemView.findViewById(R.id.tv_precio_hora)
        val delete: ImageView = itemView.findViewById(R.id.btn_delete_field)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CanchaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cancha, parent, false)
        return CanchaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CanchaViewHolder, position: Int) {
        val cancha = canchaList[position]

        val df = DecimalFormat("#.00")
        val precioHoraFormateado = df.format(cancha.precioHora)

        holder.tipoCancha.text = cancha.tipoCancha
        holder.precioHora.text = "$$precioHoraFormateado/hora"
        holder.delete.setOnClickListener {
            deleteCancha(position)
        }
    }

    override fun getItemCount(): Int = canchaList.size

    fun addCancha(cancha: Cancha) {
        canchaList.add(cancha)
        notifyItemInserted(canchaList.size - 1)
    }

    private fun deleteCancha(position: Int) {
        if(position in canchaList.indices) {
            canchaList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, canchaList.size)
        }
    }
}