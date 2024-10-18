package com.example.pintapiconv3.models

import java.io.Serializable

data class Cancha (
    var idPredio: Int,
    var idTipoCancha: Int,
    var precioHora: Double
): Serializable
