package com.example.pintapiconv3.database

import com.example.pintapiconv3.models.Invitacion
import com.example.pintapiconv3.repository.EquipoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException

class SQLServerHelper {

    // Traer los barrios desde la base de datos


    fun getEstadosCuenta(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM estados WHERE idEntidad = 1"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getGeneros(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM generos"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getHabilidades(): List<Pair<Int, String>> {

        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM habilidades"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }


    fun getPosiciones(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM posiciones"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getTipoCanchas(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM tipos_canchas"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getEstadosPredio(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM estados WHERE idEntidad = 2"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getBarrios(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "SELECT id, descripcion FROM barrios"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while (resultSet?.next() == true) {
                val id = resultSet.getInt("id")
                val descripcion = resultSet.getString("descripcion")
                list.add(Pair(id, descripcion))
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun getTiposPartidos(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()

        try {
            val conn = DBConnection.getConnection()
            val query = "SELECT id, descripcion FROM tipos_partidos"

            conn?.createStatement().use {
                val resultSet = it?.executeQuery(query)
                while(resultSet?.next() == true) {
                    val id = resultSet.getInt("id")
                    val descripcion = resultSet.getString("descripcion")
                    list.add(Pair(id, descripcion))
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return list
    }

    suspend fun getAllPendingInvitations(userId: Int): List<Invitacion> = withContext(Dispatchers.IO) {
        val invitaciones = mutableListOf<Invitacion>()
        var conn: Connection? = null

        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = false

            invitaciones.addAll(getPendingInvitationsPartidos(conn!!, userId))

            invitaciones.addAll(getPendingInvitationsEquipos(conn, userId))

            conn.commit()
        } catch (e: SQLException) {
            conn?.rollback()
            throw SQLException("Error al obtener las invitaciones pendientes. Detalles: ${e.message}")
        } finally {
            conn?.autoCommit = true
            conn?.close()
        }
        return@withContext invitaciones
    }

    private fun getPendingInvitationsPartidos(conn: Connection, userId: Int): List<Invitacion> {
        val invitaciones = mutableListOf<Invitacion>()
        val query = """
                    SELECT id,
                           fecha_invitacion,
                           organizador,
                           idPartido,
                           idCuenta,
                           idEstado
                    FROM invitaciones_partidos
                    WHERE idCuenta = ? AND idEstado = ?
                """.trimIndent()

        conn.prepareStatement(query).use { preparedStatement ->
            preparedStatement.setInt(1, userId)
            preparedStatement.setInt(2, PENDING)
            val resultSet = preparedStatement.executeQuery()
            while(resultSet != null && resultSet.next()) {
                invitaciones.add(
                    Invitacion(
                        id = resultSet.getInt("id"),
                        organizador = resultSet.getString("organizador"),
                        idPartido = resultSet.getInt("idPartido"),
                        idCuenta = resultSet.getInt("idCuenta"),
                        idEstado = resultSet.getInt("idEstado"),
                        fechaInvitacion = resultSet.getString("fecha_invitacion")
                    )
                )
            }
        }
        return invitaciones
    }

    private fun getPendingInvitationsEquipos(conn: Connection, userId: Int): List<Invitacion> {
        val invitaciones = mutableListOf<Invitacion>()
        val query = """
                    SELECT i.id AS id,
                           i.idEquipo AS idEquipo,
                           e.nombre AS equipo,
                           i.idEstado AS idEstado,
                           i.fecha_invitacion AS fecha_invitacion,
                           i.idCapitan AS idCapitan,
                           c.nombre AS capitan
                    FROM invitaciones_equipos i
                    LEFT JOIN equipos e ON i.idEquipo = e.id
                    LEFT JOIN cuentas c ON i.idCapitan = c.id
                    WHERE i.idCuenta = ? AND i.idEstado = ?
                """.trimIndent()

        conn.prepareStatement(query).use { preparedStatement ->
            preparedStatement.setInt(1, userId)
            preparedStatement.setInt(2, PENDING)
            val resultSet = preparedStatement.executeQuery()
            while(resultSet != null && resultSet.next()) {
                invitaciones.add(
                    Invitacion(
                        id = resultSet.getInt("id"),
                        idEquipo = resultSet.getInt("idEquipo"),
                        equipo = resultSet.getString("equipo"),
                        idCapitan = resultSet.getInt("idCapitan"),
                        capitan = resultSet.getString("capitan"),
                        idEstado = resultSet.getInt("idEstado"),
                        fechaInvitacion = resultSet.getString("fecha_invitacion")
                    )
                )
            }
        }
        return invitaciones
    }

    companion object InvitationStates {
        const val PENDING = 15
        const val ACCEPTED = 16
        const val REJECTED = 17
        const val EXPIRED = 18
    }
}
