package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Horario
import com.example.pintapiconv3.models.Predio

class PredioViewModel : ViewModel() {

    val predio: MutableLiveData<Predio> = MutableLiveData()
    val direccion: MutableLiveData<Direccion> = MutableLiveData()
    val canchas: MutableLiveData<MutableList<Cancha>> = MutableLiveData(mutableListOf())
    val deletedCanchas: MutableLiveData<MutableList<Cancha>> = MutableLiveData(mutableListOf())
    val horarios: MutableLiveData<MutableList<Horario>> = MutableLiveData(mutableListOf())

    fun updatePredio(updatedPredio: Predio) {
        predio.postValue(updatedPredio)
    }

    fun updateDireccion(updatedDireccion: Direccion) {
        direccion.postValue(updatedDireccion)
    }

    fun updateCanchas(updatedCanchas: MutableList<Cancha>) {
        canchas.value = updatedCanchas
    }

    fun updateHorarios(updatedHorarios: MutableList<Horario>) {
        horarios.value = updatedHorarios
    }

    fun addCancha(cancha: Cancha) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        updatedCanchas.add(cancha)
        canchas.postValue(updatedCanchas)
    }

    fun deleteCancha(cancha: Cancha) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        updatedCanchas.remove(cancha)
        canchas.postValue(updatedCanchas)

        val updatedDeletedCanchas = deletedCanchas.value ?: mutableListOf()
        updatedDeletedCanchas.add(cancha)
        deletedCanchas.postValue(updatedDeletedCanchas)
    }

    fun updateCanchaPrice(cancha: Cancha, newPrice: Double) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        val canchaToUpdate = updatedCanchas.find { it.idPredio == cancha.idPredio && it.idTipoCancha == cancha.idTipoCancha}
        if(canchaToUpdate != null) {
            canchaToUpdate.precioHora = newPrice
            canchas.postValue(updatedCanchas)
        }
    }

    fun addOrUpdateHorario(horario: Horario) {
        val updatedHorarios = horarios.value ?: mutableListOf()
        val existingHorarioIndex = updatedHorarios.indexOfFirst { it.dia == horario.dia }

        if(existingHorarioIndex != -1) {
            updatedHorarios[existingHorarioIndex] = horario
        } else {
            updatedHorarios.add(horario)
        }
        horarios.postValue(updatedHorarios)
    }

    fun getCanchasById(idPredio: Int, idTipoCancha: Int) : Cancha? {
        return canchas.value?.find {it.idPredio == idPredio && it.idTipoCancha == idTipoCancha }
    }
}