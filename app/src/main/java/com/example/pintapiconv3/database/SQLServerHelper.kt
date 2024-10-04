package com.example.pintapiconv3.database

import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.Utils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SQLServerHelper {

    // Traer los barrios desde la base de datos
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


    fun getUsers(): List<User> {
        val userList = mutableListOf<User>()
        val query = """
            SELECT c.id as idCuenta,
               email,
               contraseña,
               nombre,
               apellido,
               fecha_nacimiento,
               telefono,
               idDireccion,
               d.calle AS calle,
               d.numero AS numero,
               d.idBarrio AS idBarrio,
               b.descripcion AS barrio,
               l.descripcion AS localidad,
               p.descripcion AS provincia,
               pa.descripcion AS pais,
               idEstado,
               idGenero,
               idHabilidad,
               idPosicion,
               isAdmin
            FROM cuentas c
            JOIN direcciones d ON c.idDireccion = d.id
            JOIN barrios b ON d.idBarrio = b.id
            JOIN localidades l ON b.idLocalidad = l.id
            JOIN provincias p ON l.idProvincia = p.id
            JOIN paises pa ON p.idPais = pa.id
            JOIN estados e ON c.idEstado = e.id
            JOIN generos g ON c.idGenero = g.id
            JOIN habilidades h ON c.idHabilidad = h.id
            JOIN posiciones po ON c.idPosicion = po.id
            """.trimIndent()

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            while(resultSet?.next() == true) {
                val id = resultSet.getInt("idCuenta")
                var user = User(
                    id = resultSet.getInt("idCuenta"),
                    email = resultSet.getString("email"),
                    password = resultSet.getString("contraseña"),
                    nombre = resultSet.getString("nombre"),
                    apellido = resultSet.getString("apellido"),
                    fechaNacimiento = resultSet.getString("fecha_nacimiento"),
                    telefono = resultSet.getString("telefono"),
                    idDireccion = resultSet.getInt("idDireccion"),
                    calle = resultSet.getString("calle"),
                    numero = resultSet.getInt("numero"),
                    idBarrio = resultSet.getInt("idBarrio"),
                    barrio = resultSet.getString("barrio"),
                    localidad = resultSet.getString("localidad"),
                    provincia = resultSet.getString("provincia"),
                    pais = resultSet.getString("pais"),
                    estado = resultSet.getInt("idEstado"),
                    genero = resultSet.getInt("idGenero"),
                    habilidad = resultSet.getInt("idHabilidad"),
                    posicion = resultSet.getInt("idPosicion"),
                    isAdmin = resultSet.getInt("isAdmin")
                )
                userList.add(user)
            }

            resultSet?.close()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return userList
    }

    fun getLastUserId(): Int {
        var lastId = 0
        val query = "SELECT MAX(id) FROM cuentas"

        try {
            val conn = DBConnection.getConnection()
            val statement = conn?.createStatement()
            val resultSet = statement?.executeQuery(query)

            if(resultSet?.next() == true) {
                lastId = resultSet.getInt(1)
            }

            resultSet?.close()
            statement?.close()
            conn?.close()

            return lastId
            } catch (e: Exception) {
                e.printStackTrace()
        }
        return 0
    }

    fun addUser(user: User): Boolean {
        val query1 = "INSERT INTO direcciones (calle, numero, idBarrio) VALUES (?, ?, ?)"
        val query2 = "SELECT MAX(id) FROM direcciones"
        val query3 = """
            INSERT INTO cuentas (email, contraseña, nombre, apellido, fecha_nacimiento, telefono, fecha_creacion, 
            ultimo_acceso, isAdmin, idDireccion, idEstado, idGenero, idHabilidad, idPosicion, codigo_verificacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaCreacion = dateFormat.format(Date()).toString()

        var verificationCode = Utils.generateVerificationCode()

        try {
            val conn = DBConnection.getConnection()

            val preparedStatement1 = conn?.prepareStatement(query1)
            preparedStatement1?.setString(1, user.calle)
            preparedStatement1?.setInt(2, user.numero)
            preparedStatement1?.setInt(3, user.idBarrio)
            preparedStatement1?.executeUpdate()

            val statement = conn?.createStatement()
            val resultSet2 = statement?.executeQuery(query2)
            var idDireccion = 0
            if(resultSet2?.next() == true) {
                idDireccion = resultSet2.getInt(1)
            } else {
                resultSet2?.close()
                statement?.close()
                conn?.close()
                return false
            }

            val preparedStatement2 = conn.prepareStatement(query3)
            preparedStatement2?.setString(1, user.email)
            preparedStatement2?.setString(2, Utils.hashPassword(user.password))
            preparedStatement2?.setString(3, user.nombre)
            preparedStatement2?.setString(4, user.apellido)
            preparedStatement2?.setString(5, user.fechaNacimiento)
            preparedStatement2?.setString(6, user.telefono)
            preparedStatement2?.setString(7, fechaCreacion)
            preparedStatement2?.setNull(8, java.sql.Types.DATE)
            preparedStatement2?.setInt(9, user.isAdmin)
            preparedStatement2?.setInt(10, idDireccion)
            preparedStatement2?.setInt(11, user.estado)
            preparedStatement2?.setInt(12, user.genero)
            preparedStatement2?.setInt(13, user.habilidad)
            preparedStatement2?.setInt(14, user.posicion)
            val resultSet3 = preparedStatement2?.executeUpdate()

            resultSet2.close()
            statement.close()
            conn.close()

            return resultSet3 != 0

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
