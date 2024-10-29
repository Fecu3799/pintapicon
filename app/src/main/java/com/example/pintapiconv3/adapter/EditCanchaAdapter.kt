package com.example.pintapiconv3.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Cancha
import java.text.DecimalFormat

class EditCanchaAdapter(
    private var canchaList: MutableList<Cancha>,
    private val onDeleteCancha: (Cancha) -> Unit,
    private val onUpdateCanchaPrice: (Cancha, Double) -> Unit
) : RecyclerView.Adapter<EditCanchaAdapter.CanchaViewHolder>() {

    inner class CanchaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipoCancha: TextView = itemView.findViewById(R.id.tv_tipo_cancha)
        val precioHora: EditText = itemView.findViewById(R.id.tv_precio_hora)
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
        holder.precioHora.setText(precioHoraFormateado)


        holder.precioHora.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val nuevoPrecioStr = holder.precioHora.text.toString()
                val nuevoPrecio = nuevoPrecioStr.toDoubleOrNull()
                if(nuevoPrecio != null && nuevoPrecio != cancha.precioHora && nuevoPrecio <= 999999.99 && nuevoPrecio > 0) {
                    onUpdateCanchaPrice(cancha, nuevoPrecio)
                    holder.precioHora.clearFocus()
                    val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    Toast.makeText(holder.itemView.context, "Precio actualizado", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        holder.delete.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Eliminar cancha")
                .setMessage("¿Estas seguro que deseas eliminar esta cancha?")
                .setPositiveButton("Eliminar") { _, _ ->
                    if(position < canchaList.size) {
                        onDeleteCancha(cancha)
                    } else {
                        Toast.makeText(holder.itemView.context, "Error al eliminar cancha", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = canchaList.size

    fun updateCanchas(updatedCanchas: MutableList<Cancha>) {
        canchaList = updatedCanchas
        notifyDataSetChanged()
    }
}