package com.example.pintapiconv3.utils

import android.util.Log
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.SignatureException
import java.util.Date
import javax.crypto.SecretKey

object JWToken {

    // Clave privada
    //private val secretKey: SecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    // Generar una clave fija desde una cadena secreta (ideal para producción)
    private val secretKey: SecretKey = Keys.hmacShaKeyFor("my-very-strong-secret-key-which-should-be-secure".toByteArray())


    // Tiempo de expiracion
    private const val TOKEN_VALIDITY = 15*60*1000 // 15 minuto

    fun generateToken(email: String): String {
        val now = Date()
        val expiryDate = Date(now.time + TOKEN_VALIDITY)

        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token)
            !claims.body.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            Log.e("JWT", "El token ha expirado", e)
            false
        } catch (e: SignatureException) {
            Log.e("JWT", "Error en la firma del token", e)
            false
        } catch (e: Exception) {
            Log.e("JWT", "Error al validar el token", e)
            false
        }
    }

    fun getEmailFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
        return claims.body.subject
    }

}