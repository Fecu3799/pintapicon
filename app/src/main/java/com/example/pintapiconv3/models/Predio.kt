package com.example.pintapiconv3.models

import java.io.Serializable

data class Predio (
    var id: Int,
    var nombre: String,
    var telefono: String,
    var idDireccion: Int,
    var idEstado: Int,
    var url_google_maps: String?,
    var latitud: Double?,
    var longitud: Double?,
    var canchas: List<Cancha> = emptyList()
): Serializable