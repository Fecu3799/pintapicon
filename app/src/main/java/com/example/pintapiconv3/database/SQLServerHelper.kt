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

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }


    fun insertDireccion(calle: String, numero: Int, idBarrio: Int): Int? {
        val query = """
            INSERT INTO direcciones (calle, numero, idBarrio) 
            OUTPUT INSERTED.id
            VALUES (?, ?, ?)
        """

        try {
            val conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setString(1, calle)
            preparedStatement?.setInt(2, numero)
            preparedStatement?.setInt(3, idBarrio)

            val resultSet = preparedStatement?.executeQuery()
            resultSet?.next()
            val idDireccion = resultSet?.getInt("id")

            resultSet?.close()
            preparedStatement?.close()
            conn?.close()

            return idDireccion
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    fun insertDireccionWithConnection(conn: Connection, direccion: Direccion): Int? {
        val query = """
            INSERT INTO direcciones (calle, numero, idBarrio) 
            OUTPUT INSERTED.id
            VALUES (?, ?, ?)
        """

        try {
            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setString(1, direccion.calle)
            preparedStatement?.setInt(2, direccion.numero)
            preparedStatement?.setInt(3, direccion.idBarrio)

            val resultSet = preparedStatement?.executeQuery()
            resultSet?.next()
            val idDireccion = resultSet?.getInt("id")

            resultSet?.close()
            preparedStatement?.close()

            return idDireccion
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
