package com.example.pintapiconv3.viewmodel

import android.util.Log
import androidx.compose.ui.text.font.emptyCacheFontFamilyResolver
import androidx.compose.ui.window.DialogProperties
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
import com.example.pintapiconv3.utils.Const.MatchStatus.FINISHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val _montoAcumulado = MutableLiveData<Double>()
    val montoAcumulado: LiveData<Double> get() = _montoAcumulado

    private val _haFinalizado = MutableLiveData<Boolean>(false)
    val haFinalizado: LiveData<Boolean> get() = _haFinalizado

    private val _countdown = MutableLiveData<String>()
    val countdown: LiveData<String> get() = _countdown

    private var countdownJob: Job? = null

    fun setMatch(partidoId: Int) {
        viewModelScope.launch {
            val fetchedMatch = partidoRepository.getPartidoById(partidoId)
            _match.postValue(fetchedMatch)

            val fetchedReserva = partidoRepository.getReservaByPartidoId(partidoId)
            _reserva.postValue(fetchedReserva)

            val fetchedParticipantes = partidoRepository.getParticipantesByPartidoId(partidoId)
            _participantes.postValue(fetchedParticipantes)
            _montoAcumulado.postValue(fetchedParticipantes.sumOf { it.montoPagado ?: 0.0 })

            val fetchedCancha = partidoRepository.getCanchaByPartido(fetchedMatch.idCancha)
            _canchaElegida.postValue(fetchedCancha)

            startCountdown(fetchedReserva.fecha, fetchedReserva.horaInicio)
        }
    }

    private fun startCountdown(fecha: String, hora: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val partidoDate = dateFormat.parse("$fecha $hora")

            var countdownActive = true
            var countingUp = false

            while(countdownActive) {
                val now = Calendar.getInstance().time

                partidoDate?.let {
                    val diff = if (!countingUp) {
                        it.time - now.time
                    } else {
                        now.time - it.time
                    }

                    if(diff > 0 && !countingUp) {
                        val days = TimeUnit.MILLISECONDS.toDays(diff)
                        val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

                        _countdown.postValue(
                            if (days > 0) {
                                if(days.toInt() == 1) "$days Dia : $hours Hs : $minutes Min"
                                else "$days Dias : $hours Hs : $minutes Min"
                            } else {
                                "$hours Hs : $minutes Min : $seconds Seg"
                            }
                        )
                    } else if (!countingUp) {
                        countingUp = true
                        _countdown.postValue("Partido en curso")
                    } else {
                        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                        if(elapsedMinutes >= 1) {  // 1 minuto para pruebas
                            finalizeMatch()
                            countdownActive = false
                        }
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
            _montoAcumulado.value = fetchedParticipantes.sumOf { it.montoPagado ?: 0.0 }
        }
    }

    fun updateMatchStatus(partidoId: Int, reservaId: Int, newStatus: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            partidoRepository.updateMatchStatus(partidoId, reservaId, newStatus)

            val updatedMatch = partidoRepository.getPartidoById(partidoId)
            withContext(Dispatchers.Main) {
                _match.value = updatedMatch
            }
        }
    }

    fun addFundsToParticipant(partidoId: Int, userId: Int, amount: Double, amountPerPerson: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            partidoRepository.addFundsToParticipant(partidoId, userId, amount, amountPerPerson)

            val updatedParticipants = partidoRepository.getParticipantesByPartidoId(partidoId)
            withContext(Dispatchers.Main) {
                _participantes.value = updatedParticipants

                val accumulatedAmount = updatedParticipants.sumOf { it.montoPagado ?: 0.0 }
                _montoAcumulado.value = accumulatedAmount
            }
        }
    }

    fun removeFundsFromParticipant(partidoId: Int, userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            partidoRepository.updateParticipantFunds(partidoId, userId, 0.0)
            val updatedParticipant = partidoRepository.getParticipantesByPartidoId(partidoId)
            withContext(Dispatchers.Main) {
                _participantes.postValue(updatedParticipant)
                val accumulatedAmount = updatedParticipant.sumOf { it.montoPagado ?: 0.0 }
                _montoAcumulado.postValue(accumulatedAmount)
            }
        }
    }

    fun removeParticipant(partidoId: Int, userId: Int, abandono: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            partidoRepository.removeParticipant(partidoId, userId, abandono)

            val updatedParticipants = partidoRepository.getParticipantesByPartidoId(partidoId)
            withContext(Dispatchers.Main) {
                _participantes.postValue(updatedParticipants)
                _montoAcumulado.postValue(updatedParticipants.sumOf { it.montoPagado ?: 0.0 })
            }
        }
    }

    fun isParticipant(partidoId: Int, userId: Int): Boolean {
        return partidoRepository.esParticipanteDelPartido(partidoId, userId)
    }

    fun updateReservationStatus(partidoId: Int, newStatus: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            partidoRepository.updateReservationStatus(partidoId, newStatus)
            val updatedReserva = partidoRepository.getReservaByPartidoId(partidoId)
            withContext(Dispatchers.Main) {
                _reserva.postValue(updatedReserva)
            }
        }
    }

    fun markAsFinalized() {
        _haFinalizado.value = true
    }

    private fun finalizeMatch() {
        val partidoId = match.value?.id ?: return
        val reservaId = reserva.value?.id ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                partidoRepository.updateMatchStatus(partidoId, reservaId, FINISHED)

                withContext(Dispatchers.Main) {
                    _match.value = _match.value?.copy(idEstado = FINISHED)
                    _countdown.postValue("Partido finalizado")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _countdown.postValue("Error al finalizar partido")
                }
                Log.e("PartidoViewModel", "Error al finalizar partido: ${e.message}")
            }
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

    fun init(viewModelStoreOwner: ViewModelStoreOwner, partidoRepository: PartidoRepository, forceInit: Boolean = false) {
        if (matchViewModel == null || forceInit) {
            val partidoViewModelFactory = PartidoViewModelFactory(partidoRepository)
            matchViewModel = ViewModelProvider(viewModelStoreOwner, partidoViewModelFactory)[PartidoViewModel::class.java]
        }
    }

    fun clear() {
        matchViewModel = null
    }
}