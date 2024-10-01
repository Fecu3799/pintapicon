package com.example.pintapiconv3.database

import com.example.pintapiconv3.models.User

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
}
