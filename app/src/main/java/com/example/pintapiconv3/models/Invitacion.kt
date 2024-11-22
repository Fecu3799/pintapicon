package com.example.pintapiconv3.models

data class Invitacion(
    var id: Int,
    var idEquipo: Int? = null,
    var equipo: String? = null,
    var idCapitan: Int? = null,
    var capitan: String? = null,
    var organizador: String? = null,
    var idPartido: Int? = null,
    var idCuenta: Int? = null,
    var idEstado: Int,
    var fechaInvitacion: String
)
