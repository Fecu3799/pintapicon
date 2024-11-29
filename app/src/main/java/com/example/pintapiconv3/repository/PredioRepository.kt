package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Horario
import com.example.pintapiconv3.models.Predio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
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

    suspend fun insertPredioWithConnection(conn: Connection, predio: Predio): Int {
        var generatedId = 0

        try {

            val query = """
                INSERT INTO predios (nombre, telefono, idDireccion, idEstado, url_google_maps)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

            val preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

            preparedStatement?.setString(1, predio.nombre)
            preparedStatement?.setString(2, predio.telefono)
            preparedStatement?.setInt(3, predio.idDireccion)
            preparedStatement?.setInt(4, predio.idEstado)
            if(predio.url_google_maps != null) {
                preparedStatement?.setString(5, predio.url_google_maps)
            } else {
                preparedStatement?.setNull(5, Types.VARCHAR)
            }
            preparedStatement?.executeUpdate()

            val generatedKeys = preparedStatement?.generatedKeys
            if (generatedKeys?.next() == true) {
                generatedId = generatedKeys.getInt(1)
            }

            preparedStatement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return generatedId
    }

    suspend fun updatePredioWithConnection(conn: Connection, predio: Predio): Boolean {
        var isSuccess = false

        try {

            val query = """
                UPDATE predios
                SET nombre = ?, telefono = ?, idEstado = ?, url_google_maps = ?
                WHERE id = ?
            """.trimIndent()

            val preparedStatement = conn.prepareStatement(query)
            preparedStatement?.setString(1, predio.nombre)
            preparedStatement?.setString(2, predio.telefono)
            preparedStatement?.setInt(3, predio.idEstado)
            if(predio.url_google_maps != null) {
                preparedStatement?.setString(4, predio.url_google_maps)
            } else {
                preparedStatement?.setNull(4, Types.VARCHAR)
            }
            preparedStatement?.setInt(5, predio.id)

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

    suspend fun insertCanchaWithConnection(conn: Connection, cancha: Cancha): Boolean {
        return try {
            val query1 = "SELECT COUNT(*) AS totalCanchas FROM canchas WHERE idPredio = ?"
            val query2 = """
                INSERT INTO canchas (precio_hora, numero_cancha, disponibilidad, idPredio, idTipoCancha)
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

            var nroCancha = 0

            conn.prepareStatement(query1).use {
                it.setInt(1, cancha.idPredio)
                val rs = it.executeQuery()
                if(rs.next())
                    nroCancha = rs.getInt("totalCanchas") + 1
            }

            conn.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setDouble(1, cancha.precioHora)
                preparedStatement?.setInt(2, nroCancha)
                preparedStatement?.setInt(3, 1)
                preparedStatement?.setInt(4, cancha.idPredio)
                preparedStatement?.setInt(5, cancha.idTipoCancha)
                val rowsInserted = preparedStatement.executeUpdate()
                rowsInserted > 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateCanchaWithConnection(conn: Connection, cancha: Cancha): Boolean {
        return try {
            val query = """
                UPDATE canchas
                SET precio_hora = ?
                WHERE id = ?
            """.trimIndent()

            conn.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setDouble(1, cancha.precioHora)
                preparedStatement?.setInt(2, cancha.id)
                (preparedStatement?.executeUpdate() ?: 0) > 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteCanchaWithConnection(conn: Connection, cancha: Cancha): Boolean {
        return try {
            val query = "DELETE FROM canchas WHERE id = ?"

            conn.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, cancha.id)
                (preparedStatement?.executeUpdate() ?: 0) > 0
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            conn.close()
        }
    }

    suspend fun insertHorarioPredioWithConnection(conn: Connection, idPredio: Int, horario: Horario) : Boolean {
        val query = """
            INSERT INTO horarios_predio (dia, hora_apertura, hora_cierre, idPredio) 
            VALUES (?, ?, ?, ?)
        """.trimIndent()

        return try {
            val preparedStatement = conn.prepareStatement(query)

            preparedStatement?.setString(1, horario.dia)
            preparedStatement?.setString(2, horario.horaApertura)
            preparedStatement?.setString(3, horario.horaCierre)
            preparedStatement?.setInt(4, idPredio)

            val rowsInserted = preparedStatement?.executeUpdate() ?: 0

            preparedStatement?.close()

            rowsInserted > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateHorarioPredioWithConnection(conn: Connection, horario: Horario) : Boolean {
        var preparedStatement: PreparedStatement? = null
        val query = """
            UPDATE horarios_predio
            SET hora_apertura = ?, hora_cierre = ?
            WHERE dia = ? AND idPredio = ?
        """.trimIndent()

        return try {
            preparedStatement = conn.prepareStatement(query)
            preparedStatement?.setString(1, horario.horaApertura)
            preparedStatement?.setString(2, horario.horaCierre)
            preparedStatement?.setString(3, horario.dia)
            preparedStatement?.setInt(4, horario.idPredio)

            val rowAffected = preparedStatement?.executeUpdate()

            rowAffected!! > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        } finally {
            preparedStatement?.close()
        }
    }

    suspend fun getAllPredios(): List<Predio> {
        val prediosList = mutableListOf<Predio>()
        var conn: Connection? = null

        try {
            val query = """
                SELECT p.id, p.nombre, p.telefono, p.idDireccion, p.idEstado, d.calle, d.numero, d.idBarrio, b.descripcion
                FROM predios p
                LEFT JOIN direcciones d ON p.idDireccion = d.id
                LEFT JOIN barrios b ON d.idBarrio = b.id
            """.trimIndent()

            conn = DBConnection.getConnection()
            conn?.prepareStatement(query).use { preparedStatement ->
                val resultSet = preparedStatement?.executeQuery()

                while(resultSet?.next() == true) {
                    val predio = Predio (
                        id = resultSet.getInt("id"),
                        nombre = resultSet.getString("nombre"),
                        telefono = resultSet.getString("telefono"),
                        idDireccion = resultSet.getInt("idDireccion"),
                        idEstado = resultSet.getInt("idEstado"),
                        url_google_maps = null
                    )
                    val canchas = getCanchasByPredio(predio.id)
                    predio.canchas = canchas
                    prediosList.add(predio)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
        return prediosList
    }

    suspend fun getCanchasByPredio(id: Int): List<Cancha> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val canchasList = mutableListOf<Cancha>()

        try {
            val query = """
                SELECT c.id AS idCancha,
                       c.idPredio,
                       c.idTipoCancha,
                       c.numero_cancha,
                       c.precio_hora,
                       c.disponibilidad,
                       t.descripcion
                FROM canchas c
                LEFT JOIN tipos_canchas t ON c.idTipoCancha = t.id
                WHERE c.idPredio = ?
            """.trimIndent()

            conn = DBConnection.getConnection()
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, id)
                val resultSet = preparedStatement?.executeQuery()

                while(resultSet?.next() == true) {
                    val idCancha = resultSet.getInt("idCancha")
                    val idPredio = resultSet.getInt("idPredio")
                    val idTipoCancha = resultSet.getInt("idTipoCancha")
                    val nroCancha = resultSet.getString("numero_cancha")
                    val tipoCancha = resultSet.getString("descripcion")
                    val precioHora = resultSet.getDouble("precio_hora")
                    val disponibilidad = resultSet.getBoolean("disponibilidad")

                    val cancha = Cancha (
                        id = idCancha,
                        idPredio = idPredio,
                        idTipoCancha = idTipoCancha,
                        tipoCancha = tipoCancha,
                        nroCancha = "Cancha $nroCancha",
                        precioHora = precioHora,
                        disponibilidad = disponibilidad
                    )
                    canchasList.add(cancha)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return@withContext canchasList
    }

    suspend fun getHorariosByPredio(idPredio: Int): List<Horario> {
        var conn: Connection? = null
        val horariosList = mutableListOf<Horario>()
        val query = """
            SELECT dia, hora_apertura, hora_cierre
            FROM horarios_predio
            WHERE idPredio = ?
        """.trimIndent()

        try {
            conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setInt(1, idPredio)
            val resultSet = preparedStatement?.executeQuery()

            while(resultSet?.next() == true) {
                val dia = resultSet.getString("dia")
                val horaApertura = resultSet.getString("hora_apertura")
                val horaCierre = resultSet.getString("hora_cierre")

                val horario = Horario (
                    dia = dia,
                    horaApertura = horaApertura,
                    horaCierre = horaCierre,
                    idPredio = idPredio
                )

                horariosList.add(horario)
            }

            resultSet?.close()
            preparedStatement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return horariosList
    }

}