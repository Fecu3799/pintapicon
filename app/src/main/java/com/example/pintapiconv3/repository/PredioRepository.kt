package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Predio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types

class PredioRepository {

    var conn: Connection? = null

    fun getNextPredioId(): Int {
        try {
            conn = DBConnection.getConnection()
            val query = "SELECT MAX(id) FROM predios"
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            if(resultSet?.next() == true)
                return resultSet.getInt(1) + 1

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
        return 1
    }

    suspend fun insertPredio(predio: Predio): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        var generatedId = 0

        try {
            conn = DBConnection.getConnection()
            val query = """
                INSERT INTO predios (nombre, telefono, idDireccion, idEstado, disponibilidad, latitud, longitud, url_google_maps)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val preparedStatement = conn?.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

            preparedStatement?.setString(1, predio.nombre)
            preparedStatement?.setString(2, predio.telefono)
            preparedStatement?.setInt(3, predio.idDireccion)
            preparedStatement?.setInt(4, predio.idEstado)
            preparedStatement?.setInt(5, if (predio.disponibilidad) 1 else 0)
            if(predio.latitud != null && predio.longitud != null) {
                preparedStatement?.setDouble(6, predio.latitud!!)
                preparedStatement?.setDouble(7, predio.longitud!!)
            } else {
                preparedStatement?.setNull(6, Types.DOUBLE)
                preparedStatement?.setNull(7, Types.DOUBLE)
            }
            if(predio.url_google_maps != null) {
                preparedStatement?.setString(8, predio.url_google_maps)
            } else {
                preparedStatement?.setNull(8, Types.VARCHAR)
            }
            preparedStatement?.executeUpdate()

            val generatedKeys = preparedStatement?.generatedKeys
            if (generatedKeys?.next() == true) {
                generatedId = generatedKeys.getInt(1)
            }

            preparedStatement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
        return@withContext generatedId
    }

    suspend fun insertCancha(cancha: Cancha): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val query = """
                INSERT INTO predios_tipos_canchas (idPredio, idTipoCancha, precio_hora)
                VALUES (?, ?, ?)
            """.trimIndent()

        return@withContext try {
            conn = DBConnection.getConnection()

            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setInt(1, cancha.idPredio)
            preparedStatement?.setInt(2, cancha.idTipoCancha)
            preparedStatement?.setDouble(3, cancha.precioHora)

            val rowsInserted = preparedStatement?.executeUpdate() ?: 0

            preparedStatement?.close()
            conn?.close()

            rowsInserted > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            conn?.close()
        }
    }

}