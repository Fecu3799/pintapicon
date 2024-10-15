package com.example.pintapiconv3.models

import java.io.Serializable

data class Direccion(
    var calle: String,
    var numero: Int,
    var idBarrio: Int
): Serializable