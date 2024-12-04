package com.example.pintapiconv3.app.user.match

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.ParticipanteAdapter
import com.example.pintapiconv3.app.user.main.ShowMapDialog
import com.example.pintapiconv3.models.Participante
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Const.MatchStatus.CANCELED
import com.example.pintapiconv3.utils.Const.MatchStatus.FINISHED
import com.example.pintapiconv3.utils.Const.ReservationStatus.PAID
import com.example.pintapiconv3.utils.Const.ReservationStatus.PENDING_PAYMENT
import com.example.pintapiconv3.utils.Utils.parseLatLngFromUrl
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.PartidoViewModel
import com.example.pintapiconv3.viewmodel.PartidoViewModelFactory
import com.example.pintapiconv3.viewmodel.SharedMatchData
import com.example.pintapiconv3.viewmodel.SharedUserData
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MatchDetailsActivity : AppCompatActivity() {

    private val partidoRepository = PartidoRepository()
    private val userRepository = UserRepository()

    private lateinit var userViewModel: UserViewModel
    private lateinit var partidoViewModel: PartidoViewModel

    private var cantParticipantes = 0
    private var amountPerPerson = 0.0
    private var totalAmount: Double = 0.0
    private var urlGoogleMaps: String? = null
    private var isOrganizer: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SharedMatchData.init(this, partidoRepository, forceInit = true)
        partidoViewModel = SharedMatchData.matchViewModel!!

        if(SharedUserData.userViewModel == null) {
            SharedUserData.init(this, userRepository)
        }
        userViewModel = SharedUserData.userViewModel!!

        val userId = userViewModel.user.value?.id

        if(userId != null) {
            val sharedPref = getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
            val partidoId = sharedPref.getInt("partidoId_$userId", -1)

            if(partidoId != -1) {
                partidoViewModel.setMatch(partidoId)
            } else {
                showToast("Error al cargar el partido")
                Log.e("MatchDetailsActivity", "Error al cargar el partido")
                finish()
            }
        } else {
            showToast("Error al cargar el usuario")
            Log.e("MatchDetailsActivity", "Error al cargar el usuario")
            finish()
        }

        initViews()
        setupObservers()
    }

    private fun initViews() {
        tvEstadoPartido = findViewById(R.id.tv_estado_partido)
        tvPredio = findViewById(R.id.tv_predio)
        tvMapa = findViewById(R.id.tv_mapa)
        tvCancha = findViewById(R.id.tv_cancha)
        tvTipoPartido = findViewById(R.id.tv_tipo_partido)
        tvFechaHora = findViewById(R.id.tv_fecha_hora)
        tvMontoTotal = findViewById(R.id.tv_monto_total)
        tvMontoPorPersona = findViewById(R.id.tv_monto_por_persona)
        tvMontoAcumulado = findViewById(R.id.tv_monto_acumulado)
        tvMontoAcumulado2 = findViewById(R.id.tv_monto_acumulado2)
        btnPagar = findViewById(R.id.btn_pagar)
        btnQuitarFondos = findViewById(R.id.btn_quitar_fondos)
        tvEstadoReserva = findViewById(R.id.tv_estado_reserva)
        btnSuspender = findViewById(R.id.btn_suspender)
        rvParticipantes = findViewById(R.id.rv_participantes)
        btnInvitarJugador = findViewById(R.id.btn_invitar_jugador)

        tvMapa.isEnabled = false

        rvParticipantes.layoutManager = LinearLayoutManager(this)
        rvParticipantesAdapter = ParticipanteAdapter { participante ->
            showDeleteParticipantConfirmation(participante)
        }
        rvParticipantes.adapter = rvParticipantesAdapter

        btnPagar.setOnClickListener {
            showAddFundsDialog()
        }

        btnQuitarFondos.setOnClickListener {
            showRemoveFundsDialog()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        // Observer del partido
        partidoViewModel.match.observe(this) { partido ->

            val partidoId = partido.id
            val currentUserId = userViewModel.user.value?.id ?: return@observe

            verificarParticipacion(partidoId, currentUserId)

            if(partido.idEstado == FINISHED || partido.idEstado == CANCELED) {
                showMatchEndedDialog()
            }

            // Configuracion del rol del usuario
            val organizerId = partido.idOrganizador
            isOrganizer = currentUserId == organizerId
            configureUI()

            // Configuración del tipo de partido
            val tipoPartido = when(partido.idTipoPartido) {
                1 -> "Masculino"
                2 -> "Femenino"
                else -> "Mixto"
            }
            tvTipoPartido.text = "Tipo de partido: $tipoPartido"
        }

        // Observer de la cancha
        partidoViewModel.canchaElegida.observe(this) { cancha ->
            tvCancha.text = "Cancha N° ${cancha.nroCancha} - ${cancha.tipoCancha}"
            cantParticipantes = when(cancha.idTipoCancha) {
                1 -> 10
                2 -> 14
                3 -> 16
                else -> 22
            }
            updateAmountPerPerson()
        }

        // Observer del monto acumulado
        partidoViewModel.montoAcumulado.observe(this) { accumulated ->
            tvMontoAcumulado.text = "$${String.format("%.2f", accumulated)}"
            val totalAmount = partidoViewModel.reserva.value?.monto ?: 0.0
            if(accumulated == totalAmount) {
                val partidoId = partidoViewModel.match.value?.id ?: return@observe
                partidoViewModel.updateReservationStatus(partidoId, PAID)
            } else {
                val partidoId = partidoViewModel.match.value?.id ?: return@observe
                partidoViewModel.updateReservationStatus(partidoId, PENDING_PAYMENT)
            }
        }

        // Observer de la reserva
        partidoViewModel.reserva.observe(this) { reserva ->
            // Configuración datos de la reserva
            tvPredio.text = "Predio: ${reserva.predio}"
            tvFechaHora.text = "${reserva.fecha} - ${reserva.horaInicio}"
            totalAmount = reserva.monto
            tvMontoTotal.text = "$${totalAmount}"

            when(reserva.idEstado) {
                PENDING_PAYMENT -> {
                    tvEstadoReserva.text = "Pago pendiente"
                    tvEstadoReserva.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
                PAID -> {
                    tvEstadoReserva.text = "Pagado"
                    tvEstadoReserva.setTextColor(ContextCompat.getColor(this, R.color.green))
                }
                else -> {
                    tvEstadoReserva.text = "Cancelada"
                    tvEstadoReserva.setTextColor(ContextCompat.getColor(this, R.color.red))
                }
            }

            updateAmountPerPerson()

            // Configuración del boton del mapa
            urlGoogleMaps = reserva.ubicacion
            if(urlGoogleMaps != null) {
                tvMapa.isEnabled = true
                tvMapa.setOnClickListener {
                    val coordinates = parseLatLngFromUrl(urlGoogleMaps!!)
                    if(coordinates != null) {
                        val dialog = ShowMapDialog(coordinates.first, coordinates.second)
                        dialog.show(supportFragmentManager, "ShowMapDialog")
                    } else {
                        showToast("Error al obtener las coordenadas del predio")
                    }
                }
            } else {
                tvMapa.setOnClickListener {
                    showToast("El predio no tiene ubicación registrada")
                }
            }

            if(reserva.idMetodoPago == 1 || reserva.idMetodoPago == 2) {
                tvMontoAcumulado.visibility = View.GONE
                tvMontoAcumulado2.visibility = View.GONE
                btnPagar.visibility = View.GONE
                btnQuitarFondos.visibility = View.GONE
                when(reserva.idMetodoPago) {
                    1 -> {
                        tvEstadoReserva.text = "Pago en efectivo"
                        tvEstadoReserva.setTextColor(ContextCompat.getColor(this, R.color.green))
                        tvEstadoReserva.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                    }
                    2 ->  {
                        tvEstadoReserva.text = "Pago con transferencia"
                        tvEstadoReserva.setTextColor(ContextCompat.getColor(this, R.color.blue))
                    }
                }
            }
        }

        // Observer de los participantes
        partidoViewModel.participantes.observe(this) { participantes ->
            val organizerId = partidoViewModel.match.value?.idOrganizador ?: -1
            val isCurrentUserOrganizer = isOrganizer
            rvParticipantesAdapter.setParticipantes(
                participantesList = participantes,
                organizadorId = organizerId,
                isCurrentUserOrganizer = isCurrentUserOrganizer
            )
            rvParticipantesAdapter.setMontoPorPersona(amountPerPerson)
        }

        // Observer del contador
        partidoViewModel.countdown.observe(this) { countdown ->
            tvEstadoPartido.text = countdown
        }
    }

    private fun updateAmountPerPerson() {
        if(cantParticipantes > 0 && totalAmount > 0) {
            amountPerPerson = totalAmount / cantParticipantes
            tvMontoPorPersona.text = "$${String.format("%.2f", amountPerPerson)}"
        } else {
            tvMontoPorPersona.text = "Calculando..."
        }
    }

    private fun verificarParticipacion(partidoId: Int, userId: Int) {
        lifecycleScope.launch {
            val isParticipant = withContext(Dispatchers.IO) {
                partidoViewModel.isParticipant(partidoId, userId)
            }

            if(!isParticipant) {
                showParticipantRemoved()
            } else {
                configureUI()
            }
        }
    }

    private fun configureUI() {
        if(isOrganizer) {                                   // ORGANIZADOR
            btnInvitarJugador.visibility = View.VISIBLE
            btnInvitarJugador.setOnClickListener {
                val partidoId = partidoViewModel.match.value?.id
                if(partidoId != null) {
                    val organizador = userViewModel.user.value?.nombre
                    val capitanId = userViewModel.user.value?.id
                    if(organizador != null && capitanId != null) {
                        val dialog = InvitePlayerDialog(
                            partidoId,
                            organizador,
                            capitanId,
                            partidoRepository,
                            userRepository
                        )
                        dialog.show(supportFragmentManager, "InvitePlayerDialog")
                    } else {
                        showToast("Error al cargar los datos del usuario")
                    }
                } else {
                    showToast("Error al cargar el partido")
                }
            }
            btnSuspender.text = "Suspender"
            btnSuspender.setOnClickListener {
                showSuspendMatchConfirmation()
            }
        } else {                                          // PARTICIPANTE
            btnInvitarJugador.visibility = View.GONE
            btnSuspender.text = "Abandonar"
            btnSuspender.setOnClickListener {
                showLeaveMatchConfirmation()
            }
        }
    }

    private fun showParticipantRemoved() {
        if(isFinishing || isDestroyed) {
            return
        }

        val userId = userViewModel.user.value?.id ?: return

        AlertDialog.Builder(this)
            .setTitle("Expulsado")
            .setMessage("El organizador te ha expulsado del partido")
            .setCancelable(false)
            .setPositiveButton("Salir") { dialog, _ ->
                dialog.dismiss()
                deleteMatch(userId)
                finish()
            }
            .show()
    }

    private fun showDeleteParticipantConfirmation(participante: Participante) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar participante")
            .setMessage("¿Estas seguro que deseas eliminar a ${participante.nombre}?")
            .setPositiveButton("si") { dialog, _ ->
                val partidoId = partidoViewModel.match.value?.id
                if(partidoId != null) {
                    deleteParticipant(partidoId, participante.idParticipante)
                    dialog.dismiss()
                } else {
                    showToast("Ha ocurrido un error. Intentelo denuevo")
                    dialog.dismiss()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteParticipant(partidoId: Int, participanteId: Int) {
        partidoViewModel.removeParticipant(partidoId, participanteId, false)

        val sharedPref = getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("partidoId_$participanteId")
            apply()
        }
    }

    private fun showAddFundsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Agregar fondos")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)

        builder.setPositiveButton("Agregar") { dialog, _ ->
            val montoStr = input.text.toString()
            if(montoStr.isNotEmpty()) {
                val monto = montoStr.toDouble()
                if(validateAmount(monto)) {
                    addFunds(monto)
                } else {
                    showToast("El monto ingresado excede el monto restante")
                }
            } else {
                showToast("Ingrese un monto válido")
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun validateAmount(amount: Double): Boolean {
        val totalAmount = partidoViewModel.reserva.value?.monto ?: 0.0
        val accumulatedAmount = getAccumulatedAmount()
        val remainingAmount = totalAmount - accumulatedAmount
        return amount > 0 && amount <= remainingAmount
    }

    private fun getAccumulatedAmount(): Double {
        val participantes = partidoViewModel.participantes.value ?: emptyList()
        return participantes.sumOf { it.montoPagado ?: 0.0 }
    }

    private fun addFunds(amount: Double) {
        val userId = userViewModel.user.value?.id ?: return
        val partidoId = partidoViewModel.match.value?.id ?: return
        partidoViewModel.addFundsToParticipant(partidoId, userId, amount, amountPerPerson)
    }

    private fun showRemoveFundsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Quitar fondos")
            .setMessage("¿Estas seguro que deseas quitar los fondos?")
            .setPositiveButton("Si") { dialog, _ ->
                removeFunds()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun removeFunds() {
        val userId = userViewModel.user.value?.id ?: return
        val partidoId = partidoViewModel.match.value?.id ?: return
        val participantes = partidoViewModel.participantes.value ?: return
        val participante = participantes.find { it.idParticipante == userId }
        val montoActual = participante?.montoPagado ?: 0.0

        if(montoActual > 0) {
            partidoViewModel.removeFundsFromParticipant(partidoId, userId)
        } else {
            showToast("No tienes fondos para quitar")
        }
    }

    private fun showSuspendMatchConfirmation() {
        if(isFinishing || isDestroyed) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Suspender partido")
            .setMessage("¿Estas seguro que deseas suspender el partido?")
            .setPositiveButton("Si") { dialog, _ ->
                suspendMatch()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun suspendMatch() {
        val partidoId = partidoViewModel.match.value?.id ?: return
        val userId = userViewModel.user.value?.id ?: return

        partidoViewModel.updateMatchStatus(partidoId, CANCELED)

        deleteMatch(userId)

        showToast("El partido se ha suspendido")
        finish()
    }

    private fun showLeaveMatchConfirmation() {
        if(isFinishing || isDestroyed) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Abandonar partido")
            .setMessage("¿Estas seguro que deseas abandonar el partido?")
            .setPositiveButton("Si") { dialog, _ ->
                leaveMatch()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun leaveMatch() {
        val partidoId = partidoViewModel.match.value?.id ?: return
        val userId = userViewModel.user.value?.id ?: return

        partidoViewModel.removeParticipant(partidoId, userId, true)

        deleteMatch(userId)

        showToast("Has abandonado el partido")
        finish()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateCountdownRunnable = object : Runnable {
        override fun run() {
            val partidoId = partidoViewModel.match.value?.id ?: return
            partidoViewModel.updateParticipantes(partidoId)
            handler.postDelayed(this, 10000)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        handler.post(updateCountdownRunnable)
        
        val partido = partidoViewModel.match.value
        if(partido != null) {
            partidoViewModel.ensureCountdownIsRunning(partido.fecha, partido.hora)
            checkIfMatchEnded()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateCountdownRunnable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkIfMatchEnded() {
        val partido = partidoViewModel.match.value ?: return

        val formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHoraPartidoStr = "${partido.fecha} ${partido.hora}"
        val fechaHoraPartido = LocalDateTime.parse(fechaHoraPartidoStr, formatoFechaHora)

        val ahora = LocalDateTime.now()

        val partidoFin = fechaHoraPartido.plusHours(1)

        if(ahora.isAfter(partidoFin) && partido.idEstado != FINISHED) {
            showMatchEndedDialog()
        }
    }

    private fun showMatchEndedDialog() {
        if(isFinishing || isDestroyed) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Partido Finalizado")
            .setMessage("El partido ha finalizado.")
            .setCancelable(false)
            .setPositiveButton("Finalizar") { dialog, _ ->
                dialog.dismiss()
                finalizarPartido()
            }
            .show()
    }

    private fun finalizarPartido() {
        val partidoId = partidoViewModel.match.value?.id ?: return
        val userId = userViewModel.user.value?.id ?: return

        partidoViewModel.updateMatchStatus(partidoId, FINISHED)

        deleteMatch(userId)

        showToast("El partido ha finalizado")
        finish()
    }

    private fun deleteMatch(userId: Int) {
        val sharedPref = getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("partidoId_$userId")
            apply()
        }

        userViewModel.setIsMatch(false)

        SharedMatchData.clear()
    }

    private lateinit var tvEstadoPartido: TextView
    private lateinit var tvPredio: TextView
    private lateinit var tvMapa: TextView
    private lateinit var tvCancha: TextView
    private lateinit var tvTipoPartido: TextView
    private lateinit var tvFechaHora: TextView
    private lateinit var tvMontoTotal: TextView
    private lateinit var tvMontoPorPersona: TextView
    private lateinit var tvMontoAcumulado: TextView
    private lateinit var tvMontoAcumulado2: TextView
    private lateinit var btnPagar: ImageButton
    private lateinit var btnQuitarFondos: ImageButton
    private lateinit var tvEstadoReserva: TextView
    private lateinit var btnSuspender: AppCompatButton
    private lateinit var rvParticipantes: RecyclerView
    private lateinit var rvParticipantesAdapter: ParticipanteAdapter
    private lateinit var btnInvitarJugador: TextView
}