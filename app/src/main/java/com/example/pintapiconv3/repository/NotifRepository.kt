package com.example.pintapiconv3.repository

import android.util.Log
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.database.SQLServerHelper.InvitationStates.PENDING
import com.example.pintapiconv3.models.Invitacion
import java.sql.Connection
import java.sql.SQLException

class NotifRepository {

    suspend fun hasPendingNotifications(userId: Int): Boolean {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            conn?.autoCommit = true
            conn?.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED

            val query = """
                SELECT COUNT(*) AS count FROM invitaciones_partidos WITH (NOLOCK)
                WHERE idEstado = ? AND idCuenta = ?
            """.trimIndent()

            val query2 = """
                SELECT COUNT(*) AS count FROM invitaciones_equipos WITH (NOLOCK)
                WHERE idEstado = ? AND idCuenta = ?
            """.trimIndent()

            var totalPending = 0

            conn?.prepareStatement(query).use { preparedStatement ->
                preparedStatement?.setInt(1, PENDING)
                preparedStatement?.setInt(2, userId)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    totalPending += resultSet.getInt("count")
                }
            }

            conn?.prepareStatement(query2).use { preparedStatement ->
                preparedStatement?.setInt(1, PENDING)
                preparedStatement?.setInt(2, userId)
                val resultSet = preparedStatement?.executeQuery()
                if(resultSet?.next() == true) {
                    totalPending += resultSet.getInt("count")
                }
            }

            Log.d("NotifRepository", "Total pending notifications: $totalPending")
            return totalPending > 0
        } catch (e: SQLException) {
            throw SQLException("Error checking for pending notifications: ${e.message}")
        } finally {
            conn?.close()
        }
    }
}