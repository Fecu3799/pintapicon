package com.example.pintapiconv3.app.user.main

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EquipoAdapter
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.viewmodel.UserViewModel

class TeamManagementActivity : AppCompatActivity() {

    private lateinit var tvNombreEquipo: TextView
    private lateinit var etInvitacionEmail: EditText
    private lateinit var btnEnviarInvitacion: Button
    private lateinit var rvEquipo: RecyclerView
    private lateinit var equipoAdapter: EquipoAdapter

    private lateinit var userViewModel: UserViewModel
    private val equipoRepository = EquipoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team_management)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvNombreEquipo = findViewById(R.id.tv_nombre_equipo)
        etInvitacionEmail = findViewById(R.id.et_invitacion_email)
        btnEnviarInvitacion = findViewById(R.id.btn_enviar_invitacion)
        rvEquipo = findViewById(R.id.rv_equipo)


    }
}