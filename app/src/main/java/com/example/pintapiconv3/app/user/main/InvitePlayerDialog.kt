package com.example.pintapiconv3.app.user.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.MiembroAdapter
import com.example.pintapiconv3.models.Miembro
import com.example.pintapiconv3.repository.PartidoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.utils.Utils.isValidEmail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InvitePlayerDialog(
    private val partidoId: Int,
    private val organizador: String,
    private val capitanId: Int,
    private val partidoRepository: PartidoRepository,
    private val userRepository: UserRepository
) : DialogFragment() {

    private lateinit var etEmail: EditText
    private lateinit var btnInvitarPorEmail: Button
    private lateinit var btnDesplegarEquipo: TextView
    private lateinit var rvMiembrosEquipo: RecyclerView
    private lateinit var miembrosAdapter: MiembroAdapter

    private val miembrosInvitados = mutableSetOf<Int>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_invite_player, null)

        initViews(view)

        builder.setView(view)
            .setTitle("Invitar jugadores")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    private fun initViews(view: View) {
        etEmail = view.findViewById(R.id.et_invitar_email)
        btnInvitarPorEmail = view.findViewById(R.id.btn_invitar_email)
        btnDesplegarEquipo = view.findViewById(R.id.btn_mostrar_miembros)
        rvMiembrosEquipo = view.findViewById(R.id.rv_miembros_equipo)

        btnInvitarPorEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if(email.isNotEmpty() && isValidEmail(email)) {
                lifecycleScope.launch {
                    val emailExists = withContext(Dispatchers.IO) {
                        userRepository.emailExists(email)
                    }
                    if(emailExists) {
                        try {
                            val success = withContext(Dispatchers.IO) {
                                partidoRepository.sendInvitationByEmail(partidoId, organizador, email)
                            }
                            if (success) {
                                Log.d("InvitePlayerDialog", "Invitación enviada a $email")
                                etEmail.text.clear()
                                Toast.makeText(requireContext(), "Invitación enviada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Error al enviar la invitación", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("InvitePlayerDialog", "Error al enviar la invitación", e)
                            Toast.makeText(requireContext(), "Error al enviar la invitación", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "El email no es valido", Toast.LENGTH_SHORT).show()
            }
        }

        btnDesplegarEquipo.setOnClickListener {
            if(rvMiembrosEquipo.visibility == View.GONE) {
                rvMiembrosEquipo.visibility = View.VISIBLE
                cargarMiembrosEquipo()
                btnDesplegarEquipo.text = "Ocultar miembros del equipo"
            } else {
                rvMiembrosEquipo.visibility = View.GONE
                btnDesplegarEquipo.text = "Mostrar miembros del equipo"
            }
        }

        miembrosAdapter = MiembroAdapter(miembrosInvitados) { miembro ->
            enviarInvitacion(miembro)
        }
        rvMiembrosEquipo.layoutManager = LinearLayoutManager(requireContext())
        rvMiembrosEquipo.adapter = miembrosAdapter
    }

    private fun cargarMiembrosEquipo() {
        lifecycleScope.launch {
            try {
                val miembros = withContext(Dispatchers.IO) {
                    partidoRepository.getMiembrosByCapitan(capitanId)
                }
                if(miembros.isNotEmpty()) {
                    val miembrosFiltrados = miembros.filter { it.id != capitanId }
                    miembrosAdapter.setMiembros(miembrosFiltrados)
                } else {
                    Toast.makeText(requireContext(), "No hay miembros en el equipo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("InvitePlayerDialog", "Error al cargar los miembros del equipo", e)
            }
        }
    }

    private fun enviarInvitacion(miembro: Miembro) {
        lifecycleScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    partidoRepository.sendInvitation(partidoId, organizador, miembro.id)
                }
                if(success) {
                    Log.d("InvitePlayerDialog", "Invitación enviada a ${miembro.nombre}")
                    miembrosInvitados.add(miembro.id)
                    val index = miembrosAdapter.miembros.indexOf(miembro)
                    if(index != -1) {
                        miembrosAdapter.notifyItemChanged(index)
                    }
                    Toast.makeText(requireContext(), "Invitación enviada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error al enviar la invitación", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("InvitePlayerDialog", "Error al enviar la invitación", e)
            }
        }
    }
}