package com.example.pintapiconv3.main.user

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.Direccion
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.BarrioRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Utils.convertDateFormat
import com.example.pintapiconv3.utils.Utils.generateVerificationCode
import com.example.pintapiconv3.utils.Utils.hashPassword
import com.example.pintapiconv3.utils.Utils.isValidEmail
import com.example.pintapiconv3.utils.Utils.isValidPassword
import com.example.pintapiconv3.utils.Utils.sendVerificationEmail
import com.example.pintapiconv3.utils.Utils.setupHintOnFocusChangeListener
import com.example.pintapiconv3.utils.Utils.setupPasswordVisibilityToggle
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.utils.Utils.validateBirthDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SigninActivity : AppCompatActivity() {

    private val userRepository = UserRepository()
    private val barrioRepository = BarrioRepository()
    private val sqlServerHelper = SQLServerHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()

        btn_atras.setOnClickListener {
            finish()
        }

        btn_signin.setOnClickListener {
            registerUser()
        }

    }

    private fun initViews() {

        et_nombre = findViewById(R.id.et_nombre)
        et_apellido = findViewById(R.id.et_apellido)
        et_email = findViewById(R.id.et_email)
        et_fechaNacimiento = findViewById(R.id.et_fechaNacimiento)
        et_telefono = findViewById(R.id.et_telefono)
        et_calle = findViewById(R.id.et_calle)
        et_numero = findViewById(R.id.et_numero)
        spner_barrio = findViewById(R.id.spner_barrio)
        spner_localidad = findViewById(R.id.spner_localidad)
        spner_provincia = findViewById(R.id.spner_provincia)
        spner_pais = findViewById(R.id.spner_pais)
        spner_posicion = findViewById(R.id.spner_posicion)
        spner_habilidad = findViewById(R.id.spner_habilidad)
        rg_genero = findViewById(R.id.rg_genero)
        rb_masculino = findViewById(R.id.rb_masculino)
        rb_femenino = findViewById(R.id.rb_femenino)
        rb_otro = findViewById(R.id.rb_otro)
        et_password = findViewById(R.id.et_password)
        et_password2 = findViewById(R.id.et_password2)
        cb_terms = findViewById(R.id.cb_terms)
        btn_signin = findViewById(R.id.btn_signin)
        btn_atras = findViewById(R.id.btn_atras)

        et_nombre.setupHintOnFocusChangeListener()
        et_apellido.setupHintOnFocusChangeListener()
        et_email.setupHintOnFocusChangeListener()
        et_fechaNacimiento.setupHintOnFocusChangeListener()
        et_telefono.setupHintOnFocusChangeListener()
        et_calle.setupHintOnFocusChangeListener()
        et_numero.setupHintOnFocusChangeListener()
        et_password.setupHintOnFocusChangeListener()
        et_password2.setupHintOnFocusChangeListener()

        setupPasswordVisibilityToggle(this, et_password)
        setupPasswordVisibilityToggle(this, et_password2)

        loadSpinner(spner_barrio, "Barrio", barrioRepository.getBarrios().map { it.second })
        loadSpinner(spner_localidad, "Localidad", listOf("Córdoba Capital"))
        loadSpinner(spner_provincia, "Provincia", listOf("Córdoba"))
        loadSpinner(spner_pais, "País", listOf("Argentina"))
        loadSpinner(spner_posicion, "Seleccione una posicion de preferencia", sqlServerHelper.getPosiciones().map { it.second })
        loadSpinner(spner_habilidad, "Seleccione un nivel de habilidad", sqlServerHelper.getHabilidades().map { it.second })
    }


    private fun <T> loadSpinner(spinner: Spinner, defaultItem: String, items: List<T>) {

        val displayItems = mutableListOf(defaultItem).apply {
            addAll(items.map { it.toString() })
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun validateSignin(): String {

        var errormessage = ""
        if (et_nombre.text.isEmpty()) {
            errormessage = "Por favor ingrese su nombre"
        } else if (et_apellido.text.isEmpty()) {
            errormessage = "Por favor ingrese su apellido"
        } else if (et_email.text.isEmpty()) {
            errormessage = "Por favor ingrese su correo"
        } else if (!isValidEmail(et_email.text.toString())) {
            errormessage = "Por favor ingrese un correo válido"
        } else if (et_fechaNacimiento.text.isEmpty()) {
            errormessage = "Por favor ingrese su fecha de nacimiento"
        } else if(validateBirthDate(et_fechaNacimiento.text.toString()) != "") {
            errormessage = validateBirthDate(et_fechaNacimiento.text.toString())
        } else if (et_telefono.text.isEmpty()) {
            errormessage = "Por favor ingrese su telefono"
        } else if (et_calle.text.isEmpty()) {
            errormessage = "Por favor ingrese su calle"
        } else if (et_numero.text.isEmpty()) {
            errormessage = "Por favor ingrese su numero"
        } else if (spner_barrio.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione su barrio"
        } else if (spner_localidad.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione su localidad"
        } else if (spner_provincia.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione su provincia"
        } else if (spner_pais.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione su pais"
        } else if (spner_posicion.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione una posicion"
        } else if (spner_habilidad.selectedItemPosition == 0) {
            errormessage = "Por favor seleccione un nivel de habilidad"
        } else if (rg_genero.checkedRadioButtonId == -1) {
            errormessage = "Por favor seleccione un género"
        } else if (et_password.text.isEmpty()) {
            errormessage = "Por favor ingrese su contraseña"
        } else if (et_password2.text.isEmpty()) {
            errormessage = "Por favor confirme su contraseña"
        } else if (!cb_terms.isChecked) {
            errormessage = "Por favor acepte los terminos y condiciones"
        } else if (et_password.text.toString() != et_password2.text.toString()) {
            errormessage = "Las contraseñas no coinciden"
        }
        return errormessage
    }

    private fun registerUser() {

        lifecycleScope.launch {
            val email = et_email.text.toString()
            val error = withContext(Dispatchers.Default) { validateSignin() }
            if (error.isNotEmpty()) {
                showToast(error)
            } else {
                val emailExists = withContext(Dispatchers.IO) { userRepository.emailExists(email) }
                if(emailExists)
                    showToast("El correo ya se encuentra registrado")
                else if(!isValidPassword(et_password.text.toString()))
                    showToast("La contraseña debe contener mínimo 8 caracteres, una mayúscula, una minúscula y un número")
                else {
                    val verificationCode = generateVerificationCode()

                    val direccion = Direccion(
                        calle = et_calle.text.toString(),
                        numero = et_numero.text.toString().toInt(),
                        idBarrio = barrioRepository.getBarrios()[spner_barrio.selectedItemPosition - 1].first
                    )

                    val idGenero = when (rg_genero.checkedRadioButtonId) {
                        R.id.rb_masculino -> UserRepository.Companion.Gender.MALE
                        R.id.rb_femenino -> UserRepository.Companion.Gender.FEMALE
                        else -> UserRepository.Companion.Gender.OTHER
                    }

                    val user = User(
                        id = -1,
                        email = email,
                        password = hashPassword(et_password.text.toString()),
                        nombre = et_nombre.text.toString(),
                        apellido = et_apellido.text.toString(),
                        fechaNacimiento = convertDateFormat(et_fechaNacimiento.text.toString())!!,
                        telefono = et_telefono.text.toString(),
                        idDireccion = -1,
                        calle = direccion.calle,
                        numero = direccion.numero,
                        idBarrio = direccion.idBarrio,
                        barrio = "",
                        localidad = "",
                        provincia = "",
                        pais = "",
                        estado = UserRepository.Companion.AccountStates.NOT_VERIFIED,
                        genero = idGenero,
                        habilidad = sqlServerHelper.getHabilidades()[spner_habilidad.selectedItemPosition - 1].first,
                        posicion = sqlServerHelper.getPosiciones()[spner_posicion.selectedItemPosition - 1].first,
                        isAdmin = 0
                    )

                    withContext(Dispatchers.IO) {
                        try {
                            // Insertar la direccion en la base de datos
                            val idDireccion = userRepository.insertDireccion(direccion.calle, direccion.numero, direccion.idBarrio)

                            if (idDireccion != null) {
                                val result = userRepository.insertAccount(user, idDireccion)

                                withContext(Dispatchers.Main) {
                                    if (result) {
                                        sendVerificationEmail(email, verificationCode)
                                        showToast("Registro exitoso")
                                        finish()
                                    } else {
                                        showToast("Error en el registro")
                                    }
                                }

                            } else {
                                withContext(Dispatchers.Main) {
                                    showToast("Error al registrar la dirección")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                e.printStackTrace()
                                showToast("Error en el registro")
                            }
                        }
                    }
                }
            }
        }
    }

    private lateinit var btn_atras: View
    private lateinit var et_nombre: EditText
    private lateinit var et_apellido: EditText
    private lateinit var et_email: EditText
    private lateinit var et_fechaNacimiento: EditText
    private lateinit var et_telefono: EditText
    private lateinit var et_calle: EditText
    private lateinit var et_numero: EditText
    private lateinit var spner_barrio: Spinner
    private lateinit var spner_localidad: Spinner
    private lateinit var spner_provincia: Spinner
    private lateinit var spner_pais: Spinner
    private lateinit var spner_posicion: Spinner
    private lateinit var spner_habilidad: Spinner
    private lateinit var rg_genero: RadioGroup
    private lateinit var rb_masculino: RadioButton
    private lateinit var rb_femenino: RadioButton
    private lateinit var rb_otro: RadioButton
    private lateinit var et_password: EditText
    private lateinit var et_password2: EditText
    private lateinit var cb_terms: CheckBox
    private lateinit var btn_signin: TextView
}