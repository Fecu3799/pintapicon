package com.example.pintapiconv3.models

data class Partido(
    val id: Int,
    val fecha: String,
    val hora: String,
    val isPublic: Boolean,
    val idOrganizador: Int,
    val idCancha: Int,
    val idTipoPartido: Int,
    val idEstado: Int
)
