package com.example.pintapiconv3.repository

import androidx.compose.runtime.resetSourceInfo
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Invitacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class EquipoRepository {

    suspend fun hasTeam(userId: Int): Boolean {
            var conn: Connection? = null
            try {
                conn = DBConnection.getConnection()
                val query = "SELECT COUNT(*) FROM equipos WHERE idCapitan = ?"
                conn?.prepareStatement(query).use { preparedStatement ->
                    preparedStatement?.setInt(1, userId)
                    val resultSet: ResultSet? = preparedStatement?.executeQuery()
                    if(resultSet != null && resultSet.next())
                        return resultSet.getInt(1) > 0
                }
            } catch (e: SQLException) {
                throw SQLException("Error al verificar equipo del usuario $userId. Detalles: ${e.message}")
            } finally {
                conn?.close()
            }
            return false
        }

    suspend fun createTeam(userID: Int, teamName: String, teamDescription: String): Boolean {
        var conn: Connection? = null
        val query = """
            INSERT INTO equipos (nombre, descripcion, fecha_creacion, idCapitan)
            VALUES (?, ?, CURRENT_TIMESTAMP, ?)
        """.trimIndent()

        try {
            conn = DBConnection.getConnection()
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setString(1, teamName)
                preparedStatement?.setString(2, teamDescription)
                preparedStatement?.setInt(3, userID)
                val rowsAffected = preparedStatement?.executeUpdate()
                if(rowsAffected != null && rowsAffected > 0)
                    return true
            }
        } catch (e: SQLException) {
            throw SQLException("Error al crear el equipo (Repository). Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
        return false
    }

    suspend fun inviteUserToTeam(idEquipo: Int, emailUsuario: String): Boolean {
        return withContext(Dispatchers.IO) {
            var conn: Connection? = null
            try {
                conn = DBConnection.getConnection()
                val query1 = "SELECT id FROM cuentas WHERE email = ?"
                val query2 = "INSERT INTO invitaciones_equipos (idEquipo, idCuenta, idEstado) VALUES (?,?,?)"
                var idUsuario: Int? = null

                conn?.prepareStatement(query1).use { preparedStatement ->
                    preparedStatement?.setString(1, emailUsuario)
                    val resultSet = preparedStatement?.executeQuery()
                    if(resultSet?.next() == true) {
                        idUsuario = resultSet.getInt("id")
                    }
                }

                if(idUsuario != null) {
                    conn?.prepareStatement(query2).use { preparedStatement ->
                        preparedStatement?.setInt(1, idEquipo)
                        preparedStatement?.setInt(2, idUsuario!!)
                        preparedStatement?.setInt(3, PENDING)
                        preparedStatement?.executeUpdate()
                    }
                    return@withContext true
                } else {
                    throw SQLException("Usuario con email $emailUsuario no encontrado")
                }
            } catch (e: SQLException) {
                throw SQLException("Error al invitar al usuario al equipo. Detalles: ${e.message}")
            } finally {
                conn?.close()
            }
        }
    }


    suspend fun getInvitacionesPendientes(userId: Int): List<Invitacion> = withContext(Dispatchers.IO) {
        val invitaciones = mutableListOf<Invitacion>()
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                    SELECT i.id AS id,
                           i.idEquipo AS idEquipo,
                           e.nombre AS equipo,
                           c.nombre AS capitan,
                           i.idEstado AS idEstado,
                           i.fecha_invitacion AS fecha_invitacion
                    FROM invitaciones_equipos i
                    LEFT JOIN equipos e ON i.idEquipo = e.id
                    LEFT JOIN cuentas c ON i.idCuenta = c.id
                    WHERE i.idCuenta = ? AND i.idEstado = ?
                """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, userId)
                preparedStatement?.setInt(2, PENDING)
                val resultSet = preparedStatement?.executeQuery()
                while(resultSet != null && resultSet.next()) {
                    invitaciones.add(
                        Invitacion(
                            id = resultSet.getInt("id"),
                            idEquipo = resultSet.getInt("idEquipo"),
                            equipo = resultSet.getString("equipo"),
                            idCapitan = userId,
                            capitan = resultSet.getString("capitan"),
                            idEstado = resultSet.getInt("idEstado"),
                            fechaInvitacion = resultSet.getString("fecha_invitaion")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al obtener las invitaciones pendientes. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
        invitaciones
    }

    suspend fun actualizarEstadoInvitacion(invitacionId: Int, nuevoEstado: Int) = withContext(Dispatchers.IO){
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "UPDATE invitaciones_equipos SET idEstado = ? WHERE id = ?"
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, nuevoEstado)
                preparedStatement?.setInt(2, invitacionId)
                preparedStatement?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar el estado de la invitación. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    companion object {
        const val PENDING = 15
        const val ACCEPTED = 16
        const val REJECTED = 17
        const val EXPIRED = 18
    }
}