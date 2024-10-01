package com.example.pintapiconv3.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.pintapiconv3.R
import java.security.MessageDigest
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale
import java.util.Properties
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object Utils {

    fun hashPassword(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        for(byte in bytes)
            result.append(String.format("%02x", byte))

        return result.toString()
    }

    fun isValidEmail(email: String): Boolean {
        var pat: Pattern? = null
        var mat: Matcher? = null

        pat = Pattern.compile("^[\\w\\-\\_\\+]+(\\.[\\w\\-\\_]+)*@([A-Za-z0-9-]+\\.)+[A-Za-z]{3,4}$")
        mat = pat.matcher(email)
        return mat!!.find()
    }

    fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$"
        val pattern = Pattern.compile(passwordRegex)
        return pattern.matcher(password).matches()
    }

    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun EditText.setupHintOnFocusChangeListener() {
        var originalHint: CharSequence? = null
        onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                originalHint = hint
                hint = "" // Para quitar el hint cuando se obtiene el foco
            } else {
                hint = originalHint // Para restaurar el hint cuando se pierde el foco
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateAge(birthDate: String) : Int {
        return try {
            val fecha = birthDate.substring(0,10)

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val fecha_nacimiento = LocalDate.parse(fecha, formatter)
            val currentDate = LocalDate.now()

            Period.between(fecha_nacimiento, currentDate).years
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            Log.e("Error", "Error al calcular la edad: ${e.message}")
            0
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupPasswordVisibilityToggle(context: Context, editText: EditText) {
        // Definir los iconos de ojo
        val eyeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_open_eye)
        val eyeBlockedDrawable = ContextCompat.getDrawable(context, R.drawable.ic_close_eye)

        // Configurar los límites de los iconos
        eyeDrawable?.setBounds(0, 0, eyeDrawable.intrinsicWidth, eyeDrawable.intrinsicHeight)
        eyeBlockedDrawable?.setBounds(0, 0, eyeBlockedDrawable.intrinsicWidth, eyeBlockedDrawable.intrinsicHeight)

        // Agregar el icono de ojo al EditText
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeBlockedDrawable, null)

        // Agregar el listener al icono de ojo
        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                editText.performClick()
                // Obtener la posición del icono de ojo
                val drawableRight = 2
                if (event.rawX >= (editText.right - editText.compoundDrawables[drawableRight].bounds.width())) {
                    // Cambiar la visibilidad de la contraseña
                    if (editText.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeDrawable, null)
                    } else {
                        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, eyeBlockedDrawable, null)
                    }
                    // Mover el cursor al final del texto
                    editText.setSelection(editText.length())
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    fun validateBirthDate(birthDate: String, ageLimit: Int = 100, minAge: Int = 16): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            val parsedDate = dateFormat.parse(birthDate) ?: return "La fecha no es válida. Introduce una fecha con el formato dd/MM/yyyy"

            val calendar = Calendar.getInstance()
            calendar.time = parsedDate

            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)) {
                age -= 1
            }

            if (age > ageLimit) {
                "La edad no puede exceder los $ageLimit años"
            } else if (age < minAge) {
                "Debes tener al menos $minAge años para registrarte"
            } else if (calendar.get(Calendar.YEAR) > today.get(Calendar.YEAR)) {
                "La fecha de nacimiento no puede ser en el futuro"
            } else {
                ""
            }
        } catch (e: ParseException) {
            "La fecha no es válida. Introduce una fecha con el formato dd/MM/yyyy"
        }
    }

    fun convertDateFormat(dateStr: String): String? {
        return try {
            // Define el formato de entrada
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            // Define el formato de salida
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            // Analiza la fecha en el formato de entrada
            val date = inputFormat.parse(dateStr)
            // Formatea la fecha en el formato de salida
            outputFormat.format(date!!)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateVerificationCode(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)  // Longitud del código
            .map { charset.random() }
            .joinToString("")
    }

    fun sendVerificationEmail(email: String, verificationCode: String) {
        val fromEmail = "pintapicon.verificacion@gmail.com"
        val password = "cypz ufou rinw ufma"
        val subject = "Verificación de cuenta"
        val body = "Tu código de verificación es: $verificationCode"

        // Configurar las propiedades del servidor SMTP
        val properties = Properties()
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"

        // Crear una sesión con autenticación
        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(fromEmail, password)
            }
        })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(fromEmail))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            message.subject = subject
            message.setText(body)

            // Enviar el correo
            Transport.send(message)

        } catch (e: Exception) {
            e.printStackTrace()
            println("Error al enviar el correo de verificación: ${e.message}")
        }
    }
}



