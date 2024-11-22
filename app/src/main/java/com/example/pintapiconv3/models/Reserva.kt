package com.example.pintapiconv3.models

data class Reserva(
    var id: Int,
    var fecha: String,
    var horaInicio: String,
    var horaFin: String,
    var monto: Double,
    var idMetodoPago: Int,
    var idEstado: Int,
    var idPredio: Int,
    var predio: String? = null,
    var ubicacion: String? = null,
    var idPartido: Int,
    var idCancha: Int
)
