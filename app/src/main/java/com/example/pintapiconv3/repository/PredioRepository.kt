package com.example.pintapiconv3.repository

import com.example.pintapiconv3.adapter.Horario
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.Cancha
import com.example.pintapiconv3.models.Direccion
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

    suspend fun insertPredioWithConnection(conn: Connection, predio: Predio): Int {
        var generatedId = 0

        try {

            val query = """
                INSERT INTO predios (nombre, telefono, idDireccion, idEstado, latitud, longitud, url_google_maps)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()

            val preparedStatement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

            preparedStatement?.setString(1, predio.nombre)
            preparedStatement?.setString(2, predio.telefono)
            preparedStatement?.setInt(3, predio.idDireccion)
            preparedStatement?.setInt(4, predio.idEstado)
            if(predio.latitud != null && predio.longitud != null) {
                preparedStatement?.setDouble(5, predio.latitud!!)
                preparedStatement?.setDouble(6, predio.longitud!!)
            } else {
                preparedStatement?.setNull(5, Types.DOUBLE)
                preparedStatement?.setNull(6, Types.DOUBLE)
            }
            if(predio.url_google_maps != null) {
                preparedStatement?.setString(7, predio.url_google_maps)
            } else {
                preparedStatement?.setNull(7, Types.VARCHAR)
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

    suspend fun insertCanchaWithConnection(conn: Connection, cancha: Cancha): Boolean {
        val query = """
                INSERT INTO predios_tipos_canchas (idPredio, idTipoCancha, precio_hora)
                VALUES (?, ?, ?)
            """.trimIndent()

        return try {

            val preparedStatement = conn.prepareStatement(query)

            preparedStatement?.setInt(1, cancha.idPredio)
            preparedStatement?.setInt(2, cancha.idTipoCancha)
            preparedStatement?.setDouble(3, cancha.precioHora)

            val rowsInserted = preparedStatement?.executeUpdate() ?: 0

            preparedStatement?.close()

            rowsInserted > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            false
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

    suspend fun getAllPredios(): List<Predio> {
        val prediosList = mutableListOf<Predio>()
        var conn: Connection? = null
        val query = """
            SELECT p.id, p.nombre, p.telefono, p.idDireccion, p.idEstado, d.calle, d.numero, d.idBarrio, b.descripcion
            FROM predios p
            LEFT JOIN direcciones d ON p.idDireccion = d.id
            LEFT JOIN barrios b ON d.idBarrio = b.id
        """.trimIndent()

        try {
            conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            val resultSet = preparedStatement?.executeQuery()

            while(resultSet?.next() == true) {
                val predio = Predio (
                    id = resultSet.getInt("id"),
                    nombre = resultSet.getString("nombre"),
                    telefono = resultSet.getString("telefono"),
                    idDireccion = resultSet.getInt("idDireccion"),
                    idEstado = resultSet.getInt("idEstado"),
                    url_google_maps = null,
                    latitud = null,
                    longitud = null
                )

                val direccion = Direccion (
                    id = resultSet.getInt("idDireccion"),
                    calle = resultSet.getString("calle"),
                    numero = resultSet.getInt("numero"),
                    idBarrio = resultSet.getInt("idBarrio")
                )

                val canchas = getCanchasByPredio(predio.id)
                predio.canchas = canchas

                prediosList.add(predio)
            }

            preparedStatement?.close()
            resultSet?.close()
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
        val query = """
            SELECT c.idPredio, c.idTipoCancha, c.precio_hora, c.disponibilidad, t.descripcion
            FROM predios_tipos_canchas c
            LEFT JOIN tipos_canchas t ON c.idTipoCancha = t.id
            WHERE c.idPredio = ?
        """.trimIndent()

        try {
            conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setInt(1, id)
            val resultSet = preparedStatement?.executeQuery()

            while(resultSet?.next() == true) {
                val idPredio = resultSet.getInt("idPredio")
                val idTipoCancha = resultSet.getInt("idTipoCancha")
                val tipoCancha = resultSet.getString("descripcion")
                val precioHora = resultSet.getDouble("precio_hora")
                val disponibilidad = resultSet.getBoolean("disponibilidad")

                val cancha = Cancha (
                    idPredio = idPredio,
                    idTipoCancha = idTipoCancha,
                    tipoCancha = tipoCancha,
                    precioHora = precioHora,
                    disponibilidad = disponibilidad
                )

                canchasList.add(cancha)
            }

            resultSet?.close()
            preparedStatement?.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return@withContext canchasList
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


    suspend fun updatePredio(predio: Predio): Boolean {
        var conn: Connection? = null
        var isSuccess = false

        try {

            conn = DBConnection.getConnection()
            val query = """
                UPDATE predios
                SET nombre = ?, telefono = ?, idEstado = ?, latitud = ?, longitud = ?, url_google_maps = ?
                WHERE id = ?
            """.trimIndent()

            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setString(1, predio.nombre)
            preparedStatement?.setString(2, predio.telefono)
            preparedStatement?.setInt(3, predio.idEstado)
            if(predio.latitud != null && predio.longitud != null) {
                preparedStatement?.setDouble(4, predio.latitud!!)
                preparedStatement?.setDouble(5, predio.longitud!!)
            } else {
                preparedStatement?.setNull(4, Types.DOUBLE)
                preparedStatement?.setNull(5, Types.DOUBLE)
            }
            if(predio.url_google_maps != null) {
                preparedStatement?.setString(6, predio.url_google_maps)
            } else {
                preparedStatement?.setNull(6, Types.VARCHAR)
            }
            preparedStatement?.setInt(7, predio.id)

            val rowsAffected = preparedStatement?.executeUpdate() ?: 0
            isSuccess = rowsAffected > 0

            preparedStatement?.close()

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
        return isSuccess
    }


    suspend fun updateDireccion(direccion: Direccion) : Boolean {
        var conn: Connection? = null
        var isSuccess = false

        try {

            conn = DBConnection.getConnection()
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
            conn?.close()
        }
        return isSuccess
    }

    companion object {
        const val OPEN = 5
        const val CLOSED = 6
        const val OUT_OF_SERVICE = 7
        const val ELIMINATED = 14
    }

}