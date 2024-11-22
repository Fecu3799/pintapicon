package com.example.pintapiconv3.models

import java.io.Serializable

data class Cancha (
    var id: Int,
    var idPredio: Int,
    var nombrePredio: String? = null,
    var idTipoCancha: Int,
    var tipoCancha: String,
    var nroCancha: String,
    var precioHora: Double,
    var disponibilidad: Boolean,
    var isNew: Boolean = false
): Serializable