package com.example.pintapiconv3.models

data class Equipo(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val capitan: String,
    val miembros: List<Miembro> = emptyList()
)
