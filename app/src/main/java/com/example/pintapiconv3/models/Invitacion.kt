package com.example.pintapiconv3.models

data class Invitacion(
    var id: Int,
    var idEquipo: Int,
    var equipo: String,
    var idCapitan: Int,
    var capitan: String,
    var idEstado: Int,
    var fechaInvitacion: String
)
