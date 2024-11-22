package com.example.pintapiconv3.app.user.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.ParticipanteAdapter
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Utils.parseLatLngFromUrl
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.PartidoViewModel
import com.example.pintapiconv3.viewmodel.SharedMatchData
import com.example.pintapiconv3.viewmodel.SharedUserData
import com.example.pintapiconv3.viewmodel.UserViewModel

class MatchDetailsActivity : AppCompatActivity() {

    private val partidoRepository = PartidoRepository()
    private val userRepository = UserRepository()
    private lateinit var partidoViewModel: PartidoViewModel
    private lateinit var userViewModel: UserViewModel
    private var cantParticipantes = 0
    private var urlGoogleMaps: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_match_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        SharedMatchData.init(this, partidoRepository)
        partidoViewModel = SharedMatchData.matchViewModel!!
        if(SharedUserData.userViewModel == null) {
            SharedUserData.init(this, userRepository)
        }
        userViewModel = SharedUserData.userViewModel!!

        setupObservers()

        if(partidoViewModel.match.value == null) {
            val sharedPref = getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
            val partidoId = sharedPref.getInt("partidoId", -1)

            if(partidoId != -1) {
                partidoViewModel.setMatch(partidoId)
            } else {
                showToast("Error al cargar el partido")
            }
        }
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
        btnPagar = findViewById(R.id.btn_pagar)
        tvEstadoReserva = findViewById(R.id.tv_estado_reserva)
        btnSuspender = findViewById(R.id.btn_suspender)
        rvParticipantes = findViewById(R.id.rv_participantes)
        btnInvitarJugador = findViewById(R.id.btn_invitar_jugador)

        tvMapa.isEnabled = false

        rvParticipantes.layoutManager = LinearLayoutManager(this)
        rvParticipantesAdapter = ParticipanteAdapter()
        rvParticipantes.adapter = rvParticipantesAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {

        // Observer del partido
        partidoViewModel.match.observe(this) { partido ->
            // Configuración de botones invitacion y suspensión según el usuario
            val currentUserId = userViewModel.user.value?.id
            if(currentUserId != null && partido != null) {
                if(currentUserId == partido.idOrganizador) {
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
                        // TODO:
                    }
                } else {
                    btnInvitarJugador.setOnClickListener {
                        showToast("No tienes permisos para invitar jugadores al partido")
                    }
                    btnSuspender.text = "Bajarse"
                    btnSuspender.setOnClickListener {
                        //TODO:
                    }
                }
            } else {
                Log.e("MatchDetailsActivity", "currentUserId or partido is null")
            }
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
        }

        // Observer de la reserva
        partidoViewModel.reserva.observe(this) { reserva ->
            // Configuración datos de la reserva
            tvPredio.text = "Predio: ${reserva.predio}"
            tvFechaHora.text = "${reserva.fecha} - ${reserva.horaInicio}"
            tvMontoTotal.text = "$${reserva.monto}"
            if(cantParticipantes > 0)
                tvMontoPorPersona.text = "$${reserva.monto / cantParticipantes}"
            else
                tvMontoPorPersona.text = "Calculando..."
            tvEstadoReserva.text = when(reserva.idEstado) {
                PartidoRepository.ReservationState.PENDING_PAYMENT -> "Pago pendiente"
                PartidoRepository.ReservationState.PAID -> "Pagado"
                else -> "Cancelada"
            }

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
        }

        // Observer de los participantes
        partidoViewModel.participantes.observe(this) { participantes ->
            rvParticipantesAdapter.setParticipantes(participantes)
        }

        // Observer del contador
        partidoViewModel.countdown.observe(this) { countdown ->
            tvEstadoPartido.text = countdown
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateCountdownRunnable = object : Runnable {
        override fun run() {
            val partidoId = partidoViewModel.match.value?.id ?: return
            partidoViewModel.updateParticipantes(partidoId)
            handler.postDelayed(this, 10000)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateCountdownRunnable)
        
        val partido = partidoViewModel.match.value
        if(partido != null) {
            partidoViewModel.ensureCountdownIsRunning(partido.fecha, partido.hora)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateCountdownRunnable)
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
    private lateinit var btnPagar: Button
    private lateinit var tvEstadoReserva: TextView
    private lateinit var btnSuspender: AppCompatButton
    private lateinit var rvParticipantes: RecyclerView
    private lateinit var rvParticipantesAdapter: ParticipanteAdapter
    private lateinit var btnInvitarJugador: TextView
}