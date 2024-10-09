package com.example.pintapiconv3.main

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.UserAdapter
import com.example.pintapiconv3.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AbmUserActivity : AppCompatActivity(), UserDetailDialog.UserUpdateListener, NewUserDialog.UserCreationListener {

    private lateinit var userAdapter: UserAdapter

    private lateinit var btn_atras: View
    private lateinit var btnAgregarUsuario: View
    private lateinit var userListView: ListView

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_abm_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main_admin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn_atras = findViewById(R.id.btn_atras)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario)

        // Obtener todas las cuentas registradas en la BD
        val users = userRepository.getAllUsers()

        // Ordenar las cuentas por estado
        val sortedUsers = users.sortedBy { it.estado == UserRepository.Companion.AccountStates.DELETED}

        // Configurar el adapter del ListView
        userAdapter = UserAdapter(this, sortedUsers)
        userListView = findViewById(R.id.listViewUsuarios)
        userListView.adapter = userAdapter

        userListView.setOnItemClickListener { _, _, position, _ ->
            val user = userAdapter.getItem(position)
            user?.let {
                val dialog = UserDetailDialog.newInstance(it)
                dialog.show(supportFragmentManager, "UserDetailDialog")
            }
        }

        btnAgregarUsuario.setOnClickListener {
            val dialog = NewUserDialog()
            dialog.show(supportFragmentManager, "NewUserDialog")
        }

        btn_atras.setOnClickListener {
            finish()
        }
    }

    override fun onUserUpdated(updatedUser: User) {

        val updatedList = (0 until userAdapter.count).mapNotNull { userAdapter.getItem(it) }.toMutableList()

        // Busca el usuario actualizado en la lista
        val userIndex = updatedList.indexOfFirst { it.id == updatedUser.id }

        if (userIndex != -1) {
            // Reemplaza el usuario en la lista con el actualizado
            updatedList[userIndex] = updatedUser
        }

        // Reordena la lista
        val sortedUsers = updatedList.sortedBy { it.estado == UserRepository.Companion.AccountStates.DELETED }

        // Crear un nuevo adaptador con la lista actualizada
        val newUserAdapter = UserAdapter(this, sortedUsers)
        userListView.adapter = newUserAdapter

        // Notificar los cambios al adapter
        userAdapter = newUserAdapter
    }

    override fun onUserCreated(newUser: User) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                userRepository.addUser(newUser)
            }
        }
        userAdapter.add(newUser)
        userAdapter.notifyDataSetChanged()
    }
}