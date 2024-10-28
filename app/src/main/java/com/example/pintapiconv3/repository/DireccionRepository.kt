package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Direccion
import java.sql.Connection
import java.sql.SQLException

class DireccionRepository {

    fun insertDireccion(calle: String, numero: Int, idBarrio: Int): Int? {
        val query = """
            INSERT INTO direcciones (calle, numero, idBarrio) 
            OUTPUT INSERTED.id
            VALUES (?, ?, ?)
        """

        try {
            val conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setString(1, calle)
            preparedStatement?.setInt(2, numero)
            preparedStatement?.setInt(3, idBarrio)

            val resultSet = preparedStatement?.executeQuery()
            resultSet?.next()
            val idDireccion = resultSet?.getInt("id")

            resultSet?.close()
            preparedStatement?.close()
            conn?.close()

            return idDireccion
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun insertDireccionWithConnection(conn: Connection, direccion: Direccion): Int? {
        val query = """
            INSERT INTO direcciones (calle, numero, idBarrio) 
            OUTPUT INSERTED.id
            VALUES (?, ?, ?)
        """

        try {
            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setString(1, direccion.calle)
            preparedStatement?.setInt(2, direccion.numero)
            preparedStatement?.setInt(3, direccion.idBarrio)

            val resultSet = preparedStatement?.executeQuery()
            resultSet?.next()
            val idDireccion = resultSet?.getInt("id")

            resultSet?.close()
            preparedStatement?.close()

            return idDireccion
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun getDireccionById(idDireccion: Int): Direccion? {
        var direccion: Direccion? = null
        val query = """
            SELECT calle, numero, idBarrio
            FROM direcciones
            WHERE id = ?
        """.trimIndent()

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.prepareStatement(query)
            statement?.setInt(1, idDireccion)
            val resultSet = statement?.executeQuery()

            if(resultSet?.next() == true) {
                val calle = resultSet.getString("calle")
                val numero = resultSet.getInt("numero")
                val idBarrio = resultSet.getInt("idBarrio")

                direccion = Direccion(
                    id = idDireccion,
                    calle = calle,
                    numero = numero,
                    idBarrio = idBarrio
                )
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return direccion
    }

    suspend fun updateDireccionWithConnection(conn: Connection, direccion: Direccion) : Boolean {
        var isSuccess = false

        try {

            val query = """
                UPDATE direcciones
                SET calle = ?, numero = ?, idBarrio = ?
                WHERE id = ?
            """.trimIndent()

            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setString(1, direccion.calle)
            preparedStatement?.setInt(2, direccion.numero)
            preparedStatement?.setInt(3, direccion.idBarrio)
            preparedStatement?.setInt(4, direccion.id)

            val rowsAffected = preparedStatement?.executeUpdate() ?: 0
            isSuccess = rowsAffected > 0

            preparedStatement?.close()

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            conn.close()
        }
        return isSuccess
    }
}