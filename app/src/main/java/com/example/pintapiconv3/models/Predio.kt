package com.example.pintapiconv3.models

import java.io.Serializable

data class Predio (
    var id: Int,
    var nombre: String,
    var telefono: String,
    var idDireccion: Int,
    var idEstado: Int,
    var disponibilidad: Boolean,
    var precio_hora: Double,
    var latitud: Double?,
    var longitud: Double?,
    var url_google_maps: String?
): Serializable