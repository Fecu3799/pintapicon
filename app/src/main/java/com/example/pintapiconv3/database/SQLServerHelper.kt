package com.example.pintapiconv3.database

import com.example.pintapiconv3.models.Direccion
import java.sql.Connection

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
