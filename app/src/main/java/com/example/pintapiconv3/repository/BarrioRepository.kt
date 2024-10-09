package com.example.pintapiconv3.repository

import com.example.pintapiconv3.database.DBConnection

class BarrioRepository {

    fun getBarrios(): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        val query = "Select id, descripcion FROM barrios"

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

}