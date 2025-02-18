package com.example.pintapiconv3.models

import java.io.Serializable

data class Horario (
    var dia: String,
    var horaApertura: String,
    var horaCierre: String,
    var idPredio: Int
) : Serializable