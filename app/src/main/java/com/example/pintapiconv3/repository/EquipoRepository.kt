package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Equipo
import com.example.pintapiconv3.models.Invitacion
import com.example.pintapiconv3.models.Miembro
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
        // AGREGAR AL CAPITAN A LA TABLA JUGADORES_EQUIPO
        var conn: Connection? = null
        val query1 = """
            INSERT INTO equipos (nombre, descripcion, fecha_creacion, idCapitan)
            OUTPUT INSERTED.id
            VALUES (?, ?, CURRENT_TIMESTAMP, ?)
        """.trimIndent()
        val query2 = "INSERT INTO jugadores_equipos (idCuenta, idEquipo) VALUES (?, ?)"
        var idEquipo = 0

        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = false

            conn?.prepareStatement(query1).use { preparedStatement ->
                preparedStatement?.setString(1, teamName)
                preparedStatement?.setString(2, teamDescription)
                preparedStatement?.setInt(3, userID)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    idEquipo = resultSet.getInt("id")
                } else {
                    throw SQLException("Error al crear el equipo (Repository). No se obtuvo el ID del equipo.")
                }
            }

            conn?.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setInt(1, userID)
                preparedStatement?.setInt(2, idEquipo)
                preparedStatement?.executeUpdate()
            }
            conn?.commit()
            return true
        } catch (e: SQLException) {
            conn?.rollback()
            throw SQLException("Error al crear el equipo (Repository). Detalles: ${e.message}")
        } finally {
            conn?.autoCommit = true
            conn?.close()
        }
    }

    private suspend fun getMemberId(email: String): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        var idUsuario = 0
        try {
            conn = DBConnection.getConnection()
            val query1 = "SELECT id FROM cuentas WHERE email = ?"
            conn?.prepareStatement(query1).use { preparedStatement ->
                preparedStatement?.setString(1, email)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true)
                    idUsuario = resultSet.getInt("id")
                else
                    throw SQLException("Usuario con email $email no encontrado")
                return@withContext idUsuario
            }
        } catch (e: SQLException) {
            throw SQLException("Error al obtener el ID del usuario. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun inviteUserToTeam(idEquipo: Int, idCapitan: Int, email: String): Boolean {
        return withContext(Dispatchers.IO) {
            var conn: Connection? = null
            try {
                conn = DBConnection.getConnection()

                val query = "INSERT INTO invitaciones_equipos (idEquipo, idCuenta, idEstado, fecha_invitacion, idCapitan) VALUES (?,?,?, CURRENT_TIMESTAMP, ?)"
                val idUsuario = getMemberId(email)

                conn?.prepareStatement(query).use { preparedStatement ->
                    preparedStatement?.setInt(1, idEquipo)
                    preparedStatement?.setInt(2, idUsuario)
                    preparedStatement?.setInt(3, PENDING)
                    preparedStatement?.setInt(4, idCapitan)
                    preparedStatement?.executeUpdate()
                }

                conn?.commit()
                return@withContext true
            } catch (e: SQLException) {
                conn?.rollback()
                throw SQLException("Error al invitar al usuario al equipo. Detalles: ${e.message}")
            } finally {
                try {
                    conn?.close()
                } catch (e: SQLException) {
                    e.printStackTrace()
                    throw SQLException("Error al cerrar la conexión. Detalles: ${e.message}")
                }
            }
        }
    }

    suspend fun getPendingInvitations(userId: Int): List<Invitacion> = withContext(Dispatchers.IO) {
        val invitaciones = mutableListOf<Invitacion>()
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
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
                            idCapitan = resultSet.getInt("idCapitan"),
                            capitan = resultSet.getString("capitan"),
                            idEstado = resultSet.getInt("idEstado"),
                            fechaInvitacion = resultSet.getString("fecha_invitacion")
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

    suspend fun acceptTeamInvitation(invitacionId: Int, idEquipo: Int, idCuenta: Int, nuevoEstado: Int) = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = false

            val query1 = "UPDATE invitaciones_equipos SET idEstado = ? WHERE id = ?"
            conn?.prepareStatement(query1).use { preparedStatement ->
                preparedStatement?.setInt(1, nuevoEstado)
                preparedStatement?.setInt(2, invitacionId)
                preparedStatement?.executeUpdate()
            }

            val query2 = "INSERT INTO jugadores_equipos (idCuenta, idEquipo) VALUES (?, ?)"
            conn?.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setInt(1, idCuenta)
                preparedStatement?.setInt(2, idEquipo)
                preparedStatement?.executeUpdate()
            }

            conn?.commit()
        } catch (e: SQLException) {
            conn?.rollback()
            throw SQLException("Error al actualizar el estado de la invitación y agregar usuario al equipo. Detalles: ${e.message}")
        } finally {
            conn?.autoCommit = true
            conn?.close()
        }
    }


    suspend fun getTeamByUserId(userId: Int, capitan: String): Equipo? = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT id, nombre, descripcion FROM equipos WHERE idCapitan = ?
            """.trimIndent()
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, userId)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    val id = resultSet.getInt("id")
                    val nombre = resultSet.getString("nombre")
                    val descripcion = resultSet.getString("descripcion")

                    val miembros = getMembersByTeamId(id)

                    return@withContext Equipo(id, nombre, descripcion, capitan, miembros)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Error al obtener el equipo del usuario. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
        return@withContext null
    }

    private suspend fun getMembersByTeamId(teamId: Int): List<Miembro> {
        val miembros = mutableListOf<Miembro>()
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT c.id AS id,
                       c.nombre AS nombre,
                       h.descripcion AS habilidad,
                       p.descripcion AS posicion
                FROM cuentas c
                LEFT JOIN habilidades h ON c.idHabilidad = h.id
                LEFT JOIN posiciones p ON c.idPosicion = p.id
                LEFT JOIN jugadores_equipos j ON c.id = j.idCuenta
                WHERE j.idEquipo = ?
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, teamId)
                val resultSet = preparedStatement?.executeQuery()
                while(resultSet != null && resultSet.next()) {
                    val id = resultSet.getInt("id")
                    val nombre = resultSet.getString("nombre")
                    val habilidad = resultSet.getString("habilidad")
                    val posicion = resultSet.getString("posicion")

                    miembros.add(Miembro(id, nombre, habilidad, posicion))
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            throw SQLException("Error al obtener los miembros del equipo. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
        return miembros
    }

    suspend fun deleteMember(miembroId: Int): Boolean = withContext(Dispatchers.IO){
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "DELETE FROM jugadores_equipos WHERE idCuenta = ?"

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, miembroId)
                val rowsAffected = preparedStatement?.executeUpdate()
                if(rowsAffected != null && rowsAffected > 0) {
                    return@withContext true
                } else {
                    return@withContext false
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al eliminar el miembro del equipo. Detalles: ${e.message}")
        } finally {
            try {
                conn?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    suspend fun isMember(teamId: Int, email: String): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            val query = "SELECT COUNT(*) FROM jugadores_equipos WHERE idEquipo = ? AND idCuenta = ?"
            val userId = getMemberId(email)
            conn = DBConnection.getConnection()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, teamId)
                preparedStatement?.setInt(2, userId)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    return@withContext resultSet.getInt(1) > 0
                }
            }
        }catch (e: SQLException) {
            throw SQLException("Error al verificar si el usuario es miembro del equipo. Detalles: ${e.message}")
        }
        return@withContext false
    }

    companion object {
        const val PENDING = 15
        const val ACCEPTED = 16
        const val REJECTED = 17
        const val EXPIRED = 18
    }
}