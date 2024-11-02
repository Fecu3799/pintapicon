package com.example.pintapiconv3.app.user.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.pintapiconv3.R
import com.example.pintapiconv3.app.user.MainActivity
import com.example.pintapiconv3.repository.EquipoRepository
import com.example.pintapiconv3.repository.UserRepository
import com.example.pintapiconv3.viewmodel.SharedUserData
import com.example.pintapiconv3.viewmodel.UserViewModel
import com.example.pintapiconv3.viewmodel.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var teamButton: ImageButton
    private lateinit var txtTeamButton: TextView
    private lateinit var userViewModel: UserViewModel

    private val equipoRepository = EquipoRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        userViewModel = SharedUserData.userViewModel!!

        teamButton = view.findViewById(R.id.btn_equipo)
        txtTeamButton = view.findViewById(R.id.txt_btn_equipo)

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                if(userViewModel.hasTeam.value == null) {
                    checkIfUserHasTeam(it.id)
                } else {
                    setupTeamButton(userViewModel.hasTeam.value == true)
                }
            }
        }

    }

    private fun checkIfUserHasTeam(userID: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            val hasTeam = withContext(Dispatchers.IO) {
                equipoRepository.hasTeam(userID)
            }
            userViewModel.setHasTeam(hasTeam)
            setupTeamButton(hasTeam)
        }
    }

    private fun setupTeamButton(hasTeam: Boolean) {
        if(hasTeam) {
            txtTeamButton.text = "Mi equipo"
            teamButton.setOnClickListener {
                val intent = Intent(requireContext(), TeamManagementActivity::class.java)
                intent.putExtra("userId", userViewModel.user.value?.id)
                startActivity(intent)
            }
        } else {
            txtTeamButton.text = "Crear un equipo"
            teamButton.setOnClickListener {
                val dialog = CreateTeamDialog()
                dialog.show(childFragmentManager, "CreateTeamDialog")
            }
        }
    }
}