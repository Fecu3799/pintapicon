package com.example.pintapiconv3.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.User

class NewUserDialog : DialogFragment() {

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

    private var userCreationListener: UserCreationListener? = null
    private val sqlServerHelper = SQLServerHelper()

    // Permite saber cuándo se ha creado un nuevo usuario y avisar a AbmUserActivity para actualizar la lista
    interface UserCreationListener {
        fun onUserCreated(newUser: User)
    }

    /*companion object {
        fun newInstance(): NewUserDialog {
            return NewUserDialog()
        }
    }*/

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
    }

    private fun loadSpinners() {
        val barrios = sqlServerHelper.getBarrios() ?: emptyList()
        val estados = sqlServerHelper.getEstadosCuenta() ?: emptyList()
        val generos = sqlServerHelper.getGeneros() ?: emptyList()
        val habilidades = sqlServerHelper.getHabilidades() ?: emptyList()
        val posiciones = sqlServerHelper.getPosiciones() ?: emptyList()

        setSpinners(userHood, barrios, 1)
        setSpinners(userState, estados, 1)
        setSpinners(userGender, generos, 1)
        setSpinners(userSkill, habilidades, 1)
        setSpinners(userPosition, posiciones, 1)
    }

    private fun setSpinners(spinner: Spinner, items: List<Pair<Int, String>>, selectedItem: Int) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items.map { it.second })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val index = items.indexOfFirst { it.first == selectedItem }
        if (index != -1) {
            spinner.setSelection(index)
        } else {
            spinner.setSelection(0)
        }
    }

    private fun saveUser() {


    }

}