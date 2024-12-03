package com.example.pintapiconv3.app.user.team

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EquipoAdapter
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Utils.isValidEmail
import com.example.pintapiconv3.utils.Utils.showToast
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamManagementActivity : AppCompatActivity() {

    private lateinit var tvNombreEquipo: TextView
    private lateinit var etInvitacionEmail: EditText
    private lateinit var btnEnviarInvitacion: Button
    private lateinit var rvEquipo: RecyclerView
    private lateinit var equipoAdapter: EquipoAdapter
    private lateinit var tvDescripcionEquipo: TextView

    private var userId: Int = -1
    private var equipoId: Int = -1

    private val userRepository = UserRepository()
    private val equipoRepository = EquipoRepository()

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_team_management)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //userViewModel = SharedUserData.userViewModel!!

        tvNombreEquipo = findViewById(R.id.tv_nombre_equipo)
        etInvitacionEmail = findViewById(R.id.et_invitacion_email)
        btnEnviarInvitacion = findViewById(R.id.btn_enviar_invitacion)
        rvEquipo = findViewById(R.id.rv_equipo)
        tvDescripcionEquipo = findViewById(R.id.tv_descripcion_equipo)

        equipoAdapter = EquipoAdapter(
            onDeleteMember = { miembroId ->
                eliminarMiembro(miembroId)
            }
        )
        rvEquipo.layoutManager = LinearLayoutManager(this)
        rvEquipo.adapter = equipoAdapter

        userViewModel.user.observe(this) { user ->
            if(user != null) {
                cargarEquipo(user.id)
                Log.d("TeamManagementActivity/onCreate", "Equipo cargado correctamente")
                userId = user.id
                Log.d("TeamManagementActivity/onCreate", "userId: $userId")
            } else {
                Log.e("TeamManagementActivity/onCreate", "No se pudo cargar el equipo")
                finish()
            }
        }



        btnEnviarInvitacion.setOnClickListener {
            val email = etInvitacionEmail.text.toString().trim()

            if (email.isEmpty()) {
                showToast("Por favor ingrese un email")
                return@setOnClickListener
            } else if (!isValidEmail(email)) {
                showToast("Por favor ingrese un email válido")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val emailExists = withContext(Dispatchers.IO) {
                    userRepository.emailExists(email)
                }
                if (!emailExists) {
                    showToast("El email no se encuentra registrado")
                } else {
                    val isMember = withContext(Dispatchers.IO) {
                        equipoRepository.isMember(equipoId, email)
                    }
                    if (isMember) {
                        showToast("El usuario ya es miembro del equipo")
                    } else {
                        enviarInvitacion(equipoId, email)
                    }
                }
            }
        }
    }


    private fun cargarEquipo(userId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val capitan = userViewModel.user.value?.nombre
                Log.d("TeamManagementActivity/cargarEquipo", "capitan: $capitan")
                val equipo = withContext(Dispatchers.IO) {
                    equipoRepository.getTeamByUserId(userId, capitan!!)
                }
                if(equipo != null) {
                    equipoId = equipo.id
                    tvNombreEquipo.text = "Bienvenido a ${equipo.nombre}"
                    tvDescripcionEquipo.text = equipo.descripcion

                    val miembrosActualizados = equipo.miembros.map { miembro ->
                        if(miembro.id == equipo.idCapitan) {
                            miembro.copy(isCaptain = true)
                        } else {
                            miembro
                        }
                    }
                    equipoAdapter.setMiembros(miembrosActualizados)
                    Log.d("TeamManagementActivity/cargarEquipo", "Equipo cargado correctamente")
                } else {
                    showToast("No se pudo cargar el equipo")
                    Log.e("TeamManagementActivity/cargarEquipo", "No se pudo cargar el equipo")
                }
            } catch (e: Exception) {
                showToast("Error al cargar el equipo.")
                Log.e("TeamManagementActivity/cargarEquipo", "Error al cargar el equipo: ${e.message}")
            }
        }
    }

    private fun enviarInvitacion(equipoId: Int, email: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val success = withContext(Dispatchers.IO) {
                    equipoRepository.sendInvitation(equipoId, userId, email)
                }
                if(success) {
                    showToast("Invitación enviada")
                    etInvitacionEmail.text.clear()
                } else {
                    showToast("No se pudo enviar la invitación")
                }
            } catch (e: Exception) {
                showToast("Error al enviar la invitacion")
                Log.e("TeamManagementActivity", "Error al enviar la invitación: ${e.message}")
            }
        }
    }

    private fun eliminarMiembro(miembroId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val eliminated = withContext(Dispatchers.IO) {
                    equipoRepository.deleteMember(miembroId)
                }
                if(eliminated) {
                    showToast("Miembro eliminado")
                    val userId = userViewModel.user.value?.id ?: -1
                    cargarEquipo(userId)
                }
            } catch (e: Exception) {
                showToast("Error al eliminar el miembro")
                Log.e("TeamManagementActivity/eliminarMiembro", "Error al eliminar el miembro: ${e.message}")
            }
        }
    }
}