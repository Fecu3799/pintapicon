package com.example.pintapiconv3.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.Predio

class PredioViewModel : ViewModel() {

    val predio: MutableLiveData<Predio> = MutableLiveData()
    val direccion: MutableLiveData<Direccion> = MutableLiveData()
    val canchas: MutableLiveData<MutableList<Cancha>> = MutableLiveData(mutableListOf())

    fun updatePredio(updatedPredio: Predio) {
        predio.value = updatedPredio
    }

    fun updateDireccion(updatedDireccion: Direccion) {
        direccion.value = updatedDireccion
    }

    fun addCancha(cancha: Cancha) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        updatedCanchas.add(cancha)
        canchas.value = updatedCanchas
    }

    fun deleteCancha(cancha: Cancha) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        updatedCanchas.remove(cancha)
        canchas.value = updatedCanchas
    }

    fun updateCanchaPrice(idPredio: Int, idTipoCancha: Int, newPrice: Double) {
        val updatedCanchas = canchas.value ?: mutableListOf()
        val cancha = updatedCanchas.find { it.idPredio == idPredio && it.idTipoCancha == idTipoCancha}
        if(cancha != null) {
            cancha.precioHora = newPrice
            canchas.value = updatedCanchas
        }
    }
}