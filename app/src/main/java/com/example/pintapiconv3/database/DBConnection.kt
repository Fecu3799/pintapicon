package com.example.pintapiconv3.database

import android.os.StrictMode
import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object DBConnection {
    private const val IP = "192.168.0.102:1433"
    private const val DB = "pintapiconDBv4"
    private const val USERNAME = "facu3799"
    private const val PASSWORD = "3799Fecusql"

    private var connection: Connection? = null

    // Método para obtener la conexión
    fun getConnection(): Connection? {
        if (connection == null || connection!!.isClosed) {
            connection = createConnection()
        }
        return connection
    }

    // Método privado para crear la conexión
    private fun createConnection(): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        Log.d("DatabaseConnection", "Attempting to establish a connection")
        return try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
            val connString = "jdbc:jtds:sqlserver://$IP;databaseName=$DB;user=$USERNAME;password=$PASSWORD"
            DriverManager.getConnection(connString).also {
                Log.d("DatabaseConnection", "Connection established successfully")
            }
        } catch (ex: SQLException) {
            Log.e("DatabaseConnection Error", "SQL Exception: ${ex.message}")
            null
        } catch (ex: ClassNotFoundException) {
            Log.e("DatabaseConnection Error", "Class Not Found Exception: ${ex.message}")
            null
        } catch (ex: Exception) {
            Log.e("DatabaseConnection Error", "Exception: ${ex.message}")
            null
        }
    }

    // Método para cerrar la conexión
    fun closeConnection() {
        try {
            connection?.close()
            Log.d("DatabaseConnection", "Connection closed")
        } catch (ex: SQLException) {
            Log.e("DatabaseConnection Error", "Error closing connection: ${ex.message}")
        }
    }
}