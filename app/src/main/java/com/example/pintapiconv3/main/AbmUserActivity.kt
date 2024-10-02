package com.example.pintapiconv3.main

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.pintapiconv3.R
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.models.User
import com.example.pintapiconv3.utils.UserAdapter

class AbmUserActivity : AppCompatActivity(), UserDetailDialog.UserUpdateListener, NewUserDialog.UserCreationListener {

    private lateinit var userAdapter: UserAdapter

    private lateinit var btn_atras: View
    private lateinit var btnAgregarUsuario: View

    val sqlServerHelper = SQLServerHelper()

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

        // Obtener la lista de usuarios de la BD
        val users = sqlServerHelper.getUsers()

        userAdapter = UserAdapter(this, users)

        // Configurar el adapter del ListView

        val userListView = findViewById<ListView>(R.id.listViewUsuarios)
        userListView.adapter = userAdapter

        userListView.setOnItemClickListener { parent, view, position, id ->
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

        for(i in 0 until userAdapter.count) {
            val user = userAdapter.getItem(i)
            if(user?.id == updatedUser.id) {
                userAdapter.remove(user)
                userAdapter.insert(updatedUser, i)
                userAdapter.notifyDataSetChanged()
                break
            }
        }
    }

    override fun onUserCreated(newUser: User) {

    }
}