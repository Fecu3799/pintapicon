package com.example.pintapiconv3.models

import java.io.Serializable


data class User(
    var id: Int,
    var email: String,
    var password: String,
    var nombre: String,
    var apellido: String,
    var fechaNacimiento: String,
    var telefono: String,
    var idDireccion: Int,
    var calle: String,
    var numero: Int,
    var idBarrio: Int,
    var barrio: String,
    var localidad: String,
    var provincia: String,
    var pais: String,
    var estado: Int,
    var genero: Int,
    var habilidad: Int,
    var posicion: Int,
    var isAdmin: Int
) : Serializable
