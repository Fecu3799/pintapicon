package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Participante
import com.example.pintapiconv3.models.Partido
import com.example.pintapiconv3.models.Reserva
import com.example.pintapiconv3.repository.PartidoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class PartidoViewModel(private val partidoRepository: PartidoRepository) : ViewModel() {

    private val _match = MutableLiveData<Partido>()
    val match: LiveData<Partido> get() = _match

    private val _reserva = MutableLiveData<Reserva>()
    val reserva: LiveData<Reserva> get() = _reserva

    private val _participantes = MutableLiveData<List<Participante>>()
    val participantes: LiveData<List<Participante>> get() = _participantes

    private val _canchaElegida = MutableLiveData<Cancha>()
    val canchaElegida: LiveData<Cancha> get() = _canchaElegida

    private val _countdown = MutableLiveData<String>()
    val countdown: LiveData<String> get() = _countdown

    private var countdownJob: Job? = null

    fun setMatch(partidoId: Int) {
        viewModelScope.launch {
            val fetchedMatch = partidoRepository.getPartidoById(partidoId)
            _match.value = fetchedMatch

            val fetchedReserva = partidoRepository.getReservaByPartidoId(partidoId)
            _reserva.value = fetchedReserva

            val fetchedParticipantes = partidoRepository.getParticipantesByPartidoId(partidoId)
            _participantes.value = fetchedParticipantes

            val fetchedCancha = partidoRepository.getCanchaByPartido(fetchedMatch.idCancha)
            _canchaElegida.value = fetchedCancha

            startCountdown(fetchedReserva.fecha, fetchedReserva.horaInicio)
        }
    }

    private fun startCountdown(fecha: String, hora: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val partidoDate = dateFormat.parse("$fecha $hora")

            var countdownActive = true
            while(countdownActive) {
                val now = Calendar.getInstance().time

                partidoDate?.let {
                    val diff = it.time - now.time
                    if(diff > 0) {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                        _countdown.value = if (days > 0) {
                            if(days.toInt() == 1) "$days Dia : $hours Hs : $minutes Min"
                            else "$days Dias : $hours Hs : $minutes Min"
                        } else {
                            "$hours Hs : $minutes Min : $seconds Seg"
                        }
                    } else {
                        _countdown.value = "Partido en curso"
                        countdownActive = false
                    }
                }
                delay(1000)
            }
        }
    }

    fun ensureCountdownIsRunning(fecha: String, hora: String) {
        if (countdownJob == null || countdownJob?.isActive == false) {
            startCountdown(fecha, hora)
        }
    }

    fun updateParticipantes(partidoId: Int) {
        viewModelScope.launch {
            val fetchedParticipantes = partidoRepository.getParticipantesByPartidoId(partidoId)
            _participantes.value = fetchedParticipantes
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

class PartidoViewModelFactory(private val partidoRepository: PartidoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartidoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartidoViewModel(partidoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

object SharedMatchData {
    var matchViewModel: PartidoViewModel? = null

    fun init(viewModelStoreOwner: ViewModelStoreOwner, partidoRepository: PartidoRepository) {
        if (matchViewModel == null) {
            val partidoViewModelFactory = PartidoViewModelFactory(partidoRepository)
            matchViewModel = ViewModelProvider(viewModelStoreOwner, partidoViewModelFactory)[PartidoViewModel::class.java]
        }
    }

    fun clear() {
        matchViewModel = null
    }
}