package com.example.pintapiconv3.utils

sealed class LoginResult {

    data class Success (val email: String, val password: String, val isAdmin: Int, val estado: Int): LoginResult()

    data class ErrorCredenciales (val email: String, val message: String): LoginResult()

    data class Error (val message: String): LoginResult()
}