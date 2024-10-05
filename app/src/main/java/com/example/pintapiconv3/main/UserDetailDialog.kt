package com.example.pintapiconv3.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.UserRepository
import com.example.pintapiconv3.utils.UserViewModel
import com.example.pintapiconv3.utils.UserViewModelFactory

class UserDetailDialog : DialogFragment() {

    companion object {
        private const val ARG_USER = "user"

        fun newInstance(user: User) : UserDetailDialog {
            val fragment = UserDetailDialog()
            val args = Bundle()
            args.putSerializable(ARG_USER, user)
            fragment.arguments = args
            return fragment
        }
    }

    interface UserUpdateListener {
        fun onUserUpdated(updatedUser: User)
    }

    private lateinit var userId: EditText
    private lateinit var userName: EditText
    private lateinit var userLastName: EditText
    private lateinit var userEmail: EditText
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

    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private val sqlServerHelper = SQLServerHelper()
    private lateinit var userViewModel: UserViewModel

    private var user: User? = null

    private var userUpdateListener: UserUpdateListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is UserUpdateListener) {
            userUpdateListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) : View? {
        return inflater.inflate(R.layout.dialog_user_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userRepository = UserRepository()
        val factory = UserViewModelFactory(userRepository)
        userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

        user = arguments?.getSerializable(ARG_USER) as? User


        initViews()
        setViews()

        btnSave.setOnClickListener {
            saveUser()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

    }

    private fun initViews() {

        userId = view!!.findViewById<EditText?>(R.id.et_userId)
        userName = view!!.findViewById(R.id.et_userName)
        userLastName = view!!.findViewById(R.id.et_userLastName)
        userEmail = view!!.findViewById(R.id.et_userEmail)
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
        btnSave = view!!.findViewById(R.id.btn_save)
        btnCancel = view!!.findViewById(R.id.btn_cancel)
    }

    private fun setViews() {

        user?.let {
            userId.setText(it.id.toString())
            userName.setText(it.nombre)
            userLastName.setText(it.apellido)
            userEmail.setText(it.email)
            userDateOfBirth.setText(it.fechaNacimiento)
            userPhoneNumber.setText(it.telefono)
            userRol.check(if(it.isAdmin == 1) R.id.rb_admin else R.id.rb_user)
            userStreet.setText(it.calle)
            userStreetNumber.setText(it.numero.toString())

            userId.isEnabled = false

            loadSpinners(it)
        }
    }

    private fun loadSpinners(user: User) {
        val barrios = sqlServerHelper.getBarrios() ?: emptyList()
        val estados = sqlServerHelper.getEstadosCuenta() ?: emptyList()
        val generos = sqlServerHelper.getGeneros() ?: emptyList()
        val habilidades = sqlServerHelper.getHabilidades() ?: emptyList()
        val posiciones = sqlServerHelper.getPosiciones() ?: emptyList()

        setSpinners(userHood, barrios, user.idBarrio)
        setSpinners(userState, estados, user.estado)
        setSpinners(userGender, generos, user.genero)
        setSpinners(userSkill, habilidades, user.habilidad)
        setSpinners(userPosition, posiciones, user.posicion)
    }

    private fun setSpinners(spinner: Spinner, items: List<Pair<Int, String>>, selectedItem: Int) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items.map {it.second})
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val index = items.indexOfFirst { it.first == selectedItem }

        if(index != -1) {
            spinner.setSelection(index)
        } else {
            spinner.setSelection(0)
            Log.d("UserDetailDialog", "No se encontró el índice para el item seleccionado: $selectedItem")
        }
    }

    private fun saveUser() {

        user?.let {
            it.nombre = userName.text.toString()
            it.apellido = userLastName.text.toString()
            it.email = userEmail.text.toString()
            it.fechaNacimiento = userDateOfBirth.text.toString()
            it.telefono = userPhoneNumber.text.toString()
            it.isAdmin = if(userRol.checkedRadioButtonId == R.id.rb_admin) 1 else 0
            it.calle = userStreet.text.toString()
            it.numero = userStreetNumber.text.toString().toInt()
            it.idBarrio = userHood.selectedItemPosition + 1
            it.estado = if(userState.selectedItemPosition == 4) 13 else userState.selectedItemPosition + 1
            it.genero = userGender.selectedItemPosition + 1
            it.habilidad = userSkill.selectedItemPosition + 1
            it.posicion = userPosition.selectedItemPosition + 1

            userViewModel.updateUser(it)

            userUpdateListener?.onUserUpdated(it)

            dismiss()
        }
    }
}