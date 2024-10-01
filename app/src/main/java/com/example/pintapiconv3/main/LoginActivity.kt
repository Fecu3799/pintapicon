package com.example.pintapiconv3.main

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.database.DBConnection
import com.example.pintapiconv3.models.LoginResult
import com.example.pintapiconv3.utils.UserRepository
import com.example.pintapiconv3.utils.Utils.generateVerificationCode
import com.example.pintapiconv3.utils.Utils.hashPassword
import com.example.pintapiconv3.utils.Utils.isValidEmail
import com.example.pintapiconv3.utils.Utils.isValidPassword
import com.example.pintapiconv3.utils.Utils.sendVerificationEmail
import com.example.pintapiconv3.utils.Utils.setupHintOnFocusChangeListener
import com.example.pintapiconv3.utils.Utils.setupPasswordVisibilityToggle
import com.example.pintapiconv3.utils.Utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var et_email: EditText
    private lateinit var et_password: EditText
    private lateinit var btn_login: TextView
    private lateinit var btn_restorePassword: TextView
    private lateinit var btn_signin: TextView

    private lateinit var userRepository: UserRepository
    private lateinit var sqlServerHelper: SQLServerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userRepository = UserRepository()
        sqlServerHelper = SQLServerHelper()

        initView()

        btn_login.setOnClickListener {
            val error = validateLogin()
            if(error.isNotEmpty())
                showToast(error)
            else
                loginUser()
        }

        btn_signin.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }

        btn_restorePassword.setOnClickListener {
            if(et_email.text.isEmpty())
                showToast("Por favor ingrese un email")
            else if (!isValidEmail(et_email.text.toString()))
                showToast("El email debe ser válido")
            else if (!userRepository.emailExists(et_email.text.toString()))
                showToast("El email no se encuentra registrado")
            else if(sendPasswordResetCode(et_email.text.toString()))
                    showVerificationCodeDialog(et_email.text.toString())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        DBConnection.closeConnection()
    }

    // Inicializa los componentes visuales
    private fun initView() {
        et_email = findViewById(R.id.et_email)
        et_password = findViewById(R.id.et_password)
        btn_login = findViewById(R.id.btn_login)
        btn_signin = findViewById(R.id.btn_signin)
        btn_restorePassword = findViewById(R.id.btn_restorePassword)

        et_email.setupHintOnFocusChangeListener()
        et_password.setupHintOnFocusChangeListener()
        setupPasswordVisibilityToggle(this, et_password)
    }

    // Valida que los campos no esten vacios o que sean correctos
    private fun validateLogin(): String {
        var error_message = ""
        if (et_email.text.isEmpty())
            error_message += "Debe ingresar un email"
        else if (et_password.text.isEmpty())
            error_message += "Debe ingresar una contraseña"
        else if (!isValidEmail(et_email.text.toString()))
            error_message += "El email debe ser válido"
        else if (!isValidPassword(et_password.text.toString()))
            error_message += "La contraseña debe ser válida"
        return error_message
    }

    // Comprueba los datos ingresados e intenta loguear
    private fun loginUser() {
        val email = et_email.text.toString()
        val password = hashPassword(et_password.text.toString())

        lifecycleScope.launch(Dispatchers.IO) {
            val loginResult = userRepository.loginUser(email, password)
            withContext(Dispatchers.Main) {
                handleLoginResult(loginResult)
            }
        }
    }

    // Maneja el resultado de logueo
    private fun handleLoginResult(result: LoginResult) {
        when(result) {
            is LoginResult.Success -> {
                userRepository.resetFailedAttempts(result.email)
                userRepository.saveSession(this@LoginActivity, result.email)
                if(result.isAdmin == 1) {
                    userRepository.insertLastAccess(result.email)
                    startActivity(Intent(this@LoginActivity, MainActivityAdmin::class.java))
                    finish()
                } else {
                    when(result.estado) {
                        UserRepository.Companion.AccountStates.VERIFIED -> {
                            userRepository.insertLastAccess(result.email)
                            userRepository.saveUserData(this@LoginActivity, result.email, result.password)
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        UserRepository.Companion.AccountStates.NOT_VERIFIED -> showVerificationDialog(result.email)
                        UserRepository.Companion.AccountStates.DELETED -> showToast("Tu cuenta se encuentra eliminada. Comunicarse con Soporte")
                        UserRepository.Companion.AccountStates.SUSPENDED -> showToast("Tu cuenta se encuentra suspendida. Comunicarse con Soporte")
                        UserRepository.Companion.AccountStates.BLOCKED -> showToast("Tu cuenta se encuentra bloqueada por superar el número máximo de intentos. Comunicarse con Soporte")
                    }
                }
            }
            is LoginResult.ErrorCredenciales -> {
                userRepository.incrementFailedAttempts(result.email)
                showToast(result.message)
            }
            is LoginResult.Error -> {
                showToast(result.message)
            }
        }
    }

    // Muestra un dialogo para ingresar el codigo enviado por mail para verificar la cuenta
    private fun showVerificationDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verificación de cuenta")

        val input = EditText(this)
        input.hint = "Ingresa el codigo de verificacion que te enviamos a tu email registrado"
        builder.setView(input)

        builder.setPositiveButton("Verificar") { dialog, _ ->
            val verificationCode = input.text.toString().uppercase()
            if(verificationCode.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    verifyAccount(email, verificationCode)
                }
            } else {
                showToast("Por favor ingrese el codigo de verificacion")
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Verifica la cuenta una vez ingresado el codigo de verificacion
    private suspend fun verifyAccount(email: String, enteredCode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isVerified = userRepository.verifyAccount(email, enteredCode)

            withContext(Dispatchers.Main) {
                if (isVerified) {
                    showToast("Cuenta verificada exitosamente")
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("Codigo incorrecto")
                }
            }
        }
    }

    // Envia un codigo al mail ingresado
    private fun sendPasswordResetCode(email: String): Boolean {
        val code = generateVerificationCode()
        val query = "UPDATE cuentas SET codigo_reset = ? WHERE email = ?"

        return try {
            val conn = DBConnection.getConnection()
            val statement = conn?.prepareStatement(query)
            statement?.setString(1, code)
            statement?.setString(2, email)
            statement?.executeUpdate()
            statement?.close()
            conn?.close()

            sendVerificationEmail(email, code)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Verifica que el usuario recibio el codigo
    private fun showVerificationCodeDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperación de Contraseña")

        val input = EditText(this)
        input.hint = "Ingresa el código de verificación enviado a tu correo"
        builder.setView(input)

        builder.setPositiveButton("Verificar") { dialog, _ ->
            val verificationCode = input.text.toString().trim()
            if (verificationCode.isNotEmpty()) {
                verifyResetCode(email, verificationCode)
            } else {
                showToast("Por favor ingrese el código de verificación")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Verifica que el codigo es correcto
    private fun verifyResetCode(email: String, enteredCode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isVerified = userRepository.verifyPasswordResetCode(email, enteredCode)

            withContext(Dispatchers.Main) {
                if (isVerified) {
                    showToast("Código verificado correctamente")
                    showNewPasswordDialog(email) // Mostrar el diálogo para establecer nueva contraseña
                } else {
                    showToast("Código incorrecto o expirado")
                }
            }
        }
    }

    // Muestra dialogo para ingresar contraseña nueva
    private fun showNewPasswordDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nueva Contraseña")

        val input = EditText(this)
        input.hint = "Ingresa tu nueva contraseña"
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val newPassword = input.text.toString().trim()
            if (newPassword.isNotEmpty()) {
                updatePassword(email, newPassword)
            } else {
                showToast("Por favor ingrese una nueva contraseña")
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Comprueba y muestra si la actualizacion de la contraseña fue exitosa
    private fun updatePassword(email: String, newPassword: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val isUpdated = userRepository.updatePassword(email, newPassword)

            withContext(Dispatchers.Main) {
                if (isUpdated) {
                    showToast("Contraseña actualizada correctamente")
                } else {
                    showToast("Error al actualizar la contraseña")
                }
            }
        }
    }
}


