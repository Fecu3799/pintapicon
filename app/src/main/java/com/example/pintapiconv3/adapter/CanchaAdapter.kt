package com.example.pintapiconv3.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.repository.PredioRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class CanchaAdapter(private val canchaList: MutableList<Cancha>) : RecyclerView.Adapter<CanchaAdapter.CanchaViewHolder>() {

    private val newCanchas = mutableListOf<Cancha>()
    private val updatedCanchas = mutableListOf<Cancha>()
    private val deletedCanchas = mutableListOf<Cancha>()

    class CanchaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                    cancha.precioHora = nuevoPrecio
                    updateCancha(cancha)
                    Toast.makeText(holder.itemView.context, "Precio actualizado", Toast.LENGTH_SHORT).show()
                }
                holder.precioHora.clearFocus()
                val imm = v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
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
                        deletedCanchas.add(cancha)
                        canchaList.removeAt(position)
                        notifyItemRemoved(position)
                    } else {
                        Toast.makeText(holder.itemView.context, "Error al eliminar cancha", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount(): Int = canchaList.size

    private fun updateCancha(cancha: Cancha) {
        if(!updatedCanchas.contains(cancha) && !newCanchas.contains(cancha)) {
            updatedCanchas.add(cancha)
        }
    }

    fun addCancha(cancha: Cancha) {
        newCanchas.add(cancha)
        canchaList.add(cancha)
        notifyItemInserted(canchaList.size - 1)
    }

    fun getNewCanchas(): List<Cancha> = newCanchas
    fun getUpdatedCanchas(): List<Cancha> = updatedCanchas
    fun getDeletedCanchas(): List<Cancha> = deletedCanchas
}