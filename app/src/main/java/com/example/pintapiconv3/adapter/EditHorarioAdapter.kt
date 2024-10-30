package com.example.pintapiconv3.adapter

import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.models.Horario
import java.util.Calendar

class EditHorarioAdapter(
    private val horarios: MutableList<Horario>,
    private val onHorarioChanged: (Horario) -> Unit
) : RecyclerView.Adapter<EditHorarioAdapter.HorarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_horario, parent, false)
        return HorarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val horario = horarios[position]

        holder.tvDia.text = horario.dia

        val estados = listOf("Abierto", "Cerrado")
        val spinnerAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, estados)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spnerEstadoPredio.adapter = spinnerAdapter
        holder.spnerEstadoPredio.setSelection(if (horario.horaApertura == "00:00" && horario.horaCierre == "00:00") 1 else 0)

        holder.spnerEstadoPredio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position == 1) {
                    holder.tvHoraApertura.isEnabled = false
                    holder.tvHoraCierre.isEnabled = false
                    holder.tvHoraApertura.text = "00:00"
                    holder.tvHoraCierre.text = "00:00"
                    horario.horaApertura = "00:00"
                    horario.horaCierre = "00:00"
                } else {
                    holder.tvHoraApertura.isEnabled = true
                    holder.tvHoraCierre.isEnabled = true
                    holder.tvHoraApertura.text = if (horario.horaApertura != "00:00") horario.horaApertura else "Apertura"
                    holder.tvHoraCierre.text = if (horario.horaCierre != "00:00") horario.horaCierre else "Cierre"
                }
                onHorarioChanged(horario)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }

        holder.tvHoraApertura.setOnClickListener {
            if(holder.tvHoraApertura.isEnabled) {
                showTimePicker(holder.itemView.context) { hora ->
                    holder.tvHoraApertura.text = hora
                    horario.horaApertura = hora
                    onHorarioChanged(horario)
                }
            }
        }

        holder.tvHoraCierre.setOnClickListener {
            if(holder.tvHoraCierre.isEnabled) {
                showTimePicker(holder.itemView.context) { hora ->
                    holder.tvHoraCierre.text = hora
                    horario.horaCierre = hora
                    onHorarioChanged(horario)
                }
            }
        }
    }

    override fun getItemCount(): Int = horarios.size

    class HorarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDia: TextView = itemView.findViewById(R.id.tv_dia)
        val tvHoraApertura: TextView = itemView.findViewById(R.id.tv_hora_apertura)
        val tvHoraCierre: TextView = itemView.findViewById(R.id.tv_hora_cierre)
        val spnerEstadoPredio: Spinner = itemView.findViewById(R.id.spner_estado_predio)
    }

    private fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(selectedTime)
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    fun updateHorarios(newHorarios: MutableList<Horario>) {
        horarios.clear()
        horarios.addAll(newHorarios)
        notifyDataSetChanged()
    }
}