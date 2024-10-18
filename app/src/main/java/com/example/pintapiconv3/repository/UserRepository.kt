package com.example.pintapiconv3.repository

import android.content.Context
import android.util.Log
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.utils.LoginResult
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.JWToken
import com.example.pintapiconv3.utils.Utils.generateVerificationCode
import com.example.pintapiconv3.utils.Utils.hashPassword
import io.jsonwebtoken.ExpiredJwtException
import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository {

    @Suppress("RedundantSuspendModifier")
    suspend fun loginUser(email: String, password: String): LoginResult {
        var conn: Connection? = null
        var estado = 0
        var isAdmin = 0
        try {
            Log.d("Database Operation", "Trying to connect to the database...")
            conn = DBConnection.getConnection()

            if (conn == null) {
                Log.e("Database Error", "No se pudo conectar a la base de datos")
                return LoginResult.Error("No se pudo conectar a la base de datos")
            }

            val query = """
                SELECT email, contraseña, idEstado, isAdmin
               FROM cuentas
               WHERE email = ? AND contraseña = ?
            """.trimIndent()

            val statement = conn.prepareStatement(query)
            statement.setString(1, email)
            statement.setString(2, password)
            val resultSet = statement.executeQuery()

            if(resultSet.next()) {
                estado = resultSet.getInt("idEstado")
                if(estado == AccountStates.BLOCKED || estado == AccountStates.SUSPENDED) {
                    return LoginResult.Error("Tu cuenta se encuentra bloqueada/suspendida. Comunicarse con Soporte")
                } else {
                    isAdmin = resultSet.getInt("isAdmin")
                    return LoginResult.Success(email, password, isAdmin, estado)
                }
            } else {
                return LoginResult.ErrorCredenciales(email, "Credenciales inválidas")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Database Error", "Ocurrió un error: ${e.message}")
            return LoginResult.Error("Ocurrió un error: ${e.message}")
        } finally {
            conn?.close()
        }
    }

    fun insertLastAccess(email: String) {
        try {
            val conn = DBConnection.getConnection()
            val query = "UPDATE cuentas SET ultimo_acceso = CURRENT_TIMESTAMP WHERE email = ?"

            val statement = conn?.prepareStatement(query)
            statement?.setString(1, email)
            statement?.executeUpdate()

            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveUserData(context: Context, email: String, password: String) {
        val sharedPref = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("userEmail", email)
            putString("userPassword", password)
            apply()
        }
    }

    fun clearUserData(context: Context) {
        val sharedPref = context.getSharedPreferences("userData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    fun saveSession(context: Context, email: String) {
        val token = JWToken.generateToken(email)
        val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("sessionToken", token)
            apply()
        }
    }

    fun getSession(context: Context): String? {
        val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        return sharedPref.getString("sessionToken", null)
    }

    fun clearSession(context: Context) {
        val sharedPref = context.getSharedPreferences("session", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    fun renewSession(context: Context) {
        try {
            val token = getSession(context)
            if (token != null && JWToken.validateToken(token)) {
                val email = JWToken.getEmailFromToken(token)
                saveSession(context, email) // Renueva la sesión solo si el token sigue siendo válido
            } else {
                Log.e("JWT", "El token ha expirado")
                throw ExpiredJwtException(null, null, "El token ha expirado.")
            }
        } catch (e: ExpiredJwtException) {
            Log.e("JWT", "El token ha expirado en renewSession", e)
        } catch (e: Exception) {
            Log.e("JWT", "Error en renewSession", e)
        }
    }

    fun incrementFailedAttempts(email: String) {
        val conn: Connection?
        try {
            conn = DBConnection.getConnection()

            val checkQuery = "SELECT id, intentos_fallidos FROM cuentas WHERE email = ?"
            val checkStatement = conn?.prepareStatement(checkQuery)
            checkStatement?.setString(1, email)
            val resultSet = checkStatement?.executeQuery()
            if(resultSet?.next() == true) {
                val idCuenta = resultSet.getInt("id")
                val intentos = resultSet.getInt("intentos_fallidos")
                if(intentos < MAX_FAILED_ATTEMPTS) {
                    val query = "UPDATE cuentas SET intentos_fallidos = intentos_fallidos + 1 WHERE email = ?"
                    val statement = conn.prepareStatement(query)
                    statement?.setString(1, email)
                    statement?.executeUpdate()
                    statement?.close()
                } else if(intentos == MAX_FAILED_ATTEMPTS) {
                    val blockQuery = "UPDATE cuentas SET idEstado = ? WHERE email = ?"
                    val blockStatement = conn.prepareStatement(blockQuery)
                    blockStatement?.setInt(1, AccountStates.BLOCKED)
                    blockStatement?.setString(2, email)
                    blockStatement?.executeUpdate()
                    blockStatement?.close()
                    logActivity(idCuenta, "BLOCK_ACCOUNT", "Su cuenta ha sido bloqueada por superar el número máximo de intentos")
                } else {
                    logActivity(idCuenta, "ACCOUNT_BLOCKED", "Cuenta bloqueada")
                }
            }
            checkStatement?.close()
            conn?.close()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetFailedAttempts(email: String) {
        var conn: Connection? = null
        try {
            conn = DBConnection.getConnection()
            val query = "UPDATE cuentas SET intentos_fallidos = 0 WHERE email = ?"
            val statement = conn?.prepareStatement(query)
            statement?.setString(1, email)
            statement?.executeUpdate()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.close()
        }
    }

    fun logActivity(userId: Int, actionType: String, actionDescription: String, ipAddress: String? = null) {
        var conn: Connection? = null
        val currentTimestamp = Timestamp(System.currentTimeMillis()).toString()
        val query = """
            INSERT INTO auditorias_logs (idCuenta, tipo_accion, descripcion_accion, fecha_hora, ip_address) 
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            conn = DBConnection.getConnection()
            val statement = conn?.prepareStatement(query)
            statement?.setInt(1, userId)
            statement?.setString(2, actionType)
            statement?.setString(3, actionDescription)
            statement?.setString(4, currentTimestamp)
            statement?.setString(5, ipAddress)
            statement?.executeUpdate()
            statement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun verifyPasswordResetCode(email: String, code: String): Boolean {
        val query = "SELECT COUNT(*) FROM cuentas WHERE email = ? AND codigo_reset = ?"
        val conn = DBConnection.getConnection()
        val statement = conn?.prepareStatement(query)
        statement?.setString(1, email)
        statement?.setString(2, code)
        val resultSet = statement?.executeQuery()

        val isValid = resultSet?.next() == true && resultSet.getInt(1) > 0

        resultSet?.close()
        statement?.close()
        conn?.close()

        return isValid
    }

    fun updatePassword(email: String, newPassword: String): Boolean {
        val hashedPassword = hashPassword(newPassword)
        val query = "UPDATE cuentas SET contraseña = ?, codigo_reset = NULL WHERE email = ?"

        val conn = DBConnection.getConnection()
        val statement = conn?.prepareStatement(query)
        statement?.setString(1, hashedPassword)
        statement?.setString(2, email)
        val rowsUpdated = statement?.executeUpdate() ?: 0

        statement?.close()
        conn?.close()

        return rowsUpdated > 0
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun getUserData(email: String, password: String): HashMap<String, String> {
        val userData = HashMap<String, String>()

        try {
            val conn = DBConnection.getConnection()
            val query = """
                SELECT c.id AS idCuenta,
                       email,
                       contraseña,
                       nombre,
                       apellido, 
                       fecha_nacimiento, 
                       telefono, 
                       fecha_creacion, 
                       ultimo_acceso,
                       isAdmin,
                       idDireccion,
                       d.calle AS calle, 
                       d.numero AS numero, 
                       d.idBarrio, 
                       b.descripcion AS barrio, 
                       l.descripcion AS localidad, 
                       pr.descripcion AS provincia, 
                       p.descripcion AS pais,
                       idEstado,
                       e.descripcion AS estado,
                       idGenero,
                       g.descripcion AS genero,
                       idHabilidad,
                       h.descripcion AS habilidad,
                       idPosicion,
                       po.descripcion AS posicion
                
                FROM cuentas c
                JOIN direcciones d ON c.idDireccion = d.id
                JOIN barrios b ON d.idBarrio = b.id
                JOIN localidades l ON b.idLocalidad = l.id
                JOIN provincias pr ON l.idProvincia = pr.id
                JOIN paises p ON pr.idPais = p.id
                JOIN estados e ON c.idEstado = e.id
                JOIN generos g ON c.idGenero = g.id
                JOIN habilidades h ON c.idHabilidad = h.id
                JOIN posiciones po ON c.idPosicion = po.id
                
                WHERE email = ? AND contraseña = ?
            """

            val statement = conn?.prepareStatement(query)
            statement?.setString(1, email)
            statement?.setString(2, password)
            val resultSet = statement?.executeQuery()

            if(resultSet?.next() == true) {
                userData["id"] = resultSet.getString("idCuenta")
                userData["email"] = resultSet.getString("email")
                userData["password"] = resultSet.getString("contraseña")
                userData["nombre"] = resultSet.getString("nombre")
                userData["apellido"] = resultSet.getString("apellido")
                userData["fechaNacimiento"] = resultSet.getString("fecha_nacimiento")
                userData["telefono"] = resultSet.getString("telefono")
                userData["fechaCreacion"] = resultSet.getString("fecha_creacion")
                userData["ultimoAcceso"] = resultSet.getString("ultimo_acceso")
                userData["isAdmin"] = resultSet.getString("isAdmin")
                userData["idDireccion"] = resultSet.getString("idDireccion")
                userData["calle"] = resultSet.getString("calle")
                userData["numero"] = resultSet.getString("numero")
                userData["idBarrio"] = resultSet.getString("idBarrio")
                userData["barrio"] = resultSet.getString("barrio")
                userData["localidad"] = resultSet.getString("localidad")
                userData["provincia"] = resultSet.getString("provincia")
                userData["pais"] = resultSet.getString("pais")
                userData["idEstado"] = resultSet.getString("idEstado")
                userData["estado"] = resultSet.getString("estado")
                userData["idGenero"] = resultSet.getString("idGenero")
                userData["genero"] = resultSet.getString("genero")
                userData["idHabilidad"] = resultSet.getString("idHabilidad")
                userData["habilidad"] = resultSet.getString("habilidad")
                userData["idPosicion"] = resultSet.getString("idPosicion")
                userData["posicion"] = resultSet.getString("posicion")

                logActivity(userData["id"]!!.toInt(), "LOGN", "El usuario inició sesión")
            }
            resultSet?.close()
            statement?.close()
            conn?.close()

        } catch(e: Exception) {
            e.printStackTrace()
            Log.e("Database Error", "Ocurrio un error: ${e.message}")
        }
        return userData
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun insertAccount (user: User, idDireccion: Int): Boolean {

        val query = """
            INSERT INTO cuentas (email, contraseña, nombre, apellido, fecha_nacimiento, telefono, fecha_creacion, 
            ultimo_acceso, isAdmin, idDireccion, idEstado, idGenero, idHabilidad, idPosicion, codigo_verificacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        // Obtener la fecha actual en formato yyyy-MM-dd
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaCreacion = dateFormat.format(Date()).toString()

        val codigo_verificacion = generateVerificationCode()

        try {
            val conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)

            preparedStatement?.setString(1, user.email)
            preparedStatement?.setString(2, user.password)
            preparedStatement?.setString(3, user.nombre)
            preparedStatement?.setString(4, user.apellido)
            preparedStatement?.setString(5, user.fechaNacimiento)
            preparedStatement?.setString(6, user.telefono)
            preparedStatement?.setString(7, fechaCreacion)
            preparedStatement?.setNull(8, java.sql.Types.DATE)
            preparedStatement?.setInt(9, 0)
            preparedStatement?.setInt(10, idDireccion)
            preparedStatement?.setInt(11, user.estado)
            preparedStatement?.setInt(12, user.genero)
            preparedStatement?.setInt(13, user.habilidad)
            preparedStatement?.setInt(14, user.posicion)
            preparedStatement?.setString(15, codigo_verificacion)

            val resultSet = preparedStatement?.executeUpdate()
            preparedStatement?.close()
            conn?.close()

            return resultSet != 0

        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun emailExists(email: String): Boolean {
        val query = "SELECT COUNT(*) FROM cuentas WHERE email = ?"
        try {
            val conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setString(1, email)
            val resultSet = preparedStatement?.executeQuery()
            var exists = false

            if (resultSet?.next() == true) {
                exists = resultSet.getInt(1) > 0
            }

            resultSet?.close()
            preparedStatement?.close()
            conn?.close()

            return exists
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun verifyAccount(email: String, enteredCode: String): Boolean {
        val conn: Connection?
        val query = "SELECT codigo_verificacion FROM cuentas WHERE email = ?"
        val updateQuery = "UPDATE cuentas SET idEstado = 2, codigo_verificacion = NULL WHERE email = ?"

        try {
            conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setString(1, email)
            val resultSet = preparedStatement?.executeQuery()

            if(resultSet?.next() == true && resultSet.getString("codigo_verificacion") == enteredCode) {
                val updateStatement = conn.prepareStatement(updateQuery)
                updateStatement?.setString(1, email)
                val updateResult = updateStatement?.executeUpdate()
                updateStatement?.close()
                resultSet.close()
                preparedStatement.close()
                conn.close()

                return updateResult == 1
            } else {
                resultSet?.close()
                preparedStatement?.close()
                conn?.close()
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun reactivateAccount(email: String) {
        val conn: Connection?
        val query = "UPDATE cuentas SET idEstado = 2, codigo_reset = NULL WHERE email = ?"

        try {
            conn = DBConnection.getConnection()
            val preparedStatement = conn?.prepareStatement(query)
            preparedStatement?.setString(1, email)
            preparedStatement?.executeQuery()

            preparedStatement?.close()
            conn?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    fun getUserName(email: String, password: String): String {

        val conn = DBConnection.getConnection()
        var userName = ""

        try {
            val query = "SELECT nombre FROM cuentas WHERE email = ? AND contraseña = ?"

            val statement = conn?.prepareStatement(query)
            statement?.setString(1, email)
            statement?.setString(2, password)
            val resultSet = statement?.executeQuery()

            if(resultSet?.next() == true)
                userName = resultSet.getString("nombre")

            return userName

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Database Error", "Ocurrió un error: ${e.message}")
            return userName
        }
        finally {
            conn?.close()
        }
    }

    suspend fun addUser(user: User): Boolean {
        val query1 = "INSERT INTO direcciones (calle, numero, idBarrio) VALUES (?, ?, ?)"
        val query2 = "SELECT MAX(id) FROM direcciones"
        val query3 = """
            INSERT INTO cuentas (email, contraseña, nombre, apellido, fecha_nacimiento, telefono, fecha_creacion, 
            ultimo_acceso, isAdmin, idDireccion, idEstado, idGenero, idHabilidad, idPosicion, codigo_verificacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaCreacion = dateFormat.format(Date()).toString()

        var verificationCode = generateVerificationCode()

        val conn = DBConnection.getConnection()

        try {
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

                val preparedStatement2 = conn.prepareStatement(query3)
                preparedStatement2?.setString(1, user.email)
                preparedStatement2?.setString(2, hashPassword(user.password))
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
                preparedStatement2?.setString(15, verificationCode)

                val resultSet3 = preparedStatement2?.executeUpdate()

                preparedStatement1?.close()
                statement.close()
                conn.close()

                return resultSet3 != 0

            } else {
                resultSet2?.close()
                statement?.close()
                conn?.close()
                Log.d("SQLServerHelper", "No se pudo obtener el idDireccion")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            conn?.close()
        }
    }

    fun getAllUsers(): List<User> {
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

    fun updateUserDataInDB(user: User): Result<Unit> {
            val conn = DBConnection.getConnection()
            try {
                conn?.let {
                    val query1 = """
                        UPDATE cuentas
                        SET nombre = ?,
                            apellido = ?,
                            email = ?,
                            fecha_nacimiento = ?,
                            telefono = ?,
                            isAdmin = ?,
                            idEstado = ?,
                            idGenero = ?,
                            idHabilidad = ?,
                            idPosicion = ?
                        WHERE id = ?
                    """.trimIndent()

                    val statement1 = it.prepareStatement(query1)
                    statement1.setString(1, user.nombre)
                    statement1.setString(2, user.apellido)
                    statement1.setString(3, user.email)
                    statement1.setString(4, user.fechaNacimiento)
                    statement1.setString(5, user.telefono)
                    statement1.setInt(6, user.isAdmin)
                    statement1.setInt(7, user.estado)
                    statement1.setInt(8, user.genero)
                    statement1.setInt(9, user.habilidad)
                    statement1.setInt(10, user.posicion)
                    statement1.setInt(11, user.id)
                    statement1.executeUpdate()

                    val query2 = """
                        UPDATE direcciones
                        SET calle = ?,
                            numero = ?,
                            idBarrio = ?
                        WHERE id = ?
                    """.trimIndent()

                    val statement2 = it.prepareStatement(query2)
                    statement2.setString(1, user.calle)
                    statement2.setInt(2, user.numero)
                    statement2.setInt(3, user.idBarrio)
                    statement2.setInt(4, user.idDireccion)
                    statement2.executeUpdate()

                    statement1.close()
                    statement2.close()
                    it.close()
                }
                return Result.success(Unit)
            } catch (e: Exception) {
                Log.e("Database Error", "Ocurrió un error: ${e.message}")
                return Result.failure(e)
            }
    }

    companion object {
        object AccountStates {
            const val NOT_VERIFIED = 1
            const val VERIFIED = 2
            const val DELETED = 3
            const val SUSPENDED = 4
            const val BLOCKED = 13
        }

        /*object FieldStates {
            const val OPEN = 5
            const val CLOSE = 6
            const val OUT_OF_SERVICE = 7
        }

        object MatchStates {
            const val PENDING = 8
            const val CANCELED = 9
            const val CONFIRMED = 10
            const val IN_PROGRESS = 11
            const val FINALIZED = 12
        }*/

        object Gender {
            const val MALE = 1
            const val FEMALE = 2
            const val OTHER = 3
        }

        const val MAX_FAILED_ATTEMPTS = 5
    }
}