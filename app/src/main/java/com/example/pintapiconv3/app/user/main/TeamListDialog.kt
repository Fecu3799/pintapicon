package com.example.pintapiconv3.app.user.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pintapiconv3.R
import com.example.pintapiconv3.adapter.EquiposAdapter
import com.example.pintapiconv3.app.user.team.TeamDetailsActivity
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TeamListDialog : DialogFragment() {

    private lateinit var tvTitulo: TextView
    private lateinit var rvEquipos: RecyclerView
    private lateinit var equiposAdapter: EquiposAdapter

    private val equiposRepository = EquipoRepository()
    private val userRepository = UserRepository()

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_teams_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setLayout(1000, 1500)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvTitulo = view.findViewById(R.id.tv_titulo)
        rvEquipos = view.findViewById(R.id.rv_equipos)
        equiposAdapter = EquiposAdapter { equipoId ->
            openTeamDetails(equipoId)
        }

        rvEquipos.layoutManager = LinearLayoutManager(requireContext())
        rvEquipos.adapter = equiposAdapter

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                cargarEquipos(it.id)
            }
        }
    }

    private fun cargarEquipos(userId: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val equipos = withContext(Dispatchers.IO) {
                    equiposRepository.getTeamsByMember(userId)
                }
                if(equipos.isEmpty()) {
                    tvTitulo.text = "No perteneces a ningún equipo"
                }
                equiposAdapter.setEquipos(equipos)
                Log.d("TeamListDialog", "Equipos cargados correctamente")
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar los equipos", Toast.LENGTH_SHORT).show()
                Log.e("TeamListDialog", "Error al cargar los equipos. Detalle: ${e.message}")
            }
        }
    }

    private fun openTeamDetails(equipoId: Int) {
        val intent = Intent(requireContext(), TeamDetailsActivity::class.java)
        intent.putExtra("equipoId", equipoId)
        startActivity(intent)
        dismiss()
    }
}