package com.example.pintapiconv3.app.user.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.InvitacionAdapter
import com.example.pintapiconv3.app.user.match.MatchDetailsActivity
import com.example.pintapiconv3.database.SQLServerHelper
import com.example.pintapiconv3.database.SQLServerHelper.InvitationStates.ACCEPTED
import com.example.pintapiconv3.database.SQLServerHelper.InvitationStates.REJECTED
import com.example.pintapiconv3.models.Invitacion
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.repository.NotifRepository
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.viewmodel.NotifViewModel
import com.example.pintapiconv3.viewmodel.NotifViewModelFactory
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotifFragment : Fragment() {

    private lateinit var rvNotificaciones: RecyclerView
    private lateinit var invitacionesAdapter: InvitacionAdapter

    private val equipoRepository = EquipoRepository()
    private val sqlServerHelper = SQLServerHelper()
    private val partidoRepository = PartidoRepository()
    private val notifRepository = NotifRepository()
    private val userRepository = UserRepository()

    private val notifViewModel: NotifViewModel by activityViewModels {
        NotifViewModelFactory(notifRepository)
    }

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notif, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvNotificaciones = view.findViewById(R.id.rv_notificaciones)
        invitacionesAdapter = InvitacionAdapter { invitacion, accept ->
            responderInvitacion(invitacion, accept)
        }
        rvNotificaciones.layoutManager = LinearLayoutManager(requireContext())
        rvNotificaciones.adapter = invitacionesAdapter

        cargarInvitacionesPendientes()
    }

    private fun cargarInvitacionesPendientes() {
        lifecycleScope.launch(Dispatchers.Main) {
            val userId = userViewModel.user.value?.id
            if(userId != null) {
                try {
                    val invitaciones = withContext(Dispatchers.IO) {
                        sqlServerHelper.getAllPendingInvitations(userId)
                    }
                    invitacionesAdapter.setInvitaciones(invitaciones)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al cargar las invitaciones pendientes. Detalles: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("InvitacionAdapter", "Error al cargar las invitaciones pendientes. Detalles: ${e.message}")
                }
            }
        }
    }

    private fun responderInvitacion(invitacion: Invitacion, accept: Boolean) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val userId = userViewModel.user.value?.id
                if(invitacion.idEquipo != null) {
                    val nuevoEstado = if(accept) ACCEPTED else REJECTED
                    withContext(Dispatchers.IO) {
                        equipoRepository.respondTeamInvitation(
                            invitacion.id,
                            invitacion.idEquipo!!,
                            userViewModel.user.value!!.id,
                            nuevoEstado
                        )
                    }
                } else if(invitacion.idPartido != null) {
                    val nuevoEstado = if(accept) ACCEPTED else REJECTED
                    withContext(Dispatchers.IO) {
                        partidoRepository.respondMatchInvitation(
                            invitacion.id,
                            invitacion.idPartido!!,
                            userViewModel.user.value!!.id,
                            nuevoEstado
                        )
                    }
                    if(accept) {
                        val sharedPref = requireContext().getSharedPreferences("MatchPref", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("partidoId_$userId", invitacion.idPartido!!)
                            apply()
                        }

                        val intent = Intent(requireContext(), MatchDetailsActivity::class.java)
                        startActivity(intent)
                    }
                }
                Toast.makeText(requireContext(), if(accept) "Invitacion aceptada" else "Invitacion rechazada", Toast.LENGTH_SHORT).show()
                cargarInvitacionesPendientes()
                notifViewModel.checkPendingNotifications(userId!!)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al responder la invitacion. Detalles: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("InvitacionAdapter", "Error al responder la invitacion. Detalles: ${e.message}")
            }
        }
    }
}