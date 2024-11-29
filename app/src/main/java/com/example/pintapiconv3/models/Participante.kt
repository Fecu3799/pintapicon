package com.example.pintapiconv3.models

data class Participante(
    var id: Int,
    var idParticipante: Int,
    var nombre: String,
    var isOrganizador: Boolean,
    var posicion: String,
    var montoPagado: Double? = null,
    var montoRestante: Double? = null,
    var idEstado: Int
)
