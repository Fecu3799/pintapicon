package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection
import java.sql.Connection

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



}