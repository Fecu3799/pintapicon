package com.example.pintapiconv3.main.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.repository.BarrioRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Utils.isValidEmail
import com.example.pintapiconv3.utils.Utils.isValidPassword
import com.example.pintapiconv3.utils.Utils.validateBirthDate

class NewUserDialog : DialogFragment() {

    private val sqlServerHelper = SQLServerHelper()
    private val userRepository = UserRepository()
    private val barrioRepository = BarrioRepository()

    private var userCreationListener: UserCreationListener? = null

    // Interfaz para notificar a AbmUserActivity cuando se ha creado una cuenta y poder actualizar la lista
    interface UserCreationListener {
        fun onUserCreated(newUser: User)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is UserCreationListener) {
            userCreationListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_new_user_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        loadSpinners()

        btn_save.setOnClickListener {
            val error = validateFields()
            if (error.isEmpty()) {
                saveUser()
            } else {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        btn_cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun initViews() {
        userId = view!!.findViewById(R.id.et_userId)
        userEmail = view!!.findViewById(R.id.et_userEmail)
        userPassword = view!!.findViewById(R.id.et_userPassword)
        userName = view!!.findViewById(R.id.et_userName)
        userLastName = view!!.findViewById(R.id.et_userLastName)
        userDateOfBirth = view!!.findViewById(R.id.et_userDateOfBirth)
        userPhoneNumber = view!!.findViewById(R.id.et_userPhoneNumber)
        userRol = view!!.findViewById(R.id.rg_userRole)
        userStreet = view!!.findViewById(R.id.et_userStreet)
        userStreetNumber = view!!.findViewById(R.id.et_userStreetNumber)
        userHood = view!!.findViewById(R.id.spner_userHood)
        userState = view!!.findViewById(R.id.spner_userState)
        userGender = view!!.findViewById(R.id.spner_userGender)
        userSkill = view!!.findViewById(R.id.spner_userSkill)
        userPosition = view!!.findViewById(R.id.spner_userPosition)

        btn_save = view!!.findViewById(R.id.btn_save)
        btn_cancel = view!!.findViewById(R.id.btn_cancel)

        userId.setText((userRepository.getLastUserId() + 1).toString())
        userId.isEnabled = false
    }

    private fun loadSpinners() {
        val barrios = barrioRepository.getBarrios()
        val estados = sqlServerHelper.getEstadosCuenta()
        val generos = sqlServerHelper.getGeneros()
        val habilidades = sqlServerHelper.getHabilidades()
        val posiciones = sqlServerHelper.getPosiciones()

        setSpinners(userHood, barrios)
        setSpinners(userState, estados)
        setSpinners(userGender, generos)
        setSpinners(userSkill, habilidades)
        setSpinners(userPosition, posiciones)
    }

    private fun setSpinners(spinner: Spinner, items: List<Pair<Int, String>>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(1)
    }

    private fun validateFields(): String {

        var errormessage = ""

        if (userName.text.isEmpty()) {
            errormessage = "Por favor ingrese su nombre"
        } else if (userLastName.text.isEmpty()) {
            errormessage = "Por favor ingrese su apellido"
        } else if (userEmail.text.isEmpty()) {
            errormessage = "Por favor ingrese su correo"
        } else if (!isValidEmail(userEmail.text.toString())) {
            errormessage = "Por favor ingrese un correo válido"
        } else if (userRepository.emailExists(userEmail.text.toString())) {
            errormessage = "El email ya se encuentra registrado"
        } else if (userPassword.text.isEmpty()) {
            errormessage = "Por favor ingrese una contraseña"
        } else if (!isValidPassword(userPassword.text.toString())) {
            errormessage = "Por favor ingrese una contraseña válida"
        } else if (userDateOfBirth.text.isEmpty()) {
            errormessage = "Por favor ingrese su fecha de nacimiento"
        } else if(validateBirthDate(userDateOfBirth.text.toString()) != "") {
            errormessage = validateBirthDate(userDateOfBirth.text.toString())
        } else if (userPhoneNumber.text.isEmpty()) {
            errormessage = "Por favor ingrese su telefono"
        } else if (userRol.checkedRadioButtonId == -1) {
            errormessage = "Por favor seleccione un rol"
        }else if (userStreet.text.isEmpty()) {
            errormessage = "Por favor ingrese su calle"
        } else if (userStreetNumber.text.isEmpty()) {
            errormessage = "Por favor ingrese su numero"
        }

        return errormessage
    }

    private fun saveUser() {

        val newUser = User (
            id = userId.text.toString().toInt(),
            email = userEmail.text.toString(),
            password = userPassword.text.toString(),
            nombre = userName.text.toString(),
            apellido = userLastName.text.toString(),
            fechaNacimiento = userDateOfBirth.text.toString(),
            telefono = userPhoneNumber.text.toString(),
            idDireccion = -1,
            calle = userStreet.text.toString(),
            numero = userStreetNumber.text.toString().toInt(),
            idBarrio = userHood.selectedItemPosition + 1,
            barrio = userHood.selectedItem.toString(),
            localidad = "",
            provincia = "",
            pais = "",
            estado = if(userState.selectedItemPosition == 4) 13 else userState.selectedItemPosition + 1,
            genero = userGender.selectedItemPosition + 1,
            habilidad = userSkill.selectedItemPosition + 1,
            posicion = userPosition.selectedItemPosition + 1,
            isAdmin = if(userRol.checkedRadioButtonId == R.id.rb_admin) 1 else 0
        )

        userCreationListener?.onUserCreated(newUser)
        dismiss()
    }

    private lateinit var userId: EditText
    private lateinit var userEmail: EditText
    private lateinit var userPassword: EditText
    private lateinit var userName: EditText
    private lateinit var userLastName: EditText
    private lateinit var userDateOfBirth: EditText
    private lateinit var userPhoneNumber: EditText
    private lateinit var userRol: RadioGroup
    private lateinit var userStreet: EditText
    private lateinit var userStreetNumber : EditText
    private lateinit var userHood: Spinner
    private lateinit var userState: Spinner
    private lateinit var userGender: Spinner
    private lateinit var userSkill : Spinner
    private lateinit var userPosition : Spinner
    private lateinit var btn_save: Button
    private lateinit var btn_cancel: Button
}