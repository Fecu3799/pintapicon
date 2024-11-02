package com.example.pintapiconv3.models

data class Miembro(
    val id: Int,
    val nombre: String,
    val habilidad: String,
    val posicion: String,
    val isCaptain: Boolean = false
)
