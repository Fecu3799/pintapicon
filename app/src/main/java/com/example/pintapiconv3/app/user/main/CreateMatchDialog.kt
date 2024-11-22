package com.example.pintapiconv3.app.user.main

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.PickCanchaAdapter
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Partido
import com.example.pintapiconv3.models.Reserva
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.utils.Utils.calcularHoraFin
import com.example.pintapiconv3.viewmodel.SharedUserData
import com.example.pintapiconv3.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class CreateMatchDialog : DialogFragment() {

    private var fechaSeleccionada: String = ""
    private var horaSeleccionada: String = ""
    private var canchaSeleccionada: Cancha? = null

    private lateinit var userViewModel: UserViewModel
    private val partidoRepository = PartidoRepository()
    private val sqlServerHelper = SQLServerHelper()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_new_match, container, false)
        initViews(view)
        setupListeners()
        userViewModel = SharedUserData.userViewModel!!
        return view
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(1000, 1500)
    }

    private fun initViews(view: View) {
        layoutDetallesPartido = view.findViewById(R.id.layout_detalles_partido)
        layoutCanchaPartido = view.findViewById(R.id.layout_cancha_partido)

        btnSeleccionarFecha = view.findViewById(R.id.btn_seleccionar_fecha)
        btnSeleccionarHora = view.findViewById(R.id.btn_seleccionar_hora)

        tvFechaSeleccionada = view.findViewById(R.id.tv_fecha_seleccionada)
        tvHoraSeleccionada = view.findViewById(R.id.tv_hora_seleccionada)
        spnerSeleccionarTipoCancha = view.findViewById(R.id.spner_seleccionar_tipo_cancha)
        rgSeleccionarPrivacidad = view.findViewById(R.id.rg_seleccionar_privacidad)
        rgSeleccionarTipoPartido = view.findViewById(R.id.rg_seleccionar_tipo)
        rgSeleccionarMetodoPago = view.findViewById(R.id.rg_seleccionar_metodo_pago)
        rvSeleccionarCancha = view.findViewById(R.id.rv_seleccionar_canchas)

        btnCancelar = view.findViewById(R.id.btn_cancel)
        btnSiguiente = view.findViewById(R.id.btn_next)
        btnAtras = view.findViewById(R.id.btn_atras)
        btnCrearPartido = view.findViewById(R.id.btn_create)

        pickCanchaAdapter = PickCanchaAdapter { cancha ->
            canchaSeleccionada = cancha
        }

        rvSeleccionarCancha.layoutManager = LinearLayoutManager(requireContext())
        rvSeleccionarCancha.adapter = pickCanchaAdapter

        val dividerItemDecoration = DividerItemDecoration(rvSeleccionarCancha.context, DividerItemDecoration.VERTICAL)
        rvSeleccionarCancha.addItemDecoration(dividerItemDecoration)

        initSpinner()

    }

    private fun setupListeners() {
        btnSeleccionarFecha.setOnClickListener { mostrarDatePicker() }
        btnSeleccionarHora.setOnClickListener { mostrarTimePicker() }

        btnCancelar.setOnClickListener { dismiss() }

        btnSiguiente.setOnClickListener {
            if(isFirstLayoutVisible()) {
                if(validateFieldsFirstLayout()) {
                    layoutDetallesPartido.visibility = View.GONE
                    layoutCanchaPartido.visibility = View.VISIBLE
                    cargarCanchasDisponibles()
                }
            }
        }

        btnAtras.setOnClickListener {
            layoutCanchaPartido.visibility = View.GONE
            layoutDetallesPartido.visibility = View.VISIBLE
        }

        btnCrearPartido.setOnClickListener {
            if(canchaSeleccionada == null) {
                Toast.makeText(requireContext(), "Seleccione una cancha", Toast.LENGTH_SHORT).show()
            } else {
                crearPartido()
            }
        }

    }

    private fun isFirstLayoutVisible(): Boolean {
        return view?.findViewById<LinearLayout>(R.id.layout_detalles_partido)?.visibility == View.VISIBLE
    }

    private fun validateFieldsFirstLayout(): Boolean {
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor seleccione una fecha", Toast.LENGTH_SHORT).show()
            return false
        }
        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor seleccione una hora", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        val calendarFechaSeleccionada = Calendar.getInstance()
        val fechaSplit = fechaSeleccionada.split("-")
        val horaSplit = horaSeleccionada.split(":")

        val dia = fechaSplit[2].toInt()
        val mes = fechaSplit[1].toInt() - 1
        val anio = fechaSplit[0].toInt()

        val hora = horaSplit[0].toInt()
        val minuto = horaSplit[1].toInt()

        calendarFechaSeleccionada.set(anio, mes, dia, hora, minuto)

        val calendarFechaActual = Calendar.getInstance()

        if(calendarFechaSeleccionada.before(calendarFechaActual)) {
            Toast.makeText(requireContext(), "La fecha y hora seleccionadas deben ser futuras", Toast.LENGTH_SHORT).show()
            return false
        }

        if (spnerSeleccionarTipoCancha.selectedItem == null) {
            Toast.makeText(requireContext(), "Por favor seleccione el tipo de cancha", Toast.LENGTH_SHORT).show()
            return false
        }
        if (rgSeleccionarPrivacidad.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Por favor seleccione la privacidad del partido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (rgSeleccionarTipoPartido.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Por favor seleccione el tipo de partido", Toast.LENGTH_SHORT).show()
            return false
        }
        if(rgSeleccionarMetodoPago.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Por favor seleccione el método de pago", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun cargarCanchasDisponibles() {
        lifecycleScope.launch(Dispatchers.Main) {
            val tipoCancha = spnerSeleccionarTipoCancha.selectedItemPosition + 1
            val canchasDisponibles = withContext(Dispatchers.IO) {
                partidoRepository.getCanchasByTipoYHorario(tipoCancha, fechaSeleccionada, horaSeleccionada)
            }
            pickCanchaAdapter.setCanchas(canchasDisponibles)
        }
    }

    private fun initSpinner() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val tiposCanchas = withContext(Dispatchers.IO) {
                    sqlServerHelper.getTipoCanchas()
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tiposCanchas.map { it.second })
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnerSeleccionarTipoCancha.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al obtener los tipos de canchas", Toast.LENGTH_SHORT).show()
                Log.e("CreateMatchDialog", "Error al obtener los tipos de canchas: ${e.message}")
            }

            spnerSeleccionarTipoCancha.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    cargarCanchasDisponibles()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, day ->
            val calendarSelected = Calendar.getInstance()
            calendarSelected.set(year, month, day)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            fechaSeleccionada = dateFormat.format(calendarSelected.time)
            tvFechaSeleccionada.text = "$day/${month + 1}/$year"
        }, currentYear, currentMonth, currentDay)

        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun mostrarTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(requireContext(), { _, hour, minute ->
            val calendarSelected = Calendar.getInstance()
            calendarSelected.set(Calendar.HOUR_OF_DAY, hour)
            calendarSelected.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            horaSeleccionada = timeFormat.format(calendarSelected.time)
            tvHoraSeleccionada.text = String.format("%02d:%02d", hour, minute)
        }, currentHour, currentMinute, true)
        timePickerDialog.show()
    }

    private fun crearPartido() {
        val tipoPrivacidad = if(rgSeleccionarPrivacidad.checkedRadioButtonId == R.id.rb_publico) true else false
        val tipoPartido = when(rgSeleccionarTipoPartido.checkedRadioButtonId) {
            R.id.rb_masculino -> 1
            R.id.rb_femenino -> 2
            else -> 3
        }
        val userId = SharedUserData.userViewModel?.user?.value?.id ?: return
        val horaFin = calcularHoraFin(horaSeleccionada)
        val metodoPago = when(rgSeleccionarMetodoPago.checkedRadioButtonId) {
            R.id.rb_efectivo -> 1
            R.id.rb_transferencia -> 2
            else -> 3
        }

        val partido = Partido(
            id = 0,
            fecha = fechaSeleccionada,
            hora = horaSeleccionada,
            isPublic = tipoPrivacidad,
            idOrganizador = userId,
            idCancha = canchaSeleccionada?.id ?: return,
            idTipoPartido = tipoPartido,
            idEstado = PartidoRepository.MatchState.PENDING
        )

        val reserva = Reserva(
            id = 0,
            fecha = fechaSeleccionada,
            horaInicio = horaSeleccionada,
            horaFin = horaFin,
            monto = canchaSeleccionada!!.precioHora,
            idMetodoPago = metodoPago,
            idEstado = PartidoRepository.ReservationState.PENDING_PAYMENT,
            idPredio = canchaSeleccionada!!.idPredio,
            idPartido = 0,
            idCancha = canchaSeleccionada!!.id
        )

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val idPartido = withContext(Dispatchers.IO) {
                    partidoRepository.crearPartidoConReserva(partido, reserva)
                }
                if(idPartido > 0) {
                    Toast.makeText(requireContext(), "Partido creado exitosamente", Toast.LENGTH_SHORT).show()
                    userViewModel.setIsMatch(true)
                    userViewModel.setIsCaptain(true)

                    val sharedPref = requireContext().getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putInt("partidoId", idPartido)
                        apply()
                    }

                    startActivity(Intent(requireContext(), MatchDetailsActivity::class.java))

                    //requireActivity().supportFragmentManager.setFragmentResult("matchCreated", Bundle())
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "No se pudo crear el partido", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al crear el partido", Toast.LENGTH_SHORT).show()
                Log.e("CreateMatchDialog", "Error al crear el partido: ${e.message}")
            }
        }
    }

    private lateinit var layoutDetallesPartido: LinearLayout
    private lateinit var layoutCanchaPartido: LinearLayout

    private lateinit var btnSeleccionarFecha: Button
    private lateinit var btnSeleccionarHora: Button
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var tvHoraSeleccionada: TextView
    private lateinit var spnerSeleccionarTipoCancha: Spinner
    private lateinit var rgSeleccionarPrivacidad: RadioGroup
    private lateinit var rgSeleccionarTipoPartido: RadioGroup
    private lateinit var rgSeleccionarMetodoPago: RadioGroup
    private lateinit var rvSeleccionarCancha: RecyclerView
    private lateinit var btnCancelar: Button
    private lateinit var btnSiguiente: Button
    private lateinit var btnAtras: Button
    private lateinit var btnCrearPartido: Button

    private lateinit var pickCanchaAdapter: PickCanchaAdapter
}