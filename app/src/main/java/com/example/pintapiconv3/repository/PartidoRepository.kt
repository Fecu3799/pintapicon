package com.example.pintapiconv3.repository

import android.util.Log
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.database.SQLServerHelper.InvitationStates.ACCEPTED
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Miembro
import com.example.pintapiconv3.models.Participante
import com.example.pintapiconv3.models.Partido
import com.example.pintapiconv3.models.Reserva
import com.example.pintapiconv3.utils.Const
import com.example.pintapiconv3.utils.Const.MatchStatus.CONFIRMED
import com.example.pintapiconv3.utils.Const.MatchStatus.IN_COURSE
import com.example.pintapiconv3.utils.Const.MatchStatus.PENDING
import com.example.pintapiconv3.utils.Const.PaymentStatus.PAID
import com.example.pintapiconv3.utils.Const.PaymentStatus.PENDING_PAYMENT
import com.example.pintapiconv3.utils.Const.ReservationStatus.CANCELED
import com.example.pintapiconv3.utils.Const.ReservationStatus.FINISHED
import com.example.pintapiconv3.utils.Const.ReservationStatus.PAID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException

class PartidoRepository {

    private val equipoRepository = EquipoRepository()


    suspend fun isParticipantInActiveMatch(userId: Int): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT COUNT(*) FROM participantes p
                JOIN partidos pa ON p.idPartido = pa.id
                WHERE p.idParticipante = ?
                AND pa.idEstado IN (?, ?, ?)
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, userId)
                preparedStatement?.setInt(2, PENDING)
                preparedStatement?.setInt(3, CONFIRMED)
                preparedStatement?.setInt(4, IN_COURSE)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true)
                    return@withContext resultSet.getInt(1) > 0
            }
        } catch (e: SQLException) {
            throw SQLException("Error al verificar si el usuario es participante en un partido activo: ${e.message}")
        } finally {
            conn?.close()
        }
        return@withContext false
    }

    suspend fun crearPartidoConReserva(partido: Partido, reserva: Reserva): Int = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = false

            val query1 = """
                INSERT INTO partidos (fecha, hora, isPublic, idOrganizador, idCancha, idTipoPartido, idEstado)
                OUTPUT INSERTED.id
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val query2 = """
                INSERT INTO reservas (fecha_reserva, hora_inicio, hora_fin, monto, idMetodoPago, idEstado, idPredio, idPartido, idCancha)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val query3 = """
                INSERT INTO participantes (isOrganizador, montoPagado, montoRestante, idParticipante, idPartido, idEstado)
                    VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val query4 = """
                UPDATE cuentas SET numero_reservas = numero_reservas + 1 WHERE id = ?
            """.trimIndent()


            var partidoId = 0
            conn?.prepareStatement(query1).use { preparedStatement ->
                preparedStatement?.setString(1, partido.fecha)
                preparedStatement?.setString(2, partido.hora)
                preparedStatement?.setBoolean(3, partido.isPublic)
                preparedStatement?.setInt(4, partido.idOrganizador)
                preparedStatement?.setInt(5, partido.idCancha)
                preparedStatement?.setInt(6, partido.idTipoPartido)
                preparedStatement?.setInt(7, partido.idEstado)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    partidoId = resultSet.getInt("id")
                } else {
                    throw SQLException("No se pudo obtener el ID del partido")
                }
            }

            reserva.idPartido = partidoId
            conn?.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setString(1, reserva.fecha)
                preparedStatement?.setString(2, reserva.horaInicio)
                preparedStatement?.setString(3, reserva.horaFin)
                preparedStatement?.setDouble(4, reserva.monto)
                preparedStatement?.setInt(5, reserva.idMetodoPago)
                preparedStatement?.setInt(6, reserva.idEstado)
                preparedStatement?.setInt(7, reserva.idPredio)
                preparedStatement?.setInt(8, reserva.idPartido)
                preparedStatement?.setInt(9, reserva.idCancha)
                preparedStatement?.executeUpdate()
            }

            conn?.prepareStatement(query3).use { preparedStatement ->
                preparedStatement?.setBoolean(1, true)
                preparedStatement?.setDouble(2, 0.0)
                preparedStatement?.setDouble(3, 0.0)
                preparedStatement?.setInt(4, partido.idOrganizador)
                preparedStatement?.setInt(5, reserva.idPartido)
                preparedStatement?.setInt(6, Const.PaymentStatus.PENDING_PAYMENT)
                preparedStatement?.executeUpdate()
            }

            conn?.prepareStatement(query4).use { preparedStatement ->
                preparedStatement?.setInt(1, partido.idOrganizador)
                preparedStatement?.executeUpdate()
            }

            conn?.commit()
            return@withContext partidoId
        } catch (e: SQLException) {
            conn?.rollback()
            throw SQLException("Error al crear el partido con reserva: ${e.message}")
        } finally {
            conn?.autoCommit = true
            conn?.close()
        }
    }

    suspend fun getCanchasByTipoYHorario(idTipoCancha: Int, fechaReserva: String, horaInicio: String, horaFin: String): List<Cancha> = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        val canchasDisponibles = mutableListOf<Cancha>()

        try {
            conn = DBConnection.getConnection()

            val query = """
                SELECT c.*, t.descripcion AS tipoCancha, p.nombre AS nombrePredio,
                CASE 
                    WHEN EXISTS (
                        SELECT 1
                        FROM reservas r
                        WHERE r.idCancha = c.id
                        AND r.fecha_reserva = ?
                        AND (r.hora_inicio < ? AND ? < r.hora_fin)
                        AND r.idEstado IN (19, 20)
                    ) THEN 0
                    ELSE 1
                END AS disponible
                FROM canchas c
                LEFT JOIN tipos_canchas t ON c.idTipoCancha = t.id
                LEFT JOIN predios p ON c.idPredio = p.id
                WHERE c.idTipoCancha = ?
                ORDER BY disponible DESC, c.id
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setString(1, fechaReserva)
                preparedStatement?.setString(2, horaFin)
                preparedStatement?.setString(3, horaInicio)
                preparedStatement?.setInt(4, idTipoCancha)
                val resultSet = preparedStatement?.executeQuery()
                while(resultSet?.next() == true) {
                    val cancha = Cancha(
                        id = resultSet.getInt("id"),
                        idPredio = resultSet.getInt("idPredio"),
                        nombrePredio = resultSet.getString("nombrePredio"),
                        idTipoCancha = idTipoCancha,
                        tipoCancha = resultSet.getString("tipoCancha"),
                        nroCancha = "${resultSet.getInt("numero_cancha")}",
                        precioHora = resultSet.getDouble("precio_hora"),
                        disponibilidad = resultSet.getInt("disponible") == 1
                    )
                    canchasDisponibles.add(cancha)
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al obtener las canchas disponibles: ${e.message}")
        } finally {
            conn?.close()
        }
        return@withContext canchasDisponibles
    }

    suspend fun insertParticipante(idPartido: Int, idParticipante: Int, isOrganizador: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                INSERT INTO participantes (isOrganizador, montoPagado, montoRestante, idParticipante, idPartido, idEstado)
                    VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setBoolean(1, isOrganizador)
                preparedStatement?.setDouble(2, 0.0)
                preparedStatement?.setDouble(3, 0.0)
                preparedStatement?.setInt(4, idParticipante)
                preparedStatement?.setInt(5, idPartido)
                preparedStatement?.setInt(6, PENDING_PAYMENT)
                val resultSet = preparedStatement?.executeUpdate()
                return@withContext resultSet != 0
            }
        } catch (e: SQLException) {
            throw SQLException("Error al insertar el participante en el partido: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun sendInvitation(idPartido: Int, organizador: String, idParticipante: Int): Boolean = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "INSERT INTO invitaciones_partidos (fecha_invitacion, organizador, idPartido, idCuenta, idEstado) VALUES (CURRENT_TIMESTAMP, ?,?,?,?)"

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setString(1, organizador)
                preparedStatement?.setInt(2, idPartido)
                preparedStatement?.setInt(3, idParticipante)
                preparedStatement?.setInt(4, SQLServerHelper.PENDING)
                preparedStatement?.executeUpdate()
            }
            return@withContext true
        } catch (e: SQLException) {
            throw SQLException("Error al invitar al usuario al equipo. Detalles: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun sendInvitationByEmail(idPartido: Int, organizador: String, email: String): Boolean {
        return withContext(Dispatchers.IO) {
            var conn: Connection? = null
            try {
                conn = DBConnection.getConnection()
                conn?.autoCommit = false

                val query = "INSERT INTO invitaciones_partidos (fecha_invitacion, organizador, idPartido, idCuenta, idEstado) VALUES (CURRENT_TIMESTAMP, ?,?,?,?)"
                val idUsuario = equipoRepository.getMemberId(conn!!, email)

                conn.prepareStatement(query).use { preparedStatement ->
                    preparedStatement?.setString(1, organizador)
                    preparedStatement?.setInt(2, idPartido)
                    preparedStatement?.setInt(3, idUsuario)
                    preparedStatement?.setInt(4, SQLServerHelper.PENDING)
                    preparedStatement?.executeUpdate()
                }

                conn.commit()
                return@withContext true
            } catch (e: SQLException) {
                conn?.rollback()
                throw SQLException("Error al invitar al usuario al partido. Detalles: ${e.message}")
            } finally {
                conn?.autoCommit = true
                conn?.close()
            }
        }
    }



    suspend fun respondMatchInvitation(
        invitacionId: Int,
        idPartido: Int,
        idCuenta: Int,
        nuevoEstado: Int
    ) = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = false

            val query1 = "UPDATE invitaciones_partidos SET idEstado = ? WHERE id = ?"
            conn?.prepareStatement(query1).use { preparedStatement ->
                preparedStatement?.setInt(1, nuevoEstado)
                preparedStatement?.setInt(2, invitacionId)
                preparedStatement?.executeUpdate()
            }


            if(nuevoEstado == ACCEPTED) {
                val query2 = """
                    SELECT r.monto, c.idTipoCancha
                    FROM reservas r
                    LEFT JOIN partidos p ON r.idPartido = p.id
                    LEFT JOIN canchas c ON p.idCancha = c.id
                    WHERE r.idPartido = ?
                """.trimIndent()
                var montoPorPersona = 0.0
                conn?.prepareStatement(query2).use {
                    it?.setInt(1, idPartido)
                    val rs = it?.executeQuery()
                    if(rs?.next() == true) {
                        val montoTotal = rs.getDouble("monto")
                        val idTipoCancha = rs.getInt("idTipoCancha")
                        val cantParticipantes = when (idTipoCancha) {
                            1 -> 10
                            2 -> 14
                            3 -> 16
                            else -> 22
                        }
                        montoPorPersona = montoTotal / cantParticipantes
                    } else {
                        throw SQLException("No se encontró la reserva del partido")
                    }
                }

                val query3 = """
                INSERT INTO participantes (isOrganizador, montoPagado, montoRestante, idParticipante, idPartido, idEstado) 
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent()
                conn?.prepareStatement(query3).use { preparedStatement ->
                    preparedStatement?.setBoolean(1, false)
                    preparedStatement?.setDouble(2, 0.0)
                    preparedStatement?.setDouble(3, montoPorPersona)
                    preparedStatement?.setInt(4, idCuenta)
                    preparedStatement?.setInt(5, idPartido)
                    preparedStatement?.setInt(6, PENDING_PAYMENT)
                    preparedStatement?.executeUpdate()
                }
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

    suspend fun getPartidoById(partidoId: Int): Partido {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "SELECT * FROM partidos WHERE id = ?"

            var partido: Partido? = null

            conn?.prepareStatement(query).use {
                it?.setInt(1, partidoId)
                val resultSet = it?.executeQuery()
                if(resultSet?.next() == true) {
                    partido = Partido (
                        id = resultSet.getInt("id"),
                        isPublic = resultSet.getInt("isPublic") == 1,
                        fecha = resultSet.getString("fecha"),
                        hora = resultSet.getString("hora"),
                        idOrganizador = resultSet.getInt("idOrganizador"),
                        idCancha = resultSet.getInt("idCancha"),
                        idTipoPartido = resultSet.getInt("idTipoPartido"),
                        idEstado = resultSet.getInt("idEstado")
                    )
                } else
                    throw SQLException("No se encontró el partido con ID: $partidoId")
            }
            return partido!!
        } catch (e: SQLException) {
            throw SQLException("Error al obtener el partido por ID: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun getReservaByPartidoId(partidoId: Int): Reserva {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT r.*, p.nombre AS predio, p.url_google_maps AS ubicacion
                FROM reservas r
                LEFT JOIN predios p ON r.idPredio = p.id
                WHERE r.idPartido = ?
            """.trimIndent()

            var reserva: Reserva? = null

            conn?.prepareStatement(query).use {
                it?.setInt(1, partidoId)
                val resultSet = it?.executeQuery()
                if(resultSet?.next() == true) {
                    reserva = Reserva(
                        id = resultSet.getInt("id"),
                        fecha = resultSet.getString("fecha_reserva"),
                        horaInicio = resultSet.getString("hora_inicio"),
                        horaFin = resultSet.getString("hora_fin"),
                        monto = resultSet.getDouble("monto"),
                        idMetodoPago = resultSet.getInt("idMetodoPago"),
                        idEstado = resultSet.getInt("idEstado"),
                        idPredio = resultSet.getInt("idPredio"),
                        predio = resultSet.getString("predio"),
                        ubicacion = resultSet.getString("ubicacion"),
                        idPartido = resultSet.getInt("idPartido"),
                        idCancha = resultSet.getInt("idCancha")
                    )
                } else
                    throw SQLException("No se encontró la reserva con ID del partido: $partidoId")
            }
            return reserva!!
        } catch (e: SQLException) {
            throw SQLException("Error al obtener la reserva por ID del partido: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun getParticipantesByPartidoId(partidoId: Int): List<Participante> {
        var conn: Connection? = null
        val participantes = mutableListOf<Participante>()
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT p.id AS idParticipante,
                       c.nombre,
                       po.descripcion AS posicion,
                       p.isOrganizador,
                       p.montoPagado,
                       p.montoRestante,
                       p.idParticipante AS idCuenta,
                       p.idEstado
                FROM participantes p
                LEFT JOIN cuentas c ON p.idParticipante = c.id
                LEFT JOIN posiciones po ON c.idPosicion = po.id
                WHERE p.idPartido = ?
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, partidoId)
                val resultSet = preparedStatement?.executeQuery()
                while(resultSet?.next() == true) {
                    participantes.add(
                        Participante (
                            id = resultSet.getInt("idParticipante"),
                            idParticipante = resultSet.getInt("idCuenta"),
                            nombre = resultSet.getString("nombre"),
                            posicion = resultSet.getString("posicion"),
                            isOrganizador = resultSet.getInt("isOrganizador") == 1,
                            montoPagado = resultSet.getDouble("montoPagado").takeIf { !resultSet.wasNull() },
                            montoRestante = resultSet.getDouble("montoRestante").takeIf { !resultSet.wasNull() },
                            idEstado = resultSet.getInt("idEstado")
                        )
                    )
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al obtener los participantes por ID del partido: ${e.message}")
        } finally {
            conn?.close()
        }
        return participantes
    }

    suspend fun getCanchaByPartido(canchaId: Int): Cancha = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT c.*, t.descripcion AS tipoCancha
                FROM canchas c
                LEFT JOIN tipos_canchas t ON c.idTipoCancha = t.id
                WHERE c.id = ?
            """.trimIndent()

            var cancha: Cancha? = null

            conn?.prepareStatement(query).use {
                it?.setInt(1, canchaId)
                val resultSet = it?.executeQuery()
                if(resultSet?.next() == true) {
                     cancha = Cancha(
                        id = resultSet.getInt("id"),
                        idPredio = resultSet.getInt("idPredio"),
                        idTipoCancha = resultSet.getInt("idTipoCancha"),
                        tipoCancha = resultSet.getString("tipoCancha"),
                        nroCancha = resultSet.getString("numero_cancha"),
                        precioHora = resultSet.getDouble("precio_hora"),
                        disponibilidad = resultSet.getInt("disponibilidad") == 1
                    )
                } else {
                    throw SQLException("No se encontró la cancha con ID: $canchaId")
                }
            }
            return@withContext cancha!!
        } catch (e: SQLException) {
            throw SQLException("Error al obtener la cancha por ID: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun getMiembrosByCapitan(capitanId: Int): List<Miembro> {
        val miembros = mutableListOf<Miembro>()
        val conn: Connection?
        try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT c.id AS id,
                       c.nombre AS nombre,
                       h.descripcion AS habilidad,
                       p.descripcion AS posicion
                FROM equipos e
                LEFT JOIN jugadores_equipos je ON e.id = je.idEquipo
                LEFT JOIN cuentas c ON je.idCuenta = c.id
                LEFT JOIN habilidades h ON c.idHabilidad = h.id
                LEFT JOIN posiciones p ON c.idPosicion = p.id
                WHERE e.idCapitan = ?
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, capitanId)
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
        }
        return miembros
    }

    suspend fun updateMatchStatus(partidoId: Int, reservaId: Int, newStatus: Int) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()


            val query = "UPDATE partidos SET idEstado = ? WHERE id = ?"
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, newStatus)
                preparedStatement?.setInt(2, partidoId)
                preparedStatement?.executeUpdate()
            }

            val reservaStatus = when (newStatus) {
                Const.MatchStatus.CONFIRMED -> Const.ReservationStatus.PAID
                Const.MatchStatus.FINISHED -> Const.ReservationStatus.FINISHED
                Const.MatchStatus.CANCELED -> Const.ReservationStatus.CANCELED
                Const.MatchStatus.SUSPENDED -> Const.ReservationStatus.CANCELED
                else -> throw IllegalArgumentException("Invalid new status")
            }

            Log.d("updateMatchStatus", "Estado de reserva: $reservaStatus")
            val query2 = "UPDATE reservas SET idEstado = ? WHERE id = ?"
            conn?.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setInt(1, reservaStatus)
                preparedStatement?.setInt(2, reservaId)
                preparedStatement?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar el estado del partido: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun updateMatchesPlayed(userId: Int) {
        var conn: Connection? = null
        try {
            val query = "UPDATE cuentas SET partidos_jugados = partidos_jugados + 1 WHERE id = ?"
            conn = DBConnection.getConnection()
            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, userId)
                preparedStatement?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar partidos jugados: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun addFundsToParticipant(partidoId: Int, userId: Int, amount: Double, amountPerPerson: Double) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()

            val query = """
                UPDATE participantes
                SET montoPagado = montoPagado + ?,
                    montoRestante = montoRestante - ?
                WHERE idPartido = ? AND idParticipante = ?
            """.trimIndent()

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setDouble(1, amount)
                if(amount > amountPerPerson) preparedStatement?.setDouble(2, amountPerPerson)
                else preparedStatement?.setDouble(2, amount)
                preparedStatement?.setInt(3, partidoId)
                preparedStatement?.setInt(4, userId)
                preparedStatement?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al agregar fondos al participante: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun updateParticipantFunds(partidoId: Int, userId: Int, amount: Double) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
            UPDATE participantes
            SET montoPagado = ?
            WHERE idPartido = ? AND idParticipante = ?
        """.trimIndent()
            conn?.prepareStatement(query).use { stmt ->
                stmt?.setDouble(1, amount)
                stmt?.setInt(2, partidoId)
                stmt?.setInt(3, userId)
                stmt?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar los fondos del participante: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun removeParticipant(partidoId: Int, userId: Int, abandono: Boolean) = withContext(Dispatchers.IO) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()

            val query1 = """
                DELETE FROM participantes
                WHERE idPartido = ? AND idParticipante = ?
            """.trimIndent()

            conn?.prepareStatement(query1).use { ps ->
                ps?.setInt(1, partidoId)
                ps?.setInt(2, userId)
                ps?.executeUpdate()
            }

            if(abandono) {
                val query2 = """
                    UPDATE cuentas 
                    SET abandonos = abandonos + 1
                    WHERE id = ?
                """.trimIndent()
                conn?.prepareStatement(query2).use { ps ->
                    ps?.setInt(1, userId)
                    ps?.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al eliminar al participante: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    fun esParticipanteDelPartido(partidoId: Int, userId: Int): Boolean {
        var conn: Connection? = null
        return try {
            conn = DBConnection.getConnection()
            val query = """
                SELECT COUNT(*) FROM participantes
                WHERE idPartido = ? AND idParticipante = ?
            """.trimIndent()
            conn?.prepareStatement(query).use { stmt ->
                stmt?.setInt(1, partidoId)
                stmt?.setInt(2, userId)
                val rs = stmt?.executeQuery()
                if (rs?.next() == true) {
                    rs.getInt(1) > 0
                } else {
                    false
                }
            }
        } catch (e: SQLException) {
            throw SQLException("Error al verificar si el usuario es participante del partido: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            conn?.close()
        }
    }

    suspend fun updateReservationStatus(partidoId: Int, newStatus: Int) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "UPDATE reservas SET idEstado = ? WHERE idPartido = ?"

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, newStatus)
                preparedStatement?.setInt(2, partidoId)
                preparedStatement?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar el estado de la reserva: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    suspend fun updateParticipantStatus(partidoId: Int, userId: Int, newStatus: Int) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = """
            UPDATE participantes
            SET idEstado = ?
            WHERE idPartido = ? AND idParticipante = ?
        """.trimIndent()
            conn?.prepareStatement(query).use { stmt ->
                stmt?.setInt(1, newStatus)
                stmt?.setInt(2, partidoId)
                stmt?.setInt(3, userId)
                stmt?.executeUpdate()
            }
        } catch (e: SQLException) {
            throw SQLException("Error al actualizar los fondos del participante: ${e.message}")
        } finally {
            conn?.close()
        }
    }
}