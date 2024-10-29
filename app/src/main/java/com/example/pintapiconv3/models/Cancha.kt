package com.example.pintapiconv3.models

import java.io.Serializable

data class Cancha (
    var idPredio: Int,
    var idTipoCancha: Int,
    var tipoCancha: String,
    var precioHora: Double,
    var disponibilidad: Boolean,
    var isNew: Boolean = false
): Serializable