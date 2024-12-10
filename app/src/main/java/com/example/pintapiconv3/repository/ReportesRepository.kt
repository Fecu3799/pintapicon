package com.example.pintapiconv3.repository

import com.example.pintapiconv3.adapter.DynamicReportItem
import com.example.pintapiconv3.database.DBConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

class ReportesRepository {

    suspend fun getReservas(fechaDesde: String? = null, fechaHasta: String? = null): List<DynamicReportItem> = withContext(Dispatchers.IO) {
        val reservas = mutableListOf<DynamicReportItem>()
        var conn: Connection? = null

        try {
            conn = DBConnection.getConnection()

            val query = buildString {
                append(
                    """
                        SELECT r.fecha_reserva, r.hora_inicio, r.hora_fin, r.monto, e.descripcion AS estado, p.nombre AS predio
                        FROM reservas r
                        JOIN estados e ON r.idEstado = e.id
                        JOIN predios p ON r.idPredio = p.id
                    """.trimIndent()
                )
                if(fechaDesde != null && fechaHasta != null) {
                    append(" WHERE r.fecha_reserva BETWEEN ? AND ?")
                }
                append(" ORDER BY r.fecha_reserva, r.hora_inicio")
            }

            conn?.prepareStatement(query).use { preparedStatement ->
                if(fechaDesde != null && fechaHasta != null) {
                    preparedStatement?.setString(1, fechaDesde)
                    preparedStatement?.setString(2, fechaHasta)
                }

                val resultSet = preparedStatement?.executeQuery()
                reservas.addAll(mapResultSet(resultSet!!))
            }
        } catch (e: SQLException) {
            throw SQLException("Error al obtener las reservas: ${e.message}")
        } finally {
            conn?.close()
        }
        return@withContext reservas
    }

    suspend fun getPartidos(fechaDesde: String? = null, fechaHasta: String? = null): List<DynamicReportItem> = withContext(Dispatchers.IO) {
        val partidos = mutableListOf<DynamicReportItem>()
        var conn: Connection? = null

        try {

            conn = DBConnection.getConnection()
            val query = buildString {
                append(
                    """
                        SELECT p.fecha, p.hora, e.descripcion AS estado, COUNT(part.idParticipante) AS participantes, tc.descripcion AS tipo_cancha
                        FROM partidos p
                        JOIN estados e ON p.idEstado = e.id
                        LEFT JOIN participantes part ON p.id = part.idPartido
                        JOIN canchas c ON p.idCancha = c.id
                        JOIN tipos_canchas tc ON c.idTipoCancha = tc.id
                    """.trimIndent()
                )
                if(fechaDesde != null && fechaHasta != null) {
                    append(" WHERE p.fecha BETWEEN ? AND ?")
                }
                append(" GROUP BY p.fecha, p.hora, e.descripcion, tc.descripcion")
                append(" ORDER BY p.fecha, p.hora")
            }

            conn?.prepareStatement(query).use { preparedStatement ->
                if(fechaDesde != null && fechaHasta != null) {
                    preparedStatement?.setString(1, fechaDesde)
                    preparedStatement?.setString(2, fechaHasta)
                }

                val resultSet = preparedStatement?.executeQuery()
                partidos.addAll(mapResultSet(resultSet!!))
            }

        } catch (e: SQLException) {
            throw SQLException("Error al obtener los partidos: ${e.message}")
        } finally {
            conn?.close()
        }
        return@withContext partidos
    }

    suspend fun getUsuarios(fechaDesde: String? = null, fechaHasta: String? = null): List<DynamicReportItem> = withContext(Dispatchers.IO) {
        val usuarios = mutableListOf<DynamicReportItem>()
        var conn: Connection? = null

        try {

            conn = DBConnection.getConnection()
            val query = """
                        SELECT nombre, apellido, numero_reservas, partidos_jugados, abandonos
                        FROM cuentas
                        ORDER BY nombre, apellido
                    """.trimIndent()

            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)
            usuarios.addAll(mapResultSet(resultSet!!))

        } catch (e: SQLException) {
            throw SQLException("Error al obtener los usuarios: ${e.message}")
        } finally {
            conn?.close()
        }

        return@withContext usuarios
    }


    private fun mapResultSet(resultSet: ResultSet): List<DynamicReportItem> {
        val results = mutableListOf<DynamicReportItem>()

        while(resultSet.next()) {
            val row = mutableListOf<String>()
            val metadata = resultSet.metaData
            for(i in 1..metadata.columnCount) {
                row.add(resultSet.getString(i) ?: "")
            }
            results.add(DynamicReportItem(row))
        }
        return results
    }

}